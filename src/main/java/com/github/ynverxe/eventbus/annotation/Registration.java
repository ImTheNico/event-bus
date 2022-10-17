package com.github.ynverxe.eventbus.annotation;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Registration {
    @NotNull
    Class<?> value();
    
    @Nullable
    Class<?>[] acceptTo() default {};
    
    int priority() default 0;
    
    boolean onlyChildren() default false;
}
