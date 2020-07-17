package com.farthestgate.android.utils;

import java.io.IOException;

/**
 * Created by Hanson on 18/04/2014.
 */
@SuppressWarnings("serial")
public class TimeoutException extends Exception {
    public TimeoutException(IOException e) {
        super(e);
    }
}