package com.github.ynverxe.eventbus.listener;

import org.jetbrains.annotations.NotNull;

public interface EventHandler<E> {
    void handleEvent(@NotNull E event) throws Throwable;
}
