package com.github.ynverxe.eventbus;

import java.util.concurrent.CompletableFuture;
import com.github.ynverxe.eventbus.result.PublishResult;
import com.github.ynverxe.eventbus.annotation.Registration;
import com.github.ynverxe.eventbus.key.Key;
import com.github.ynverxe.eventbus.listener.EventHandler;
import com.github.ynverxe.eventbus.listener.ListenerClass;
import com.github.ynverxe.eventbus.priority.Priority;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class SimpleEventBus<E> implements EventBus<E> {

    private final Map<Class, EventSubscriber> subscriptions;
    private final Map<Class, EventValidator> validatorMap;
    private final Class<E> eventClass;
    private final EventValidator<E> defaultValidator;
    private final Executor executor;
    
    public SimpleEventBus(Class<E> eventClass, EventValidator<E> defaultValidator, Executor executor) {
        this.subscriptions = new ConcurrentHashMap<>();
        this.validatorMap = new ConcurrentHashMap<>();
        this.defaultValidator = defaultValidator;
        this.executor = executor;
        Objects.requireNonNull(this.eventClass = eventClass);
    }

    @Override
    public <T extends E> @NotNull Subscription<T> subscribe(
            @NotNull Key key,
            @NotNull Class<T> eventClass,
            @NotNull EventHandler<T> handler,
            @NotNull Priority priority
    ) {
        EventSubscriber<T> subscriber = getSubscriber(eventClass);
        subscriber.unsubscribe(key);

        return subscriber.subscribe(key, handler, priority);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> @NotNull List<Subscription<T>> subscribeEventHandler(
            @NotNull Key key, @NotNull EventHandler<T> eventHandler
     ) {
        List<Subscription<T>> subscriptions = new ArrayList<>();
        Registration registration = eventHandler.getClass().getAnnotation(Registration.class);

        if (registration == null) {
            throw new IllegalStateException("Handler is not annotated with @Registration");
        }

        Class parentClass = registration.value();

        try {
            eventHandler.getClass().getMethod("handleEvent", parentClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No handler method found with '" + parentClass + "' parameter", e);
        }

        Class[] childrenClasses = registration.acceptTo();
        Priority priority = Priority.of(registration.priority());
        if (childrenClasses != null) {
            for (Class childrenClass : childrenClasses) {
                if (!parentClass.isAssignableFrom(childrenClass)) {
                    throw new IllegalStateException(childrenClass + " is not assignable from " + parentClass);
                }

                Subscription<T> subscription = subscribe(key, childrenClass, eventHandler, priority);
                subscriptions.add(subscription);
            }
        }

        if (!registration.onlyChildren()) {
            Subscription<T> subscription2 = subscribe(key, parentClass, eventHandler, priority);
            subscriptions.add(subscription2);
        }

        return subscriptions;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Map<Class<? extends E>, Map<Key, Subscription<? extends E>>> subscribeAll(
            @NotNull Key key, @NotNull ListenerClass listenerClass
    ) {
        List<Subscription<? extends E>> subscriptions = new ArrayList<>();
        Class<ListenerClass> clazz = (Class<ListenerClass>) listenerClass.getClass();

        for (Method method : clazz.getMethods()) {
            Registration registration = method.getAnnotation(Registration.class);

            if (registration != null) {
                Parameter[] parameters = method.getParameters();

                if (parameters.length == 0) {
                    throw new IllegalStateException(method.getName() + " does not contains parameters");
                }

                Class parameterType = parameters[0].getType();
                Class handlerTarget = registration.value();

                if (!parameterType.isAssignableFrom(handlerTarget)) {
                    throw new IllegalStateException(method.getName() + " parameter type is not of the specified type of @Registration");
                }

                Key methodKey = key.namespace(method.getName());
                method.setAccessible(true);

                EventHandler eventHandler = event -> {
                    try {
                        method.invoke(listenerClass, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                };

                Class[] childrenClasses = registration.acceptTo();
                Priority priority = Priority.of(registration.priority());
                if (childrenClasses != null) {
                    for (Class childrenClass : childrenClasses) {
                        if (!handlerTarget.isAssignableFrom(childrenClass)) {
                            throw new IllegalStateException(childrenClass + " is not assignable from " + handlerTarget);
                        }

                        Subscription<E> subscription = subscribe(methodKey, childrenClass, (EventHandler<E>) eventHandler, priority);
                        subscriptions.add(subscription);
                    }
                }

                Subscription<E> subscription = subscribe(methodKey, handlerTarget, (EventHandler<E>) eventHandler, Priority.of(registration.priority()));
                subscriptions.add(subscription);
            }
        }

        Map<Class<? extends E>, Map<Key, Subscription<? extends E>>> subscriptionMap = new HashMap<>();
        for (Subscription<? extends E> subscription : subscriptions) {
            subscriptionMap.computeIfAbsent(subscription.getTargetListening(), k -> new HashMap()).put(subscription.getKey(), subscription);
        }

        return subscriptionMap;
    }

    @Override
    public <T extends E> @Nullable Map<Key, Subscription<T>> unsubscribeAll(
            @NotNull Class<T> eventClass
    ) {
        EventSubscriber<T> subscriber = getSubscriber(eventClass);

        Map<Key, Subscription<T>> subscriptions = subscriber.getSubscriptions();
        subscriptions.keySet().forEach(subscriber::unsubscribe);

        return subscriptions;
    }
    
    @Nullable
    @Override
    public <T extends E> Subscription<T> unsubscribe(
            @NotNull Class<T> eventClass, @NotNull Key key
    ) {
        return getSubscriber(eventClass).unsubscribe(key);
    }
    
    @Override
    public <T extends E> void cancelSubscription(@NotNull Subscription<T> subscription) {
        unsubscribe(subscription.getTargetListening(), subscription.getKey());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> @NotNull EventSubscriber<T> getSubscriber(@NotNull Class<T> eventClass) {
        return this.subscriptions.computeIfAbsent(eventClass, k -> new SimpleEventSubscriber(eventClass));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends E> @NotNull PublishResult dispatch(@NotNull T event) {
        try {
            Class eventClass = event.getClass();
            if (defaultValidator != null) {
                Throwable throwable = defaultValidator.validate(event);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }

            EventValidator<T> validator = validatorMap.get(eventClass);
            if (validator != null) {
                Throwable throwable = validator.validate(event);
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            }

            EventSubscriber<E> subscriber = getSubscriber(eventClass);
            return subscriber.callHandlers(event);
        } catch (Throwable throwable3) {
            throw new RuntimeException(throwable3);
        }
    }
    
    @Override
    public @NotNull <T extends E> CompletableFuture<PublishResult> dispatchAsync(@NotNull T event) {
        return CompletableFuture.supplyAsync(() -> dispatch(event), executor);
    }
    
    @Override
    public <T extends E> void addEventValidator(
            @NotNull Class<T> eventClass, @NotNull EventValidator<T> validator
    ) {
        validatorMap.put(eventClass, validator);
    }
    
    @Nullable
    @Override
    public EventValidator<E> getDefaultValidator() {
        return defaultValidator;
    }

    @Override
    public @NotNull Class<E> getEventClass() {
        return eventClass;
    }
}
