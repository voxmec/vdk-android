package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.PlayablePart;

import java.net.URI;

public class Play extends AbstractDialogPart implements PlayablePart {
	private URI resource;
	private String title;
	private String content;
	private boolean displayable;
	
	public Play(URI res, String title, String content){
		setMetaPart(MetaPart.PLAY);
		resource = res;
		this.title = title;
		this.content = content;
		displayable = (title != null || content != null);
	}

	public Play(URI res){
		this(res, null, null);
	}
	
	@Override
	public URI getResourceUri() {
		return resource;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getContent() {
		return content;
	}
	
	@Override
	public boolean isDisplayed() {
		return displayable;
	}

	@Override
	public String toString() {
		return "Play {" + " resource: "
				+ ((resource != null) ? resource.toString() : "") + "]}";
	}
	
}
