package com.exceptions;

import java.io.IOException;

public class UnsupportedDataTypeException extends IOException {

    private String from;

    public UnsupportedDataTypeException(String message) {
        super(message);
    }

    public UnsupportedDataTypeException(String message, String from) {
        super(message);
        this.from = from;
    }

    public UnsupportedDataTypeException(Throwable cause, String from) {
        super(cause.getMessage());
        this.from = from;
    }

    public String getFrom() {
        return from;
    }
}
