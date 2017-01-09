package com.tzp.springTest.controller;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tzp.fieldvalidate.ValidateExecutor;
@Controller
@RequestMapping("/test1")
public class TestController1 {
	@Autowired
	private ValidateExecutor validateExecutor;
	
	@RequestMapping(value="/method1",method=RequestMethod.POST)
	@ResponseBody
	public Object testMethod1(final HttpServletRequest request){
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
		}
		Map<String,Object> param = copyParameterFromRequest(request);
		List<Map<String, String>> errors = validateExecutor.validate("/test1/method1", param);
		return errors;
	}
	
	@RequestMapping(value="/method2",method=RequestMethod.POST)
	@ResponseBody
	public Object testMethod2(final HttpServletRequest request,@RequestBody Map param){
		Long begin = System.currentTimeMillis();
		List<Map<String, String>> errors = validateExecutor.validate("/test1/method2", param);
		Long end = System.currentTimeMillis();
		System.out.println("------------------"+(end-begin)+"---------------------------");
		return errors;
	}
	
	@RequestMapping(value="/method3",method=RequestMethod.POST)
	@ResponseBody
	public Object testMethod3(final HttpServletRequest request,@RequestBody Object[] params){
		Long begin = System.currentTimeMillis();
		List<Map<String, String>> errors = validateExecutor.validate("/test1/method3", params);
		Long end = System.currentTimeMillis();
		System.out.println("------------------"+(end-begin)+"---------------------------");
		return errors;
	}
	
	public Map<String,Object> copyParameterFromRequest(HttpServletRequest request){
		Map<String,Object> returnMap = new HashMap<String,Object>();
		Enumeration<String> enumeration= request.getParameterNames();
		while(enumeration.hasMoreElements()){
			String parameterName = enumeration.nextElement();
			String[] values = request.getParameterValues(parameterName);
			if(values.length == 1){
				returnMap.put(parameterName,values[0]);
			}else{
				returnMap.put(parameterName,values);
			}
		}
		return returnMap;
	}
}
