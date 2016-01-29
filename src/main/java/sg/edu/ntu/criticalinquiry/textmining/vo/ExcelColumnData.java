package sg.edu.ntu.criticalinquiry.textmining.vo;

import java.util.List;

public class ExcelColumnData {
	private String columnName;
	private String worksheetName;
	private List<String> data;

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getWorksheetName() {
		return worksheetName;
	}

	public void setWorksheetName(String worksheetName) {
		this.worksheetName = worksheetName;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

}
