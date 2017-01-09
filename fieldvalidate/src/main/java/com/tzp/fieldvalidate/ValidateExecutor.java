package com.tzp.fieldvalidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ognl.Ognl;
import ognl.OgnlException;

import com.tzp.fieldvalidate.validator.BreakableValidator;
import com.tzp.fieldvalidate.validator.ReturnValueValidator;
import com.tzp.fieldvalidate.validator.Validator;

public class ValidateExecutor {
	/**请求字段校验规则配置**/
	private ValidateConfig validateConfig;
	/**校验器注册表**/
	private Map<String,Validator> validatorRegistry;
	
	public List<Map<String,String>> validate(String requestId,Object paramObj){
		List<Map<String,String>> errors = new ArrayList<Map<String,String>>();
		Map<String, Object> fieldsConfig = validateConfig.getFieldConfig().get(requestId);//从请求字段配置中取出待校验请求的字段校验样式配置，Map<校验字段的ognl表达式, 字段校验style配置>
		if(fieldsConfig == null){
			throw new RuntimeException(String.format("can't find the fieldsConfig of the requestId:%s", requestId));//未到找到校验请求的字段校验样式配置
		}
		if((paramObj instanceof Collection) || (paramObj != null && paramObj.getClass().isArray())){
			recursiveCallInternalValidate("top",fieldsConfig,paramObj,errors,null);
		}else{
			internalValidate(fieldsConfig, paramObj, errors, null);
		}
		
		return errors;
	}
	
	private void internalValidate(Map<String, Object> fieldsConfig,Object paramObj,List<Map<String,String>> errors,String fieldPathPrefix){
		if(paramObj == null){
			throw new RuntimeException(String.format("the object waiting for validating can't be null,path:%s", fieldPathPrefix==null?"top":fieldPathPrefix));
		}
		/*循环校验请求里配置的字段样式校验规则，根据style对字段进行校验*/
		for(Entry<String, Object> fieldConfig : fieldsConfig.entrySet()){
			String fieldOgnlExpress = fieldConfig.getKey();//字段ognl表达式
			Object fieldStyle = fieldConfig.getValue();//字段style
			Object fieldValue = null;//字段值
			try {
				fieldValue = Ognl.getValue(fieldOgnlExpress, paramObj);
			} catch (OgnlException e) {
				throw new RuntimeException(String.format("an exception occurs when try to use 【%s】 to get the fieldValue from %s", fieldOgnlExpress,paramObj.getClass()),e);
			}
			
			if(fieldStyle instanceof Map){
				recursiveCallInternalValidate(fieldOgnlExpress,(Map<String, Object>)fieldStyle,fieldValue,errors,fieldPathPrefix);
			}else if(fieldStyle instanceof String){
				validateField(fieldOgnlExpress,(String)fieldStyle,fieldValue,fieldPathPrefix,errors,paramObj);
			}else{
				throw new RuntimeException(String.format("the style config 【%s】 of field 【%s】 is error,the config of field must be String or map", fieldStyle,(fieldPathPrefix==null?fieldOgnlExpress:fieldPathPrefix+"."+fieldOgnlExpress)));
			}
		}
	}
	
	private void recursiveCallInternalValidate(String fieldOgnlExpress,Map<String, Object> fieldsConfig,Object recursiveObj,List<Map<String,String>> errors,String fieldPathPrefix){
		if(!((recursiveObj instanceof Collection) || (recursiveObj != null && recursiveObj.getClass().isArray()))){
			throw new RuntimeException(String.format("according to the format,【%s】 should be a collection or a array but actual %s",(fieldPathPrefix==null?fieldOgnlExpress:fieldPathPrefix+"."+fieldOgnlExpress),(recursiveObj == null?"null":recursiveObj.getClass())));
		}
		if(recursiveObj instanceof Collection){
			Collection fieldValueCollection = (Collection)recursiveObj;
			for(Object obj : fieldValueCollection){
				String fieldPathPrefixNew = (fieldPathPrefix == null?fieldOgnlExpress+"[idx]":fieldPathPrefix+"."+fieldOgnlExpress+"[idx]");
				internalValidate((Map<String, Object>) fieldsConfig,obj,errors,fieldPathPrefixNew);
			}
		}
		else if(recursiveObj.getClass().isArray()){
			Object[] fieldValueArray = (Object[]) recursiveObj;
			for(int i=0;i<fieldValueArray.length;i++){
				Object obj = fieldValueArray[i];
				String fieldPathPrefixNew = (fieldPathPrefix == null?fieldOgnlExpress+"[idx]":fieldPathPrefix+"."+fieldOgnlExpress+"[idx]");
				internalValidate((Map<String, Object>) fieldsConfig,obj,errors,fieldPathPrefixNew);
			}
		}
	}
	
