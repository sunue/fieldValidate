package com.tzp.fieldvalidate.validator;

public interface BreakableValidator extends Validator{
	public boolean validate(String fieldName,Object fieldValue,String rule);
}
