package com.adai.camera.sunplus.tool;


public class ResolutionConvert {

	public static String convert(String resolution) {
		String ret = null;
		String[] temp;
		temp = resolution.split("\\?|&");
		temp[1] = temp[1].replace("W=", "");
		temp[2] = temp[2].replace("H=", "");
		temp[3] = temp[3].replace("BR=", "");
		ret = temp[0] + "?W=" + temp[1] + "&H=" + temp[2] + "&BR=" + temp[3];

		if (resolution.contains("FPS")) {
			if (temp[2].equals("720")) {
				ret = ret + "&FPS=15&";
			} else if (temp[2].equals("1080")) {
				ret = ret + "&FPS=10&";
			} else {
				ret = resolution;
			}
		} else {
			ret = resolution;
		}
		return ret;
	}
}
