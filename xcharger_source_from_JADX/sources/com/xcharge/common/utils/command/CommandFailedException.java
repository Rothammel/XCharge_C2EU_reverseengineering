package com.xcharge.common.utils.command;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class CommandFailedException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private final List<String> args;
    private final List<String> outputLines;

    public CommandFailedException(List<String> args2, List<String> outputLines2) {
        super(formatMessage(args2, outputLines2));
        this.args = args2;
        this.outputLines = outputLines2;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public List<String> getOutputLines() {
        return this.outputLines;
    }

    public static String formatMessage(List<String> args2, List<String> outputLines2) {
        StringBuilder result = new StringBuilder();
        result.append("Command failed:");
        for (String arg : args2) {
            result.append(StringUtils.SPACE).append(arg);
        }
        for (String outputLine : outputLines2) {
            result.append("\n  ").append(outputLine);
        }
        return result.toString();
    }
}
