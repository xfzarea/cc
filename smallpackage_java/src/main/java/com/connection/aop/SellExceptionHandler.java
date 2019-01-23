package com.connection.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SellExceptionHandler {
	
	   
	    @ExceptionHandler(value = MyCustomException.class)
	    public void handlerAuthorizeException(){
	        

}
}
