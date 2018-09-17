package com.example.config;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadUpdate implements View.OnClickListener{//----下载更新
	private String path=GetFiles.getAppPath()+"desktop.apk";
    private Context context;
	public DownloadUpdate(Context context){
		this.context=context;
	}
	public void onClick(View v) {

		new Thread(()->{
			OkHttpClient client=new OkHttpClient();

			Request request=new Request.Builder().url("http://112.74.25.13/app/desktop/desktop.apk").build();

			try {
				Response response=client.newCall(request).execute();
				if(response.isSuccessful()){
					ResponseBody body=response.body();
					if(body==null){
						throw new IOException("null body");
					}
					InputStream is=body.byteStream();

					BufferedInputStream bis=new BufferedInputStream(is);
					BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(path));
					int offset;
					byte buff[]=new byte[1024];
					while((offset=bis.read(buff))>0){
						bos.write(buff,0,offset);
					}
					bis.close();
					bos.flush();
					bos.close();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
					context.startActivity(intent);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();

	}

}