package cn.edu.aku.email.main;

import java.util.ArrayList;
import java.util.List;

import cn.edu.aku.email.R;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.account.AddAccountActivity;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.ReceiveService;
import cn.edu.aku.email.setup.SetUpActivity;
import cn.edu.aku.email.write.WriteActivity;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class MainActivity extends Activity implements ActionBar.OnNavigationListener{
	//定义ActionBar
	private ActionBar actionBar;
	//定义适配器
	private SpinnerAdapter adapter;
	//标记当前Fragment
	private String FragmentTag = "";
	private ReceiveService receiveService;
	private AccountService accountService;
	private List<String> accountList;
	
	private Thread mThread;
	private static final int REFRESH_SUCCESS = 1001;
	private static final int REFRESH_FAILURE = 1002;
	
	//定义Handler句柄
	private final Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REFRESH_SUCCESS:
				Toast.makeText(getApplicationContext(), "更新成功", Toast.LENGTH_LONG).show();
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initView();

		initData();
	}
	/**
	 * 初始化组件
	 */
	private void initView(){
		//得到ActionBar
		actionBar = getActionBar();
		
		
	}

	/**
	 * 初始化数据
	 */
	@SuppressLint("NewApi")
	private void initData(){
		//设置ActionBar标题不显示
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//设置ActionBar的背景
//		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_gradient_bg));

		//设置ActionBar左边默认的图标是否可用
		actionBar.setDisplayUseLogoEnabled(true);

		//设置导航模式为Tab选项下拉导航模式
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);  
		
		//定义一个下拉列表数据适配器
		accountService = new AccountService(getApplicationContext());
		accountList = accountService.findAllAccount();
		adapter =  new SpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, accountList);
		actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				// 生成自定的Fragment
				FragmentManager manager = getFragmentManager();
				FragmentTransaction transaction = manager.beginTransaction();
				// 将Activity中的内容替换成对应选择的Fragment 
				MainFragment fragment = new MainFragment();
				final int size = accountList.size();
				if (size == (itemPosition+1)) {
					Intent intent = new Intent(getApplicationContext(), AddAccountActivity.class);
					overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					MainActivity.this.startActivity(intent);
				} else {
					String [] accounts = accountList.toArray(new String[size]);
					transaction.replace(android.R.id.content, fragment, accounts[itemPosition]);
					transaction.commit();
					FragmentTag = accounts[itemPosition];
				}
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);	
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent1 = new Intent(MainActivity.this, SetUpActivity.class);
			MainActivity.this.startActivity(intent1);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			return true;
		case R.id.main_menu_edit:
			Intent intent2 = new Intent(MainActivity.this, WriteActivity.class);
			intent2.putExtra("Tag", FragmentTag);
			MainActivity.this.startActivity(intent2);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			return true;
		case R.id.main_menu_refresh:
			//选中刷新按钮后刷新一秒钟
			item.setActionView(R.layout.actionbar_progress);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					item.setActionView(null);
				}
			}, 1000);
			if (mThread == null) {
				mThread = new Thread(refreshRunnable);
				mThread.start();// 线程启动
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	Runnable refreshRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			receiveService = new ReceiveService(getApplicationContext());
			ArrayList<Account> accounts = accountService.findAll();
			for (Account account : accounts) {
				receiveService.ReceiveEml(account);
			}
			mHandler.obtainMessage(REFRESH_SUCCESS).sendToTarget();
		}
	};
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		for ( MyOntouchListener listener : touchListeners )	{
			listener.onTouchEvent( event );
		}
		return super.onTouchEvent( event );
	}

	private ArrayList<MyOntouchListener> touchListeners = new ArrayList<MainActivity.MyOntouchListener>();

	public void registerListener(MyOntouchListener listener) {
		touchListeners.add( listener );
	}

	public void unRegisterListener(MyOntouchListener listener) {
		touchListeners.remove( listener );
	}

	public interface MyOntouchListener {
		public void onTouchEvent(MotionEvent event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		for ( MyDispatchTouchListener listener : dispatchTouchListeners )	{
			listener.dispatchTouchEvent( event);
		}
		return super.dispatchTouchEvent(event);
	}
	private ArrayList<MyDispatchTouchListener> dispatchTouchListeners = new ArrayList<MainActivity.MyDispatchTouchListener>();

	public void registerListener(MyDispatchTouchListener listener) {
		dispatchTouchListeners.add( listener );
	}

	public void unRegisterListener(MyDispatchTouchListener listener) {
		dispatchTouchListeners.remove( listener );
	}

	public interface MyDispatchTouchListener {
		public void dispatchTouchEvent(MotionEvent event);
	}
	/**
	 * 自定义SpinnerAdapter
	 * @author yankai
	 *
	 */
	private class SpinnerAdapter extends ArrayAdapter<String> {
	    Context context;
	    List<String> items = new ArrayList<String>();
	    public SpinnerAdapter(final Context context, final int textViewResourceId, final List<String> accountList) {
	        super(context, textViewResourceId, accountList);
	        this.items = accountList;
	        this.context = context;
	    }
	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            LayoutInflater inflater = LayoutInflater.from(context);
	            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
	        }
	        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
	        tv.setText(items.get(position));
	        tv.setTextColor(Color.WHITE);
	        return convertView;
	    }
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            LayoutInflater inflater = LayoutInflater.from(context);
	            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
	        }
	        // android.R.id.text1 is default text view in resource of the android.
	        // android.R.layout.simple_spinner_item is default layout in resources of android.
	        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
	        tv.setText(items.get(position));
	        tv.setTextColor(Color.WHITE);
	        return convertView;
	    }

	}
}
