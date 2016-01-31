package com.voxmecanica.vdk.core;

import com.voxmecanica.vdk.api.DialogPart;
import com.voxmecanica.vdk.parser.PartType;

/**
 * Part represents dialog parts (i.e. speak, listen, pause, etc)
 */
public class Part {
    private PartType type;
    private String title;
    private String text;
    private String sensivitiy;

    private DialogPart part;

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
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
