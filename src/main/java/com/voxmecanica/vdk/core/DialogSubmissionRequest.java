package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogRequest;
import com.voxmecanica.vdk.http.HttpService;

import java.net.URI;

public class DialogSubmissionRequest implements DialogRequest {
	private URI submitLocation;
	private String submitMethod;
	
	public DialogSubmissionRequest(URI location, String method){
		submitLocation = location;
		submitMethod = method;
	}
	
	public DialogSubmissionRequest(URI location){
		this(location, HttpService.Config.DEFAULT_SUBMIT_METHOD);
	}
	
	@Override
	public URI getLocation() {
		return submitLocation;
	}

	@Override
	public String getMethod() {
		return submitMethod;
	}

	@Override
	public String toString() {
		return "VoxDialogSubmission [location=" + submitLocation
				+ ", method=" + submitMethod + "]";
	}

}
