/**
 * 
 */
package com.slapps.kwfinder;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvToBean;

/**
 * @author Muthukumaran
 *
 */
public class KWData {

	@CsvBindByPosition(position = 0)
	private String URL;
	
	@CsvBindByPosition(position = 1)
	private String keyword;
	
	@CsvBindByPosition(position = 2)
	private String textToExport;

	
	public String getURL() {
		return URL;
	}



	public void setURL(String uRL) {
		URL = uRL;
	}



	public String getKeyword() {
		return keyword;
	}



	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}



	public String getTextToExport() {
		return textToExport;
	}



	public void setTextToExport(String textToExport) {
		this.textToExport = textToExport;
	}



	/** 
	 * Beware ! This will return all the values.
	 */
	@Override
	public String toString() {
		String para  = (textToExport != null && textToExport.length() > 0) ? "Paragraph = " + 
								textToExport.substring(0, textToExport.length()/2) : 
							"Paragraph = ";
		return "URL = " + getURL() + "   Keyword = " + getKeyword() + "  " + para;
	}
}
