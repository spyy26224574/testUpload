package com.adai.hanzi2pinyin;
import java.util.ArrayList;
import com.adai.hanzi2pinyin.HanziToPinyin.Token;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class PinYin {
	
	 /**

	     * 将字符串中的中文转化为拼音,其他字符不变
	     * 花花大神->huahuadashen
	     * @param inputString
	     * @return
	     */
	    public String getPingYin(String inputString) {
	        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
	        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
	        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
	        format.setVCharType(HanyuPinyinVCharType.WITH_V);
	        char[] input = inputString.trim().toCharArray();
	        String output = "";
	        try {
	            for (char curchar : input) {
	                if (java.lang.Character.toString(curchar).matches("[\\u4E00-\\u9FA5]+")) {
	                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
	                            curchar, format);
	                    output += temp[0];
	                } else
	                    output += java.lang.Character.toString(curchar);
	            }
	        } catch (BadHanyuPinyinOutputFormatCombination e) {
	            e.printStackTrace();
	        }
	        return output;
	    }
	
	
	//汉字返回拼音，字母原样返回，都转换为小写
	public  String getPinYin(String input) {
		ArrayList<Token> tokens = HanziToPinyin.getInstance().get(input);
		StringBuilder sb = new StringBuilder();
		if (tokens != null && tokens.size() > 0) {
			for (Token token : tokens) {
				if (Token.PINYIN == token.type) {
					sb.append(token.target);
				} else {
					sb.append(token.source);
				}
			}
		}
		return sb.toString().toLowerCase();
	}
	
	   /**		   
	   * 获取两字符串的相似度		   
	   * 		   
	   * @param str		   
	   * @param target		   
	   * @return		   
	   */	   
	   public  float getSimilarityRatio(String strSource, String strTarget) {
	    return 1 - (float)compare(strSource, strTarget)/Math.max(strSource.length(), strTarget.length());
	   }		
	   
	   public  float getSimilarityRatio(String strSource, String strTarget,boolean bsrcChange,boolean bTarChange)
	   {
		   String strSrcPy = strSource; //source string to pinyin
		   String strTarPy = strTarget;  //target string to pinyin
		   if(bsrcChange)
		   	   //strSrcPy = getPinYin(strSource);
		       strSrcPy = getPingYin(strSource);
		   
		   if(bTarChange)
			   //strTarPy = getPinYin(strTarget);
			   strTarPy = getPingYin(strTarget);
		   
		    return 1 - (float)compare(strSrcPy, strTarPy)/Math.max(strSrcPy.length(), strTarPy.length());
		}	
	   
	   private  int compare(String str, String target) {
		  	  int d[][]; // 矩阵
		  	  int n = str.length();
		  	  int m = target.length();
		  	  int i; // 遍历str的
		  	  int j; // 遍历target的
		  	  char ch1; // str的
		  	  char ch2; // target的
		  	  int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		  	  if (n == 0) {
		  	   	  return m;
		  	  } 
		  	  if (m == 0) {
		  		   return n;
		  	  }
		  	  d = new int[n + 1][m + 1];
		  	  for (i = 0; i <= n; i++) { // 初始化第一列
		  	  	d[i][0] = i;
		  	  }
		  	 
		  	  for (j = 0; j <= m; j++) { // 初始化第一行
		  	   	d[0][j] = j;
		  	  }
		  	 
		  	  for (i = 1; i <= n; i++) { // 遍历str
		  		   ch1 = str.charAt(i - 1);
		  		   // 去匹配target
		  		   for (j = 1; j <= m; j++) {
		  			    ch2 = target.charAt(j - 1);
		  			    if (ch1 == ch2) {
		  				     temp = 0;
		  			    } else {
		  				     temp = 1;
		  			    }
		  			 
		  			    // 左边+1,上边+1, 左上角+temp取最小
		  			    d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
		  		   }
		  	  }
		  	  return d[n][m];
		   }
		   
		    
		   
		   private  int min(int one, int two, int three) {
		    return (one = one < two ? one : two) < three ? one : three;
		   } 
		   

}
