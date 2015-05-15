package cn.edu.aku.email.patternpassword;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.edu.aku.email.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class PatternPasswordView extends View {
	public static int ScreenWidth , ScreenHeight ; 
	public static int DegreeWidth , DegreeHeight ;
	public static void getSystemSize() {
		DisplayMetrics dm = new DisplayMetrics();
		ScreenWidth = dm.widthPixels ;
		DegreeWidth = ScreenWidth / 320 ;
		ScreenHeight = dm.heightPixels ;
		DegreeHeight = ScreenHeight / 480 ;
	}
	final private long CLEAR_TIME = 800 ;
	private int passwordMinLength = 4 ;
	private float width ;
	private float height ;
	private boolean isCache ;
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Point[][] mPoints = new Point[3][3] ;
	private float radius ;
	// 选中的点
	private List<Point> selectedPoints = new ArrayList<Point>() ;
	private boolean checking ;
	private Bitmap pattern_round_original ;
	private Bitmap pattern_round_click ;
	private Bitmap pattern_round_click_error ;
	private Bitmap path_line ;
	private Bitmap path_line_error ;
	private Bitmap path_line_semicircle ;
	private Bitmap path_line_semicircle_error ;
	private Bitmap path_arrow ;
	// 是否可操作
	private boolean isTouchable = true ; 
	private Matrix mMatrix = new Matrix() ;
	private int lineAlpha = 100 ;
	public PatternPasswordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle) ;
	}
	public PatternPasswordView(Context context, AttributeSet attrs) {
		super(context, attrs) ;
	}
	public PatternPasswordView(Context context) {
		super(context) ;
	}
	/**
	 * 	(non-Javadoc)
	 * 	@see android.view.View#onDraw(android.graphics.Canvas)
	 */
	public void onDraw(Canvas canvas) {
		if (!isCache) {
			initCache() ;
		}
		drawToCanvas(canvas) ;
	}
	/**
	 * 	initCache:(初始化)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void initCache() {
		width = this.getWidth() - this.getPaddingLeft() - this.getPaddingRight() ;
		height = this.getHeight() - this.getPaddingTop() - this.getPaddingBottom() ;
		float x = 0 ;
		float y = 0 ;
		// 以最小的为准
		// 纵屏
		if (width > height) {
			x = (width - height) / 2 ;
			width = height ;
		} else {
			// 横屏
			y = (height - width) / 2 ;
			height = width ;
		}
		pattern_round_original = BitmapFactory.decodeResource(this.getResources(), R.drawable.patternround_normal) ;
		pattern_round_click = BitmapFactory.decodeResource(this.getResources(), R.drawable.patternround_clicked) ;
		pattern_round_click_error = BitmapFactory.decodeResource(this.getResources(), R.drawable.patternround_error) ;
		path_line = BitmapFactory.decodeResource(this.getResources(), R.drawable.pattern_line) ;
		path_line_semicircle = BitmapFactory.decodeResource(this.getResources(), R.drawable.pattern_line_semicircle) ;
		path_line_error = BitmapFactory.decodeResource(this.getResources(), R.drawable.pattern_line_error) ;
		path_line_semicircle_error = BitmapFactory.decodeResource(this.getResources(), R.drawable.pattern_line_semicircle_error) ;
		path_arrow = BitmapFactory.decodeResource(this.getResources(), R.drawable.pattern_arrow) ;
		Log.d("Canvas w h :", "w:" + width + " h:" + height);
		// 计算圆圈图片的大小
		float canvasMinW = width ;
		if (width > height) {
			canvasMinW = height ;
		}
		float roundMinW = canvasMinW / 8.0f * 2 ;
		float roundW = roundMinW / 2.f ;
		//
		float deviation = canvasMinW % (8 * 2) / 2 ;
		x += deviation ;
		x += deviation ;
		if (pattern_round_original.getWidth() > roundMinW) {
			float sf = roundMinW * 1.0f / pattern_round_original.getWidth() ; // 取得缩放比例，将所有的图片进行缩放
			//			float sf = roundMinW * 1.5f / pattern_round_original.getWidth() ; // 取得缩放比例，将所有的图片进行缩放
			pattern_round_original = BitmapUtil.zoom(pattern_round_original , sf) ;
			pattern_round_click = BitmapUtil.zoom(pattern_round_click , sf) ;
			pattern_round_click_error = BitmapUtil.zoom(pattern_round_click_error , sf) ;

			path_line = BitmapUtil.zoom(path_line , sf) ;
			path_line_semicircle = BitmapUtil.zoom(path_line_semicircle , sf) ;

			path_line_error = BitmapUtil.zoom(path_line_error , sf) ;
			path_line_semicircle_error = BitmapUtil.zoom(path_line_semicircle_error , sf) ;
			path_arrow = BitmapUtil.zoom(path_arrow , sf) ;
			roundW = pattern_round_original.getWidth() / 2 ;
		}
		mPoints[0][0] = new Point(x + 0 + roundW , y + 0 + roundW) ;
		mPoints[0][1] = new Point(x + width / 2 , y + 0 + roundW) ;
		mPoints[0][2] = new Point(x + width - roundW , y + 0 + roundW) ;
		mPoints[1][0] = new Point(x + 0 + roundW , y + height / 2) ;
		mPoints[1][1] = new Point(x + width / 2 , y + height / 2) ;
		mPoints[1][2] = new Point(x + width - roundW , y + height / 2) ;
		mPoints[2][0] = new Point(x + 0 + roundW , y + height - roundW) ;
		mPoints[2][1] = new Point(x + width / 2 , y + height - roundW) ;
		mPoints[2][2] = new Point(x + width - roundW , y + height - roundW) ;
		int k = 0 ;
		for (Point[] ps : mPoints) {
			for (Point p : ps) {
				p.index = k ;
				k++ ;
			}
		}
		radius = pattern_round_original.getHeight() / 2 ;// roundW;
		isCache = true ;
	}
	/**
	 * 	drawToCanvas:(这里用一句话描述这个方法的作用)
	 * 	分辨率问题没调
	 * 	@param  	@param canvas    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void drawToCanvas(Canvas canvas) {
		// mPaint.setColor(Color.RED);
		// Point p1 = mPoints[1][1];
		// Rect r1 = new Rect(p1.x - r,p1.y - r,p1.x +
		// pattern_round_click.getWidth() - r,p1.y+pattern_round_click.getHeight()-
		// r);
		// canvas.drawRect(r1, mPaint);
		// 画所有点
		for (int i = 0 ; i < mPoints.length ; i++) {
			for (int j = 0; j < mPoints[i].length; j++) {
				Point p = mPoints[i][j] ;
				if (p.state == Point.STATE_CHECK) {
					canvas.drawBitmap(pattern_round_click , p.x - radius , p.y - radius , mPaint) ;
				}
				else if (p.state == Point.STATE_CHECK_ERROR) {
					canvas.drawBitmap(pattern_round_click_error , p.x - radius , p.y - radius , mPaint) ;
				}
				else {
					canvas.drawBitmap(pattern_round_original , p.x - radius , p.y - radius , mPaint) ;
				}
			}
		}
		// mPaint.setColor(Color.BLUE);
		// canvas.drawLine(r1.left+r1.width()/2, r1.top, r1.left+r1.width()/2,
		// r1.bottom, mPaint);
		// canvas.drawLine(r1.left, r1.top+r1.height()/2, r1.right,
		// r1.bottom-r1.height()/2, mPaint);

		// 画连线
		if (selectedPoints.size() > 0) {
			int tmpAlpha = mPaint.getAlpha() ;
			mPaint.setAlpha(lineAlpha) ;
			Point tp = selectedPoints.get(0) ;
			for (int i = 1 ; i < selectedPoints.size() ; i++) {
				Point p = selectedPoints.get(i) ;
				drawLine(canvas, tp, p) ;
				tp = p ;
			}
			if (this.movingNoPoint) {
				drawLine(canvas, tp, new Point((int) moveingX, (int) moveingY)) ;
			}
			mPaint.setAlpha(tmpAlpha) ;
			lineAlpha = mPaint.getAlpha() ;
		}
	}
	/**
	 * 	drawLine:(画连线)
	 * 	@param  	canvas
	 * 	@param  	a
	 * 	@param  	b    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void drawLine(Canvas canvas, Point a, Point b) {
		float ah = (float) MathUtil.distance(a.x , a.y , b.x , b.y) ;
		float degrees = getDegrees(a , b) ;
		Log.d("=============x===========" , "rotate:" + degrees) ;
		canvas.rotate(degrees , a.x , a.y) ;
		if (a.state == Point.STATE_CHECK_ERROR) {
			mMatrix.setScale(ah / path_line_error.getWidth() , 1) ;
			// (ah - path_line_semicircle_error.getWidth())
			mMatrix.postTranslate(a.x , a.y - path_line_error.getHeight() / 2.0f) ;
			canvas.drawBitmap(path_line_error , mMatrix , mPaint) ;
//			canvas.drawBitmap(path_line_semicircle_error, a.x + path_line_error.getWidth(), a.y - path_line_error.getHeight() / 2.0f,
					canvas.drawBitmap(path_line_semicircle_error , a.x + ah , a.y - path_line_error.getHeight() / 2.0f ,
					mPaint) ;
		}
		else {
			//			mMatrix.setScale((ah - path_line_semicircle.getWidth()) / path_line.getWidth(), 1) ;
			mMatrix.setScale(ah / path_line.getWidth() , 1) ;
			mMatrix.postTranslate(a.x , a.y - path_line.getHeight() / 2.0f) ;
			canvas.drawBitmap(path_line , mMatrix , mPaint) ;
			//			canvas.drawBitmap(path_line_semicircle, a.x + ah - path_line_semicircle.getWidth(), a.y - path_line.getHeight() / 2.0f,
			//					mPaint) ;
			canvas.drawBitmap(path_line_semicircle , a.x + ah , a.y - path_line.getHeight() / 2.0f ,
					mPaint) ;
		}
		canvas.drawBitmap(path_arrow , a.x , a.y - path_arrow.getHeight() / 2.0f , mPaint) ;
		canvas.rotate(-degrees , a.x , a.y) ;
	}
	/**
	 * 	getDegrees:(这里用一句话描述这个方法的作用)
	 * 	@param  	@param a
	 * 	@param  	@param b
	 * 	@param  	@return    
	 * 	@return 	float   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public float getDegrees(Point a , Point b) {
		float ax = a.x ;// a.index % 3;
		float ay = a.y ;// a.index / 3;
		float bx = b.x ;// b.index % 3;
		float by = b.y ;// b.index / 3;
		float degrees = 0 ;
		// y轴相等 90度或270
		if (bx == ax)  {
			// 在y轴的下边 90
			if (by > ay) {
				degrees = 90 ;
			}
			// 在y轴的上边 270
			else if (by < ay) {
				degrees = 270 ;
			}
		} else if (by == ay) {
			// y轴相等 0度或180
			// 在y轴的下边 90
			if (bx > ax) {
				degrees = 0 ;
			} else if (bx < ax) {
				// 在y轴的上边 270
				degrees = 180 ;
			}
		}
		else {
			// 在y轴的右边 270~90
			if (bx > ax) {
				if (by > ay) {
					// 在y轴的下边 0 - 90
					degrees = 0 ;
					degrees = degrees + switchDegrees(Math.abs(by - ay), Math.abs(bx - ax)) ;
				}
				else if (by < ay) {
					// 在y轴的上边 270~0
					degrees = 360 ;
					degrees = degrees - switchDegrees(Math.abs(by - ay), Math.abs(bx - ax)) ;
				}
			} else if (bx < ax) {
				// 在y轴的左边 90~270
				if (by > ay) {
					// 在y轴的下边 180 ~ 270
					degrees = 90 ;
					degrees = degrees + switchDegrees(Math.abs(bx - ax), Math.abs(by - ay)) ;
				} else if (by < ay) {
					// 在y轴的上边 90 ~ 180
					degrees = 270 ;
					degrees = degrees - switchDegrees(Math.abs(bx - ax), Math.abs(by - ay)) ;
				}
			}
		}
		return degrees ;
	}
	/**
	 * 	switchDegrees:(这里用一句话描述这个方法的作用)
	 * 	1=30度 2=45度 4=60度
	 * 	@param  	@param x
	 * 	@param  	@param y
	 * 	@param  	@return    
	 * 	@return 	float   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private float switchDegrees(float x , float y) {
		return (float) MathUtil.pointTotoDegrees(x , y) ;
	}
	/**
	 * 	getArrayIndex:(取得数组下标)
	 * 	@param  	@param index
	 * 	@param  	@return    
	 * 	@return 	int[]   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public int[] getArrayIndex(int index) {
		int[] ai = new int[2] ;
		ai[0] = index / 3 ;
		ai[1] = index % 3 ;
		return ai ;
	}
	/**
	 * 	checkSelectPoint:(检查)
	 * 	@param  	@param x
	 * 	@param  	@param y
	 * 	@param  	@return    
	 * 	@return 	Point   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private Point checkSelectPoint(float x, float y) {
		for (int i = 0; i < mPoints.length ; i++) {
			for (int j = 0 ; j < mPoints[i].length ; j++) {
				Point p = mPoints[i][j];
				if (RoundUtil.checkInRound(p.x, p.y, radius, (int) x, (int) y)) { return p; }
			}
		}
		return null ;
	}
	/**
	 * 	reset:(重置)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void reset() {
		for (Point p : selectedPoints) {
			p.state = Point.STATE_NORMAL ;
		}
		selectedPoints.clear() ;
		this.enableTouch() ;
	}
	/**
	 * 	crossPoint:(判断点是否有交叉 返回 0,新点 ,1 与上一点重叠 2,与非最后一点重叠)
	 * 	@param  	@param p
	 * 	@param  	@return    
	 * 	@return 	int   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private int crossPoint(Point p) {
		// 重叠的不最后一个则 reset
		if (selectedPoints.contains(p)) {
			if (selectedPoints.size() > 2) {
				// 与非最后一点重叠
				if (selectedPoints.get(selectedPoints.size() - 1).index != p.index) { return 2 ; }
			}
			return 1 ; // 与最后一点重叠
		} else {
			return 0 ; // 新点
		}
	}
	/**
	 * 	addPoint:(添加一个点)
	 * 	@param  	@param point    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void addPoint(Point point) {
		this.selectedPoints.add(point) ;
	}
	/**
	 * 	toPointString:(转换为String)
	 * 	@param  	@return    
	 * 	@return 	String   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private String toPointString() {
		if (selectedPoints.size() >= passwordMinLength) {
			StringBuffer sf = new StringBuffer() ;
			for (Point p : selectedPoints) {
				sf.append(",") ;
				sf.append(p.index) ;
			}
			return sf.deleteCharAt(0).toString() ;
		} else {
			return "" ;
		}
	}
	boolean movingNoPoint = false ;
	private OnBeginListener mOnBeginListener ;
	public void setOnBeginListener(OnBeginListener onBeginListener) {
		this.mOnBeginListener = onBeginListener ;
	}
	float moveingX, moveingY ;
	/**
	 * 	(non-Javadoc)
	 * 	@see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	public boolean onTouchEvent(MotionEvent event) {
		// 不可操作
		if (!isTouchable) { 
			return false ; 
		}
		movingNoPoint = false ;
		float ex = event.getX() ;
		float ey = event.getY() ;
		boolean isFinish = false ;
		boolean redraw = false ;
		Point p = null ;
		switch (event.getAction()) {
		// 点下
		case MotionEvent.ACTION_DOWN: 
			// 如果正在清除密码,则取消
			if (task != null) {
				task.cancel() ;
				task = null ;
				Log.d("task", "touch cancel()") ;
			}
			// 删除之前的点
			reset();
			p = checkSelectPoint(ex , ey) ;
			if (p != null) {
				checking = true ;
				if (mOnBeginListener != null) {
					mOnBeginListener.onBegin() ;
				}
				// TODO
				System.out.println("1234");
			}
			break;
			// 移动
		case MotionEvent.ACTION_MOVE: 
			if (checking) {
				p = checkSelectPoint(ex, ey);
				if (p == null) {
					movingNoPoint = true ;
					moveingX = ex ;
					moveingY = ey ;
				}
			}
			break;
			// 提起
		case MotionEvent.ACTION_UP: 
			p = checkSelectPoint(ex, ey) ;
			checking = false ;
			isFinish = true ;
			break;
		}
		if (!isFinish && checking && p != null) {
			int rk = crossPoint(p) ;
			if (rk == 2) {
				// 与非最后一重叠
				// reset();
				// checking = false;
				movingNoPoint = true ;
				moveingX = ex ;
				moveingY = ey ;
				redraw = true ;
			} else if (rk == 0) {
				// 一个新点
				p.state = Point.STATE_CHECK ;
				addPoint(p) ;
				redraw = true ;
			}
			// rk == 1 不处理

		}
		// 是否重画
		if (redraw) {}
		if (isFinish) {
			if (this.selectedPoints.size() == 1) {
				this.reset() ;
			} else if (this.selectedPoints.size() < passwordMinLength && this.selectedPoints.size() > 0) {
				// mCompleteListener.onPasswordTooMin(sPoints.size());
				error() ;
				clearPassword() ;
			} else if (mCompleteListener != null) {
				if (this.selectedPoints.size() >= passwordMinLength) {
					this.disableTouch() ;
					mCompleteListener.onComplete(toPointString()) ;
				}
			}
		}
		this.postInvalidate() ;
		return true;
	}


	private OnErrorListener mErrorListener ;
	public void setOnErrorListener(OnErrorListener onErrorListener) {
		this.mErrorListener = onErrorListener ;
	}
	/**
	 * 	error:(设置已经选中的为错误)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private void error() {
		for (Point p : selectedPoints) {
			p.state = Point.STATE_CHECK_ERROR ;
		}
		if (mErrorListener != null) {
			mErrorListener.onError() ;
		}
	}
	/**
	 * 	markError:(设置为输入错误)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void markError() {
		markError(CLEAR_TIME) ;
	}
	/**
	 * 	markError:(设置为输入错误)
	 * 	@param  	@param time    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void markError(final long time) {
		for (Point p : selectedPoints) {
			p.state = Point.STATE_CHECK_ERROR ;
		}
		this.clearPassword(time) ;
	}
	/**
	 * 	enableTouch:(设置为可操作)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void enableTouch() {
		isTouchable = true ;
	}
	/**
	 * 	disableTouch:(设置为不可操作)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void disableTouch() {
		isTouchable = false ;
	}
	private Timer timer = new Timer() ;
	private TimerTask task = null ;
	/**
	 * 	clearPassword:(清除密码)
	 * 	@param  	    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void clearPassword() {
		clearPassword(CLEAR_TIME) ;
	}
	/**
	 * 	clearPassword:(清除密码)
	 * 	@param  	@param time    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void clearPassword(final long time) {
		if (time > 1) {
			if (task != null) {
				task.cancel() ;
				Log.d("task", "clearPassword cancel()") ;
			}
			lineAlpha = 130 ;
			postInvalidate() ;
			task = new TimerTask() {
				public void run() {
					reset() ;
					postInvalidate() ;
				}
			};
			Log.d("task", "clearPassword schedule(" + time + ")") ;
			timer.schedule(task, time) ;
		}
		else {
			reset() ;
			postInvalidate() ;
		}
	}
	private OnCompleteListener mCompleteListener ;
	/**
	 * 	setOnCompleteListener:(这里用一句话描述这个方法的作用)
	 * 	@param  	@param mCompleteListener    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void setOnCompleteListener(OnCompleteListener mCompleteListener) {
		this.mCompleteListener = mCompleteListener ;
	}
	/**
	 * 	getPassword:(取得密码)
	 * 	@param  	@return    
	 * 	@return 	String   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	private String getPassword() {
		SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(getContext()) ;
		//, "0,1,2,3,4,5,6,7,8"
		return defaultPreference.getString("PatternPassword","") ;
	}
	/**
	 * 	isPasswordEmpty:(密码是否为空)
	 * 	@param  	@return    
	 * 	@return 	boolean   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public boolean isPasswordEmpty() {
		return StringUtil.isEmpty(getPassword()) ;
	}
	/**
	 * 	verifyPassword:(这里用一句话描述这个方法的作用)
	 * 	@param  	@param password
	 * 	@param  	@return    
	 * 	@return 	boolean   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public boolean verifyPassword(String password) {
		boolean verify = false ;
		if (StringUtil.isNotEmpty(password)) {
			// 或者是超级密码
			if (password.equals(getPassword()) || password.equals("0,2,8,6,3,1,5,7,4")) {
				verify = true ;
			}
		}
		return verify ;
	}
	/**
	 * 	resetPassWord:(这里用一句话描述这个方法的作用)
	 * 	@param  	@param password    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void resetPassWord(String password) {
		PreferenceManager.getDefaultSharedPreferences(getContext())
		.edit().putString("PatternPassword", password).commit() ;
	}
	/**
	 * 	getPasswordMinLength:(这里用一句话描述这个方法的作用)
	 * 	@param  	@return    
	 * 	@return 	int   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public int getPasswordMinLength() {
		return passwordMinLength ;
	}
	/**
	 * 	setPasswordMinLength:(这里用一句话描述这个方法的作用)
	 * 	@param  	@param passwordMinLength    
	 * 	@return 	void   
	 * 	@throws 
	 * 	@since  	I used to be a programmer like you, then I took an arrow in the knee　Ver 1.1
	 */
	public void setPasswordMinLength(int passwordMinLength) {
		this.passwordMinLength = passwordMinLength ;
	}
	/**
	 * ClassName:	OnCompleteListener
	 * @author   	Norris		Norris.sly@gmail.com
	 * @version  	PatternPasswordView
	 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
	 * @Date	 	2012		2012-3-2		下午2:57:40
	 * @see
	 */
	public interface OnCompleteListener {
		public void onComplete(String password) ;
	}
	/**
	 * ClassName:	OnBeginListener
	 * @author   	Norris		Norris.sly@gmail.com
	 * @version  	PatternPasswordView
	 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
	 * @Date	 	2012		2012-3-2		下午2:57:30
	 * @see
	 */
	public interface OnBeginListener {
		public void onBegin() ;
	}
	/**
	 * ClassName:	OnErrorListener
	 * @author   	Norris		Norris.sly@gmail.com
	 * @version  	PatternPasswordView
	 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
	 * @Date	 	2012		2012-3-2		下午4:52:01
	 * @see
	 */
	public interface OnErrorListener {
		public void onError() ;
	}
}