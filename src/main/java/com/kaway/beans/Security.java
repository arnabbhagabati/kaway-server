package com.kaway.beans;

import java.io.Serializable;
import java.util.List;

public class Security implements Serializable {
    private String id;
    private String code;
    private String name;
    private String displayName;
    private SecType type;
    private List<String> constituents;
    private String exchange;

    //Todo add exchange in constructor
    public Security(String id, String code, String name, String displayName, SecType type) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.displayName = displayName;
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
}
