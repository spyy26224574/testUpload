package com.example.ipcamera.domain;

public class Person {

	private String name;
	private String number;
	private float  fRadioLevel;  //level
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	public float setRadioLevel(float fLevel)
	{
		fRadioLevel = fLevel;
		return fRadioLevel;
	}
	
	public float getRadioLevel()
	{
		//fRadioLevel = fLevel;
		return fRadioLevel;
	}
	
}
