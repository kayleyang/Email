package cn.edu.aku.email.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.edu.aku.email.R;
import cn.edu.aku.email.account.Account;
import cn.edu.aku.email.read.ReadActivity;
import cn.edu.aku.email.receive.Receive;
import cn.edu.aku.email.service.AccountService;
import cn.edu.aku.email.service.MailService;
import cn.edu.aku.email.service.MimeMessageService;
import cn.edu.aku.email.service.ReceiveService;

public class MainFragment extends ListFragment implements OnGestureListener {

	// Fragment对应的标签，当Fragment依附于Activity时得到
	private String TAG;

	private ExpandableListView elistView = null; // 定义树型组件
	private ExpandableListAdapter adapter = null; // 定义适配器对象
	
	private CheckBox chk_star;
	private CheckBox chk_select;
	private TextView child_from;
	private TextView child_subject;
	private TextView child_sendDate;
	private TextView groupTextView;
	
	private LinearLayout linearLayout;
	private Button selectAllButton;
	private Button reversSelectedButton;
	private Button deleteSelectedButton;
	private Button flagSelectedButton;
	
	private HashMap<String, Boolean> selectStatusHashMap;
	private HashMap<String, Boolean> starStatusHashMap;

	private ArrayList<String> folders = null;
	private ArrayList<ArrayList<MimeMessageService>> data = null;
	
	private ReceiveService receiveService;

	private static final int MSG_SUCCESS = 0;// 获取数据成功的标识
	private static final int MSG_FAILURE = 1;// 获取数据失败的标识

	private GestureDetector gestureDetector;
	private Activity activity;
    // 事件状态
    private static  final int SINGLE_TAPUP = 0; //单击
    private static  final int FLING_LEFT = 1; //左滑
    private static  final int FLING_RIGHT = 2; //右滑
    private static  final int LONG_PRESS = 3; //长按
    
    private int eventState = LONG_PRESS;
	
	private Thread mThread;
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SUCCESS:

				adapter = new MyExpandableListAdapter(getActivity()); // 实例化适配器
				elistView.setAdapter(adapter); // 设置适配器
				((BaseExpandableListAdapter)adapter).notifyDataSetChanged();// 通知数据发生了变化
				initDate();
				break;
			case MSG_FAILURE:
				Toast.makeText(getActivity(), "获取邮件失败，请稍候再试！",
						Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		TAG = getTag();
	}
	
