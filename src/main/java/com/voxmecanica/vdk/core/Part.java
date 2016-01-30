package com.voxmecanica.vdk.core;

/**
 * Part represents dialog parts (i.e. speak, listen, pause, etc)
 */
public class Part {
    private Type type;
    private String title;
    private String text;
    private String sensivitiy;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSensivitiy() {
        return sensivitiy;
    }

    public void setSensivitiy(String sensivitiy) {
        this.sensivitiy = sensivitiy;
    }
}
