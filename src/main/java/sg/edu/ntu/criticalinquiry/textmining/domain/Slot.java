package sg.edu.ntu.criticalinquiry.textmining.domain;

import org.apache.ibatis.type.Alias;

@Alias("Slot")
public class Slot {
	private Long id;
	private Long userReviewId;
	private String slotText;
	private Integer hasSideEffect;
	private String sideEffects;
	
	
	public Integer getHasSideEffect() {
		return hasSideEffect;
	}
	public void setHasSideEffect(Integer hasSideEffect) {
		this.hasSideEffect = hasSideEffect;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
 
	public Long getUserReviewId() {
		return userReviewId;
	}
	public void setUserReviewId(Long userReviewId) {
		this.userReviewId = userReviewId;
	}
	public String getSlotText() {
		return slotText;
	}
	public void setSlotText(String slotText) {
		this.slotText = slotText;
	}
	public String getSideEffects() {
		return sideEffects;
	}
	public void setSideEffects(String sideEffects) {
		this.sideEffects = sideEffects;
	}
	
	

}
