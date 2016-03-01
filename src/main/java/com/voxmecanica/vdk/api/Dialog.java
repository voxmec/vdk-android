package com.voxmecanica.vdk.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog {
    private Map<String,String> properties;
    private List<Part> parts;
    private List<DialogParam> params;

    public Map<String, String> getProperties() {
        return (properties != null) ? properties : (properties = new HashMap<String, String>());
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public List<Part> getParts() {
        return (parts != null) ? parts : (parts = new ArrayList<Part>());
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }

    public List<DialogParam> getParams() {
        return (params != null) ? params : (params = new ArrayList<DialogParam>());
    }

    public void setParams(ArrayList<DialogParam> params) {
        this.params = params;
    }

    public String getOrigUriProp() {
        if (getProperties() != null){
            return getProperties().get(Prop.ORIG_URI);
        }
        return null;
    }

    public String getSubmitUriProp(){
        if (getProperties() != null){
            return getProperties().get(Prop.SUBMIT_URI);
        }
        return null;
    }

    public String getSubmintMethodProp(){
        if (getProperties() != null){
            return getProperties().get(Prop.SUBMIT_METHOD);
        }
        return null;
    }

    public static class Prop {
        public static final String ORIG_URI = "originUri";
        public static final String SUBMIT_URI = "submitUri";
        public static final String SUBMIT_METHOD = "submitMethod";
        public static final String SUBMIT_FORMAT = "submitFormat";

    }
}
