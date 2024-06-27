package com.kaway.beans;

import java.io.Serializable;
import java.util.List;

public class Security implements Serializable {
    private String key;
    private String id;
    private String code;
    private String exchange;
    private SecType type;
    private String name;
    private String displayName;
    private String displayId;
    private List<String> constituents;


    //Todo add exchange in constructor
    public Security(String exchange,String id, String code, String name, String displayName, SecType type) {
        this.exchange = exchange;
        this.key = exchange+"_"+code;
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.displayId = exchange+"_"+displayName;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDisplayId() {
        return displayId;
    }

    public void setDisplayId(String displayId) {
        this.displayId = displayId;
    }
}
