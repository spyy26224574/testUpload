package com.adai.gkdnavi.gson;

public class Respond {
	private int rc;   //应答码     0为成功  1、2、3、4为失败
	private String operation; //天气 QUERY  电话  CALL 导航 ROUTE 闲聊 ANSWER
	private String service;   //天气 weather  电话  telephone 导航 map 闲聊 chat
	private String text;      //识别的语音文本
	private Answer answer = new Answer();  //闲聊返回对象
	private WebPage webPage = new WebPage();  //天气HTML5返回对象
	private Data data = new Data();   //天气返回对象
	private Semantic semantic = new Semantic();  //语义结构化返回对象
	public int getRc() {
		return rc;
	}

	public void setRc(int rc) {
		this.rc = rc;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Answer getAnswer() {
		return answer;
	}

	public void setAnswer(Answer answer) {
		this.answer = answer;
	}

	public WebPage getWebPage() {
		return webPage;
	}

	public void setWebPage(WebPage webPage) {
		this.webPage = webPage;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Respond [rc=" + rc + ", operation=" + operation + ", service="
				+ service + ", text=" + text + ", answer=" + answer
				+ ", webPage=" + webPage + ", data=" + data + ", semantic="
				+ semantic + "]";
	}

	public Semantic getSemantic() {
		return semantic;
	}

	public void setSemantic(Semantic semantic) {
		this.semantic = semantic;
	}

}
