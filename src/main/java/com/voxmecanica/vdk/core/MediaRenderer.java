package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.api.PlayablePart;
import com.voxmecanica.vdk.logging.Logger;
import com.voxmecanica.vdk.parser.Dialog;
import com.voxmecanica.vdk.parser.Part;

import java.net.URI;

public class MediaRenderer implements PartRenderer {
    private Logger LOG = new Logger("MediaRenderer");
    private DialogContext dialogContext;
    private PartRenderer.OnCompleted onCompletedEvent;
    private Part part;
    private URI resolvedUri;
    private VoxRuntime runtime;

    public MediaRenderer(DialogContext ctx, Part playable, PartRenderer.OnCompleted onCompleted) {
        runtime = ctx.getRuntime();
        dialogContext = ctx;
        part = playable;
        resolvedUri = resolveUri(ctx, part.getSrc());
        onCompletedEvent = onCompleted;
    }

    private URI resolveUri(DialogContext ctx, String src) {
        Dialog dialog = ctx.getDialog();
        if (dialog.getOrigUriProp() != null) {
            URI origUri = URI.create(dialog.getOrigUriProp());
            return origUri.resolve(src);
        }
        return URI.create(src);
    }

    @Override
    public void run() {
        LOG.d("Rendering media resource [" + resolvedUri.toASCIIString() + "]");
        runtime.playbackUri(
                resolvedUri.toASCIIString(),
                new VoxMediaPlayer.Callback.OnCompleted() {
                    @Override
                    public void exec() {
                        if (onCompletedEvent != null) {
                            onCompletedEvent.completed(RENDERING_OK);
                        }
                    }
                },
                new VoxMediaPlayer.Callback.OnError() {
                    @Override
                    public void exec(int errorCode) {
                        if (onCompletedEvent != null) {
                            onCompletedEvent.completed(RENDERING_FAILED);
                        }
                    }
                }
        );
    }

}
