package com.voxmec.vdk.test;

import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.Part;
import com.voxmecanica.vdk.core.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.doNothing;

@RunWith(MockitoJUnitRunner.class)
public class PauseRendererTest {
    @Mock
    VoxRuntime runtime;
    @Mock
    DialogContext ctx;

    @Test
    public void testPauseRenderer_New() {
        Dialog dialog = new Dialog();
        dialog.getProperties().put(Dialog.Prop.ORIG_URI, "http://test");
        DialogContext context = new VoxDialogContext(runtime);
        context.setDialog(dialog);

        Part part = new Part();
        part.setPause(1200);
        PauseRenderer r = new PauseRenderer(context, part, null);
        Assert.assertNotNull(r);
    }

    @Test
    public void testMediaRenderer_Run() {
        DialogContext context = new VoxDialogContext(runtime);
        Dialog dialog = new Dialog();
        context.setDialog(dialog);

        Part part = new Part();
        part.setPause(500);

        long sd = new Date().getTime();
        PauseRenderer r = new PauseRenderer(context, part, null);
        r.run();
        long ed = new Date().getTime() - sd;
        Assert.assertTrue(ed >= 500);
    }
}
