package com.voxmecanica.vdk.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.api.DialogExecutor;
import com.voxmecanica.vdk.http.HttpService;
import com.voxmecanica.vdk.logging.Logger;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class VoxRuntime {
    private Logger LOG = new Logger("VoxRuntime");
    private Context context;
    private HttpService httpService;
    private VoxMediaPlayer mediaPlayer;
    private TextToSpeech ttsService;
    private volatile boolean ready = false;
    private volatile boolean ttsEnabled = false;
    private volatile boolean speechRecEnabled = false;
    private VoxDialogExecutor dialogExec;
    private SpeechRecognizer recognizer;
    private ConnectivityManager connManager;

    private Looper runtimeLooper;
    private RuntimeHandler eventBus;
    private Callback.OnReady onReady;

    private static class Event {
        public static final int INIT_START = 100;
        public static final int INIT_RUNTIME = 200;
        public static final int INIT_SPEECH_SYSTEM = 300;
        public static final int INIT_FINISH = 400;
        public static final int STAT_OK = 500;
        public static final int STAT_FAIL = 600;
    }

    private final AtomicInteger counter = new AtomicInteger(0);
    private final int[] INIT_SEQUENCES  = {
        Event.INIT_RUNTIME,
        Event.INIT_SPEECH_SYSTEM,
        Event.INIT_FINISH
    };

    private final class RuntimeHandler extends Handler {
        public RuntimeHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            LOG.d("RuntimeHandler got message ID " + msg.what);
            switch(msg.what){
                case Event.STAT_OK:
                    counter.getAndIncrement();
                    if(counter.get() < INIT_SEQUENCES.length){
                        int nextEvent = INIT_SEQUENCES[counter.get()];
                        eventBus.sendMessage(Message.obtain(eventBus, nextEvent));
                    }
                    break;
                case Event.INIT_RUNTIME:
                    initRuntime();
                    break;
                case Event.INIT_SPEECH_SYSTEM:
                    initSpeech();
                    break;
                case Event.INIT_FINISH:
                    doOnFinished();
                    break;
            }
        }
    }

    public interface Callback{
        public static interface OnReady {
            public void exec(VoxRuntime runtime);
        }
    }

    public VoxRuntime (Context context, Callback.OnReady cb) {
        if(context == null)
            throw new IllegalArgumentException("Unable to start VoxRuntime. Need an Application Context.");
        LOG.i("Starting runtime...");
        this.context = context;
        onReady = cb;

        // use application context main looper.
        runtimeLooper = context.getMainLooper();
        eventBus = new RuntimeHandler(runtimeLooper);

        // start initialization sequence
        counter.set(0);
        eventBus.sendMessage(Message.obtain(eventBus, INIT_SEQUENCES[counter.get()]));
    }

    public void shutdown() {
        //Log.i(TAG, "VoxRuntimeService - Shutting down services...");
        if (mediaPlayer != null)
            mediaPlayer.shutdown();
        shutdownRecognizer();
        shutdownTTS();
        if (httpService != null)
            httpService.stop();
        if (dialogExec != null)
            dialogExec.shutdown();
    }
    
    protected Context getApplicationContext() {
        return context;
    }

    public DialogExecutor getDialogExecutor(){
        assertReadiness();
        return dialogExec;
    }

    protected HttpService getHttpClientService(){
        assertReadiness();
        return httpService;
    }
    
    protected SpeechRecognizer getSpeechRecognizer() {
        return recognizer;
    }

    public boolean isTextToSpeechEnabled(){
        assertReadiness();
        return (ttsService != null && ttsEnabled);
    }

    public boolean isVoiceRecognitionEnabled() {
        assertReadiness();
        return (speechRecEnabled);
    }

    public void speakText(String txtToSay, UtteranceProgressListener listener){
        assertReadiness();
        ttsService.setOnUtteranceProgressListener(listener);
        HashMap<String,String> ttsContext = new HashMap<String,String>();
        String utteranceId = Integer.toString(txtToSay.hashCode());
        ttsContext.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        int result = ttsService.speak(txtToSay, TextToSpeech.QUEUE_ADD, ttsContext);
        if(result != TextToSpeech.SUCCESS){
            throw new VoxException(
                    VoxException.ErrorType.SpeechError,
                    String.format(
                            "Failed to speech-synthesize text [%s].",
                            txtToSay
                    )
            );
        }
    }

    public void stopSpeaking(){
        if(ttsService != null){
            ttsService.stop();
        }
    }

    public void playbackUri(String uri, VoxMediaPlayer.Callback.OnCompleted onCompleted, VoxMediaPlayer.Callback.OnError onError){
        mediaPlayer.setCallbacks(onCompleted, onError);
        mediaPlayer.playbackUri(uri);
    }

    public void stopPlayback() {
        mediaPlayer.stop();
    }


    private void initRuntime() {
        LOG.d("Initializing runtime resources...");
        connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        httpService = new HttpService();
        httpService.start();
        mediaPlayer = new VoxMediaPlayer();
        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecEnabled = (SpeechRecognizer.isRecognitionAvailable(context));
        dialogExec = new VoxDialogExecutor(this);
        eventBus.sendMessage(Message.obtain(eventBus, Event.STAT_OK));
    }

    private void initSpeech() {
        LOG.i("Initializing TTS connection");
        ttsService = new TextToSpeech(context, new TextToSpeech.OnInitListener(){
            public void onInit(int stat){
                ttsEnabled = (stat == TextToSpeech.SUCCESS);
                if(ttsEnabled){
                    LOG.d("TextToSpeech is enabled, status (" + stat + ")");
                    eventBus.sendMessage(Message.obtain(eventBus, Event.STAT_OK));
                }else{
                    LOG.d("TextToSpeech is disabled, status (" + stat + ")");
                    eventBus.sendMessage(Message.obtain(eventBus, Event.STAT_FAIL));
                }
            }
        });
    }
    
    private void doOnFinished() {
        LOG.i("VoxRuntime --> Finished Initializing OK.");
        counter.set(0);
        ready = true;
        if (onReady != null)
            onReady.exec(this);
    }
    
    private void shutdownTTS() {
        LOG.d("Shutting down Text-to-Speech service...");
        if(ttsService != null) {
            stopSpeaking();
            ttsService.shutdown();
        }
    }

    private void shutdownRecognizer() {
        if (recognizer != null){
            recognizer.stopListening();
            recognizer.cancel();
            recognizer.destroy();
        }
    }

    private void assertReadiness(){
        if(!ready)
            throw new VoxException(VoxException.ErrorType.RuntimeError, "Runtime not ready.");
    }


}
