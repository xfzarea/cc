package com.connection.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class SellExceptionHandler {
	
	   //如果单使用@ExceptionHandler，只能在当前Controller中处理异常。但当配合@ControllerAdvice一起使用的时候，就可以摆脱那个限制了。
	    @ExceptionHandler(value = MyCustomException.class)
	    public void handlerAuthorizeException(){
	        

}
}
