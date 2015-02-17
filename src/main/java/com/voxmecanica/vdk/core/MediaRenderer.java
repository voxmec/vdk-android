package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.api.PlayablePart;
import com.voxmecanica.vdk.logging.Logger;

import java.net.URI;

public class MediaRenderer implements PartRenderer {
    private Logger LOG = new Logger("MediaRenderer");
    private DialogContext dialogContext;
    private PartRenderer.OnCompleted onCompletedEvent;
    private PlayablePart playablePart;
    private URI resolvedUri;
    private VoxRuntime runtime;

    public MediaRenderer(DialogContext ctx, PlayablePart playable, PartRenderer.OnCompleted onCompleted) {
        runtime = ctx.getRuntime();
        dialogContext = ctx;
        playablePart = playable;
        resolvedUri = resolveUri(ctx, playablePart);
        onCompletedEvent = onCompleted;
    }

    private URI resolveUri(DialogContext ctx, PlayablePart part) {
        VoxDialog currentDialog = (VoxDialog) ctx.getValue(VoxDialogContext.KEY_DIALOG);
        URI originUri = currentDialog.getOriginUri();
        return originUri.resolve(part.getResourceUri());
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
