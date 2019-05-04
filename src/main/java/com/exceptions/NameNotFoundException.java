package com.exceptions;

public class NameNotFoundException extends Exception {

    public NameNotFoundException() {
    }

    public NameNotFoundException(String message) {
        super(message);
    }
}
