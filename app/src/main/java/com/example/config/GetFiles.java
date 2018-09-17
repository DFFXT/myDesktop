package com.example.config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.URL;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Environment;
import android.webkit.JavascriptInterface;

@SuppressLint("SimpleDateFormat") public class GetFiles {
	public static File url=Environment.getExternalStorageDirectory();
	public File[] files=url.listFiles();
	static String rootPath=url.toString()+"/.0/";
	public static String logPath=rootPath+"log.txt";
	static String appPath=rootPath+"app/";
	static String cachePath=rootPath+"cache/";
	static String iconPath=cachePath+"icon/";
	public long kb=0;
	private Date date;
	MediaPlayer player=new MediaPlayer();
	private SimpleDateFormat DateFormat = new SimpleDateFormat("HH:mm:ss");
	public ArrayList<String> name=new ArrayList<String>();
	public ArrayList<String> path=new ArrayList<String>();
	public ArrayList<String> size=new ArrayList<String>();
	public DecimalFormat format=new DecimalFormat("0.00");
	public GetFiles(){//--构建文件目录
		createPath(rootPath);
		createPath(appPath);
		createPath(cachePath);
		createPath(iconPath);
	}
	public static String getRootPath(){
		return createPath(rootPath);
	}
	public static String getAppPath(){
		return createPath(appPath);
	}
	public static String getCachePath(){
		return createPath(cachePath);
	}
	public static String getIconPath(){
		return createPath(iconPath);
	}
	@JavascriptInterface
	public String JSUSE(){//---网页js调用路径
		return rootPath;
	}
	public void getfiles(File[] files,String[] suffix,int len){//---读取文件信息 [文件树，后缀，读取长度[负数无限制]]
		if(files!=null){
			for(File file_D:files){
				if(file_D.isFile()){
					for(int i=0;i<suffix.length;i++){//---判断后缀
						if(file_D.getName().endsWith(suffix[i])){
							name.add(file_D.getName());
							path.add(file_D.getAbsolutePath());
							size.add(format.format(file_D.length()/1024.0/1024));//--获取文件的大小MB
							break;
						}
					}
					if(path.size()>=len&&len>0){
						return;
					}
				}
				else{//---递归
					getfiles(file_D.listFiles(),suffix,len);
				}
			}
		}
	}
	@JavascriptInterface
	public void getfiles(String[] suffix,int len){
		getfiles(files,suffix, len);
	}
	public void clearEmptyFolder(File[] files,File file){//--清除空文件夹和空文件
		if(files!=null){
			for(File file_T:files){//--无循环及为空//---不能删除 【空->空】 文件夹
				if(file_T.isDirectory()){
					if(file_T.listFiles().length==0){//---空文件jia
						file_T.delete();
						continue;
					}
					clearEmptyFolder(file_T.listFiles(),file_T);
				}
				if(file_T.length()==0&&file_T.isFile()){//--空文件
					file_T.delete();
				}
			}
		}
	}
	public boolean isFileExists(String path){
		File file=new File(path);
		if(!file.exists()) return false;
		return true;
	}
	@JavascriptInterface
	public String readText(String file){//---读取文本文件
		String text="";
		try {
			InputStream fInputStream=new FileInputStream(file);
			Reader reader=new InputStreamReader(fInputStream);
			int tmpchar;
			while((tmpchar=reader.read())!=-1){
				text+=(char)tmpchar;
			}
			reader.close();
		} catch (Exception e) {
			date=new Date(System.currentTimeMillis());
			String daString="读取文件："+DateFormat.format(date);
			write(rootPath+"log.txt", daString+e+"\r\n\r\n", true);
			return "";
		}
		return text;
	}
	@JavascriptInterface
	public String readNetData(String url){//--需要新线程
		String text="";
		try {
			URL Url=new URL(url);
			InputStream stream = Url.openConnection().getInputStream();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(stream));
			String tmp=null;
			while((tmp=bReader.readLine())!=null){
				text+=tmp;
			}
		} catch (Exception e) {
			date=new Date(System.currentTimeMillis());
			String daString="Get Net Data："+DateFormat.format(date);
			write(rootPath+"log.txt", daString+e+"\r\n\r\n", true);
			return "ERROR";
		}
		return text;
	}
	public boolean NetDataToLocal(String url,String path){//--需要新线程  保存至本地
		byte[] byte1= new byte[1024];
		try {
			URL Url=new URL(url);
			InputStream stream = Url.openConnection().getInputStream();
			OutputStream oStream = new BufferedOutputStream(new FileOutputStream(path));
			int tmp=0;
			long all=0;
			while((tmp=stream.read(byte1,0,1024))!=-1){
				oStream.write(byte1,0,tmp);
				all+=tmp;
				kb=all;
			}
			oStream.close();
			stream.close();
		} catch (Exception e) {
			date=new Date(System.currentTimeMillis());
			String daString="Net To Local："+DateFormat.format(date);
			write(rootPath+"log.txt", daString+e+"\r\n\r\n", true);
			return false;
		}
		return true;
	}

	public void append(byte[] b){
		try{
			RandomAccessFile file=new RandomAccessFile(rootPath+"log.txt", "rw");
			file.seek(file.length()-1);
			file.write(b);
			file.close();
		}catch(Exception e){
			write( rootPath+"log.txt", "测试"+e, true);
		}
	}
	@JavascriptInterface
	public boolean write(String path,String text,boolean buer){//----文件写入
		try {
			File FILE=new File(path);
			FileWriter fWriter=new FileWriter(FILE,buer);//-------buer为是否向文件追加
			BufferedWriter bWriter=new BufferedWriter(fWriter);
			bWriter.write(text);
			bWriter.close();
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	public void saveDrawableToImg(Drawable drawable,String path,String fileName){//--保存drawbale图片
		Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
		saveBitmapToImg(bitmap, path, fileName);
	}
	public void saveBitmapToImg(Bitmap bitmap,String path,String fileName){
		createPath(path);
		ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 100, outputStream);
		byte[] b=outputStream.toByteArray();
		try {
			FileOutputStream outputStream2=new FileOutputStream(path+fileName);
			outputStream2.write(b);
			outputStream2.close();
			outputStream.close();
		} catch (Exception e) {
			write(logPath, "save Image false:"+e+"\r\n", false);
		}
	}
	public void UnZip(String ZipFile,String UnZipPath){//--UnZipPath带 '/'
		try {
			createPath(UnZipPath);
			ZipFile file=new ZipFile(ZipFile);
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> zlist=(Enumeration<ZipEntry>) file.entries();
			ZipEntry zEntry=null;
			while(zlist.hasMoreElements()){
				zEntry=(ZipEntry) zlist.nextElement();
				if(zEntry.isDirectory()){
					File folder=new File(UnZipPath+zEntry.getName());
					folder.mkdirs();
					continue;
				}
				OutputStream stream = new BufferedOutputStream(new FileOutputStream(UnZipPath+zEntry.getName()));
				InputStream iStream= new BufferedInputStream(file.getInputStream(zEntry));
				int len=0;
				byte[] byte1=new byte[1024];
				while((len=iStream.read(byte1, 0, 1024))!=-1){
					stream.write(byte1,0,len);
				}
				iStream.close();
				stream.close();
				file.close();
			}
		} catch (Exception e) {
			date=new Date(System.currentTimeMillis());
			String daString="解压缩："+DateFormat.format(date);
			write(rootPath+"log.txt", daString+e+"\r\n\r\n", true);
			e.printStackTrace();
		}
	}

	public static String createPath(String path){
		File file=new File(path);
		if(!file.exists())
			file.mkdirs();
		return path;
	}

















}
