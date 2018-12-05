package com.adai.gkdnavi.gson;

import java.util.List;

public class Data {
	private List<Result> result; //天气list对象

	public List<Result> getResult() {
		return result;
	}

	public void setResult(List<Result> result) {
		this.result = result;
	}

	@Override
	public String toString() {
		return "Data [result=" + result + "]";
	}

}