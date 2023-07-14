package com.kaway.beans;

import java.util.List;

public class Dashboard {
    String name;
    List<Security> securityList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Security> getSecurityList() {
        return securityList;
    }

    public void setSecurityList(List<Security> securityList) {
        this.securityList = securityList;
    }

    public Dashboard(String name, List<Security> securityList) {
        this.name = name;
        this.securityList = securityList;
    }
}
