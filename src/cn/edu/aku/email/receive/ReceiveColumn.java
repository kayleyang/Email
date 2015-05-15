package cn.edu.aku.email.receive;

import android.provider.BaseColumns;

public class ReceiveColumn implements BaseColumns {
	public ReceiveColumn(){}
	//列名
	public static final String ACCOUNT = "account";// 帐号
	public static final String FOLDER = "folder";// 文件夹
	public static final String MESSAGE_ID = "message_id";// MessageId
	public static final String SEND_DATE = "send_date"; //发送日期
	public static final String SUBJECT = "subject"; //主题
	
	//列 索引值
	public static final int _ID_COLUMN = 0;
	public static final int ACCOUNT_COLUMN = 1;
	public static final int FOLDER_COLUMN = 2;
	public static final int MESSAGE_ID_COLUMN = 3;
	public static final int SEND_DATE_COLUMN = 4;
	public static final int SUBJECT_COLUMN = 5;
	
	//查询结果
		public static final String[] COLUMNS ={
			_ID,
			ACCOUNT,
			FOLDER,
			MESSAGE_ID,
			SEND_DATE,
			SUBJECT,
		};
}
