package com.tzp.fieldvalidate.validator.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tzp.fieldvalidate.validator.ReturnValueValidator;


public class PatternValidator implements ReturnValueValidator{
	private Map<String, Pattern> patternPool = new ConcurrentHashMap<String, Pattern>();
	
	public Object validate(String fieldName, Object fieldValue, String rule) {
		if(fieldValue != null && fieldValue.getClass().isArray()){
			fieldValue = ((Object[])fieldValue)[0];
		}else if(fieldValue != null && fieldValue instanceof List){
			fieldValue = ((List)fieldValue).get(0);
		}
		Pattern pattern = patternPool.get(rule);
		if(pattern == null){
			pattern = Pattern.compile(rule);
			patternPool.put(rule, pattern);
		}
		Matcher matcher = pattern.matcher(String.valueOf(fieldValue));
		if(!matcher.matches()){
			throw new RuntimeException("validate.error");
		}
		return null;
	}

}
