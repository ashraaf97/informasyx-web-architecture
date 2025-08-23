package com.example.demo.domain;

public enum Role {
    USER("USER"),
    ADMIN("ADMIN"), 
    SUPER_ADMIN("SUPER_ADMIN");

    private final String name;

    Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}