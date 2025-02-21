package com.System;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

public class SysLogger {
    private static final Logger logger = Logger.getLogger("SysLogger");
    private static final String LOG_DIR = "log";
    private static final String LOG_FILE = LOG_DIR + "/SysLogger.log";
    private static final int MAX_FILE_SIZE = 16 * 1024; // 512KB rollover size
    private static final int FILE_COUNT = 5; // Keep last 5 log files
    private static final Set<String> skippedClasses = new HashSet<>();
    private static final Set<String> skippedMethods = new HashSet<>();

    static {
        initializeLogger();
    }

    private static void initializeLogger() {
        LogManager.getLogManager().reset();
        logger.setLevel(Level.ALL);
        ensureLogDirectory();

        // Console Handler
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.ALL);
        logger.addHandler(consoleHandler);

        // File Handler with built-in rollover
        try {
            FileHandler fileHandler = new FileHandler(LOG_FILE, MAX_FILE_SIZE, FILE_COUNT, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to initialize file handler", e);
        }
    }

    private static void ensureLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public static void skipClass(String className) {
        skippedClasses.add(className);
    }

    public static void skipMethod(String methodName) {
        skippedMethods.add(methodName);
    }

    private static boolean shouldSkip() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (skippedClasses.contains(element.getClassName()) || skippedMethods.contains(element.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    public static void logInfo(String message) {
        if (shouldSkip()) return;
        logger.info(message);
    }

    public static void logInfo(String message, Throwable thrown) {
        if (shouldSkip()) return;
        logger.log(Level.INFO, message, thrown);
    }

    public static void logWarning(String message) {
        if (shouldSkip()) return;
        logger.warning(message);
    }

    public static void logWarning(String message, Throwable thrown) {
        if (shouldSkip()) return;
        logger.log(Level.WARNING, message, thrown);
    }

    public static void logSevere(String message) {
        if (shouldSkip()) return;
        logger.severe(message);
    }

    public static void logSevere(String message, Throwable thrown) {
        if (shouldSkip()) return;
        logger.log(Level.SEVERE, message, thrown);
    }
}