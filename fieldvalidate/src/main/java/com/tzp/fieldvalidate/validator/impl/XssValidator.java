package com.tzp.fieldvalidate.validator.impl;

import java.util.List;

import com.tzp.fieldvalidate.validator.ReturnValueValidator;

public class XssValidator implements ReturnValueValidator{

	public Object validate(String fieldName, Object fieldValue, String rule) {
		if(fieldValue != null && fieldValue.getClass().isArray()){
			fieldValue = ((Object[])fieldValue)[0];
		}else if(fieldValue != null && fieldValue instanceof List){
			fieldValue = ((List)fieldValue).get(0);
		}
		return xssEncode(String.valueOf(fieldValue));
	}
	
	private String xssEncode(String s) {
		if (s == null || s.isEmpty()) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '&':
				sb.append("&amp;");
				break;
			case '<':
				sb.append("&lt;");
				break;
			case '>':
				sb.append("&gt;");
				break;
			case '\"':
				sb.append("&quot;");
				break;
			case '\'':
				sb.append("&apos;");
				break;
			case ' ':
				sb.append("&nbsp;");
				break;
			default:
				sb.append(c);
				break;
			}
		}
		return sb.toString();
	}
}
