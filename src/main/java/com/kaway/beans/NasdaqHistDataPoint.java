package com.kaway.beans;


public class NasdaqHistDataPoint {
    private String time;
    private float value;

    public NasdaqHistDataPoint(String time, float value){
        //Date dt = new Date(date);
        this.time = time;
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
