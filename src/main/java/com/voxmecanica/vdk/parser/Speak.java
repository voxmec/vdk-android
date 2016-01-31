package com.voxmecanica.vdk.parser;

public class Speak {
    private PartType type;
    private String title;
    private String alt;
    private String text;

    public Speak(){
        type = PartType.SPEAK;
    }

    public Speak(String textToSpeak){
        text = textToSpeak;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType t){
        type = t;
    }

    public String getText(){
        return text;
    }

    public void setText(String s){
        text = s;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }
}
