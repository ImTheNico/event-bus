package com.github.imthenico.eventbus.result;

public class PublishResult {
    private final Throwable error;

    public PublishResult(Throwable error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return error == null;
    }

    public Throwable getError() {
        return error;
    }
}
