package com.example.config;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class SqlOperate {
    private Context context;
    private SQLiteDatabase sql;
    public SqlOperate(Context context,DataBaseName dataBaseName){
        this.context=context;
        sql=context.openOrCreateDatabase(dataBaseName.name(),Context.MODE_PRIVATE,null);
    }







    public enum DataBaseName{
        AppDataBase
    }
}
