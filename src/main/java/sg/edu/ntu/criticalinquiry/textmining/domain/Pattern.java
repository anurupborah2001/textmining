package sg.edu.ntu.criticalinquiry.textmining.domain;

import org.apache.ibatis.type.Alias;

@Alias("pattern")
public class Pattern {
	private Long id;
	private String pattern;
	private Integer nValue;
	private String slotPosition;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public Integer getnValue() {
		return nValue;
	}

	public void setnValue(Integer nValue) {
		this.nValue = nValue;
	}

	public String getSlotPosition() {
		return slotPosition;
	}

	public void setSlotPosition(String slotPosition) {
		this.slotPosition = slotPosition;
	}

}
