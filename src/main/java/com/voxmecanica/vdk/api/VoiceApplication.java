package com.voxmecanica.vdk.api;


public class VoiceApplication {
	public String key;
	public String name;
	public String description;
	public String url;
	
	public VoiceApplication(){}
	public VoiceApplication(String key, String name, String desc, String url) {
		this.key = key;
		this.name = name;
		this.description = desc;
		this.url = url;
	}
}
