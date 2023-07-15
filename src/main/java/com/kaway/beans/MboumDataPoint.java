package com.kaway.beans;

public class MboumDataPoint extends DataPoint{

    public MboumDataPoint(String time, Long utcTimestamp, float open, float close, float high, float low, int volume) {
        super(time, utcTimestamp, open, close, high, low, volume);
    }
}
