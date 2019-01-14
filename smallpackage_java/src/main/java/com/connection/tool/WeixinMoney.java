package com.connection.tool;

import java.util.Random;
/**
 *  ΢�ź�������㷨@��ؿƼ�
 * @author ����
 * @version v 1.0
 *
 */
public class WeixinMoney {

    private int peoples; //�����˷�
    private double allMoney; //�ܽ��


    public WeixinMoney(int  peoples,double allMoney){
        this.peoples =peoples;
        this.allMoney = allMoney;
    }
    public static double getMoney(WeixinMoney wmoney){
         if (wmoney.getPeoples()==1) {//���ʣ������Ϊ1����������ʣ�µ�Ǯ
             wmoney.setPeoples(0);
             return wmoney.getAllMoney();
         }
         //��������㷨
         Random r     = new Random();
         double min   = 0.01; //
         double max = wmoney.getAllMoney()/wmoney.getPeoples()*2;

         double money = r.nextDouble()*max;
         money = money <= min ? 0.01: money;
         money = Math.floor(money * 100) / 100;

         wmoney.setPeoples(wmoney.getPeoples()-1);
         wmoney.setAllMoney(wmoney.getAllMoney()-money);
         return money;
    }
    public int getPeoples() {
        return peoples;
    }
    public void setPeoples(int peoples) {
        this.peoples = peoples;
    }


    public double getAllMoney() {
        return allMoney;
    }


    public void setAllMoney(double allMoney) {
        this.allMoney = allMoney;
    }
    
    public static void main(String[] args) {
        int people = 5;
        WeixinMoney w  = new WeixinMoney(people,-100);
//        double all  =0d;
//        for (int i = 0; i <people; i++) {
//            double d = WeixinMoney.getMoney(w);
//            System.out.println(d);
//            all+=d;
//        }
//        System.out.println("�ܹ�=="+all);
        double m = WeixinMoney.getMoney(w);
        System.out.println("m:="+m);
    }
}