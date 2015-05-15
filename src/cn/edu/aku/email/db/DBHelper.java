package cn.edu.aku.email.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import cn.edu.aku.email.account.AccountColumn;
import cn.edu.aku.email.receive.ReceiveColumn;

public class DBHelper extends SQLiteOpenHelper{
	public static final int DATABASE_VERSION = 4;//版本
	public static final String DATABASE_NAME = "email.db";//数据库名
	public static final String RECEIVE_TABLE = "receive";//表名
	public static final String ACCOUNT_TABLE = "account";//表名
	//创建表
	private static final String CREATE_RECEIVE_TABLE = 
			"CREATE TABLE " + RECEIVE_TABLE +" ("
					+ ReceiveColumn._ID+" integer primary key autoincrement,"
					+ ReceiveColumn.ACCOUNT +" text,"
					+ ReceiveColumn.FOLDER +" text,"
					+ ReceiveColumn.MESSAGE_ID +" text,"
					+ ReceiveColumn.SEND_DATE +" text,"
					+ ReceiveColumn.SUBJECT+" text);";
	private static final String CREATE_ACCOUNT_TABLE = 
			"CREATE TABLE " + ACCOUNT_TABLE +" ("
					+ AccountColumn._ID+" integer primary key autoincrement,"
					+ AccountColumn.ACCOUNT+" text,"
					+ AccountColumn.PASSWORD+" text,"
					+ AccountColumn.TRANSPORT_PROTOCOL+" text,"
					+ AccountColumn.TRANSPORT_HOST+" text,"
					+ AccountColumn.TRANSPORT_PORT+" text,"
					+ AccountColumn.STORE_PROTOCOL+" text,"
					+ AccountColumn.STORE_HOST+" text,"
					+ AccountColumn.STORE_PORT+" text);";

	public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(CREATE_RECEIVE_TABLE);
			db.execSQL(CREATE_ACCOUNT_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			db.execSQL("DROP TABLE IF EXISTS " + RECEIVE_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE);
			onCreate(db);
		}
}
