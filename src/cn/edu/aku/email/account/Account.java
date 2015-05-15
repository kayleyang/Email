package cn.edu.aku.email.account;


public class Account
{
	//列名
	private String account;// 帐号
	private String password;// 密码
	private String transport_protocol = "smtp";// 发送协议
	private String transport_host;// 发送协议服务器
	private int transport_port = 25;// 发送协议端口
	private String store_protocol = "pop3";// 接收协议
	private String store_host;// 接收协议服务器
	private int store_port = 110;// 接收协议端口
	//空参构造方法
	public Account(){}
	//
	public Account(String account, String password){
		String domain = account.substring(account.indexOf("@") + 1,	account.length());
		this.transport_host = this.transport_protocol + "." + domain;
		this.store_host = this.store_protocol + "." + domain;
		this.account = account;
		this.password = password;
	}
	public Account(String account, String password,
			String transport_protocol, String transport_host, int transport_port,
			String store_protocol, String store_host, int store_port) {
		super();
		this.account = account;
		this.password = password;
		this.transport_protocol = transport_protocol;
		this.transport_host = transport_host;
		this.transport_port = transport_port;
		this.store_protocol = store_protocol;
		this.store_host = store_host;
		this.store_port = store_port;
	}

	public String getTransport_host() {
		return transport_host;
	}
	public void setTransport_host(String transport_host) {
		this.transport_host = transport_host;
	}
	public String getStore_host() {
		return store_host;
	}
	public void setStore_host(String store_host) {
		this.store_host = store_host;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTransport_protocol() {
		return transport_protocol;
	}
	public void setTransport_protocol(String transport_protocol) {
		this.transport_protocol = transport_protocol;
	}
	public int getTransport_port() {
		return transport_port;
	}
	public void setTransport_port(int transport_port) {
		this.transport_port = transport_port;
	}
	public String getStore_protocol() {
		return store_protocol;
	}
	public void setStore_protocol(String store_protocol) {
		this.store_protocol = store_protocol;
	}
	public int getStore_port() {
		return store_port;
	}
	public void setStore_port(int store_port) {
		this.store_port = store_port;
	}
	@Override
	public String toString() {
		return "Account [account=" + account + ", password=" + password
				+ ", transport_protocol=" + transport_protocol
				+ ", transport_port=" + transport_port + ", transport_host="
				+ transport_host + ", store_protocol=" + store_protocol
				+ ", store_port=" + store_port + ", store_host=" + store_host
				+ "]";
	}
	
}