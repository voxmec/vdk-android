package com.voxmec.vdk.test;

import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.Part;
import com.voxmecanica.vdk.core.MediaRenderer;
import com.voxmecanica.vdk.core.VoxDialogContext;
import com.voxmecanica.vdk.core.VoxMediaPlayer;
import com.voxmecanica.vdk.core.VoxRuntime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MediaRendererTest {
    @Mock
    VoxRuntime runtime;
    @Mock
    DialogContext ctx;

    @Test
    public void testMediaRenderer_New() {
        Dialog dialog = new Dialog();
        dialog.getProperties().put(Dialog.Prop.ORIG_URI, "http://test");
        DialogContext context = new VoxDialogContext(runtime);
        context.setDialog(dialog);

        Part part = new Part();
        part.setSrc("/a/b/c");
        MediaRenderer r = new MediaRenderer(context, part, null);
        Assert.assertNotNull(r);
    }

    @Test
    public void testMediaRenderer_Run() {
        VoxMediaPlayer.Callback.OnCompleted cbCompleted = new VoxMediaPlayer.Callback.OnCompleted(){
            @Override
            public void exec() {

            }
        };
        VoxMediaPlayer.Callback.OnError cbError = new VoxMediaPlayer.Callback.OnError(){
            @Override
            public void exec(int err){

            }
        };

        DialogContext context = new VoxDialogContext(runtime);
        doNothing().
        when(runtime).playbackUri("http://test/a/b/c", cbCompleted, cbError);
         Dialog dialog = new Dialog();
        dialog.getProperties().put(Dialog.Prop.ORIG_URI, "http://test");
        context.setDialog(dialog);

        Part part = new Part();
        part.setSrc("/a/b/c");

        MediaRenderer r = new MediaRenderer(context, part, null);
        r.run();
    }
}
