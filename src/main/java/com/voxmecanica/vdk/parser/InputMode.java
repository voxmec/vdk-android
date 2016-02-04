package com.voxmecanica.vdk.parser;

import com.google.gson.annotations.SerializedName;

public enum InputMode {
    @SerializedName("ASR") ASR,
    @SerializedName("ALPHANUM") ALPHANUM,
    @SerializedName("NUM") NUM,
    @SerializedName("REC") REC
}
