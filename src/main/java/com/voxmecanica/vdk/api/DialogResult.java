package com.voxmecanica.vdk.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DialogResult {
    private List<DialogParam> params;
    private Map<String,String> properties;

    public List<DialogParam> getParams() {
        return params;
    }

    public void setParams(List<DialogParam> params) {
        this.params = params;
    }

    public Map<String, String> getParamsAsMap() {
        if (params == null){
            return null;
        }

        Map<String, String> result = new HashMap<String, String>();
        for (DialogParam param : params){
            result.put(param.getId(), param.getValue());
        }
        return result;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
