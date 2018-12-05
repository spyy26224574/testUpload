package com.adai.camera.hisi.net;

import java.util.List;
import java.util.Map;


public class StringParser {

	/**
	 * 以";"为分隔符拆分字符串为字符串列表
	 * @param content 输入的要解析的字符串，如"file1;file2;file3;"
	 * @param list 输出参数，放入解析后的字符串
	 * @return 解析成功的个数
	 */

	public static int getStringList(String content, List<String> list)
	{
		if( null == list )
		{
			return 0;
		}
		int count = 0;  //解析成功的个数
		String delim = ";" ;  //分隔符
		int start = 0;
		int end = 0;
		
		end = content.indexOf(delim, start); //查找分隔符出现位置
		while( end >= 0 && start<=end )
		{
			//取子字符串(start到end-1)添加到list中
			String value = content.substring(start, end).trim();
			if( 0 != value.length() )
			{
				list.add( value );
				count++;
			}
			
			start = end + delim.length();                //start向后移动
			end = content.indexOf(delim, start);         //继续找下一个出现位置			
		}		

		return count;
	}



    private static final String HEAD_VAR       = "var ";
    private static final String MID_EQUAL_MARK = "=\"" ;
    private static final String TAIL_SEMICOLON = "\";\r\n";

	/**
	 * 解析含有"var name="value";\r\n"格式的多个字符串，解析成Map键值对集合
	 * @param content 输入的要解析的字符串，如"var size="10";\r\nvar name="mike";\r\n"
	 * @param map 输出参数，放入解析后的键值对
	 * @return count 解析出来的键值对的个数
	 */
	public static int getKeyValueMap(String content, Map<String,String> map)
	{
		if( null == map )
		{
			return 0;
		}

		int count = 0;
		int start,mid,end;
		String key,value;
		
		start = content.indexOf(HEAD_VAR, 0); //找不到返回 -1
		while( start >= 0 )
		{
			mid = content.indexOf(MID_EQUAL_MARK, start);    //找 =" 出现位置
			end = content.indexOf(TAIL_SEMICOLON, start);    //找 ";\r\n 出现位置
			
			if( !(start<mid && mid<end ) )
			{
				break;  //格式错乱
			}
			
			key = content.substring(start + HEAD_VAR.length(), mid).trim();
			value = content.substring(mid + MID_EQUAL_MARK.length(),  end).trim();

			map.put( key, value );
			count++;

			//start向后移动，继续向后找
            start = end + TAIL_SEMICOLON.length();
			start = content.indexOf(HEAD_VAR, start);
		}		
		return count;
	}
}
