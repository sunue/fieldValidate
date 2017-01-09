package com.tzp.fieldvalidate.validator;

public interface ReturnValueValidator extends Validator{
	public Object validate(String fieldName,Object fieldValue,String rule);
}
