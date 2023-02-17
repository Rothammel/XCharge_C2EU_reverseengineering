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

public final class Command {
    private final List<String> args;
    private final Map<String, String> env;
    private final boolean nativeOutput;
    private final boolean permitNonZeroExitStatus;
    private volatile Process process;
    private final PrintStream tee;
    private final File workingDirectory;

    public Command(String... args2) {
        this((List<String>) Arrays.asList(args2));
    }

    public Command(List<String> args2) {
        this.args = new ArrayList(args2);
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
        if (isStarted()) {
            return this.process.getInputStream();
        }
        throw new IllegalStateException("Not started!");
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
        if (exitCode == 0 || this.permitNonZeroExitStatus) {
            outputLines.set(0, "0");
        } else {
            outputLines.set(0, String.valueOf(exitCode));
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
        if (timeoutSeconds == 0) {
            return execute();
        }
        try {
            List<String> list = executeLater().get((long) timeoutSeconds, TimeUnit.SECONDS);
            destroy();
            return list;
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while executing process: " + this.args, e);
        } catch (ExecutionException e2) {
            throw new RuntimeException(e2);
        } catch (Throwable th) {
            destroy();
            throw th;
        }
    }

    public Future<List<String>> executeLater() {
        ExecutorService executor = Threads.fixedThreadsExecutor("command", 1);
        Future<List<String>> result = executor.submit(new Callable<List<String>>() {
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
                Log.d("Command.destroy", "received exit value " + this.process.exitValue() + " from destroyed command " + this);
            } catch (IllegalThreadStateException e) {
                Log.w("Command.destroy", "couldn't destroy " + this);
            } catch (InterruptedException e2) {
                Log.w("Command.destroy", "couldn't destroy " + this);
            }
        }
    }

    public String toString() {
        return String.valueOf(!this.env.isEmpty() ? String.valueOf(Strings.join((Iterable<?>) this.env.entrySet(), StringUtils.SPACE)) + StringUtils.SPACE : "") + Strings.join((Iterable<?>) this.args, StringUtils.SPACE);
    }

    public static class Builder {
        /* access modifiers changed from: private */
        public final List<String> args = new ArrayList();
        /* access modifiers changed from: private */
        public final Map<String, String> env = new LinkedHashMap();
        /* access modifiers changed from: private */
        public int maxLength = -1;
        /* access modifiers changed from: private */
        public boolean nativeOutput;
        /* access modifiers changed from: private */
        public boolean permitNonZeroExitStatus = false;
        /* access modifiers changed from: private */
        public PrintStream tee = null;
        /* access modifiers changed from: private */
        public File workingDirectory;

        public Builder args(Object... objects) {
            int length = objects.length;
            for (int i = 0; i < length; i++) {
                args(objects[i].toString());
            }
            return this;
        }

        public Builder setNativeOutput(boolean nativeOutput2) {
            this.nativeOutput = nativeOutput2;
            return this;
        }

        public Builder args(String... args2) {
            return args((Collection<String>) Arrays.asList(args2));
        }

        public Builder args(Collection<String> args2) {
            this.args.addAll(args2);
            return this;
        }

        public Builder env(String key, String value) {
            this.env.put(key, value);
            return this;
        }

        public Builder workingDirectory(File workingDirectory2) {
            this.workingDirectory = workingDirectory2;
            return this;
        }

        public Builder tee(PrintStream printStream) {
            this.tee = printStream;
            return this;
        }

        public Builder maxLength(int maxLength2) {
            this.maxLength = maxLength2;
            return this;
        }

        public Command build() {
            return new Command(this, (Command) null);
        }

        public List<String> execute() {
            return build().execute();
        }
    }
}
