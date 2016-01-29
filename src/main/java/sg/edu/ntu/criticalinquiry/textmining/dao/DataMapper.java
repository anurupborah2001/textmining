package sg.edu.ntu.criticalinquiry.textmining.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import sg.edu.ntu.criticalinquiry.textmining.domain.Pattern;
import sg.edu.ntu.criticalinquiry.textmining.domain.SideEffect;
import sg.edu.ntu.criticalinquiry.textmining.domain.Slot;
import sg.edu.ntu.criticalinquiry.textmining.domain.SplittedReview;
import sg.edu.ntu.criticalinquiry.textmining.domain.UserReview;

public interface DataMapper {
	
	@Select("SELECT * FROM Pattern WHERE id = #{id}")
	Pattern getPattern(@Param("id") Long id);
	
	@Select("SELECT * from Pattern order by nValue desc")
	List<Pattern> getAllPattern();
	
	@Select("Select * from SideEffect")
	List<SideEffect> getAllSideEffect();
	
	@Select ("SELECT * FROM textmining.SplittedReview  where reviewText regexp #{searchText}")
	List<SplittedReview> getReviewsBySearchText(String searchText);
	
	@Insert("INSERT into SplittedReview(reviewText,userReviewId) VALUES(#{reviewText},#{userReviewId})")
	void addSplittedReview(SplittedReview review);

	@Insert("INSERT into Pattern(pattern, nValue, slotPosition) VALUES (#{pattern}, #{nValue}, #{slotPosition})")
	void addPattern(Pattern pattern);
	
	@Insert("INSERT into Slot(userReviewId, slotText,hasSideEffect) VALUES (#{userReviewId},#{slotText},#{hasSideEffect})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void addSlot(Slot slot);
	
	@Insert("INSERT into SideEffect( sideEffect) VALUES (#{sideEffect})")
	void addSideEffect(SideEffect sideEffect);
	
	@Insert("INSERT into UserReview(text) VALUES (#{text})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void addUserReview(UserReview userReview);
	
	 
	
	//@Select("SELECT * FROM SideEffect s1 WHERE MATCH (s1.sideeffect) AGAINST(#{slotText} IN NATURAL LANGUAGE MODE)")
	@Select("SELECT s1.sideeffect, MATCH (s1.sideeffect) AGAINST (#{slotText} IN NATURAL LANGUAGE MODE) AS score FROM textmining.sideeffect s1 order by score desc")
    List<SideEffect> getMatchingSideEffects(String slotText);
	
	@Select("SELECT * from SplittedReview where userReviewId = #{id}")
	List<SplittedReview> getSplittedReviewByUserReviewId(Long userReviewId);
	
	;
	
	@Select("SELECT * from UserReview")
	List<UserReview> getUserReviews();
	
 
	
	@Update("Update UserReview set sideEffects= #{newExtractedSideEffects} where id =#{id}")
	void updateUserReviewWithSideEffects(@Param("newExtractedSideEffects")String newExtractedSideEffects, @Param("id") Long id);
	
	@Select("SELECT * from UserReview where id=#{id}")
	UserReview getUserReviewById(@Param("id") Long id);
	
	
	@Select("select * from Slot where CHAR_LENGTH(slotText)")
	List<Slot> getSlots();
	
	@Update("Update Slot set sideEffects= #{newExtractedSideEffects} where id =#{id}")
	void updateSlotSideEffects(@Param("newExtractedSideEffects")String newExtractedSideEffects, @Param("id") Long id);
}
