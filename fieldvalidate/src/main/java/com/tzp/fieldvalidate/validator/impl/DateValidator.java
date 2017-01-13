package com.tzp.fieldvalidate.validator.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.tzp.fieldvalidate.validator.ReturnValueValidator;

public class DateValidator implements ReturnValueValidator{
	private String formatPattern;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	//hdsdhsh	
	public Object validate(String fieldName, Object fieldValue, String rule){
		if(fieldValue != null && fieldValue.getClass().isArray()){
			fieldValue = ((Object[])fieldValue)[0];
		}else if(fieldValue != null && fieldValue instanceof List){
			fieldValue = ((List)fieldValue).get(0);
		}
		
		Date date = null;
		try {
			date = dateFormat.parse(String.valueOf(fieldValue));
		} catch (ParseException e) {
			throw new RuntimeException(String.format("can't parse string【%s】 to date using the pattern【%s】", fieldValue,formatPattern));
		}
		return date;
	}
	
	public void setFormatPattern(String formatPattern) {
		this.formatPattern = formatPattern;
		dateFormat.applyPattern(formatPattern);
	}
}
