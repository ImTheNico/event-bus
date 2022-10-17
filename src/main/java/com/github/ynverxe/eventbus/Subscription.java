package com.github.ynverxe.eventbus;

import com.github.ynverxe.eventbus.key.Key;
import com.github.ynverxe.eventbus.listener.EventHandler;
import com.github.ynverxe.eventbus.priority.Priority;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;

public class Subscription<E> implements Comparable<Subscription<E>> {

    private final Key key;
    private final Class<E> targetListening;
    private final EventHandler<E> handler;
    private final Priority priority;
    
    public Subscription(Key key, Class<E> targetListening, EventHandler<E> handler, Priority priority) {
        Objects.requireNonNull(this.key = key);
        Objects.requireNonNull(this.targetListening = targetListening);
        Objects.requireNonNull(this.handler = handler);
        Objects.requireNonNull(this.priority = priority);
    }

    public @NotNull Key getKey() {
        return this.key;
    }

    public @NotNull Class<E> getTargetListening() {
        return targetListening;
    }
    
    public @NotNull EventHandler<? extends E> getHandler() {
        return handler;
    }
    
    public @NotNull Priority getPriority() {
        return priority;
    }
    
    @Override
    public int compareTo(@NotNull Subscription<E> o) {
        return this.priority.compareTo(o.priority);
    }
}
