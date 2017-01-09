package com.tzp.fieldvalidate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ValidateConfig {
	private Map<String,Map<String,Object>> fieldConfig;
	private Map<String,List<String>> styleConfig;
	
	public Map<String, Map<String, Object>> getFieldConfig() {
		return fieldConfig;
	}
	
	public void setFieldConfig(Map<String, Map<String, Object>> fieldConfig) {//Map<校验请求key以逗号分隔(key,parentKey1,parentKey2...), Map<字段ognl表达式, 字段配置(字符串或map)>>
		Map<String,String[]> parentRelation = new HashMap<String,String[]>();//校验请求的父子继承关系
		Map<String,Map<String,Object>> resolvedFieldConfig = new HashMap<String,Map<String,Object>>();//解析后的校验请求字段配置
		for(Entry<String,Map<String,Object>> entry : fieldConfig.entrySet()){
			String validateReqKey = entry.getKey();
			String[] validateReqKeys = validateReqKey.split(",");//validateReq1,validateReq2,validateReq3含义:validateReq1是实际的校验请求key;validateReq1继承validateReq2和validateReq3
			if(validateReqKeys.length > 1){
				parentRelation.put(validateReqKeys[0], Arrays.copyOfRange(validateReqKeys, 1, validateReqKeys.length));//配置校验请求的父子继承关系
			}
			resolvedFieldConfig.put(validateReqKeys[0], entry.getValue());//初步设置校验请求及其字段配置映射
		}
		for(String validateReqKey : resolvedFieldConfig.keySet()){
			resolveParent(validateReqKey,resolvedFieldConfig,parentRelation);//处理请求校验和其继承的父请求校验的关系（继承父请求校验的字段配置）
		}
		this.fieldConfig = Collections.unmodifiableMap(resolvedFieldConfig);
	}
	
	/**
	 * 通过递归，处理校验请求的父子继承关系，使得子校验请求能够继承所有的父请求的字段配置
	 * @param validateReqKey 子校验请求key
	 * @param resolvedFieldConfig 承载校验请求字段配置解析结果的map
	 * @param parentRelation 校验请求的父子关系map
	 */
	private void resolveParent(String validateReqKey,
			Map<String, Map<String, Object>> resolvedFieldConfig,//Map<校验请求key, Map<字段ognl表达式, 字段配置(字符串或map)>>
			Map<String, String[]> parentRelation) {
		String[] parents = parentRelation.get(validateReqKey);
		if(parents == null) return;
		for(String parentValidateReq : parents){
			if(parentRelation.containsKey(parentValidateReq)){//如果父校验请求还有父继承，则递归向上处理
				resolveParent(parentValidateReq, resolvedFieldConfig, parentRelation);
			}
			for(Entry<String, Object> parentEntry : resolvedFieldConfig.get(parentValidateReq).entrySet()){//将父校验请求的字段配置加到子校验请求的字段配置里
				if(!resolvedFieldConfig.get(validateReqKey).containsKey(parentEntry.getKey())){//如果父配置和子配置都有一样的字段配置，则以子配置为准
					resolvedFieldConfig.get(validateReqKey).put(parentEntry.getKey(), parentEntry.getValue());
				}
			}
		}
		parentRelation.remove(validateReqKey);
	}

	public Map<String, List<String>> getStyleConfig() {
		return styleConfig;
	}
	
	public void setStyleConfig(Map<String, List<String>> styleConfig) {
		this.styleConfig = styleConfig;
	}
}
