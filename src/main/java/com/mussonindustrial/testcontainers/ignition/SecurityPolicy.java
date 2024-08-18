package com.mussonindustrial.testcontainers.ignition;

public enum SecurityPolicy {
    APPROVE_ONLY("ApproveOnly"),
    SPECIFIED_LIST("SpecifiedList"),
    UNRESTRICTED("Unrestricted");

    private final String value;

    SecurityPolicy(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
