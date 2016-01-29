package sg.edu.ntu.criticalinquiry.textmining.domain;

import org.apache.ibatis.type.Alias;

@Alias("userreview")
public class UserReview {

	private Long id;
	private String text;
	private String sideEffects;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSideEffects() {
		return sideEffects;
	}
	public void setSideEffects(String sideEffects) {
		this.sideEffects = sideEffects;
	}


	
}
