package cn.edu.aku.email.setup;

import cn.edu.aku.email.R;
import cn.edu.aku.email.patternpassword.PatternPasswordActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SetUpActivity extends Activity {
	
	private static final String TAG = "SetUpActivity";
	private Button settingButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
		initData();
	}

	private void initView() {
		// TODO Auto-generated method stub
		settingButton = (Button) findViewById(R.id.btn_setting);
	}

	private void initData() {
		// TODO Auto-generated method stub
		settingButton.setOnClickListener(new OnSettingClickListener());
	}
	
	private class OnSettingClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(getApplicationContext(), PatternPasswordActivity.class);
			startActivity(intent);
		}
		
	}
}
