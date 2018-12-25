package com.connection.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.connection.service.interfaces.RedisService;
import com.connection.tool.Result;

@RestController
public class RedisTestController {
	@Autowired
	private RedisService redis;
	@GetMapping("/redisGetJob")
	public Result test(@RequestParam("id")int id){
		
		Map<String,Object>job = redis.getJobById(id);
		System.out.println(job);
		return Result.successResult();
	}
}
