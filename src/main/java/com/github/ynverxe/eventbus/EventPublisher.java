package com.github.imthenico.eventbus;

import java.util.concurrent.CompletableFuture;
import com.github.imthenico.eventbus.result.PublishResult;
import org.jetbrains.annotations.NotNull;

public interface EventPublisher<E> {
     <T extends E> @NotNull PublishResult dispatch(@NotNull T event);
    
     <T extends E> @NotNull CompletableFuture<PublishResult> dispatchAsync(@NotNull T event);
}
