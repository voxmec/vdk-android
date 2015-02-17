package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.api.DialogPart;
import com.voxmecanica.vdk.api.DialogPart.MetaPart;
import com.voxmecanica.vdk.api.DialogRequest;
import com.voxmecanica.vdk.http.HttpService;
import com.voxmecanica.vdk.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

public class VoxDialogParser {
    private static final Logger LOG = new Logger("VoxDialogParser");
    
    public static VoxDialog parse(String dialog){
        if (dialog == null) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Dialog string cannot be null");
        }
        
        LOG.d(String.format("VoxDialogParser - Received string [%s]", dialog));
        JSONObject jsonDialog = null;
        try {
            JSONTokener tknr = new JSONTokener(dialog);
            jsonDialog = (JSONObject) tknr.nextValue();
        } catch (JSONException e) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError,
                    "Syntax error found in dialog:" + e.getMessage());
        } catch (RuntimeException e) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, e);
        }
        
        return parse (jsonDialog);
    }
    
    

    private static VoxDialog parse(JSONObject jsonDialog) {
        VoxDialog dialog = new VoxDialog();
        // Parse {dialogs:[]} array
        try {
            JSONArray jsonParts = getDialogParts(jsonDialog);
            if (jsonParts != null) {
                List<DialogPart> dialogParts = new LinkedList<DialogPart>();
                dialog.setParts(dialogParts);
                for (int i = 0; i < jsonParts.length(); i++) {
                    JSONObject dialogObj = jsonParts.getJSONObject(i);
                    LOG.d("Parsing JSON string " + dialogObj.toString());
                    DialogPart part = parseDialogPart(dialogObj);
                    dialogParts.add(part);
                }
            } else {
                throw new VoxException(VoxException.ErrorType.DialogExecutionError, "\"dialog:\" seems to be empty or null.");
            }
        } catch (JSONException ex) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Error parsing dialog:[] array object: " + ex.getMessage());
        } catch (Exception ex) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Error parsing dialog:[] array object:  " + ex.getMessage());
        }

        // Parsing optional {submit:{}} object
        if (!jsonDialog.isNull(MetaPart.SUBMIT.label())) {
            try {
                JSONObject submitObject = jsonDialog.getJSONObject(MetaPart.SUBMIT.label());
                //Log.d(TAG, "Parsing JSON string " + submitObject.toString());
                DialogRequest submission = parseSubmissionRequest(submitObject);
                dialog.setDialogSubmission(submission);
            } catch (JSONException e) {
                throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Error parsing submit:{} object : " + e.getMessage());
            } catch (Exception e) {
                throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Error parsing submit:{} object : " + e.getMessage());
            }
        }

        return dialog;
    }

    private static JSONArray getDialogParts(JSONObject jsonObj) {
        JSONArray jsonParts;
        try {
            jsonParts = jsonObj.getJSONArray(MetaPart.DIALOG.label());
        } catch (JSONException e) {
            throw new VoxException("Missing dialog:[] array: " + e.getMessage());
        }

        return jsonParts;
    }

    /**
     * Parses the dialog[] array of dialog objects.
     *
     * @param part
     * @return
     * @throws Exception
     */
    private static DialogPart parseDialogPart(JSONObject part) throws Exception {
        // parse SPEAK
        String value = null;
        if (!part.isNull(MetaPart.SPEAK.label())) {
            LOG.d(MetaPart.SPEAK.name() + " object found.");
            String txtToSpeak = getStringAttributeSafely(part, MetaPart.SPEAK.label());
            String title = getStringAttributeSafely(part, MetaPart.TITLE.label());
            String content = getStringAttributeSafely(part, MetaPart.CONTENT.label());
            return new Speak(txtToSpeak, title, content);
        }

        // parse PLAY
        if (!part.isNull(MetaPart.PLAY.label())) {
            LOG.d(MetaPart.PLAY.name() + " object found.");
            String uri = getStringAttributeSafely(part, MetaPart.PLAY.label());
            String title = getStringAttributeSafely(part, MetaPart.TITLE.label());
            String content = getStringAttributeSafely(part, MetaPart.CONTENT.label());
            return new Play(URI.create(uri), title, content);
        }

        // parse PAUSE
        if (!part.isNull(MetaPart.PAUSE.label())) {
            LOG.d(MetaPart.PAUSE.name() + " object found.");
            long dur = 500L;
            try {
                dur = part.getLong(MetaPart.PAUSE.label());
            } catch (JSONException ex) {
                LOG.d("Pause duration value was bad, using default value.");
            }
            return new Pause(dur);
        }

        // parse INPUT
        if (!part.isNull(MetaPart.INPUT.label())) {
            LOG.d(MetaPart.INPUT.name() + " object found.");
            String paramName = part.getString(MetaPart.INPUT.label());
            String prompt = getStringAttributeSafely(part, MetaPart.INPUT_PROMPT.label());
            boolean displayed = getBoolAttributeSafely(part, MetaPart.DISPLAYED.label());
            return new Input(prompt, paramName, displayed);
        }

        // parse PARAM
        if (!part.isNull(MetaPart.PARAM.label())) {
            LOG.d(MetaPart.PARAM.name() + " object found.");
            String name = part.getString(MetaPart.PARAM.label());
            String val = null;
            if (!part.isNull(MetaPart.PARAM_VALUE.label())) {
                val = part.getString(MetaPart.PARAM_VALUE.label());
                return new DialogParameter(name, val);
            } else {
                LOG.d("Param object missing value attribute. Object will be ignored.");
                return null;
            }
        }

        return null;
    }

    private static DialogRequest parseSubmissionRequest(JSONObject submit) throws Exception {
        String toVal = null;
        try {
            toVal = submit.getString(MetaPart.SUBMIT_TO.label());
        } catch (JSONException ex) {
            throw new Exception("Missing \"to:\" attribute");
        }
        String methodVal = null;
        try {
            methodVal = submit.getString(MetaPart.SUBMIT_METHOD.label());
        } catch (JSONException ex) {
            methodVal = HttpService.Config.DEFAULT_SUBMIT_METHOD;
        }

        return new DialogSubmissionRequest(URI.create(toVal), methodVal);
    }

    private static String getStringAttributeSafely(JSONObject part, String attribName) throws JSONException {
        return (!part.isNull(attribName)) ? part.getString(attribName) : null;
    }

    private static boolean getBoolAttributeSafely(JSONObject part, String attribName) throws JSONException {
        return (!part.isNull(attribName)) ? part.getBoolean(attribName) : false;
    }
}