	private void validateField(String fieldOgnlExpress,String fieldStyle,Object fieldValue,String fieldPathPrefix,List<Map<String,String>> errors,Object paramObj){
		Map<String, String> fieldValidatorRuleMap = new HashMap<String, String>();//字段级校验器配置map
		int beginIndex = fieldStyle.indexOf('[');
		int endIndex = fieldStyle.indexOf(']');
		if(beginIndex*endIndex <= 0){//中括号要么成对出现要么不出现，否则格式错误
			throw new RuntimeException(String.format("the style config 【%s】 of field 【%s】 is error,please check the format of '[]'", fieldStyle,(fieldPathPrefix==null?fieldOgnlExpress:fieldPathPrefix+"."+fieldOgnlExpress)));
		}else if(beginIndex>0){
			String fieldValidatorRuleStr = fieldStyle.substring(beginIndex+1, endIndex);//字段级校验器配置
			fieldStyle = fieldStyle.substring(0,beginIndex);//字段的校验style名称
			String[] fieldValidatorRules = fieldValidatorRuleStr.split(";");//多个字段级校验器用;隔开
			for(String fieldValidatorRule : fieldValidatorRules){
				int firstColonIndex = fieldValidatorRule.indexOf(":");//校验器和其对应rule用：隔开
				String validatorName = firstColonIndex==-1?fieldValidatorRule:fieldValidatorRule.substring(0, firstColonIndex);
				String validateRule = firstColonIndex==-1?"":fieldValidatorRule.substring(firstColonIndex+1);
				fieldValidatorRuleMap.put(validatorName, validateRule);
			}
		}
		
		List<String> styleConfig = validateConfig.getStyleConfig().get(fieldStyle);//style级校验器及其rule配置
		if(styleConfig == null){
			throw new RuntimeException(String.format("can't find the config of style 【%s】 in the styleConfig", fieldStyle));
		}
		for(String validatorConfig : styleConfig){//循环style配置的校验器列表，对字段进行各项校验
			int firstColonIndex = validatorConfig.indexOf(":");//校验器和其对应rule用：隔开
			String validatorName = firstColonIndex==-1?validatorConfig:validatorConfig.substring(0, firstColonIndex);
			String validateRule = firstColonIndex==-1?"":validatorConfig.substring(firstColonIndex+1);
			
			if(fieldValidatorRuleMap.containsKey(validatorName)){//字段级校验器配置将覆盖style级的对应的校验器配置
				validateRule = fieldValidatorRuleMap.get(validatorName);
			}
			Validator validator = validatorRegistry.get(validatorName);
			if(validator == null){
				throw new RuntimeException(String.format("can't find the validator 【%s】 in the validatorRegistry", validatorName));
			}
			try {
				if(validator instanceof ReturnValueValidator){
					Object convertedObj = ((ReturnValueValidator)validator).validate(fieldOgnlExpress, fieldValue,validateRule);//校验器根据规则校验字段值并进行类型转换
					if(convertedObj != null){
						Ognl.setValue(fieldOgnlExpress, paramObj, convertedObj);//将类型转换后的值set回待校验root对象
					}
				}else if(validator instanceof BreakableValidator){
					boolean breakable = ((BreakableValidator)validator).validate(fieldOgnlExpress, fieldValue, validateRule);//校验器根据规则校验字段值并根据返回值判断是否退出检验流程
					if(breakable) break;
				}
			} catch (RuntimeException e) {
				if("validate.error".equals(e.getMessage())){
					String errorCode = "validate.error."+validatorName;
					String fieldName = (fieldPathPrefix==null?fieldOgnlExpress:fieldPathPrefix+"."+fieldOgnlExpress);
					Map<String,String> error = new HashMap<String, String>();
					error.put("fieldName", fieldName);
					error.put("fieldValue", String.valueOf(fieldValue));
					error.put("errorCode", errorCode);
					errors.add(error);
					break;
				}else{
					throw e;
				}
			} catch (OgnlException e) {
				throw new RuntimeException(String.format("an exception occurs when try to use 【%s】 to set the convertedValue into %s", fieldOgnlExpress,paramObj.getClass()),e);
			}
		}
	}
	/**  
	 * 设置请求字段校验规则配置  
	 * @param validateConfig 请求字段校验规则配置  
	 */
	public void setValidateConfig(ValidateConfig validateConfig) {
		this.validateConfig = validateConfig;
	}
	/**  
	 * 设置校验器注册表  
	 * @param validatorRegistry 校验器注册表  
	 */
	public void setValidatorRegistry(Map<String, Validator> validatorRegistry) {
		this.validatorRegistry = validatorRegistry;
	}
	
}
