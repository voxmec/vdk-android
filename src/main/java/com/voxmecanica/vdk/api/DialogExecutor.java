package com.voxmecanica.vdk.api;

import java.net.URI;

public interface DialogExecutor {
    public void execute(Dialog dialog);
    public void execute(URI uriDialog);
    public void execute(String dialog);
    public void interject(Dialog dialog, DialogContext ctx);
    
    public void setCallbacks(DialogExecutor.Callback ... callbacks);

    public static interface Callback {
        public static interface OnEventReceived extends Callback {
            public void exec(int e);
        }
        public static interface OnDialogPartRendering extends Callback {
            public void exec(Part dp);
        }
        public static interface OnDialogPartRendered extends Callback{
            public void exec(Part dp);
        }
        public static interface OnProgramStarted extends Callback{
            public void exec(DialogContext ctx);
        }
        public static interface OnProgramEnded extends Callback{
            public void exec(DialogContext ctx);
        }
        public static interface OnSpeechInputRecognized extends Callback{
            public void exec(DialogContext ctx, Part part);
        }
        public static interface OnSpeechInputRequested extends Callback{
            public void exec(DialogContext ctx, Part part);
        }
        public static interface OnSpeechInputError extends Callback{
            public void exec(DialogContext ctx, Part part, int error);
        }
    }
}
