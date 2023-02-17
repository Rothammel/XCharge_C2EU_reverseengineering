package com.xcharge.common.utils.command;

import android.util.Log;
import com.xcharge.common.utils.Strings;
import com.xcharge.common.utils.Threads;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public final class Command {
    private final List<String> args;
    private final Map<String, String> env;
    private final boolean nativeOutput;
    private final boolean permitNonZeroExitStatus;
    private volatile Process process;
    private final PrintStream tee;
    private final File workingDirectory;

    public Command(String... args) {
        this(Arrays.asList(args));
    }

    public Command(List<String> args) {
        this.args = new ArrayList(args);
        this.env = Collections.emptyMap();
        this.workingDirectory = null;
        this.permitNonZeroExitStatus = false;
        this.tee = null;
        this.nativeOutput = false;
    }

    private Command(Builder builder) {
        this.args = new ArrayList(builder.args);
        this.env = builder.env;
        this.workingDirectory = builder.workingDirectory;
        this.permitNonZeroExitStatus = builder.permitNonZeroExitStatus;
        this.tee = builder.tee;
        if (builder.maxLength != -1) {
            String string = toString();
            if (string.length() > builder.maxLength) {
                throw new IllegalStateException("Maximum command length " + builder.maxLength + " exceeded by: " + string);
            }
        }
        this.nativeOutput = builder.nativeOutput;
    }

    /* synthetic */ Command(Builder builder, Command command) {
        this(builder);
    }

    public void start() throws IOException {
        if (isStarted()) {
            throw new IllegalStateException("Already started!");
        }
        Log.d("Command.start", "executing " + this);
        ProcessBuilder processBuilder = new ProcessBuilder(new String[0]).command(this.args).redirectErrorStream(true);
        if (this.workingDirectory != null) {
            processBuilder.directory(this.workingDirectory);
        }
        processBuilder.environment().putAll(this.env);
        this.process = processBuilder.start();
    }

    public boolean isStarted() {
        return this.process != null;
    }

    public InputStream getInputStream() {
        if (!isStarted()) {
            throw new IllegalStateException("Not started!");
        }
        return this.process.getInputStream();
    }

    public List<String> gatherOutput() throws IOException, InterruptedException {
        if (!isStarted()) {
            throw new IllegalStateException("Not started!");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(), CharEncoding.UTF_8));
        List<String> outputLines = new ArrayList<>();
        outputLines.add("-1");
        while (true) {
            String outputLine = in.readLine();
            if (outputLine == null) {
                break;
            }
            if (this.tee != null) {
                this.tee.println(outputLine);
            }
            if (this.nativeOutput) {
                Log.d("Command.gatherOutput", outputLine);
            }
            outputLines.add(outputLine);
        }
        int exitCode = this.process.waitFor();
        if (exitCode != 0 && !this.permitNonZeroExitStatus) {
            outputLines.set(0, String.valueOf(exitCode));
        } else {
            outputLines.set(0, "0");
        }
        return outputLines;
    }

    public List<String> execute() {
        try {
            start();
            return gatherOutput();
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute process: " + this.args, e);
        } catch (InterruptedException e2) {
            throw new RuntimeException("Interrupted while executing process: " + this.args, e2);
        }
    }

    public List<String> executeWithTimeout(int timeoutSeconds) throws TimeoutException {
        try {
            if (timeoutSeconds == 0) {
                return execute();
            }
            try {
                return executeLater().get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while executing process: " + this.args, e);
            } catch (ExecutionException e2) {
                throw new RuntimeException(e2);
            }
        } finally {
            destroy();
        }
    }

    public Future<List<String>> executeLater() {
        ExecutorService executor = Threads.fixedThreadsExecutor("command", 1);
        Future<List<String>> result = executor.submit(new Callable<List<String>>() { // from class: com.xcharge.common.utils.command.Command.1
            @Override // java.util.concurrent.Callable
            public List<String> call() throws Exception {
                Command.this.start();
                return Command.this.gatherOutput();
            }
        });
        executor.shutdown();
        return result;
    }

    public void destroy() {
        if (this.process != null) {
            this.process.destroy();
            try {
                this.process.waitFor();
                int exitValue = this.process.exitValue();
                Log.d("Command.destroy", "received exit value " + exitValue + " from destroyed command " + this);
            } catch (IllegalThreadStateException e) {
                Log.w("Command.destroy", "couldn't destroy " + this);
            } catch (InterruptedException e2) {
                Log.w("Command.destroy", "couldn't destroy " + this);
            }
        }
    }

    public String toString() {
        String envString = !this.env.isEmpty() ? String.valueOf(Strings.join(this.env.entrySet(), StringUtils.SPACE)) + StringUtils.SPACE : "";
        return String.valueOf(envString) + Strings.join(this.args, StringUtils.SPACE);
    }

    /* loaded from: classes.dex */
    public static class Builder {
        private boolean nativeOutput;
        private File workingDirectory;
        private final List<String> args = new ArrayList();
        private final Map<String, String> env = new LinkedHashMap();
        private boolean permitNonZeroExitStatus = false;
        private PrintStream tee = null;
        private int maxLength = -1;

        public Builder args(Object... objects) {
            for (Object object : objects) {
                args(object.toString());
            }
            return this;
        }

        public Builder setNativeOutput(boolean nativeOutput) {
            this.nativeOutput = nativeOutput;
            return this;
        }

        public Builder args(String... args) {
            return args(Arrays.asList(args));
        }

        public Builder args(Collection<String> args) {
            this.args.addAll(args);
            return this;
        }

        public Builder env(String key, String value) {
            this.env.put(key, value);
            return this;
        }

        public Builder workingDirectory(File workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder tee(PrintStream printStream) {
            this.tee = printStream;
            return this;
        }

        public Builder maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Command build() {
            return new Command(this, null);
        }

        public List<String> execute() {
            return build().execute();
        }
    }
}
