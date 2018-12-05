package com.adai.gkdnavi.gson;

public class Semantic {

	private Slots slots = new Slots();  //语义结构化返回对象

	public Slots getSlots() {
		return slots;
	}

	public void setSlots(Slots slots) {
		this.slots = slots;
	}

	@Override
	public String toString() {
		return "Semantic [slots=" + slots + "]";
	}
}
