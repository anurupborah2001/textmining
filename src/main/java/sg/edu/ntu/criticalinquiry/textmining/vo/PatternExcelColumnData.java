package sg.edu.ntu.criticalinquiry.textmining.vo;

public class PatternExcelColumnData extends ExcelColumnData implements Comparable<PatternExcelColumnData> {
	
	private Integer nValue;
	private String slotPosition;
	 
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
	public int compareTo(PatternExcelColumnData o) {
		this.nValue.compareTo(o.nValue);
		return 0;
	}
	
	

}
