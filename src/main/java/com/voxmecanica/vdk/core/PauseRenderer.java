package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.api.PausablePart;
import com.voxmecanica.vdk.logging.Logger;

public class PauseRenderer implements PartRenderer {
    private Logger LOG = new Logger("PauseRenderer");
	private PartRenderer.OnCompleted onCompletedEvent;
	private PausablePart pausablePart;
    private VoxRuntime runtime;

	public PauseRenderer(DialogContext ctx, PausablePart pausable, PartRenderer.OnCompleted onCompleted){
		runtime = ctx.getRuntime();
        pausablePart = pausable;
		onCompletedEvent = onCompleted;
	}
	
	@Override
	public void run() {
		long dur = pausablePart.getDuration();
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
