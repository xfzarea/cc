package com.connection.interceptor;

import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.alibaba.fastjson.JSON;
import com.connection.dao.DataDao;
import com.connection.entity.Admin;
import com.connection.tool.Result;
import com.connection.tool.Util;

public class CheckUserInterceptor extends HandlerInterceptorAdapter {
	@Autowired
	private DataDao dataDao;
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String ip = Util.getIpAddress(request);
		boolean flag = false;
		HashMap<String,Object>secondLimit = dataDao.checkSecond(ip);
		System.out.println(secondLimit);
		if(secondLimit == null||secondLimit.get("second_limit")==null){
			flag = true;
		}else{
			if((Long)secondLimit.get("second_limit")>2){
				flag = true;
			}else{
				flag = false;
			}
		}
		dataDao.saveIp(ip);
		secondLimit = null;
		return flag;
	}
}
