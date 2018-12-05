package com.adai.gkdnavi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by huangxy on 2017/8/25 16:51.
 */

public class VoicePhone {
    //@Title: isPhoneNumberValid
    //@Description: 验证号码 手机号 固话均可
    //@author qinyl
    //@date 2014年6月20日 下午3:16:03
    //@param @param phoneNumber
    //@param @return 设定文件
    //@return boolean 返回类型
    //@throws
    public static boolean isPhoneNumberValid(String phoneNumber) {
        boolean isValid = false;
        String expression = "((^(13|15|17|18)[0-9]{9}$)|(^0[1,2]{1}d{1}\\d{8}$)|"
                + "(^0[3-9]{1}\\d{2}\\d{7,8}$))";
        CharSequence inputStr = phoneNumber;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}
