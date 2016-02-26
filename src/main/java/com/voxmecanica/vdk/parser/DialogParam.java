package com.voxmecanica.vdk.parser;

public class DialogParam {
    private String id;
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DialogParam param = (DialogParam) o;

        if (id != null ? !id.equals(param.id) : param.id != null) return false;
        return value != null ? value.equals(param.value) : param.value == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Param{" +
                "id='" + id + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
