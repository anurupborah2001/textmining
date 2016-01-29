package sg.edu.ntu.criticalinquiry.textmining.domain;

import org.apache.ibatis.type.Alias;

@Alias("sideeffect")
public class SideEffect {
	private Long id;
	private String sideEffect;
	private Double score;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getSideEffect() {
		return sideEffect;
	}
	public void setSideEffect(String sideEffect) {
		this.sideEffect = sideEffect;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	@Override
	public String toString() {
		return "SideEffect [sideEffect=" + sideEffect + ", score=" + score
				+ "]";
	}
	
	
	

}
