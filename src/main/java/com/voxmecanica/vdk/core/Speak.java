package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.SpeakablePart;

public class Speak extends AbstractDialogPart implements SpeakablePart {

    private String textToSpeak;
    private String title;
    private String content;
    private float speachRate;
    private float speachPitch;
    private boolean displayed;

    public Speak(String text, String title, String content) {
        setMetaPart(MetaPart.SPEAK);
        textToSpeak = text;
        this.title = title;
        this.content = content;
        displayed = (title != null || content != null);
    }

    public Speak(String text) {
        this(text, null, null);
    }

    public Speak(String text, String title, String content, float rate, float pitch) {
        this(text, title, content);
        speachRate = rate;
        speachPitch = pitch;
    }

    @Override
    public String getTextToSpeak() {
        return textToSpeak;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public float getSpeachRate() {
        return speachRate;
    }

    @Override
    public float getSpeachPitch() {
        return speachPitch;
    }

    @Override
    public boolean isDisplayed() {
        return displayed;
    }

    @Override
    public String toString() {
        return "Speak [textToSpeak=" + textToSpeak + ", speachRate="
                + speachRate + ", speachPitch=" + speachPitch + "]";
    }
}
