package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.Dialog;
import com.voxmecanica.vdk.api.DialogContext;
import com.voxmecanica.vdk.api.DialogResult;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class VoxDialogContext implements DialogContext {

    private Map<String, ? super Object> context;

    public VoxDialogContext(VoxRuntime runtime) {
        context = new HashMap<String, Object>();
        context.put(DialogContext.KEY_RUNTIME, runtime);
        context.put(KEY_PARAMS, new HashMap<String, String>());
        context.put(DialogContext.KEY_DIALOG_PROGRAM_COUNTER, new Integer(0));
    }

    @Override
    public Map<String, String> getParameters() {
        return (Map<String, String>) context.get(KEY_PARAMS);
    }

    @Override
    public void setParameters(Map<String, String> params) {
        context.put(KEY_PARAMS, params);
    }

    @Override
    public void putValue(String key, Object value) {
        context.put(key, value);
    }

    @Override
    public Object getValue(String key) {
        return context.get(key);
    }

    @Override
    public Map<String, ? super Object> getValues() {
        HashMap<String,Object> copy = new HashMap<String, Object>(context);
        return copy;
    }
    protected void setValues(Map<String, ? super Object> vals){
        context = vals;
    }

    @Override
    public VoxRuntime getRuntime() {
        return (VoxRuntime) context.get(KEY_RUNTIME);
    }

    @Override
    public void setDialog(Dialog d) {
        context.put(DialogContext.KEY_DIALOG, d);
    }

    @Override
    public Dialog getDialog() {
        return (Dialog) context.get(DialogContext.KEY_DIALOG);
    }

    @Override
    public void setDialogResult(DialogResult d) {
        context.put(DialogContext.KEY_DIALOG_RESULT, d);
    }

    @Override
    public DialogResult getDialogResult() {
        return (DialogResult) context.get(DialogContext.KEY_DIALOG_RESULT);
    }

    @Override
    public void setDialogUri(URI uri) {
        context.put(DialogContext.KEY_DIALOG_REQUEST, uri);
    }

    @Override
    public URI getDialogUri() {
        return (URI) context.get(DialogContext.KEY_DIALOG_REQUEST);
    }

}
