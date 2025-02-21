package com.System;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.time.Instant;
import java.util.Map;

public class JSONFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        StringBuilder json = new StringBuilder();
        json.append("{");

        // Timestamp
        appendField(json, "timestamp", Instant.ofEpochMilli(record.getMillis()).toString());
        json.append(", ");

        // Level
        appendField(json, "level", record.getLevel().toString());
        json.append(", ");

        // Logger name
        appendField(json, "logger", record.getLoggerName());
        json.append(", ");

        // Message
        appendField(json, "message", record.getMessage());
        json.append(", ");

        // Parameters (e.g., id, request, response, systemState)
        Object[] params = record.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paramMap = (Map<String, Object>) params[0];
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                appendField(json, entry.getKey(), entry.getValue());
                json.append(", ");
            }
        }

        // Error context
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            json.append("\"error\": {");
            appendField(json, "message", thrown.getMessage());
            json.append(", ");
            appendField(json, "stackTrace", getStackTrace(thrown));
            json.append("}, ");
        }

        // Remove trailing comma and space if any
        if (json.length() >= 2 && json.substring(json.length() - 2).equals(", ")) {
            json.setLength(json.length() - 2);
        }

        json.append("}\n");
        return json.toString();
    }

    private static void appendField(StringBuilder json, String key, Object value) {
        json.append("\"").append(key).append("\": ");
        if (value == null) {
            json.append("null");
        } else if (value instanceof String) {
            json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
        } else {
            json.append(value);
        }
    }

    private static String getStackTrace(Throwable throwable) {
        StringBuilder stackTrace = new StringBuilder("[");
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.append("\"")
                      .append(element.toString().replace("\"", "\\\""))
                      .append("\", ");
        }
        // Remove trailing comma and space if any
        if (stackTrace.length() >= 2 && stackTrace.substring(stackTrace.length() - 2).equals(", ")) {
            stackTrace.setLength(stackTrace.length() - 2);
        }
        stackTrace.append("]");
        return stackTrace.toString();
    }
}
