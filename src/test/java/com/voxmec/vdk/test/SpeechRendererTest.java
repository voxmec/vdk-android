package com.voxmec.vdk.test;

import android.speech.tts.UtteranceProgressListener;
import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.Part;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.core.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SpeechRendererTest {
    @Mock
    VoxRuntime runtime;
    @Mock
    DialogContext ctx;

    @Test
    public void testSpeechRenderer_New() {
        Dialog dialog = new Dialog();
        DialogContext context = new VoxDialogContext(runtime);
        context.setDialog(dialog);

        Part part = new Part();
        part.setText("Hello World");
        SpeechRenderer r = new SpeechRenderer(context, part, null);
        Assert.assertNotNull(r);
    }

    public void testMediaRenderer_Run() {
        class UtterListener extends UtteranceProgressListener{
            UtterListener(){}

            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {}

            @Override
            public void onError(String uttId){}
        };

        DialogContext context = new VoxDialogContext(runtime);
        doNothing().
        when(runtime).speakText("Je;;p", new UtterListener());

        Dialog dialog = new Dialog();
        context.setDialog(dialog);
        Part part = new Part();
        part.setText("Hello");

        SpeechRenderer r = new SpeechRenderer(context, part, null);
        r.run();
    }
}
