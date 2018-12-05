package com.adai.gkdnavi.gson;

public class Answer {
	private String text;  // 闲聊返回的字符串
	private String type;  // T  text数据

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Answer [text=" + text + ", type=" + type + "]";
	}

}
