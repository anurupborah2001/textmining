package sg.edu.ntu.criticalinquiry.textmining.app;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import sg.edu.ntu.criticalinquiry.textmining.domain.Pattern;
import sg.edu.ntu.criticalinquiry.textmining.domain.SideEffect;
import sg.edu.ntu.criticalinquiry.textmining.domain.SplittedReview;
import sg.edu.ntu.criticalinquiry.textmining.domain.UserReview;
import sg.edu.ntu.criticalinquiry.textmining.service.Service;
import sg.edu.ntu.criticalinquiry.textmining.service.ServiceException;
import sg.edu.ntu.criticalinquiry.textmining.service.ServiceImpl;
import sg.edu.ntu.criticalinquiry.textmining.vo.ExcelColumnData;
import sg.edu.ntu.criticalinquiry.textmining.vo.PatternExcelColumnData;

public class App {
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"spring-config.xml");

		Service service = (ServiceImpl) context.getBean("service");
		try {
			
			
			
			//readAndInsertReviews(service); 
			// readAndInsertPatterns(service);
			
			//readAndInsertSideEffects(service);
			
			 
			//findSlots(service);
			//processSlot(service);
			//processUserReview(service);
			
			 
		 
		  // eliminateStopWord(service);
	 
		} catch (Exception e) {
		 
			e.printStackTrace();
		}
	}

	private static void processSlot(Service service) throws ServiceException {
		service.processSlotForMining();
	}

	private static void eliminateStopWord(Service service)
			throws ServiceException, IOException {
		service.extractReviewWithoutSelectedKeyWordsAndWriteAsCSVFile();
	}

	/**
	 * Process and extract side effect
	 * @param service
	 */
	private static void processUserReview(Service service) {
		service.processUserReviewForMining();
	}

	/**
	 * Find slots for pattern
	 * @param service
	 * @throws ServiceException
	 */
	private static void findSlots(Service service) throws ServiceException {
		service.findSlotsForPatternsAndInsertSlots();
	}

	/**
	 * Reads SideEffects from Excel and Insert into table
	 * @param service
	 * @throws ServiceException
	 */
	private static void readAndInsertSideEffects(Service service) throws ServiceException {
	 
		
		ExcelColumnData column = service.readSideEffects();
		SideEffect newSideEffect;
		for(String eachSideEffect: column.getData()){
			newSideEffect = new SideEffect();
			newSideEffect.setSideEffect(eachSideEffect);
			try{
				System.out.println(">>"+newSideEffect.getSideEffect());
				System.out.println(">>");
				
				 
				service.addSideEffect(newSideEffect);
				System.out.println("Inserting");
			}catch(Exception  e ){
				e.printStackTrace();
			}
			
		}
		
	}

	/**
	 * read pattern from excel and inserts into DB
	 * @param service
	 * @throws ServiceException
	 */
	private static void readAndInsertPatterns(Service service)
			throws ServiceException {
		Pattern pattern;
		 List<PatternExcelColumnData> list = service.readPattern();
		 Collections.sort(list);
		 for(PatternExcelColumnData patternData:list){
			 for(String data:patternData.getData()){
				 pattern = new Pattern();
				 pattern.setnValue(patternData.getnValue());
				 pattern.setSlotPosition(patternData.getSlotPosition());
				 pattern.setPattern(data);
				 service.addPattern(pattern);
			 }
		 }
	}

	/**
	 * Imports review from Excel to DB 
	 * Split the review and Inserts the splitted review
	 * @param service
	 * @throws ServiceException
	 */
	private static void readAndInsertReviews(Service service)
			throws ServiceException {
		ExcelColumnData data = service.readReviews();
		 
		 UserReview userReview = new UserReview();
		 SplittedReview splittedReview;
		 for(String aReview : data.getData()){
			 userReview = new UserReview();
			 userReview.setText(aReview);
			  service.addUserReview(userReview);
			  System.out.println("Inserted user reviews");
			 List<String> sentences = service.convertToSentences(aReview);
				 for(String string: sentences){
					 splittedReview = new SplittedReview();
					 splittedReview.setUserReviewId(userReview.getId());
					 splittedReview.setReviewText(string);
					 service.addSplittedReview(splittedReview);
					 System.out.println("-------> Inserting splitted reviews");
				 
					
				 } 
			
		 }
		 
	  
	}
}
