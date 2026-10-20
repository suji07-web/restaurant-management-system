package com.restaurant.demo;

/** A tiny class used only to show WHEN a container creates a bean. */
public class DemoBean {

    private static final java.util.List<String> creationLog = new java.util.ArrayList<>();

    private final String name;

    public DemoBean(String name) {
        this.name = name;
        creationLog.add("DemoBean '" + name + "' constructor called");
    }

    public String greet() {
        return "Hello from " + name;
    }

    public static java.util.List<String> getCreationLog() {
        return creationLog;
    }

    public static void clearLog() {
        creationLog.clear();
    }
}
