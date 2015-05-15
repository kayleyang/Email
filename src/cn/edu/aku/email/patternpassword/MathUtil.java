package cn.edu.aku.email.patternpassword;
/**
 * ClassName:	MathUtil
 * @author   	Norris		Norris.sly@gmail.com
 * @version  	
 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
 * @Date	 	2012		2012-3-6		上午11:20:24
 * @see
 */
public class MathUtil {
	/**
	 * 两点间的距离
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double distance(double x1 , double y1 , double x2 , double y2) {
		return Math.sqrt(Math.abs(x1 - x2) * Math.abs(x1 - x2) 
				+ Math.abs(y1 - y2) * Math.abs(y1 - y2)) ;
	}
	/**
	 * 计算点a(x,y)的角度
	 * @param x
	 * @param y
	 * @return
	 */
	public static double pointTotoDegrees(double x , double y) {
		return Math.toDegrees(Math.atan2(x , y)) ;
	}
}

