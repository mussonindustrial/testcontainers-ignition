package com.mussonindustrial.testcontainers.ignition;

public enum EAMType {
    AGENT("Agent"),
    CONTROLLER("Controller");

    private final String value;

    EAMType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

