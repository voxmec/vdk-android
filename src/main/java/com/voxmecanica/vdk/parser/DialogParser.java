package com.voxmecanica.vdk.parser;

import com.google.gson.Gson;
import com.voxmecanica.vdk.api.Dialog;

public class DialogParser {
    private Gson gson;

    public DialogParser(){
        gson = new Gson();
    }

    public Dialog parse(String json) {
        Dialog dialog = gson.fromJson(json, Dialog.class);
        return dialog;
    }

}
