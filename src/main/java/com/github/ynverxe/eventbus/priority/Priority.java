package com.github.ynverxe.eventbus.priority;

import org.jetbrains.annotations.NotNull;

public class Priority implements Comparable<Priority> {

    public static final byte LOWEST_VALUE = Byte.MIN_VALUE;
    public static final byte LOW_VALUE = -64;
    public static final byte NORMAL_VALUE = 0;
    public static final byte HIGH_VALUE = 64;
    public static final byte HIGHEST_VALUE = Byte.MAX_VALUE;
    public static final Priority LOWEST = new Priority(LOWEST_VALUE);
    public static final Priority LOW = new Priority(LOW_VALUE);
    public static final Priority NORMAL = new Priority(NORMAL_VALUE);
    public static final Priority HIGH = new Priority(HIGH_VALUE);
    public static final Priority HIGHEST = new Priority(HIGHEST_VALUE);
    
    private final byte value;
    
    private Priority(byte value) {
        this.value = value;
    }
    
    public byte getValue() {
        return this.value;
    }
    
    @Override
    public int compareTo(@NotNull Priority o) {
        return Integer.compare(this.value, o.value);
    }
    
    public static Priority of(int value) {
        return new Priority((byte)value);
    }
}
