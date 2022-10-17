package com.github.ynverxe.eventbus;

public interface EventValidator<E> {
    Throwable validate(E event) throws Throwable;
}
