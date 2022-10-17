package com.github.imthenico.eventbus;

import java.util.Arrays;
import java.util.ArrayList;
import com.github.imthenico.eventbus.result.PublishResult;
import java.util.Collections;
import org.jetbrains.annotations.Nullable;
import com.github.imthenico.eventbus.priority.Priority;
import com.github.imthenico.eventbus.listener.EventHandler;
import org.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.github.imthenico.eventbus.listener.SubscribedHandler;
import java.util.List;
import com.github.imthenico.eventbus.key.Key;
import java.util.Map;

public class SimpleEventSubscriber<E> implements EventSubscriber<E> {

    private final Map<Key, Subscription<E>> subscriptionMap;
    private final Class<E> eventClass;
    private final List<SubscribedHandler<E>> subscribedHandlers;
    
    public SimpleEventSubscriber(Class<E> eventClass) {
        this.subscriptionMap = new ConcurrentHashMap<>();
        this.subscribedHandlers = new CopyOnWriteArrayList<>();
        this.eventClass = Objects.requireNonNull(eventClass);
    }

    @Override
    public @NotNull Subscription<E> subscribe(
            @NotNull Key key, @NotNull EventHandler<E> eventHandler, @NotNull Priority priority
    ) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(eventHandler);
        Objects.requireNonNull(priority);

        if (this.subscriptionMap.containsKey(key)) {
            throw new IllegalStateException("'" + key.getName() + "' is already registered");
        }

        Subscription<E> subscription = new Subscription<>(key, this.eventClass, eventHandler, priority);
        this.subscriptionMap.put(key, subscription);

        SubscribedHandler<E> subscribedHandler = new SubscribedHandler<>(key, priority, eventHandler);
        this.subscribedHandlers.add(subscribedHandler);
        this.sortHandlers();

        return subscription;
    }
    
    @Override
    public @Nullable Subscription<E> unsubscribe(@NotNull Key key) {
        if (!subscriptionMap.containsKey(key)) {
            return null;
        }

        Subscription<E> subscription = subscriptionMap.remove(key);
        subscribedHandlers.removeIf(handler -> key.equals(handler.getKey()));
        return subscription;
    }

    @Override
    public @Nullable Subscription<E> getSubscription(@NotNull Key key) {
        return subscriptionMap.get(key);
    }

    @Override
    public @NotNull Map<Key, Subscription<E>> getSubscriptions() {
        return Collections.unmodifiableMap(subscriptionMap);
    }
    
    @Override
    public boolean isSubscribed(@NotNull Key key) {
        return subscriptionMap.containsKey(key);
    }

    @Override
    public @NotNull PublishResult callHandlers(@NotNull E event) {
        Objects.requireNonNull(event);

        if (!subscribedHandlers.isEmpty()) {
            for (SubscribedHandler<E> handler : subscribedHandlers) {
                try {
                    handler.getEventHandler().handleEvent(event);
                } catch (Throwable throwable) {
                    return new PublishResult(throwable);
                }
            }
        }

        return new PublishResult(null);
    }

    @SuppressWarnings("unchecked")
    private void sortHandlers() {
        SubscribedHandler<E>[] array = subscribedHandlers.toArray(new SubscribedHandler[0]);
        Arrays.sort(array);

        int i = 0;
        for (SubscribedHandler<E> subscribedHandler : array) {
            subscribedHandlers.set(i, subscribedHandler);
            ++i;
        }
    }
}
