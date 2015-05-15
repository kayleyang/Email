package cn.edu.aku.email.patternpassword;
/**
 * ClassName:	StringUtil
 * @author   	Norris		Norris.sly@gmail.com
 * @version  	
 * @since   	Ver 1.1		I used to be a programmer like you, then I took an arrow in the knee 
 * @Date	 	2012		2012-3-6		上午11:28:37
 * @see
 */
public class StringUtil {
	/**
	 * 是否不为空
	 * @param s
	 * @return
	 */
	public static boolean isNotEmpty(String s) {
		return s != null && !"".equals(s.trim()) ;
	}
	/**
	 * 是否为空
	 * @param s
	 * @return
	 */
	public static boolean isEmpty(String s) {
		return s == null || "".equals(s.trim()) ;
	}
	/**
	 * 通过{n},格式化.
	 * @param src
	 * @param objects
	 * @return
	 */
	public static String format(String src , Object... objects) {
		int k = 0 ;
		for(Object obj : objects) {
			src = src.replace("{" + k + "}", obj.toString()) ;
			k++ ;
		}
		return src ;
	}
}

