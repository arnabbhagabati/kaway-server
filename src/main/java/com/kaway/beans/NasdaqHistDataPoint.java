package com.kaway.beans;


public class NasdaqHistDataPoint {
    private String date;
    private float data;

    public NasdaqHistDataPoint(String date,float data){
        //Date dt = new Date(date);
        this.date =date;
        this.data = data;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getData() {
        return data;
    }

    public void setData(float data) {
        this.data = data;
    }
}
