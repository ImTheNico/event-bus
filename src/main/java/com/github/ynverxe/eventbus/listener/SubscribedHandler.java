package com.github.imthenico.eventbus.listener;

import org.jetbrains.annotations.NotNull;
import com.github.imthenico.eventbus.priority.Priority;
import com.github.imthenico.eventbus.key.Key;

public class SubscribedHandler<E> implements Comparable<SubscribedHandler<E>> {
    private final Key key;
    private final Priority priority;
    private final EventHandler<E> eventHandler;
    
    public SubscribedHandler(Key key, Priority priority, EventHandler<E> eventHandler) {
        this.key = key;
        this.priority = priority;
        this.eventHandler = eventHandler;
    }
    
    public Key getKey() {
        return this.key;
    }
    
    public Priority getPriority() {
        return this.priority;
    }
    
    public EventHandler<E> getEventHandler() {
        return this.eventHandler;
    }
    
    @Override
    public int compareTo(@NotNull SubscribedHandler<E> o) {
        return this.priority.compareTo(o.priority);
    }
}
