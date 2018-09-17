package com.example.config;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ClickData implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 6422366080994647519L;
	public Map<String, Integer> list=new HashMap<String, Integer>();
	public String startDate="wew";
	public ClickData(){
		Calendar calendar=Calendar.getInstance();
		startDate=calendar.get(Calendar.YEAR)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
	}
}