	/**
	 * 在Fragment中要使用到GestureDetector，但是在Fragment中么有onTouchEvent(MotionEvent event)方法
	 * 每个Activity都会有onTouchEvent(MotionEvent event)方法，况且Fragment就是一个Activity的组成部分
	 * ，那我们是不是可以用某一种机制来传递这种情况。
	 * 在Activity中，我们任然复写onTouchEvent(MotionEvent event)，不过参数的event需要用我们的Fragment
	 * 中的GestureDetector对象的event代替而已，那么问题的关键就是获取不同Fragment中生成的不同的GestureDetector对象了
	 */
	MainActivity.MyOntouchListener myOntouchListener;
	MainActivity.MyDispatchTouchListener myDispatchTouchListener;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		activity = getActivity();
		gestureDetector = new GestureDetector( activity, this );
		myOntouchListener = new MainActivity.MyOntouchListener() {
			/**
			 * 覆写此方法，以使用手势识别
			 */
			@Override
			public void onTouchEvent(MotionEvent event) {
				// TODO Auto-generated method stub
				gestureDetector.onTouchEvent( event );
			}
		};
		((MainActivity) activity).registerListener( myOntouchListener );
		myDispatchTouchListener = new MainActivity.MyDispatchTouchListener(){
			/**
			 * 覆写此方法，以解决ListView滑动被屏蔽问题
			 */
			@Override
			public void dispatchTouchEvent(MotionEvent event) {
				gestureDetector.onTouchEvent(event);
			}
		};
		((MainActivity) activity).registerListener( myDispatchTouchListener );
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "--------onCreate--------");
		if (mThread == null) {
			mThread = new Thread(getFolderRunnable);
			mThread.start();// 线程启动
		} else {
			// TODO 线程创建失败，提醒
			Log.e(TAG, "创建接收线程失败");
		}
	}

	Runnable getFolderRunnable = new Runnable() {
		@Override
		public void run() {// run()在新的线程中运行
			ReceiveService service = new ReceiveService(getActivity());
			folders = ReceiveService.findFolders(TAG);
			data = ReceiveService.getData(TAG);
			if (data != null) {
				mHandler.obtainMessage(MSG_SUCCESS).sendToTarget();
				Log.i(TAG, "获取data成功");
			} else {
				mHandler.obtainMessage(MSG_FAILURE).sendToTarget();
				Log.i(TAG, "获取data失败");
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(TAG, "--------onCreateView--------");
		View view = inflater.inflate(R.layout.fragment_elist_view, container, false);
		elistView = (ExpandableListView) view.findViewById(android.R.id.list);
		linearLayout = (LinearLayout) view.findViewById(R.id.fragemet_linearLayout);
		selectAllButton = (Button) view.findViewById(R.id.bt_selectAll);
		reversSelectedButton = (Button) view.findViewById(R.id.bt_reversSelected);
		deleteSelectedButton = (Button) view.findViewById(R.id.bt_deleteSelected);
		flagSelectedButton = (Button) view.findViewById(R.id.bt_flagSelected);
		return view;
	}


	private void initDate() {
		// TODO Auto-generated method stub
		linearLayout.setVisibility(View.GONE);
//		elistView.setOnItemClickListener(this);
		selectAllButton.setOnClickListener(new OnSelectAllClickListenerImpl());
		reversSelectedButton.setOnClickListener(new OnReversSelectedClickListenerImpl());
		deleteSelectedButton.setOnClickListener(new OnDeleteSelectedClickListenerImpl());
		flagSelectedButton.setOnClickListener(new OnFlagSelectedClickListenerImpl());
		
		elistView.setOnChildClickListener(new OnChildClickListenerImpl()); // 设置子项单击事件
		elistView.setOnGroupClickListener(new OnGroupClickListenerImpl());// 设置组项单击事件
		elistView.setOnGroupCollapseListener(new OnGroupCollapseListenerImpl());// 关闭分组事件
		elistView.setOnGroupExpandListener(new OnGroupExpandListenerImpl()); // 展开分组事件
		
		receiveService = new ReceiveService(getActivity());
	}

	private class OnSelectAllClickListenerImpl implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Iterator iter = selectStatusHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String)entry.getKey();
				selectStatusHashMap.put(key, true);
			}
			int gourpsSum = adapter.getGroupCount();// 组的数量
			boolean isLast = false;
			for (int i = 0; i < gourpsSum; i++) {
				int childSum = adapter.getChildrenCount(i);// 组中子项的数量
				for (int k = 0; k < childSum; k++) {
					if (k == (childSum - 1)) {
						isLast = true;
					}
					CheckBox cBoxSelect = (CheckBox) adapter.getChildView(i, k, isLast, null, null).findViewById(R.id.evi_chk_select);
					cBoxSelect.setChecked(true);
				}
			}
			((BaseExpandableListAdapter)adapter).notifyDataSetChanged();
		}
		
	}
	
	private class OnReversSelectedClickListenerImpl implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Iterator iter = selectStatusHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Boolean value = (Boolean) entry.getValue();
				selectStatusHashMap.put(key, !value);
			}
			int gourpsSum = adapter.getGroupCount();// 组的数量
			boolean isLast = false;
			for (int i = 0; i < gourpsSum; i++) {
				int childSum = adapter.getChildrenCount(i);// 组中子项的数量
				for (int k = 0; k < childSum; k++) {
					if (k == (childSum - 1)) {
						isLast = true;
					}
					CheckBox cBoxSelect = (CheckBox) adapter.getChildView(i, k, isLast, null, null).findViewById(R.id.evi_chk_select);
					cBoxSelect.toggle();
				}
			}
			((BaseExpandableListAdapter)adapter).notifyDataSetChanged();
		}
		
	}
	
	private class OnDeleteSelectedClickListenerImpl implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Iterator iter = selectStatusHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Boolean value = (Boolean) entry.getValue();
				if (value) {
					receiveService.deleteEml(key);
					Toast.makeText(getActivity(), "删除成功", Toast.LENGTH_LONG).show();
				}
			}
			((BaseExpandableListAdapter)adapter).notifyDataSetChanged();
			Intent intent = new Intent(getActivity(), MainActivity.class);
			getActivity().startActivity(intent);
			getActivity().finish();
		}
		
	}
	
	private class OnFlagSelectedClickListenerImpl implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Iterator iter = selectStatusHashMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Boolean value = (Boolean) entry.getValue();
				starStatusHashMap.put(key, value);
			}
			int gourpsSum = adapter.getGroupCount();// 组的数量
			boolean isLast = false;
			for (int i = 0; i < gourpsSum; i++) {
				int childSum = adapter.getChildrenCount(i);// 组中子项的数量
				for (int k = 0; k < childSum; k++) {
					if (k == (childSum - 1)) {
						isLast = true;
					}
					try {
					MimeMessageService mms = (MimeMessageService)adapter.getChild(i, k);
					CheckBox cBoxSelect = (CheckBox) adapter.getChildView(i, k, isLast, null, null).findViewById(R.id.evi_chk_star);
					System.out.println(mms.getMessageId() +  "/////" + starStatusHashMap.get(mms.getMessageId()));
					cBoxSelect.setChecked(starStatusHashMap.get(mms.getMessageId()));
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			((BaseExpandableListAdapter)adapter).notifyDataSetChanged();
		}
		
	}
	
	private class OnChildClickListenerImpl implements OnChildClickListener {// 监听子项点击事件
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			onItemClick(parent, v, groupPosition, childPosition, id);
			return true;
		}
	}

	private class OnGroupClickListenerImpl implements OnGroupClickListener {// 组被点击事件
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			return false;
		}
	}

	private class OnGroupCollapseListenerImpl implements OnGroupCollapseListener {// 组收缩事件
		@Override
		public void onGroupCollapse(int groupPosition) {
		}
	}

	private class OnGroupExpandListenerImpl implements OnGroupExpandListener {// 打开组事件
		@Override
		public void onGroupExpand(int groupPosition) {
		}
	}

	// 自定义适配器！！！！
	private class MyExpandableListAdapter extends BaseExpandableListAdapter {
		private Context context = null;
		public ArrayList<String> groups = null; // 组名称  
		public ArrayList<ArrayList<MimeMessageService>> children = null; // 定义子选项  

		public MyExpandableListAdapter(Context context) {
			this.context = context;
			children = data;
			groups = folders;
			selectStatusHashMap = new HashMap<String, Boolean>();
			starStatusHashMap = new HashMap<String, Boolean>();

			for (int i = 0; i < data.size(); i++) {// 初始化所有的子选项,并让其均未被选中
				ArrayList<MimeMessageService> folderList = data.get(i);
				for (int j = 0; j < folderList.size(); j++) {
					try {
						selectStatusHashMap.put(children.get(i).get(j).getMessageId(), false);
						starStatusHashMap.put(children.get(i).get(j).getMessageId(), false);
					} catch (MessagingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {// 取得组显示组件
			if (convertView == null) {// 第一次的时候convertView是空,所以要生成convertView
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.fragment_elist_view_groups, null);
			}
			
			groupTextView = (TextView) convertView.findViewById(R.id.evg_tv_group);
			groupTextView.setText(getGroup(groupPosition)); // 设置文字
			return groupTextView;
		}

		@Override
		public String getGroup(int groupPosition) { // 取得组对象
			return groups.get(groupPosition);
		}

		@Override
		public int getGroupCount() { // 取得组个数
			return groups.size();
		}

		@Override
		public long getGroupId(int groupPosition) { // 取得组ID
			return groupPosition;
		}

		// 点击事件发生后:先执行事件监听,然后调用此getChildView()
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {// 返回子项组件
			if (convertView == null) {// 第一次的时候convertView是空,所以要生成convertView
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.fragment_elist_view_item, null);
			}
			MimeMessageService mms = getChild(groupPosition, childPosition);
			child_subject = (TextView) convertView.findViewById(R.id.evi_tv_subject);
			child_subject.setText(mms.getSubject());
			
			child_from = (TextView) convertView.findViewById(R.id.evi_tv_from);
			child_from.setText(mms.getFrom());
			
			child_sendDate = (TextView) convertView.findViewById(R.id.evi_tv_time);
			child_sendDate.setText(mms.getSendDate());
			
			chk_select = (CheckBox) convertView.findViewById(R.id.evi_chk_select);
			chk_star = (CheckBox) convertView.findViewById(R.id.evi_chk_star);
			Boolean nowStatus;
			try {
				nowStatus = selectStatusHashMap.get(mms.getMessageId());
				chk_select.setChecked(nowStatus);
				nowStatus = starStatusHashMap.get(mms.getMessageId());
				chk_star.setChecked(nowStatus);
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// 当前状态
			return convertView;
		}

		@Override
		public MimeMessageService getChild(int groupPosition, int childPosition) { // 取得指定的子项
			return children.get(groupPosition).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) { // 取得子项ID
			return childPosition;
		}

		@Override
		public int getChildrenCount(int groupPosition) { // 取得子项个数
			return children.get(groupPosition).size();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void notifyDataSetChanged() {// 通知数据发生变化
			super.notifyDataSetChanged();
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}

	

	/**
	 * 重写onFling方法
	 * 触发条件 ： X坐标位移大于minX，Y坐标位移小于maxY，移动速度大于minV像素/秒
	 * @param e1 手势起点的移动事件
	 * @param e2 当前手势点的移动事件
	 * @param velocityX X轴上的移动速度，像素/秒
	 * @param velocityY Y轴上的移动速度，像素/秒
	 * @return
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		final int minX = 50 ,maxY = 50, minV = 20;
		int x1 = (int) e1.getX(), x2 = (int) e2.getX();
		int y1 = (int) e1.getY(), y2 = (int) e2.getY();

		if( Math.abs(x1-x2)>minX && Math.abs(y1-y2)<maxY && Math.abs(velocityX)>minV) {
			if(x1 > x2) {
				Log.v("MY_TAG", "Fling Left");
				eventState = FLING_LEFT;
			}
			else {
				Log.v("MY_TAG", "Fling Right");
				eventState = FLING_RIGHT;
			}
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
//		Log.v("MY_TAG", "onDown");
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("MY_TAG", "onLongPress");
		eventState = LONG_PRESS;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// TODO Auto-generated method stub
//		Log.v("MY_TAG", "onScroll");
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
//		Log.v("MY_TAG", "onShowPress");
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		Log.v("MY_TAG", "onSingleTapUp");
		eventState = SINGLE_TAPUP;
		return false;
	}


	/**
	 * 事件处理
	 * 其中eventState的值为事件
	 * 参数position为ListView的每一项
	 */
	public void onItemClick(ExpandableListView parent, View arg1, int groupPosition, int childPosition, long arg3) {
		// TODO Auto-generated method stub
		Log.v("MY_TAG", "onItemClick: state="+eventState+", gp="+groupPosition + ",cp="+childPosition );

		switch(eventState) {
		case FLING_LEFT: // 处理左滑事件
//			Toast.makeText(getActivity(), "Fling Left: gp="+groupPosition + ",cp="+childPosition , Toast.LENGTH_SHORT).show();
			eventState = LONG_PRESS;
			break;
		case FLING_RIGHT:	// 处理右滑事件
//			Toast.makeText(getActivity(), "Fling Right: gp="+groupPosition + ",cp="+childPosition , Toast.LENGTH_SHORT).show();
			eventState = LONG_PRESS;
			break;
		case SINGLE_TAPUP:// 处理点击事件
//			Toast.makeText(getActivity(), "Long Press: gp="+groupPosition + ",cp="+childPosition , Toast.LENGTH_SHORT).show();
			eventState = LONG_PRESS;
			linearLayout.setVisibility(View.VISIBLE);
			int gourpsSum = adapter.getGroupCount();// 组的数量
			boolean isLast = false;
			a:for (int i = 0; i < gourpsSum; i++) {
				int childSum = adapter.getChildrenCount(i);// 组中子项的数量
				b:for (int k = 0; k < childSum; k++) {
					if (childPosition == (childSum - 1)) {
						isLast = true;
						break a;
					}
				}
			}
			CheckBox cBoxSelect = (CheckBox) adapter.getChildView(groupPosition, childPosition, isLast, null, null).findViewById(R.id.evi_chk_select);
			cBoxSelect.toggle();// 切换CheckBox状态
			boolean itemIsCheck = cBoxSelect.isChecked();
			MimeMessageService itemMms = (MimeMessageService)adapter.getChild(groupPosition, childPosition);
			try {
				selectStatusHashMap.put(itemMms.getMessageId(), itemIsCheck);
//				starStatusHashMap.put(itemMms.getMessageId(), itemIsCheck);
				((BaseExpandableListAdapter)adapter).notifyDataSetChanged();// 通知数据发生了变化
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		default:
			MimeMessageService mms = (MimeMessageService)adapter.getChild(groupPosition, childPosition);
			Intent intent = new Intent(getActivity(), ReadActivity.class);
			intent.putExtra("subject", mms.getSubject());
			intent.putExtra("from", mms.getFrom());
			intent.putExtra("sendData", mms.getSendDate());
			intent.putExtra("text", mms.getBodyText());
			getActivity().startActivity(intent);
			break;
		}
	}

}
