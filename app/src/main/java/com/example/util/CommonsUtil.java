package com.example.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.StringRes;

import org.litepal.LitePalApplication;

public final class CommonsUtil {
    public static String getString(@StringRes int id){
        return LitePalApplication.getContext().getResources().getString(id);
    }
    public static Bitmap drawableToBitmap(Drawable drawable){
        Bitmap bitmap=null;
        if(drawable instanceof BitmapDrawable){
            bitmap=((BitmapDrawable)drawable).getBitmap();
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(drawable instanceof AdaptiveIconDrawable) {
                Bitmap tmp=Bitmap.createBitmap(drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
                Canvas canvas=new Canvas(tmp);
                drawable.draw(canvas);
                bitmap=tmp;
            }
        }
        return bitmap;

    }
    public static Bitmap roundBitmap(Bitmap bitmap,int radius){
        RectF res=new RectF(0,0,bitmap.getWidth(),bitmap.getHeight());
        Paint paint=new Paint();
        Bitmap dst=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        paint.setColor(0xffffffff);
        Canvas canvas=new Canvas(dst);
        canvas.drawRoundRect(res,radius,radius,paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap,0,0,paint);
        return dst;
    }
}
