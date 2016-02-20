package com.voxmecanica.vdk.api;

import com.voxmecanica.vdk.core.VoxRuntime;
import com.voxmecanica.vdk.parser.Dialog;
import com.voxmecanica.vdk.parser.DialogResult;

import java.net.URI;
import java.util.Map;

public interface DialogContext {
    public static final String KEY_RUNTIME = "key.runtime";
    public static final String KEY_PARAMS = "key.params";
    public static final String KEY_DIALOG = "key.dialog";
    public static final String KEY_DIALOG_RESULT = "key.dialog.result";
    public static final String KEY_DIALOG_PROGRAM_COUNTER = "key.dialog.pc";
    public static final String KEY_DIALOG_REQUEST = "key.dialog.request";
    public static final String KEY_VOICE_REC_RESULT_CODE = "key.voice.rec.rc";
    public static final String KEY_VOICE_REC_MATCHES = "key.voice.rec.matches";
    public static final String KEY_VOICE_REC_SCORES = "key.voice.rec.scores";
    public static final String KEY_VOICE_REC_ERROR = "key.voice.rec.error";
    public static final String KEY_CURRENT_INPUT_PART = "key.current.input.part";
    public static final String KEY_CURRENT_DIALOG_PART = "key.current.dialog.part";

    Map<String,String> getParameters();
    void setParameters(Map<String, String> params);
    void putValue(String key, Object value);
    Object getValue(String key);
    Map<String,? super Object> getValues();
    VoxRuntime getRuntime();
    public void setDialog(Dialog d);
    public Dialog getDialog();
    public DialogResult getDialogResult();
    public void setDialogResult(DialogResult dr);
    public void setDialogUri(URI uri);
    public URI getDialogUri();
}
