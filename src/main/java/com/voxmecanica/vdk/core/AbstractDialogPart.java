package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogPart;

public abstract class AbstractDialogPart implements DialogPart{
	private MetaPart metaPart;
	
	public MetaPart getMetaPart(){
		return metaPart;
	}
	
	protected void setMetaPart(MetaPart part){
		metaPart = part;
	}
}
