package com.github.imthenico.eventbus;

public interface EventValidator<E> {
    Throwable validate(E event) throws Throwable;
}
