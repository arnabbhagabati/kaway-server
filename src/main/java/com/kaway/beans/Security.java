package com.kaway.beans;

import java.io.Serializable;
import java.util.List;

public class Security implements Serializable {
    private String code;
    private String id;
    private String name;
    private SecType type;
    private List<String> constituents;

    public Security(String code, String id, String name,SecType type) {
        this.code = code;
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SecType getType() {
        return type;
    }

    public void setType(SecType type) {
        this.type = type;
    }

    public List<String> getConstituents() {
        return constituents;
    }

    public void setConstituents(List<String> constituents) {
        this.constituents = constituents;
    }
}
