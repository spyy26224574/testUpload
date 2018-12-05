package com.adai.gkdnavi.gson;

public class WebPage {
	private String header; //导语部分
	private String url;    //对data进行UI展示的链接

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "webPage [header=" + header + ", url=" + url + "]";
	}

}
