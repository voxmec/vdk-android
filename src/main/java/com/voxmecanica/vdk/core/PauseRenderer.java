package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.logging.Logger;
import com.voxmecanica.vdk.api.Part;

public class PauseRenderer implements PartRenderer {
    private Logger LOG = new Logger("PauseRenderer");
	private PartRenderer.OnCompleted onCompletedEvent;
	private Part part;
    private VoxRuntime runtime;

	public PauseRenderer(DialogContext ctx, Part pausable, PartRenderer.OnCompleted onCompleted){
		runtime = ctx.getRuntime();
        part = pausable;
		onCompletedEvent = onCompleted;
	}
	
	@Override
	public void run() {
		long dur = part.getPause();
		LOG.d("Pausing dialog for " + dur + " millis");
		try{
			Thread.sleep(dur);
			if(onCompletedEvent != null)
				onCompletedEvent.completed(PartRenderer.RENDERING_OK);	
		}catch(InterruptedException ex){
			//Log.d(TAG, "PauseRenderer - Pause interrupted.");
			if(onCompletedEvent != null)
				onCompletedEvent.completed(PartRenderer.RENDERING_FAILED);			
		}
	}
}
