package sg.edu.ntu.criticalinquiry.textmining.service;

import java.io.IOException;
import java.util.List;

import sg.edu.ntu.criticalinquiry.textmining.domain.Pattern;
import sg.edu.ntu.criticalinquiry.textmining.domain.SideEffect;
import sg.edu.ntu.criticalinquiry.textmining.domain.SplittedReview;
import sg.edu.ntu.criticalinquiry.textmining.domain.UserReview;
import sg.edu.ntu.criticalinquiry.textmining.vo.ExcelColumnData;
import sg.edu.ntu.criticalinquiry.textmining.vo.PatternExcelColumnData;

public interface Service {

	Pattern getPattern(Long id) throws ServiceException;
	void addUserReview(UserReview userReview) throws ServiceException;
	void addSplittedReview(SplittedReview review) throws ServiceException;
	void addPattern(Pattern pattern) throws ServiceException;
	void addSideEffect(SideEffect effect) throws ServiceException;
	void findSlotsForPatternsAndInsertSlots() throws ServiceException;
	  String[]  extractNouns(String sentenceWithTags);
	void extractReviewWithoutSelectedKeyWordsAndWriteAsCSVFile() throws ServiceException,IOException;
 
	ExcelColumnData readReviews() throws ServiceException;
	ExcelColumnData readSideEffects() throws ServiceException;
	
	
	List<PatternExcelColumnData> readPattern() throws ServiceException;
	List<String> convertToSentences(String aReview) throws ServiceException;
	String posTag(String text);
	 String makeHTTPCallsToExtractSideEffect(String slotText);
	 void updateUserReview(Long id,
				String sideEffects);
	
 
	
	 void processSlotForMining() throws ServiceException;
	 void processUserReviewForMining();
	 public  String getOnlyStrings(String s);
}