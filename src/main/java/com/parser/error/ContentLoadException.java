package com.parser.error;

public class ContentLoadException extends RuntimeException {

    private final int status;

    public ContentLoadException(String message, int status) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
