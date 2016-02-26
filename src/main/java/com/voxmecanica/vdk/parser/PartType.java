package com.voxmecanica.vdk.parser;

import com.google.gson.annotations.SerializedName;

// Type is the name of dialog parts supported.
public enum PartType {
    @SerializedName("INPUT")INPUT,
    @SerializedName("SPEAK")SPEAK,
    @SerializedName("PLAYBACK")PLAYBACK,
    @SerializedName("DISPLAY")DISPLAY,
    @SerializedName("PAUSE")PAUSE,
    @SerializedName("END")END
}

