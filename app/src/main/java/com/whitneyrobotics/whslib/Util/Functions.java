package com.whitneyrobotics.whslib.Util;

public class Functions {

    private Functions(){}
    public static <T> T requireNotNull(T arg){
        if (arg == null){
            throw new IllegalArgumentException("Null argument");
        }
        return arg;
    }
}
