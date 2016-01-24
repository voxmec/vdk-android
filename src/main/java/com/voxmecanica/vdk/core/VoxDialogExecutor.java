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
import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.DialogExecutor;
import com.voxmecanica.vdk.api.DialogPart;
import com.voxmecanica.vdk.api.DialogRequest;
import com.voxmecanica.vdk.api.ParameterPart;
import com.voxmecanica.vdk.api.PartRenderer;
import com.voxmecanica.vdk.api.PausablePart;
import com.voxmecanica.vdk.api.PlayablePart;
import com.voxmecanica.vdk.api.SpeakablePart;
import com.voxmecanica.vdk.http.HttpService;
import com.voxmecanica.vdk.logging.Logger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
    private DialogSubmissionRequest DialogRequest;

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
                Input input = (Input) cloned.getValue(DialogContext.KEY_CURRENT_INPUT_PART);
                Integer error = (Integer)cloned.getValue(DialogContext.KEY_VOICE_REC_ERROR);
                onSpeechInputError.exec(cloned, input, error);
            }
            
        }

        @Override
        public void onResults(Bundle results) {
            LOG.d("RecognitionListener.OnResults triggered...");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            dialogContext.putValue(DialogContext.KEY_VOICE_REC_MATCHES, matches);
            float[] scores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
            dialogContext.putValue(DialogContext.KEY_VOICE_REC_SCORES, scores);
            
            if (onSpeechInputRecognized != null) {
                final DialogContext cloned = makeCtxClone(dialogContext);
                final Input input = (Input)cloned.getValue(DialogContext.KEY_CURRENT_INPUT_PART);
                engine.post(new Runnable() {
                    @Override
                    public void run() {
                        onSpeechInputRecognized.exec(cloned, input);
                    }
                });
            }
        
            collectRecognizedValues(dialogContext);
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
        DialogContext context = new VoxDialogContext(runtime);
        context.setDialog(dialog);
        execute(context);    
    }

    /**
     * Launch dialog via a given URI.
     *
     * @param uri location of voice dialog
     */
    @Override
    public void execute(URI uri) {
        DialogContext context = new VoxDialogContext(runtime);
        if (uri != null) {
            LOG.i("Executing dialog from URI " + uri.toASCIIString());
            DialogRequest dialogReq = new DialogSubmissionRequest(uri, HttpService.Config.DEFAULT_SUBMIT_METHOD);
            context.putValue(VoxDialogContext.KEY_DIALOG_REQUEST, dialogReq);
            // load and execute remote dialog
            if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
                engine.sendMessage(Message.obtain(engine, Event.OP_FETCH_REMOTE_PROG, context));
            }
        } else {
            LOG.d("Expected dialog URI is null.  Terminating.");
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, context));
        }
    }

    @Override
    public void execute(String dialog) {

    }
    
    @Override
    // interjects new dialog into an existing dialog context (useful for error-handling)
    public void interject(Dialog newDialog, DialogContext ctx){
        LOG.d("Interjecting new dialog into existing context...");
        if (newDialog == null || newDialog.getParts() == null || newDialog.getParts().size() == 0){
            throw new VoxException ("A new  instance must be provided to interject.");
        }
        VoxDialog existingDialog = (VoxDialog)ctx.getDialog();
        VoxDialog interjection   = (VoxDialog)newDialog;
        interjection.setDialogSubmission(existingDialog.getSubmissionRequest());
        interjection.setOriginUri(existingDialog.getOriginUri());
        List<DialogPart> parts = existingDialog.getParts();
        int pc = ((Integer)ctx.getValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER)).intValue();
        if( pc > parts.size()){
            throw new VoxException("DialogContext is in unexpected state. PC value bigger than size of dialog.");
        }
        if (parts.size() > 0){
            List<DialogPart> rest = parts.subList(pc, parts.size());
            interjection.getParts().addAll(rest);
            ctx.setDialog(interjection);
        } else {
            ctx.setDialog(interjection);
        }
         
        ctx.putValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER, new Integer(0));
        execute(ctx);
    }
    
    @Override
    // submits dialog result to remote server and starts new dialog if one returned.
    public void submit(DialogContext ctx) {
        Dialog dialog = (Dialog) ctx.getValue(VoxDialogContext.KEY_DIALOG);
        if (dialog != null) {
            DialogRequest submission = dialog.getSubmissionRequest();
            //follow submission link.
            if (submission != null && submission.getLocation() != null) {
                LOG.d("Dialog has DialogSubmission : " + submission);
                URI location = dialog.getOriginUri().resolve(submission.getLocation());
                ctx.putValue(DialogContext.KEY_DIALOG_REQUEST, new DialogSubmissionRequest(location, submission.getMethod()));
                
                // if dialog from http, submit it
                if (location.getScheme().equals("http") || location.getScheme().equals("https")) {
                    engine.sendMessage(Message.obtain(engine, Event.OP_FETCH_REMOTE_PROG, ctx));
                }
            }
        }
    }
    
    private void execute(DialogContext context) {
        Dialog dialog = context.getDialog();
        if (dialog != null && dialog.getParts().size() > 0) {
            LOG.d("Executing VoxDialog " + dialog + " with " + dialog.getParts().size() + " parts.");
            engine.sendMessage(Message.obtain(engine, Event.OP_START, context));
        } else {
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, context));
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

    private void loadNextPart(DialogContext ctx) {
        List<DialogPart> parts = ctx.getDialog().getParts();
        int pc = ((Integer)ctx.getValue(DialogContext.KEY_DIALOG_PROGRAM_COUNTER)).intValue();
        if (pc < parts.size()) {
            LOG.d("Loading next part at PC " + pc);
            DialogPart currPart = parts.get(pc);
            ctx.putValue(DialogContext.KEY_CURRENT_DIALOG_PART, currPart);
            engine.sendMessage(Message.obtain(engine, Event.OP_EXEC_PART, ctx));
        } else {
            engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, ctx));
        }
    }
        
    
    // Starts recognizer listening.  Results handled by VoxRecognitionListener
    private void startListening(DialogContext ctx) {
       LOG.d("About to listen...");
        dialogContext = ctx; // save copy.
        
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        recognizer.startListening(intent);        
    }
    
    // Called when recognition results are available from VoxRecognitionListener
    private void collectRecognizedValues(final DialogContext ctx) {
        LOG.d("Received speech recognition results.");
        List<String> possibleMatches = (List<String>) ctx.getValue(DialogContext.KEY_VOICE_REC_MATCHES);
        String matchesString = matchesAsString(possibleMatches);

        if (ctx.getValue(DialogContext.KEY_CURRENT_INPUT_PART) != null) {
            final Input input = (Input) ctx.getValue(DialogContext.KEY_CURRENT_INPUT_PART);
            input.setValues(possibleMatches);
            ctx.getParameters().put(input.getParamName(), matchesString);
            LOG.d(String.format("Voice input param received [%s = %s]", input.getParamName(), matchesString));
        }
        engine.sendMessage(Message.obtain(engine, Event.STAT_OK, ctx));
    }

    private void fetchRemoteProgram(DialogContext ctx) {
        DialogRequest dialogReq = (DialogRequest) ctx.getValue(DialogContext.KEY_DIALOG_REQUEST);
        String dialogStr;
        try {
            String url = dialogReq.getLocation().toString();
            String method = dialogReq.getMethod();
            dialogStr = HttpService.ResponseAsString(
                    runtime.getHttpService().serve(url, ctx.getParameters(), method)
            );
        } catch (Exception ex) {
            throw new VoxException(
                    VoxException.ErrorType.HttpError,
                    "Encountered error while fetching remote dialog."
            );
        }

        if (dialogStr.length() > 0) {
            try {
                VoxDialog dialog = VoxDialogParser.parse(dialogStr);
                dialog.setOriginUri(dialogReq.getLocation());
                ctx.setDialogUri(dialogReq.getLocation());
                ctx.setDialog(dialog);
                execute(ctx); // start dialog program.
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

    private void render(final DialogContext context) {
        final DialogPart part = (DialogPart) context.getValue(DialogContext.KEY_CURRENT_DIALOG_PART);
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

        switch (part.getMetaPart()) {
            case SPEAK:
                SpeakablePart sp = (SpeakablePart) part;
                engine.post(new SpeechRenderer(context, sp, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        if (result == PartRenderer.RENDERING_OK) {
                            engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                        }
                    }
                }));
                break;

            case PLAY:
                PlayablePart pl = (PlayablePart) part;
                engine.post(new MediaRenderer(context, pl, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        if (result == PartRenderer.RENDERING_OK) {
                            engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                        }
                    }
                }));
                break;

            case PAUSE:
                PausablePart ps = (PausablePart) part;
                engine.post(new PauseRenderer(context, ps, new PartRenderer.OnCompleted() {
                    @Override
                    public void completed(int result) {
                        engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                    }
                }));
                break;

            case PARAM:
                ParameterPart prm = (ParameterPart) part;
                context.getParameters().put(prm.getName(), prm.getValue());
                engine.sendMessage(Message.obtain(engine, Event.STAT_OK, context));
                break;

            case INPUT:
                // rendering done by Android platform via Intent.
                final Input in = (Input) part;
                context.putValue(DialogContext.KEY_CURRENT_INPUT_PART, in);
                if (onSpeechInputRequested != null) {
                    engine.post(new Runnable(){
                        @Override
                        public void run() {
                            onSpeechInputRequested.exec(makeCtxClone(context), in);
                        }
                    });
                }
                
                startListening(context);
                
                break;

            case TERMINATION:
                LOG.d("Encoutered a termination.  Stopping program.");
                engine.sendMessage(Message.obtain(engine, Event.OP_PROG_END, context));
                break;

            default:
                LOG.d("Unable to process dialog part " + part.getMetaPart().label() + ". It may be an unsupported dialog tag.");
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

    private void endProgram(final DialogContext ctx) {
        LOG.d("Dialog program ending...");
        if (onProgramEndedCallback != null) {
            LOG.d("Found ProgramEndedCallBack, delegating...");
            onProgramEndedCallback.exec(makeCtxClone(ctx));
        }else{
            submit(ctx);
        }        
    }
    
    private DialogContext makeCtxClone(DialogContext ctx){
        VoxDialogContext newCtx = new VoxDialogContext(ctx.getRuntime());
        newCtx.setValues(ctx.getValues());
        return newCtx;
    }
    
}
