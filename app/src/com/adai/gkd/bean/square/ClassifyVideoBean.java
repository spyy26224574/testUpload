package com.adai.gkd.bean.square;

import java.io.Serializable;
import java.util.List;

public class ClassifyVideoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int typeId;
	
	public String typeName;
	
	public String typeDescribe;
	
	public List<VideoGridBean> squareColle;
}
