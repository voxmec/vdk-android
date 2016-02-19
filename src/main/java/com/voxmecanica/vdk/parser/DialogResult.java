package com.voxmecanica.vdk.parser;

import java.util.ArrayList;
import java.util.Map;

public class DialogResult {
    private java.util.ArrayList<Param> params;
    private java.util.Map<String,String> properties;

    public ArrayList<Param> getParams() {
        return params;
    }

    public void setParams(ArrayList<Param> params) {
        this.params = params;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
