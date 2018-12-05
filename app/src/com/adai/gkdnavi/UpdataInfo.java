package com.adai.gkdnavi;

import java.util.ArrayList;
import java.util.List;

public class UpdataInfo {
	private String version;
	private String url;
	private List<UpdateDescriptioninfo> descriptions = new ArrayList<>();
	private String md5;
	private String timer;
	private String applicationname;
	private String contentcn;
	private String contenttw;
	private String contentdefault;
	private String type;
	private String needs;
	private List<UpdataInfo> modules=new ArrayList<UpdataInfo>();

	public String getNeeds() {
		return needs;
	}

	public void setNeeds(String needs) {
		this.needs = needs;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContentcn() {
		return contentcn;
	}

	public void setContentcn(String contentcn) {
		this.contentcn = contentcn;
	}

	public String getContentdefault() {
		return contentdefault;
	}

	public void setContentdefault(String contentdefault) {
		this.contentdefault = contentdefault;
	}

	public String getContenttw() {
		return contenttw;
	}

	public void setContenttw(String contenttw) {
		this.contenttw = contenttw;
	}

	public String getApplicationname() {
		return applicationname;
	}

	public void setApplicationname(String applicationname) {
		this.applicationname = applicationname;
	}

	public String getTimer() {
		return timer;
	}

	public void setTimer(String timer) {
		this.timer = timer;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<UpdateDescriptioninfo> getDescriptions() {
		return descriptions;
	}

	public void addDescription(UpdateDescriptioninfo description) {
		descriptions.add(description);
	}

	public void removeDescription(UpdateDescriptioninfo description) {
		descriptions.remove(description);
	}

	public void addModule(UpdataInfo module){
		modules.add(module);
	}

	public void removeModule(UpdataInfo module){
		modules.remove(module);
	}
}
