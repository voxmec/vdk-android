package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogPart;

public class Termination implements DialogPart {

	@Override
	public MetaPart getMetaPart() {
		return MetaPart.TERMINATION;
	}

}
