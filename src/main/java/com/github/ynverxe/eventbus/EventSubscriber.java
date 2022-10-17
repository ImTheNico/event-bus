package com.github.imthenico.eventbus;

import com.github.imthenico.eventbus.result.PublishResult;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import com.github.imthenico.eventbus.priority.Priority;
import com.github.imthenico.eventbus.listener.EventHandler;
import org.jetbrains.annotations.NotNull;
import com.github.imthenico.eventbus.key.Key;

public interface EventSubscriber<E> {

    @NotNull Subscription<E> subscribe(
            @NotNull Key key, @NotNull EventHandler<E> eventHandler, @NotNull Priority priority
    );
    
    @Nullable Subscription<E> unsubscribe(@NotNull Key key);
    
    @Nullable Subscription<E> getSubscription(@NotNull Key key);
    
    @NotNull Map<Key, Subscription<E>> getSubscriptions();
    
    boolean isSubscribed(@NotNull Key key);
    
    @NotNull PublishResult callHandlers(@NotNull E event);
}
