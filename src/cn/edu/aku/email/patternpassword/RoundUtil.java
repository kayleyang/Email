package cn.edu.aku.email.patternpassword;
/**
 * ClassName:	RoundUtil
 * @author   	Norris		Norris.sly@gmail.com
 * @version  	
 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
 * @Date	 	2012		2012-3-6		上午11:28:51
 * @see
 */
public class RoundUtil {
	/**
	 * 点在圆内
	 * @param sx
	 * @param sy
	 * @param r
	 * @param x
	 * @param y
	 * @return
	 */
	public static boolean checkInRound(float sx , float sy , float r , float x , float y) {
		return Math.sqrt((sx - x) * (sx - x) + (sy - y) * (sy - y)) < r ;
	}
}

