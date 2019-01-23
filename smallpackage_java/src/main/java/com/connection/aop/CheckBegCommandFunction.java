package com.connection.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.connection.dao.SysAdminDao;
@Component
@Aspect
public class CheckBegCommandFunction {
	@Autowired
	private SysAdminDao sys;
	@Autowired
	HttpServletResponse response;
	@Before("@annotation(com.connection.aop.FruitAspectBC)")
	public void watch() throws Throwable {
		
		int userBegCommand =sys.getcheckFunction("userBegCommand");
	
		HashMap<String,Integer> result = new HashMap<String,Integer>() ;
		

		
		
	
		if(userBegCommand==0) {
			result.put("userBegCommand", 0);
		}
		

		if(!result.isEmpty()) {
			ServletOutputStream out = null;
			 out = response.getOutputStream ();
			 out.print(result.toString());
			 throw new MyCustomException();
			
		}
		
	}
}
