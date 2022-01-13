package com.java.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

public class SQLiteController {

	private Context context;
	private SQLiteDatabase db = null;
	OpenHelper openHelper;

	public  String TODO="CREATE TABLE IF NOT EXISTS todo" +
	        "(  todo_id INTEGER PRIMARY KEY AUTOINCREMENT" +
	        ",  name TEXT" +
			",  descr TEXT" +
	        ",  created_by INTEGER " +
			",  selected INTEGER " +
			",  due_date TEXT" +
	        ",  updated_date TEXT" +
			",  view_order INTEGER DEFAULT 0" +
	        ",  created_date TEXT" +
	        ",  active_status INTEGER);";

	public  String SUBTODO="CREATE TABLE IF NOT EXISTS subtodo" +
			"(  subtodo_id INTEGER PRIMARY KEY AUTOINCREMENT" +
			",  name TEXT" +
			",  descr TEXT" +
			",  created_by INTEGER " +
			",  todo_id INTEGER " +
			",  selected INTEGER " +
			",  view_order INTEGER DEFAULT 0" +
			",  due_date TEXT" +
			",  updated_date TEXT" +
			",  created_date TEXT" +
			",  active_status INTEGER);";


	
	public  String SimpleTable="CREATE TABLE IF NOT EXISTS simple" +
	        "(  simpleid INTEGER PRIMARY KEY AUTOINCREMENT" +
	        ",  fullname TEXT" +
	        ",  phone TEXT " +
	        ");";
	
	
	public String createTables()
	{ 
	    String return_value="Pending"; 
	    //db.execSQL("DROP TABLE attendance");
	    db.execSQL(TODO);
		db.execSQL(SUBTODO);
	    //db.execSQL(Attendance);
	 
	    return return_value;
	}

	public SQLiteController(Context par_context)
	{
		this.context = par_context;
		if (db != null  && db.isOpen()) {
			db.close();
			db = null;
		}
		openHelper = OpenHelper.getInstance(context);

		this.db = openHelper.getWritableDatabase();
			
		createTables();	
	}

	public void open(){

		if(!db.isOpen()){
			this.db = openHelper.getWritableDatabase();
		}

	}
	
	public void close() {
		if (openHelper != null) {
			openHelper.close();
			db.close();
			Log.d("CLOSED", "YES");
		}
	}

	public String fireQuery(String query)
	{
	    String return_value="Done";
	    try
	    {
	        db.execSQL(query);
	    }
	    catch(Exception ex)
	    {
	        return_value=ex.getMessage();
	    }
	    return return_value;
	}

	public Cursor retrieve(String query)
	{ 
		return db.rawQuery(query,null);
	}

	public String fireSafeQuery(String query,String[] values)
	{
	    String return_value="";
	
	    try
	    {
	        //SQLiteStatement stmt = db.compileStatement("SELECT * FROM Country WHERE code = ?");
	        //stmt=database.prepareStatement(query);
	        SQLiteStatement stmt = db.compileStatement(query);
	
	        int i=1;
	        for(String data:values)
	        {
	            stmt.bindString(i, data);
	            i++;
	        }
	        stmt.execute();
	        return_value="Done";
	
	    }
	    catch(Exception ex)
	    {
	        return_value=ex.getMessage();
	    }
	    return return_value;
	}

	public Cursor safeRetrieve(String query,String[] values)
	{
	    return db.rawQuery(query,values);
	}


}

