package com.voxmecanica.vdk.core;

/**
 * Part represents dialog parts (i.e. speak, listen, pause, etc)
 */
public class Part {
    private Name name;
    private String title;
    private String text;
    private String sensivitiy;

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
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
