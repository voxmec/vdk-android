package com.voxmecanica.vdk.core;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.parser.*;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.DialogExecutor;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.http.HttpService;
import com.voxmecanica.vdk.logging.Logger;
import com.voxmecanica.vdk.parser.Part;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class that implements the state machine for the Voice Dialog.
 */
public class VoxDialogExecutor implements DialogExecutor {

    private Logger LOG = new Logger("VoxDialogExecutor");
    private VoxRuntime runtime;
    private EngineHandler engine;
    private Looper engineLooper;
    private SpeechRecognizer recognizer;
    private DialogContext dialogContext;

    private VoxDialogExecutor.Callback.OnProgramStarted onProgramStartedCallback;
    private VoxDialogExecutor.Callback.OnProgramEnded onProgramEndedCallback;
    private VoxDialogExecutor.Callback.OnDialogPartRendering onDialogPartRenderingCallback;
    private VoxDialogExecutor.Callback.OnDialogPartRendered onDialogPartRenderedCallback;
    private VoxDialogExecutor.Callback.OnEventReceived onEventReceivedCallback;
    private VoxDialogExecutor.Callback.OnSpeechInputRequested onSpeechInputRequested;
    private VoxDialogExecutor.Callback.OnSpeechInputRecognized onSpeechInputRecognized;
    private VoxDialogExecutor.Callback.OnSpeechInputError onSpeechInputError;

    private static class Event {

        public final static int OP_FETCH_REMOTE_PROG    = 100;
        public final static int OP_START                = 300;
        public final static int OP_EXEC_PART            = 400;
        public final static int OP_PROG_END             = 600;
        public final static int OP_STOP                 = 700;
        public final static int STAT_OK                 = 200;
        public final static int STAT_FAIL               = 500;
    }

    public VoxDialogExecutor(VoxRuntime runtime) {
        this.runtime = runtime;
        if (runtime == null) {
            throw new IllegalArgumentException("VoxDialogExecutor missing a valid runtime instance.");
        }
        dialogContext = new VoxDialogContext(runtime);
        recognizer = runtime.getSpeechRecognizer();
        recognizer.setRecognitionListener(new VoxRecognitionListener());
        engineLooper = runtime.getApplicationContext().getMainLooper();
        engine = new EngineHandler(engineLooper);
    }

    private class EngineHandler extends Handler {

        public EngineHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            DialogContext context = (DialogContext) msg.obj;
            int e = msg.what;
            if (onEventReceivedCallback != null) {
                onEventReceivedCallback.exec(e);
            }

            LOG.d("VoxDialogExecutor - EventBus received event " + e);

