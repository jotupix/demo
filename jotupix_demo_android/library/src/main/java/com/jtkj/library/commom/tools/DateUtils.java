package com.jtkj.library.commom.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
	public static String getStringDateShort(int year, int month, int day) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		String dateString = formatter.format(calendar.getTime());
		return dateString;
	}
	
	 public static int comparDate(String DATE1, String DATE2) {
	        DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
	        try {
	            Date dt1 = df.parse(DATE1);
	            Date dt2 = df.parse(DATE2);
	            if (dt1.getTime() > dt2.getTime()) {
	                System.out.println("dt1 在dt2前");
	                return 1;
	            } else if (dt1.getTime() < dt2.getTime()) {
	                System.out.println("dt1在dt2后");
	                return -1;
	            } else {
	                return 0;
	            }
	        } catch (Exception exception) {
	            exception.printStackTrace();
	        }
	        return 0;
	    }
}
