package com.voxmecanica.vdk.parser;

public class Part{
    private String id;
    private PartType type;
    private Speak speak;

    public Speak getSpeak(){
        return speak;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
    }

    public void setSpeak(Speak speak) {
        this.speak = speak;
    }
}
