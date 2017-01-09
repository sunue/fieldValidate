package com.tzp.fieldvalidate.validator.impl;

import java.util.List;

import com.tzp.fieldvalidate.validator.BreakableValidator;


public class RequiredValidator implements BreakableValidator{
	
	public boolean validate(String fieldName, Object fieldValue, String rule) {
		if(fieldValue != null && fieldValue.getClass().isArray()){
			fieldValue = ((Object[])fieldValue)[0];
		}else if(fieldValue != null && fieldValue instanceof List){
			fieldValue = ((List)fieldValue).get(0);
		}
		Boolean required = Boolean.valueOf(rule);
		if(required){
			if(fieldValue == null || "".equals(String.valueOf(fieldValue))){
				throw new RuntimeException("validate.error");
			}
		}else{
			if(fieldValue == null || "".equals(String.valueOf(fieldValue))){
				return true;
			}
		}
		return false;
	}

}
