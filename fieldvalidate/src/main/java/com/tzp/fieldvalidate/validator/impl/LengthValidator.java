package com.tzp.fieldvalidate.validator.impl;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tzp.fieldvalidate.validator.ReturnValueValidator;

public class LengthValidator implements ReturnValueValidator{
	private Pattern pattern = Pattern.compile("^([0-9]|[1-9][0-9]+)|(([0-9]|[1-9][0-9]+)-([0-9]|[1-9][0-9]+))$");//num或num-num格式
	
	public Object validate(String fieldName, Object fieldValue, String rule) {
		if(fieldValue != null && fieldValue.getClass().isArray()){
			fieldValue = ((Object[])fieldValue)[0];
		}else if(fieldValue != null && fieldValue instanceof List){
			fieldValue = ((List)fieldValue).get(0);
		}
		
		Matcher matcher = pattern.matcher(String.valueOf(rule));
		if(!matcher.matches()){
			throw new RuntimeException(String.format("the rule of LengthValidator must like 'n' or 'n-m',【%s】 is error", rule));
		}
		String[] lengthRange = rule.split("-");
		int begin = 0;
		int end = 0;
		if(lengthRange.length == 2){
			begin = Integer.valueOf(lengthRange[0]);
			end = Integer.valueOf(lengthRange[1]);
		}else if(lengthRange.length == 1){
			end = Integer.valueOf(lengthRange[0]);
		}
		if(begin > end){
			throw new RuntimeException(String.format("the begin of range 【%s】 must not be bigger than the end of range 【%s】", begin,end));
		}
		int length = String.valueOf(fieldValue).length();
		if(length>end || length<begin){
			throw new RuntimeException("validate.error");
		}
		return null;
	}

}
