package com.github.lucky44x.luckyutil.numbers;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtilities {

    private static final Pattern integerPattern = Pattern.compile("[+-]?[0-9][0-9]*");
    private static final Pattern floatPattern = Pattern.compile("[+-]?\\d+(\\.\\d+)?([Ee][+-]?\\d+)?");

    public static boolean isStringValidFloat(String in) {
        Matcher m = floatPattern.matcher(in);
        return m.find() && m.group().equals(in);
    }

    public static boolean isStringValidInt(String in) {
        Matcher m = integerPattern.matcher(in);
        return m.find() && m.group().equals(in);
    }
}
