package com.github.ynverxe.eventbus.key;

import java.util.UUID;
import java.util.Objects;

public class Key {
    private final String name;
    
    private Key(String name) {
        Objects.requireNonNull(this.name = name);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Key namespace(String value) {
        Objects.requireNonNull(value);
        return new Key(this.name + ":" + value);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Key)) {
            return false;
        }
        Key key = (Key)o;
        return this.name.equals(key.name);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.name);
    }
    
    public static Key random() {
        return new Key(UUID.randomUUID().toString());
    }
    
    public static Key of(String name) {
        return new Key(name);
    }
    
    public static Key namespace(String namespace, String value) {
        return of(namespace + ":" + value);
    }
}
