package com.kaway.beans;

public class Security {
    private String code;
    private String id;
    private String name;
    private SecType type;

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
}