            // handle state switch
            switch (e) {
                case Event.OP_FETCH_REMOTE_PROG:
                    DialogContext ctx = (DialogContext) msg.obj;
                    fetchRemoteProgram(ctx);
                    break;

                case Event.OP_START:
                    resetCounter(context);
                    loadNextPart(context);
                    if (onProgramStartedCallback != null) {
                        onProgramStartedCallback.exec(context);
                    }
                    break;

                case Event.OP_EXEC_PART:
                    render(context);
                    break;

                case Event.STAT_OK:
                    incCounter(context);
                    loadNextPart(context);
                    break;

                // force an abrupt stop and shutsdown the machine.
                case Event.OP_STOP:
                    clear(dialogContext);
                    shutdown();
                    break;

                // ends program naturally
                case Event.OP_PROG_END:
                    endProgram(context);
                    break;
            }
        }
    }
    
    private class VoxRecognitionListener implements RecognitionListener {

        @Override
        public void onReadyForSpeech(Bundle bundle) {}

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float f) {}

        @Override
        public void onBufferReceived(byte[] bytes) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int i) {
            // when speech input, pause program, set program counter to previous speechpart.
            LOG.d("RecognitionListener.OnError encountered... .");
            final DialogContext cloned = makeCtxClone(dialogContext);
            cloned.putValue(DialogContext.KEY_VOICE_REC_ERROR, i);
            clear(dialogContext);

            //dispatch error
            if (onSpeechInputError != null) {
                Part part = (Part) cloned.getValue(DialogContext.KEY_CURRENT_INPUT_PART);
                onSpeechInputError.exec(cloned, part, i);
            }
            
        }

        @Override
        public void onResults(Bundle results) {
            LOG.d("RecognitionListener.OnResults triggered...");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String matchesStr = matchesAsString(matches);
            float[] scores  = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            final Part part = (Part) dialogContext.getValue(DialogContext.KEY_CURRENT_INPUT_PART);

            if (part != null) {
                DialogResult result = dialogContext.getDialogResult();
                DialogParam param = new DialogParam();
                param.setId(part.getId());
                param.setValue(matchesStr);
                result.getParams().add(param);
                LOG.d(String.format("Voice input [%s = %s]", part.getType(), matchesStr));
            }

            // emit recognized input to api client.
            if (onSpeechInputRecognized != null) {
                final DialogContext cloned = makeCtxClone(dialogContext);
                engine.post(new Runnable() {
                    @Override
                    public void run() {
                        onSpeechInputRecognized.exec(cloned, part);
                    }
                });
            }

            // continue
            engine.sendMessage(Message.obtain(engine, Event.STAT_OK, dialogContext));
        }

        @Override
        public void onPartialResults(Bundle bundle) {}

        @Override
        public void onEvent(int i, Bundle bundle) {}
        
    }

    @Override
    public void setCallbacks(DialogExecutor.Callback... callbacks) {
        if (callbacks == null) {
            return;
        }
        for (VoxDialogExecutor.Callback cb : callbacks) {
            if (cb instanceof Callback.OnEventReceived) {
                onEventReceivedCallback = (Callback.OnEventReceived) cb;
            }
            if (cb instanceof Callback.OnDialogPartRendered) {
                onDialogPartRenderedCallback = (Callback.OnDialogPartRendered) cb;
            }
            if (cb instanceof Callback.OnDialogPartRendering) {
                onDialogPartRenderingCallback = (Callback.OnDialogPartRendering) cb;
            }
            if (cb instanceof Callback.OnProgramStarted) {
                onProgramStartedCallback = (Callback.OnProgramStarted) cb;
            }
            if (cb instanceof Callback.OnProgramEnded) {
                onProgramEndedCallback = (Callback.OnProgramEnded) cb;
            }
            if (cb instanceof Callback.OnSpeechInputRequested) {
                onSpeechInputRequested = (Callback.OnSpeechInputRequested) cb;
            }
            if (cb instanceof Callback.OnSpeechInputRecognized) {
                onSpeechInputRecognized = (Callback.OnSpeechInputRecognized) cb;
            }
            if (cb instanceof Callback.OnSpeechInputError) {
                onSpeechInputError = (Callback.OnSpeechInputError) cb;
            }
            
        }
    }

    public void shutdown() {
        LOG.i("VoxDialogExecutor - Shutting down");
    }

    /**
     * Launch a dialog from a Dialog object graph.
     *
     * @param dialog A Dialog object representing the dialog.
     */
    @Override
    public void execute(Dialog dialog) {
       dialogContext.setDialog(dialog);
        execute(dialogContext);
    }

    /**
     * Launch dialog via a given URI.
     *
     * @param uri location of voice dialog
     */
    @Override
    public void execute(URI uri) {
        if (uri != null) {
            LOG.i("Executing dialog from URI " + uri.toASCIIString());
            try{
               String dialogStr = HttpService.ResponseAsString(
                    runtime.getHttpService().serve(uri.toString(), null, "GET")
               );
               execute(dialogStr);

            }catch(Exception ex){
                throw new VoxException(
                    VoxException.ErrorType.HttpError,
                    "Encountered error while fetching remote dialog."
                );
            }
        } else {
            LOG.d("Expected dialog URI is null.  Terminating.");
        }
    }

      @Override
     public void execute(String dialogJson) {
          Dialog dialog = runtime.getDialogParser().parse(dialogJson);
          execute(dialog);
     }

     private void execute(DialogContext context) {
        Dialog dialog = context.getDialog();
        if (dialog != null && dialog.getParts().size() > 0) {
            LOG.d("Executing VoxDialog " + dialog + " with " + dialog.getParts().size() + " parts.");
            DialogResult dr = new DialogResult();
            dr.setParams(dialog.getParams());
            context.setDialogResult(new DialogResult());
            engine.sendMessage(Message.obtain(engine, Event.OP_START, context));
        } else {
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, context));
        }
    }


    // TODO - Update to properly submit data from DialogResult.
    private void fetchRemoteProgram(DialogContext ctx) {
        Dialog dialog = ctx.getDialog();
        DialogResult result = ctx.getDialogResult();

        if (result == null || result.getProperties() == null){
            LOG.d("WARNING: Unable to fetch. DialogResult properties are empty. Ending.");
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, ctx));
            return;
        }

        String dialogStr;
        try {
            Map<String,String> props = result.getProperties();
            String url = props.get(Dialog.Prop.SUBMIT_URI);
            if (dialog.getOrigUriProp() != null) {
                URI origUri = URI.create(dialog.getOrigUriProp());
                url = origUri.resolve(url).toString();
            }

            // TODO - If DialogResult.Format = JSON, send JSON

            String method = props.get(Dialog.Prop.SUBMIT_METHOD);
            if (method == null){
                method = "GET";
            }
            dialogStr = HttpService.ResponseAsString(
                    runtime.getHttpService().serve(url, result.getParamsAsMap(), method)
            );
        } catch (Exception ex) {
            throw new VoxException(
                    VoxException.ErrorType.HttpError,
                    "Encountered error while fetching remote dialog."
            );
        }

        if (dialogStr.length() > 0) {
            try {
                execute(dialogStr); // start new dialog.
            } catch (Exception ex) {
                throw new VoxException(
                        VoxException.ErrorType.InternalError,
                        "Encountered an error while parsing the dialog."
                );
            }
        } else {
            LOG.d("WARNING: Did not fetch dialog program. Ending.");
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, ctx));
        }
    }


     private void loadNextPart(DialogContext ctx) {
        List<Part> parts = ctx.getDialog().getParts();
        int pc = ((Integer)ctx.getValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER)).intValue();
        if (pc < parts.size()) {
            LOG.d("Loading next part at PC " + pc);
            Part currPart = parts.get(pc);
            ctx.putValue(DialogContext.KEY_CURRENT_DIALOG_PART, currPart);
            engine.sendMessage(Message.obtain(engine, Event.OP_EXEC_PART, ctx));
        } else {
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, ctx));
        }
    }

    @Override
    // interjects new dialog into an existing dialog context (useful for error-handling)
    public void interject(Dialog newDialog, DialogContext ctx){
        LOG.d("Interjecting new dialog into existing context...");
        if (newDialog == null || newDialog.getParts() == null || newDialog.getParts().size() == 0){
            throw new VoxException ("A new  instance must be provided to interject.");
        }
        Dialog existingDialog = ctx.getDialog();
        Dialog interjection   = newDialog;
        // transfer properties from running dialog to interjection dialog
        interjection.setProperties(existingDialog.getProperties());
        List<Part> parts = existingDialog.getParts();
        int pc = ((Integer)ctx.getValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER)).intValue();
        if( pc > parts.size()){
            throw new VoxException("DialogContext is in unexpected state. PC value bigger than size of dialog.");
        }
        if (parts.size() > 0){
            List<Part> rest = parts.subList(pc, parts.size());
            interjection.getParts().addAll(rest);
            ctx.setDialog(interjection);
        } else {
            ctx.setDialog(interjection);
        }
         
        ctx.putValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER, new Integer(0));
        execute(ctx);
    }

     // endProgram will generates a DialogResult.
     // Sends a DialogResult to remote server if remote.
     // Returns result as callback param other wise.
     private void endProgram(final DialogContext ctx) {
         LOG.d("Dialog program ending...");
         Dialog dialog = ctx.getDialog();
         if (dialog != null && dialog.getSubmitUriProp() != null){
             engine.sendMessage(Message.obtain(engine, Event.OP_FETCH_REMOTE_PROG, ctx));
         }else if (onProgramEndedCallback != null) {
            LOG.d("Found ProgramEndedCallBack, delegating...");
            onProgramEndedCallback.exec(makeCtxClone(ctx));
        }
    }

   private void clear(DialogContext ctx){
        LOG.d("Clearing internal context...");
        resetCounter(ctx);
        dialogContext.getValues().clear();
        dialogContext = null;
    }
    
    private String matchesAsString(List<String> matches) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < matches.size(); i++) {
            result.append(matches.get(i));
            if (i < matches.size() - 1) {
                result.append(";");
            }
        }
        return result.toString();
    }

    private void resetCounter(DialogContext ctx) {
        LOG.d("Resetting internal program counter to 0");
        ctx.putValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER, new Integer(0));
    }

    private void incCounter(DialogContext ctx) {
        int pc = ((Integer)ctx.getValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER)).intValue();
        pc = pc + 1;
        ctx.putValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER, new Integer(pc));
        LOG.d("PC = " + pc);
    }


    
    // Starts recognizer listening.  Results handled by VoxRecognitionListener
    private void startListening(DialogContext ctx) {
       LOG.d("About to listen...");
        dialogContext = ctx; // save reference.
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        recognizer.startListening(intent);        
    }

    private void render(final DialogContext context) {
        final Part part = (Part) context.getValue(DialogContext.KEY_CURRENT_DIALOG_PART);
        if (part == null) {
            LOG.d("DialogExecutor.render() - Missing expected part.  Sending program termination.");
            engine.sendMessage(Message.obtain(engine, Event.OP_STOP, context));            
            return;
        }
        LOG.d("Rendering part " + part);

        // invoke callback
        if (onDialogPartRenderingCallback != null) {
            engine.post(new Runnable(){
                @Override
                public void run() {
                    onDialogPartRenderingCallback.exec(part);
                }
            });
        }

        switch (part.getType()) {
            case SPEAK:
                engine.post(new SpeechRenderer(context, part, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        if (result == PartRenderer.RENDERING_OK) {
                            engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                        }
                    }
                }));
                break;

            case PLAYBACK:
                engine.post(new MediaRenderer(context, part, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        if (result == PartRenderer.RENDERING_OK) {
                            engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                        }
                    }
                }));
                break;

            case PAUSE:
                engine.post(new PauseRenderer(context, part, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                    }
                }));
                break;

            case INPUT:
                // rendering done by Android platform via Intent.
                context.putValue(DialogContext.KEY_CURRENT_INPUT_PART, part);
                if (onSpeechInputRequested != null) {
                    engine.post(new Runnable(){
                        @Override
                        public void run() {
                            onSpeechInputRequested.exec(makeCtxClone(context), part);
                        }
                    });
                }
                
                startListening(context);
                
                break;

            case END:
                LOG.d("Encountered END.  Stopping program.");
                engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, context));
                break;

            default:
                LOG.d("Unable to process part " + part.getType().toString() + ", unsupported.");
                engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context)); // move to next op
                break;
        }

        // invoke callback
        if (onDialogPartRenderedCallback != null) {
            engine.post(new Runnable(){
                @Override
                public void run() {
                    onDialogPartRenderedCallback.exec(part);
                }
            });

        }
    }

   private DialogContext makeCtxClone(DialogContext ctx){
        VoxDialogContext newCtx = new VoxDialogContext(ctx.getRuntime());
        newCtx.setValues(ctx.getValues());
        return newCtx;
    }
    
}
