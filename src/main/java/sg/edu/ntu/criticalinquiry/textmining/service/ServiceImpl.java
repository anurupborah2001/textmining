package sg.edu.ntu.criticalinquiry.textmining.service;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import sg.edu.ntu.criticalinquiry.textmining.dao.DataMapper;
import sg.edu.ntu.criticalinquiry.textmining.domain.Pattern;
import sg.edu.ntu.criticalinquiry.textmining.domain.SideEffect;
import sg.edu.ntu.criticalinquiry.textmining.domain.Slot;
import sg.edu.ntu.criticalinquiry.textmining.domain.SplittedReview;
import sg.edu.ntu.criticalinquiry.textmining.domain.UserReview;
import sg.edu.ntu.criticalinquiry.textmining.vo.ExcelColumnData;
import sg.edu.ntu.criticalinquiry.textmining.vo.PatternExcelColumnData;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ServiceImpl implements Service {
	//Four ontologies are chosen from 3rd party
	private static final String RCD_ONTOLOGY = "rcd";
	private static final String ONTOAD_ONTOLOGY = "ontoad";
	private static final String MESH_ONTOLOGY = "mesh";
	private static final String ABNORMALITIES_ONTOLOGY = "abnormalities";
	
	private static final int REVIEW_COLUMN_INDEX = 3;
	private static final String REVIEW_WORK_SHEET_NAME = "TestData"; //Prelim dataset of 100 reviews";
																	//Psychotropic drug reviews- Training set
																	//TestData  Actual test data
	private static final String REVIEW_EXCEL = "data.xlsx";
	private static final String REVIEW_COLUMN = "Comment";
	private static final String PATTERN_EXCEL = "pattern_new.xlsx";
	private static final String SIDEEFFECT_EXCEL = "sideeffectconsolidated.xlsx"; // This
																					// file
																					// is
																					// used
																					// import
																					// Training
																					// Set
																					// Side
																					// Effects
																					// Library,
																					// Authoritative
																					// Website
																					// Side
																					// Effects
																					// Library
																					// and
																					// Combination
																					// 1
																					// and
																					// 2
	private static final String SIDEEFFECT_WORK_SHEET_NAME = "Sheet1";
	private static final String SIDE_EFFECTS_COLUMN = "SideEffects";
	private static final int SIDEEFFECTS_COLUMN_INDEX = 0;
	private static final int PATTERN_COLUMN_INDEX = 2;

	private static final int PATTERN_IN_MIDDLE = 2;
	private static final int PATTERN_IN_BEGINNING = 1;
	private static final int PATTERN_IN_LAST = 3;

	public enum SLOT {
		LEFT("LEFT"), RIGHT("RIGHT"), NONE("NONE");

		public String position;

		SLOT(String slotPostionLocal) {
			this.position = slotPostionLocal;
		}
	}

	public enum PATTERN_WORKSHEET {
		_5X("##### Side Effect", SLOT.LEFT, 5), 
		_4X("#### Side Effect",	SLOT.LEFT, 4), 
		_3X("### Side Effect", SLOT.LEFT, 3), 
		_2X("## Side Effect", SLOT.LEFT, 2), 
		_1X("# Side Effect",SLOT.LEFT, 1), 
		_0X("Side Effect", SLOT.NONE, 1), 
		_X1("Side Effect #", SLOT.RIGHT, 1), 
		_X2("Side Effect ##",SLOT.RIGHT, 2), 
		_X3("Side Effect ###", SLOT.RIGHT, 3), 
		_X4("Side Effect ####", SLOT.RIGHT, 4), 
		_X5("Side Effect #####",SLOT.RIGHT, 5);

		public String worksheetName;
		public String slotPosition;
		public int nValue;

		PATTERN_WORKSHEET(String worksheetNameLocal, SLOT slotPos, int nValue) {
			this.worksheetName = worksheetNameLocal;
			this.slotPosition = slotPos.position;
			this.nValue = nValue;
		}
	}

	@Autowired
	private DataMapper dataMapper;
	private MaxentTagger tagger;

	public DataMapper getDataMapper() {
		return dataMapper;
	}

	public void setDataMapper(DataMapper dataMapper) {
		this.dataMapper = dataMapper;
	}

	public Pattern getPattern(Long id) throws ServiceException {

		return dataMapper.getPattern(id);
	}

	public void addSplittedReview(SplittedReview review)
			throws ServiceException {
		dataMapper.addSplittedReview(review);

	}

	private ExcelColumnData readExcel(InputStream fileInputStream,
			String worksheetName, String columnName, Integer columnIndex)
			throws ServiceException {
		ExcelColumnData returnData = new ExcelColumnData();
		Workbook workbook;

		List<String> columnData = new ArrayList<String>();
		try {

			workbook = WorkbookFactory.create(fileInputStream);

			Sheet workSheet = workbook.getSheet(worksheetName);

			readColumn(worksheetName, columnName, columnIndex, returnData,
					columnData, workSheet);

		} catch (Exception e) {

			e.printStackTrace();
		}

		return returnData;
	}

	private void readColumn(String worksheetName, String columnName,
			Integer columnIndex, ExcelColumnData returnData,
			List<String> columnData, Sheet workSheet) {
		Iterator<Row> rowIterator = workSheet.iterator();
		while (rowIterator.hasNext()) {
			readRow(columnIndex, columnData, rowIterator);
		}

		returnData.setColumnName(columnName);
		returnData.setWorksheetName(worksheetName);
		returnData.setData(columnData);
	}

	private void readRow(Integer columnIndex, List<String> columnData,
			Iterator<Row> rowIterator) {
		Row row = rowIterator.next();
		Iterator<Cell> cellIterator = row.cellIterator();
		while (cellIterator.hasNext()) {
			Cell cell = cellIterator.next();

			if (row.getRowNum() > 0) { // To filter column headings
				if (cell.getColumnIndex() == columnIndex.intValue()) {// To
																		// match
																		// column
																		// index
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_NUMERIC:
						columnData.add(cell.getNumericCellValue() + "");
						break;
					case Cell.CELL_TYPE_STRING:
						columnData.add(cell.getStringCellValue());
						break;
					}
				}
			}
		}
	}

	public ExcelColumnData readReviews() throws ServiceException {
		Resource resource = new ClassPathResource(REVIEW_EXCEL);

		try {
			return readExcel(resource.getInputStream(), REVIEW_WORK_SHEET_NAME,
					REVIEW_COLUMN, REVIEW_COLUMN_INDEX);
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				resource.getInputStream().close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		return null;
	}

	public List<PatternExcelColumnData> readPattern() throws ServiceException {

		List<PatternExcelColumnData> returnList = new ArrayList<PatternExcelColumnData>();
		PatternExcelColumnData temp;
		Resource resource = new ClassPathResource(PATTERN_EXCEL);
		for (PATTERN_WORKSHEET sheet : PATTERN_WORKSHEET.values()) {
			try {

				ExcelColumnData columnData = readExcel(
						resource.getInputStream(), sheet.worksheetName,
						sheet.worksheetName, PATTERN_COLUMN_INDEX);
				temp = new PatternExcelColumnData();
				temp.setColumnName(columnData.getColumnName());
				temp.setnValue(sheet.nValue);
				temp.setSlotPosition(sheet.slotPosition);
				temp.setData(columnData.getData());
				temp.setWorksheetName(columnData.getWorksheetName());

				returnList.add(temp);
			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		try {
			resource.getInputStream().close();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return returnList;
	}

	public List<String> convertToSentences(String aReview)
			throws ServiceException {
		String commaRemoved, bracketsRemoved;

		List<String> sentences = new ArrayList<String>();

		DocumentPreprocessor dp = new DocumentPreprocessor(new StringReader(
				aReview));

		for (List<HasWord> sentence : dp) {
			commaRemoved = sentence.toString().replace(",", "");
			bracketsRemoved = commaRemoved.substring(1,
					commaRemoved.length() - 1);

			sentences.add(bracketsRemoved);
		}

		return sentences;
	}

	public void addPattern(Pattern pattern) throws ServiceException {
		dataMapper.addPattern(pattern);
	}

	public void findSlotsForPatternsAndInsertSlots() throws ServiceException {
		List<Pattern> patterns = dataMapper.getAllPattern();

		Slot newSlot = new Slot();
		String searchString;
		int i = patterns.size();
		int count = 0;
		for (Pattern pattern : patterns) {

			searchString = getOnlyStrings(pattern.getPattern());

			List<SplittedReview> reviewList = dataMapper
					.getReviewsBySearchText(searchString);
			if (reviewList != null && reviewList.size() > 0) {
				processMatchInSplitReviewAndInsertTheSlot(newSlot, pattern,
						reviewList);
			}
			count = count + 1;
			System.out.println("Total Pattern Count-> " + i + " Processed-> "
					+ count);
		}

	}

	public String getOnlyStrings(String s) {
		java.util.regex.Pattern pattern = java.util.regex.Pattern
				.compile("[^a-z A-Z]");
		Matcher matcher = pattern.matcher(s);
		String number = matcher.replaceAll("");
		return number;
	}

	private void processMatchInSplitReviewAndInsertTheSlot(Slot newSlot,
			Pattern pattern, List<SplittedReview> reviewList) {

		for (SplittedReview matchingReview : reviewList) {

			String slotText;
			slotText = findSlot(pattern, matchingReview);

			if (slotText.isEmpty())
				return;

			insertNewSlot(matchingReview, newSlot, pattern, slotText);

		}
	}

	private void compareSlotWithExternalParties(SplittedReview matchingReview,
			String slotText) {
		String sideEffects = makeHTTPCallsToExtractSideEffect(slotText);
		if (sideEffects.isEmpty() == false) {
			updateUserReview(matchingReview.getUserReviewId(), sideEffects);
			System.out.println("inserted>>");
		}
	}

	public void updateUserReview(Long id, String sideEffects) {
		UserReview fromDB = dataMapper.getUserReviewById(id);
		if (fromDB.getSideEffects() == null
				|| fromDB.getSideEffects().isEmpty()) {
			dataMapper.updateUserReviewWithSideEffects(sideEffects, id);
		} else {
			String[] incoming = sideEffects.split(",");
			String[] existing = fromDB.getSideEffects().split(",");
			Set<String> unique = new HashSet<String>();
			for (String s : incoming) {
				unique.add(s);
			}
			for (String s : existing) {
				unique.add(s);
			}
			String output = convertSetIntoString(unique);
			dataMapper.updateUserReviewWithSideEffects(output, id);
		}

	}

	private String convertSetIntoString(Set<String> unique) {
		String output = "";
		for (String s : unique) {
			if (s.isEmpty()) {
				output = s;
			} else {
				output = output + "," + s;
			}
		}
		return output;
	}

 

	/* Processes the slot
	 * @see sg.edu.ntu.criticalinquiry.textmining.service.Service#processSlotForMining()
	 */
	public void processSlotForMining() {
		List<Slot> slots = dataMapper.getSlots();
		for (Slot s : slots) {
			System.out.println(s.getSlotText());

			// processSlotTextAgainstEntityExtractor(s);
			processSlotTextAgainstLibrary(s);
		 
		}
	}

	/**
	 * Process entire UserReview against Ontology - 3rd party
	 * @param userReview
	 */
	private void processUserReviewAgainstEntityExtractor(UserReview userReview) {
		String matches = makeHTTPCallsToExtractSideEffect(getOnlyStrings(userReview
				.getText()));
		if (matches.isEmpty() == false) {

			updateUserReview(userReview.getId(), matches);
		}

		System.out.println("---------" + matches);

	}

	/**
	 * Processes Slot Against against Ontology - 3rd party
	 * @param slot
	 */
	private void processSlotTextAgainstEntityExtractor(Slot slot) {
		String matches = makeHTTPCallsToExtractSideEffect(slot.getSlotText());
		if (matches.isEmpty() == false) {
			dataMapper.updateSlotSideEffects(matches, slot.getId());
			updateUserReview(slot.getUserReviewId(), matches);
		}

		System.out.println("---------" + matches);

	}

	public void processUserReviewForMining() {
		List<UserReview> userReviews = dataMapper.getUserReviews();
		int count = userReviews.size();
		for (UserReview s : userReviews) {

			processUserReviewAgainstLibrary(s);
			// processUserReviewAgainstEntityExtractor(s);
			System.out.println("---------" + count--);

		}
	}

	/**
	 * Process entire User review (Not splitted one)
	 * @param userReview
	 */
	private void processUserReviewAgainstLibrary(UserReview userReview) {

		String matches = "";
		List<SideEffect> sideEffects = dataMapper
				.getMatchingSideEffects(userReview.getText());
		for (SideEffect effect : sideEffects) {
			if (containsWord(effect.getSideEffect(), userReview.getText())) {

				if (matches.isEmpty()) {
					matches = effect.getSideEffect();
				} else {
					matches = matches + ", " + effect.getSideEffect();
				}

			}
		}
		if (matches.isEmpty() == false) {

			updateUserReview(userReview.getId(), matches);
		}

	}

	/**
	 * Process slot text against side effect library
	 * @param slot
	 */
	private void processSlotTextAgainstLibrary(Slot s) {
		String matches = "";
		List<SideEffect> sideEffects = dataMapper
				.getMatchingSideEffects(getOnlyStrings(s.getSlotText()));
		for (SideEffect effect : sideEffects) {
			if (containsWord(effect.getSideEffect(), s.getSlotText())) {

				if (matches.isEmpty()) {
					matches = effect.getSideEffect();
				} else {
					matches = matches + ", " + effect.getSideEffect();
				}

			}
		}
		if (matches.isEmpty() == false) {
			dataMapper.updateSlotSideEffects(matches, s.getId());
			updateUserReview(s.getUserReviewId(), matches);
		}

		System.out.println("-------------" + matches);
	}

	private Boolean containsWord(String word, String sentence) {

		return getOnlyStrings(sentence.toLowerCase()).contains(
				word.toLowerCase());

	}

	private String findSlot(Pattern pattern, SplittedReview matchingReview) {
		String slotText = "";
		matchingReview.setReviewText(getOnlyStrings(
				matchingReview.getReviewText()).toLowerCase());
		pattern.setPattern(getOnlyStrings(pattern.getPattern()).toLowerCase());
		int patternStartLocation = matchingReview.getReviewText().indexOf(
				pattern.getPattern());
		int patternEndLocation = matchingReview.getReviewText().indexOf(
				pattern.getPattern())
				+ pattern.getPattern().length();
		int patternLocation = findLocationInAText(
				matchingReview.getReviewText(), patternStartLocation,
				patternEndLocation);

		if (pattern.getSlotPosition().equals(SLOT.LEFT.position)) {

			if (patternLocation != PATTERN_IN_BEGINNING) {
				slotText = matchingReview.getReviewText().substring(0,
						patternStartLocation);
			}

		} else if (pattern.getSlotPosition().equals(SLOT.LEFT.position)) {

			if (patternLocation != PATTERN_IN_LAST) {
				slotText = matchingReview.getReviewText().substring(
						patternEndLocation,
						matchingReview.getReviewText().length());
			}

		} else {
			slotText = StringUtils.remove(matchingReview.getReviewText(),
					pattern.getPattern());
		}

		return slotText;
	}

	private int findLocationInAText(String sourceText,
			int searchPatternStartLocation, int searchPatternEndLocation) {
		int patternLocation;
		if (searchPatternEndLocation >= sourceText.length()) {
			patternLocation = PATTERN_IN_LAST;
		} else {
			if (searchPatternStartLocation == 0) {
				patternLocation = PATTERN_IN_BEGINNING;
			} else {
				patternLocation = PATTERN_IN_MIDDLE;
			}
		}
		return patternLocation;
	}

	public String posTag(String text) {
		String tagged = tagger.tagString(text);

		return tagged;
	}

	public String extractNounsFromSlotText(String slotText) {

		return convertArrayIntoSingleString(extractNouns(posTag(slotText)));
	}

	public String extractAdjectivesFromSlotText(String slotText) {
		return convertArrayIntoSingleString(extractAdjectives(posTag(slotText)));
	}

	public String[] extractNouns(String sentenceWithTags) {
		// Split String into array of Strings whenever there is a tag that
		// starts with "._NN"
		// followed by zero, one or two more letters (like "_NNP", "_NNPS", or
		// "_NNS")
		String[] nouns = sentenceWithTags.split("_NN\\w?\\w?\\b");
		// remove all but last word (which is the noun) in every String in the
		// array
		for (int index = 0; index < nouns.length; index++) {
			nouns[index] = nouns[index].substring(
					nouns[index].lastIndexOf(" ") + 1)
			// Remove all non-word characters from extracted Nouns
					.replaceAll("[^\\p{L}\\p{Nd}]", "");
		}
		return nouns;
	}

	private String convertArrayIntoSingleString(String[] array) {
		String singleString = "";
		if (array != null && array.length > 0) {
			for (String str : array) {
				if (str.length() <= 3)
					continue;
				singleString = singleString + " " + str;
			}

		}

		return singleString;
	}

	public String[] extractAdjectives(String sentenceWithTags) {

		String[] adjectives = sentenceWithTags.split("_JJ\\w?\\w?\\b");

		for (int index = 0; index < adjectives.length; index++) {
			adjectives[index] = adjectives[index].substring(
					adjectives[index].lastIndexOf(" ") + 1)

			.replaceAll("[^\\p{L}\\p{Nd}]", "");
		}
		return adjectives;
	}

	public String getLongestString(String[] array) {
		int maxLength = 0;
		String longestString = null;
		for (String s : array) {
			if (s.length() > maxLength) {
				maxLength = s.length();
				longestString = s;
			}
		}
		return longestString;
	}

	private Slot insertNewSlot(SplittedReview splitReview, Slot newSlot,
			Pattern pattern, String subString) {
		newSlot.setUserReviewId(splitReview.getUserReviewId());
		newSlot.setSlotText(subString);
		// newSlot.setHasSideEffect(zeroOrOne);
		dataMapper.addSlot(newSlot);
		return newSlot;
	}

	public void extractReviewWithoutSelectedKeyWordsAndWriteAsCSVFile()
			throws ServiceException, IOException {
		ExcelColumnData reviewColumn = readReviews();
		List<String> data = reviewColumn.getData();

		ICsvListWriter listWriter = null;
		try {
			listWriter = new CsvListWriter(new FileWriter(
					"target/extracted.csv"), CsvPreference.STANDARD_PREFERENCE);

			// the header elements are used to map the bean values to each
			// column (names must match)
			final String[] header = new String[] { "review" };
			final CellProcessor[] processors = getProcessors();

			// write the header
			listWriter.writeHeader(header);

			for (String row : data) {
				if (checkForStopWord(row))
					continue;
				listWriter.write(row, processors);
			}

		} finally {
			if (listWriter != null) {
				listWriter.close();
			}
		}

	}

	private boolean checkForStopWord(String row) {
		return row != null
				&& row.isEmpty() == false
				& (row.toLowerCase().contains("side effect")
						|| row.toLowerCase().contains("ill effect")
						|| row.toLowerCase().contains("side-effect")
						|| row.toLowerCase().contains("ill-effect")
						|| row.toLowerCase().contains("sideeffect") || row
						.toLowerCase().contains("illeffect"));
	}

	private static CellProcessor[] getProcessors() {

		final CellProcessor[] processors = new CellProcessor[] {

		new NotNull() // data
		};

		return processors;
	}

	private static CellProcessor[] getExportProcessors() {

		final CellProcessor[] processors = new CellProcessor[] {

		new NotNull(), new NotNull()
		// data
		};

		return processors;
	}

	public ExcelColumnData readSideEffects() throws ServiceException {
		Resource resource = new ClassPathResource(SIDEEFFECT_EXCEL);

		try {
			return readExcel(resource.getInputStream(),
					SIDEEFFECT_WORK_SHEET_NAME, SIDE_EFFECTS_COLUMN,
					SIDEEFFECTS_COLUMN_INDEX);
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				resource.getInputStream().close();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		return null;
	}

	public void addSideEffect(SideEffect effect) throws ServiceException {
		dataMapper.addSideEffect(effect);

	}

	public void addUserReview(UserReview userReview) throws ServiceException {
		dataMapper.addUserReview(userReview);

	}

	/* compares any text with 3rd party ontology and extracts the Entity
	 * @see sg.edu.ntu.criticalinquiry.textmining.service.Service#makeHTTPCallsToExtractSideEffect(java.lang.String)
	 */
	public String makeHTTPCallsToExtractSideEffect(String text) {

		text = text.replace(" ", "+");

		String http = "http://data.bioontology.org/annotator?text="
				+ text
				+ "&apikey=93e8f13c-97e8-4e07-a85c-30d3737361bc&exclude_synonyms=true&expand_mappings=true";
		Set<String> bestCandidates = new HashSet<String>();
		try {

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet getRequest = new HttpGet(new URI(http));

			HttpResponse response = httpClient.execute(getRequest);
			JSONParser parser = new JSONParser();
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(response.getEntity().getContent())));

			String output;

			while ((output = br.readLine()) != null) {
				Object obj = parser.parse(output);
				JSONArray array = (JSONArray) obj;
				if (array != null && array.size() > 0) {
					for (int i = 0; i < array.size(); i++) {
						JSONObject each = (JSONObject) array.get(i);

						JSONObject annotatedClass = (JSONObject) each
								.get("annotatedClass");
						String id = annotatedClass.get("@id").toString()
								.toLowerCase();
						if ((id.contains(ABNORMALITIES_ONTOLOGY)
								|| id.contains(ONTOAD_ONTOLOGY) || id.contains(MESH_ONTOLOGY) || id
									.contains(RCD_ONTOLOGY))) { //
							JSONArray annotationArray = (JSONArray) each
									.get("annotations");
							JSONObject annoObj = (JSONObject) annotationArray
									.get(0);
							bestCandidates.add(annoObj.get("text").toString());
						}

					}
				}

			}

			httpClient.getConnectionManager().shutdown();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (Exception e) {

			e.printStackTrace();

		}

		if (bestCandidates.size() > 0) {
			return convertSetIntoString(bestCandidates).toLowerCase();
		}
		return "";

	}

}
