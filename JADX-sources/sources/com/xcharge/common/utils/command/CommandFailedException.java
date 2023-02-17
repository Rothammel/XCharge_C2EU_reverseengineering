package com.xcharge.common.utils.command;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class CommandFailedException extends RuntimeException {
    private static final long serialVersionUID = 0;
    private final List<String> args;
    private final List<String> outputLines;

    public CommandFailedException(List<String> args, List<String> outputLines) {
        super(formatMessage(args, outputLines));
        this.args = args;
        this.outputLines = outputLines;
    }

    public List<String> getArgs() {
        return this.args;
    }

    public List<String> getOutputLines() {
        return this.outputLines;
    }

    public static String formatMessage(List<String> args, List<String> outputLines) {
        StringBuilder result = new StringBuilder();
        result.append("Command failed:");
        for (String arg : args) {
            result.append(StringUtils.SPACE).append(arg);
        }
        for (String outputLine : outputLines) {
            result.append("\n  ").append(outputLine);
        }
        return result.toString();
    }
}
