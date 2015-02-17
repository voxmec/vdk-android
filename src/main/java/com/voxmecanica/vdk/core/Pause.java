package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.PausablePart;

public class Pause extends AbstractDialogPart implements PausablePart {
	private long duration = 500L;
	
	public Pause(long dur){
		setMetaPart(MetaPart.PAUSE);
		duration = dur;
	}
	
	@Override
	public long getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "Pause [duration=" + duration + "]";
	}

}
