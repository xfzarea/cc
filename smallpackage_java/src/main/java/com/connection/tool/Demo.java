package com.connection.tool;

import java.text.DecimalFormat;

import javazoom.jl.converter.Converter;
import javazoom.jl.decoder.JavaLayerException;

public class Demo {
	public static boolean Mp3ToWav(String inputFilePath, String outputFilePath){ 
		Converter aConverter = new Converter();
			try{
				aConverter.convert(inputFilePath, outputFilePath);
		} catch (JavaLayerException e){
			e.printStackTrace();
				return false;
			}
			return true; 
		}
	public static void main(String[] args) {
		
		double a = 10.17;
		double b = Math.round((a*0.02));
		System.out.println("123::"+a*2/100);
		System.out.println(b);
		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println(df.format(a*0.02));
		System.out.println(String.format("%.2f", 4.9*0.02));
		int amount = (int)(Double.parseDouble(a*98+""));//单位为分
//		System.out.println((int)(Double.parseDouble(5.1+"")));
		int total_fee = (int)(Double.parseDouble("5.1")*100);
		System.out.println(Double.parseDouble("5.1")*100);
		System.out.println(0.28*100);
		
		System.out.println(total_fee);
		System.out.println(Double.parseDouble(String.format("%.2f", 0.29*2)));
		
	}
}
