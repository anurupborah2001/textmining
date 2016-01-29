package sg.edu.ntu.criticalinquiry.textmining.domain;

import org.apache.ibatis.type.Alias;

@Alias("splittedreview")
public class SplittedReview {
	
	private Long id;
	private String reviewText;
	private Boolean isProcessed;
	private Long userReviewId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getReviewText() {
		return reviewText;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public Boolean getIsProcessed() {
		return isProcessed;
	}
	public void setIsProcessed(Boolean isProcessed) {
		this.isProcessed = isProcessed;
	}
	public Long getUserReviewId() {
		return userReviewId;
	}
	public void setUserReviewId(Long userReviewId) {
		this.userReviewId = userReviewId;
	}
	
	
	

}
