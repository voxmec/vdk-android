package com.voxmecanica.vdk.api;

public interface PartRenderer extends Runnable{
	public static final int RENDERING_OK = 100;
	public static final int RENDERING_FAILED = 400;
	
	public static interface OnCompleted{
		public void completed(int result);
	}
}
