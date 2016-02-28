package com.voxmecanica.vdk.core;

import android.speech.tts.UtteranceProgressListener;

import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.logging.Logger;
import com.voxmecanica.vdk.api.Part;

public class SpeechRenderer implements PartRenderer{
    private Logger LOG = new Logger("SpeechRenderer");
	private PartRenderer.OnCompleted onCompletedEvent;
	private Part part;
	private VoxRuntime runtime;
	
	public SpeechRenderer(DialogContext ctx, Part part, PartRenderer.OnCompleted onCompleted){
		runtime = ctx.getRuntime();
        this.part = part;
		onCompletedEvent = onCompleted;
	}
	
	@Override
	public void run() {
		String textToSpeak = part.getText();
		LOG.d("Rendering text [" + textToSpeak + "]");
		
		runtime.speakText(textToSpeak, new UtteranceProgressListener(){
            @Override
            public void onStart(String utteranceId) {
                LOG.d("Utterance [" + utteranceId + "] started OK");
            }

            @Override
            public void onDone(String utteranceId) {
                LOG.d("Utterance [" + utteranceId + "] completed OK");
                if(onCompletedEvent != null)
                    onCompletedEvent.completed(PartRenderer.RENDERING_OK);
            }

            @Override
            public void onError(String utteranceId) {
                LOG.d("Utterance [" + utteranceId + "] Failed.");
                if(onCompletedEvent != null)
                    onCompletedEvent.completed(PartRenderer.RENDERING_FAILED);
            }
        });
	}

}
