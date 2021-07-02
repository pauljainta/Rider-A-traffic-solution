package com.example.rider_atrafficsolution;

import android.util.Log;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CalculateFareClass {

    public static float nextFloatBetween(float min, float max) {
        return (new Random().nextFloat() * (max - min)) + min;
    }
    public static double CalculateFare(double estimated_fare)
    {

        double extra=nextFloatBetween(0,1)*100;

        Log.i("extra " , String.valueOf(extra));

        Random random=new Random();
        int prob=random.nextInt(100);
        if(prob<20)
        {
            return estimated_fare;
        }

        return estimated_fare+ Math.abs(extra);


    }
}
