package com.example.rider_atrafficsolution;

import java.util.Comparator;

public class TimestampSorter implements Comparator<History> {


    @Override
    public int compare(History h1, History h2) {
        return h2.finishTime.compareTo(h1.finishTime);
    }
}
