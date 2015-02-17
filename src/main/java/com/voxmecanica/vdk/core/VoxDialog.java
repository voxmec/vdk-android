package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogPart;
import com.voxmecanica.vdk.api.DialogRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VoxDialog implements Dialog {
	private List<DialogPart> dialogParts;
	private DialogRequest dialogRequest;
	private URI originUri;
	
	public VoxDialog(){
		dialogParts = new ArrayList<DialogPart>();
	}
	
	public void setDialogSubmission(DialogRequest submit){
		dialogRequest = submit;
	}
	
	@Override
	public DialogRequest getSubmissionRequest() {
		return dialogRequest;
	}
	
	public void setParts(List<DialogPart> parts){
		dialogParts = parts;
	}
	
	@Override
	public List<DialogPart> getParts() {
		return dialogParts;
	}
	
	public void setOriginUri(URI origin){
		originUri = origin;
	}
	
	@Override
	public URI getOriginUri() {
		return originUri;
	}
	
	@Override
	public String toString() {
		return "VoxDialog [dialogParts=" + dialogParts
				+ ", dialogSumission =" + dialogRequest 
				+ ", originUri = " + originUri
 				+ "]";
	}
	
	
}
