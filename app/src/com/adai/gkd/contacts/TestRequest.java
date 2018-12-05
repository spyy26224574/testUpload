package com.adai.gkd.contacts;

import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.httputils.HttpUtil.Callback;

import android.test.AndroidTestCase;

public class TestRequest extends AndroidTestCase {

	public void testUserSingle(){
		RequestMethods.userSingle("lim", "111111", "test@gkd.com", new Callback<UserSingleupPagebean>() {
			
			@Override
			public void onCallback(UserSingleupPagebean result) {
				// TODO Auto-generated method stub
				if(result!=null){
					System.out.println(result.message);
				}
			}
		});
	}
}
