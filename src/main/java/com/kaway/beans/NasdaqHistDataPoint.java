package com.kaway.beans;

import org.springframework.stereotype.Component;

import java.util.Date;


public class NasdaqHistDataPoint {
    private String date;
    private float closeAmt;

    public NasdaqHistDataPoint(String date,float closeAmt){
        //Date dt = new Date(date);
        this.date =date;
        this.closeAmt =closeAmt;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getCloseAmt() {
        return closeAmt;
    }

    public void setCloseAmt(float closeAmt) {
        this.closeAmt = closeAmt;
    }
}
