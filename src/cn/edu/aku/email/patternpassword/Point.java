package cn.edu.aku.email.patternpassword;
/**
 * ClassName:	Point
 * @author   	Norris		Norris.sly@gmail.com
 * @version  	
 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
 * @Date	 	2012		2012-3-6		上午11:29:10
 * @see
 */
public class Point {
	// 未选中
	public static int STATE_NORMAL = 0 ; 
	// 选中
	public static int STATE_CHECK = 1 ; 
	// 已选中,但错误
	public static int STATE_CHECK_ERROR = 2 ; 

	public float x ;
	public float y ;
	public int state = 0 ;
	public int index = 0 ;// 下标

	public Point() {}
	public Point(float x , float y) {
		this.x = x ;
		this.y = y ;
	}
}