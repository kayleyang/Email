package cn.edu.aku.email.read;

import cn.edu.aku.email.R;
import cn.edu.aku.email.main.MainActivity;
import cn.edu.aku.email.service.MimeMessageService;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ReadActivity extends Activity {

	private static final String TAG = "ReadActivity";
	private ActionBar actionBar;
	
	private TextView fromTextView;
	private TextView senddataTextView;
	private WebView textWebView;
	
	private String subject = "";
	private String from = "";
	private String sendData = "";
	private String text = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read);
		initView();
		initData();
	}

	private void initView() {
		// TODO Auto-generated method stub
		actionBar = getActionBar();
		fromTextView = (TextView) findViewById(R.id.read_tv_from);
		senddataTextView = (TextView) findViewById(R.id.read_tv_senddate);
		textWebView = (WebView) findViewById(R.id.read_wv_text);
	}
	
	private void initData() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		subject = intent.getStringExtra("subject");
		from = intent.getStringExtra("from");
		sendData = intent.getStringExtra("sendData");
		text = intent.getStringExtra("text");
		
		setTitle(subject);
		fromTextView.setText(from);
		senddataTextView.setText(sendData);
		textWebView.loadDataWithBaseURL(null, text, "text/html", "utf-8", null);

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		getMenuInflater().inflate(R.menu.read, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home://取消
			Intent intent = new Intent(ReadActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
}
