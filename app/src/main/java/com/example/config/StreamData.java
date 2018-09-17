package com.example.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class StreamData implements Serializable{
	private static final long serialVersionUID = -4814317626664283732L;
	public ArrayList<Map<String, String>> list=new ArrayList<Map<String,String>>();
	public long todayStream=-1;//--今日用了的流量，用于在关机时保存
	public long base=0;
}
