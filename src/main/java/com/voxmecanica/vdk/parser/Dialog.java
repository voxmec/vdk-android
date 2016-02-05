package com.voxmecanica.vdk.parser;

import java.util.ArrayList;
import java.util.Map;

public class Dialog {
    private Map<String,String> properties;
    private ArrayList<Part> parts;
    private ArrayList<Param> params;

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public ArrayList<Part> getParts() {
        return parts;
    }

    public void setParts(ArrayList<Part> parts) {
        this.parts = parts;
    }

    public ArrayList<Param> getParams() {
        return params;
    }

    public void setParams(ArrayList<Param> params) {
        this.params = params;
    }
}
