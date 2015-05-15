package cn.edu.aku.email.account;

import android.provider.BaseColumns;
public class AccountColumn implements BaseColumns
{
	public AccountColumn(){}
	//列名
	public static final String ACCOUNT = "account";// 帐号
	public static final String PASSWORD = "password";// 密码
	public static final String TRANSPORT_PROTOCOL = "transport_protocol";// 发送协议
	public static final String TRANSPORT_HOST = "transport_host";// 发送协议
	public static final String TRANSPORT_PORT = "transport_port";// 发送协议端口
	public static final String STORE_PROTOCOL = "store_protocol";// 接收协议
	public static final String STORE_HOST = "store_host";// 接收协议
	public static final String STORE_PORT = "store_port";// 接收协议端口

	//列 索引值
	public static final int _ID_COLUMN = 0;
	public static final int ACCOUNT_COLUMN = 1;
	public static final int PASSWORD_COLUMN = 2;
	public static final int TRANSPORT_PROTOCOL_COLUMN = 3;
	public static final int TRANSPORT_HOST_COLUMN = 4;
	public static final int TRANSPORT_PORT_COLUMN = 5;
	public static final int STORE_PROTOCOL_COLUMN = 6;
	public static final int STORE_HOST_COLUMN = 7;
	public static final int STORE_PORT_COLUMN = 8;

	//查询结果
	public static final String[] COLUMNS ={
		_ID,
		ACCOUNT,
		PASSWORD,
		TRANSPORT_PROTOCOL,
		TRANSPORT_HOST,
		TRANSPORT_PORT,
		STORE_PROTOCOL,
		STORE_HOST,
		STORE_PORT,
	};
}