package com.System;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import com.google.gson.Gson;

public class TaskLogger {

    private static final Logger consoleLogger = Logger.getLogger(TaskLogger.class.getName());

    private static final String LOG_DIR = "log";
    private static final long MAX_FILE_SIZE = 5 * 1024; // 512KB for rollover

    private static final String DEFAULT_NORMAL_LOG_FILE = LOG_DIR + "/normal.log";
    private static final String CANONICAL_LOG_FILE = LOG_DIR + "/canonical.json";
    private static final String TASK_LOG_FILE = LOG_DIR + "/task.json";
    private static final String NORMAL_CANONICAL_LOG_FILE = LOG_DIR + "/normal_canonical.log";
    private static final String NORMAL_TASK_LOG_FILE = LOG_DIR + "/normal_task.log";

    public static final Gson gson = new Gson();

    static {
        ensureLogDirectory();
        consoleLogger.info("Ensuring log directory exists...");

        try {
            FileHandler fileHandler = new FileHandler(DEFAULT_NORMAL_LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            consoleLogger.addHandler(fileHandler);
        } catch (IOException e) {
            consoleLogger.log(Level.SEVERE, "Failed to setup file logging", e);
        }
    }

    public static void ensureLogDirectory() {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    private static String formatLog(Map<String, Object> params) {
        return gson.toJson(params);
    }

    public static void logCanonical(String id, String request, String response, String systemState) {
        Map<String, Object> params = new HashMap<>();
        params.put("Label", "Canonical");
        params.put("id", id);
        params.put("request", request);
        params.put("response", response);
        params.put("systemState", systemState);
        String jsonLog = formatLog(params);
        consoleLogger.info(jsonLog);
        appendLogEntry(CANONICAL_LOG_FILE, jsonLog);
        logInfoTaskNormal(id, request, response, systemState);
    }

    public static void logTask(String id, String request, String response, String systemState) {
        Map<String, Object> params = new HashMap<>();
        params.put("Label", "Task");
        params.put("id", id);
        params.put("request", request);
        params.put("response", response);
        params.put("systemState", systemState);
        String jsonLog = formatLog(params);
        consoleLogger.info(jsonLog);
        appendLogEntry(TASK_LOG_FILE, jsonLog);
        logInfoTaskNormal(id, request, response, systemState);
    }

    public static void logWarning(String id, String request, String systemState, String warningMessage) {
        Map<String, Object> params = new HashMap<>();
        params.put("Label", "Warning");
        params.put("id", id);
        params.put("request", request);
        params.put("systemState", systemState);
        params.put("warningMessage", warningMessage);
        String jsonLog = formatLog(params);

        consoleLogger.warning(jsonLog);
        appendLogEntry(TASK_LOG_FILE, jsonLog);
        logInfoTaskNormal(id, request, warningMessage, systemState);
    }

    public static void logError(String id, String request, String systemState, Throwable error) {
        Map<String, Object> params = new HashMap<>();
        params.put("Label", "Error");
        params.put("id", id);
        params.put("request", request);
        params.put("systemState", systemState);
        params.put("error", error.getMessage());
        String jsonLog = formatLog(params);

        consoleLogger.severe(jsonLog);
        appendLogEntry(TASK_LOG_FILE, jsonLog);
        logInfoTaskNormal(id, request, error.getMessage(), systemState);
    }

    public static void logFatal(String id, String request, String systemState, Throwable error) {
        Map<String, Object> params = new HashMap<>();
        params.put("Label", "Fatal");
        params.put("id", id);
        params.put("request", request);
        params.put("systemState", systemState);
        params.put("error", error.getMessage());
        String jsonLog = formatLog(params);

        consoleLogger.severe(jsonLog);
        appendLogEntry(TASK_LOG_FILE, jsonLog);
        logInfoTaskNormal(id, request, error.getMessage(), systemState);
    }

    public static void logInfoTaskNormal(String id, String request, String response, String systemState) {
        String logLine = "Task Info: id=" + id + " | request=" + request
                + " | response=" + response + " | systemState=" + systemState;
        consoleLogger.info(logLine);
        appendNormalLogEntry(NORMAL_TASK_LOG_FILE, logLine);
    }

    /**
     * Appends a log entry (JSON format) to a log file with rollover support.
     */
    private static void appendLogEntry(String filePath, String logEntry) {
        try {
            File file = new File(filePath);
            checkAndRollover(file); // Check file size and roll over if needed

            String content;
            if (file.exists() && file.length() > 0) {
                content = new String(Files.readAllBytes(Paths.get(filePath))).trim();
                if (!content.startsWith("[")) {
                    content = "[" + content;
                }
                if (!content.endsWith("]")) {
                    content = content + "]";
                }
                content = content.substring(0, content.length() - 1);
                if (!content.trim().equals("[")) {
                    content += ",";
                }
            } else {
                content = "[";
            }
            content += logEntry + "]";
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(content);
            }
        } catch (IOException e) {
            consoleLogger.log(Level.SEVERE, "Failed to append log entry to file: " + filePath, e);
        }
    }

    /**
     * Appends a normal log entry with rollover.
     */
    private static void appendNormalLogEntry(String filePath, String logLine) {
        try {
            File file = new File(filePath);
            checkAndRollover(file); // Check file size and roll over if needed

            try (FileWriter writer = new FileWriter(filePath, true)) {
                writer.write(logLine + System.lineSeparator());
            }
        } catch (IOException e) {
            consoleLogger.log(Level.SEVERE, "Failed to append normal log entry to file: " + filePath, e);
        }
    }

    /**
     * Checks file size and performs a rollover if the file exceeds the max size.
     */
    private static void checkAndRollover(File file) {
        if (file.exists() && file.length() >= MAX_FILE_SIZE) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File rolledFile = new File(file.getParent(), file.getName() + "." + timestamp);
            boolean success = file.renameTo(rolledFile);
            if (!success) {
                consoleLogger.warning("Failed to roll over log file: " + file.getName());
            }
        }
    }
}
