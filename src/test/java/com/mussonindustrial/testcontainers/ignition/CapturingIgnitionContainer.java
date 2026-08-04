package com.mussonindustrial.testcontainers.ignition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.testcontainers.utility.DockerImageName;

public final class CapturingIgnitionContainer extends IgnitionContainer {

    private List<WarningCall> warnings;
    private Logger capturingLogger;

    public CapturingIgnitionContainer(DockerImageName dockerImageName) {
        super(dockerImageName);
    }

    public void applyConfiguration() {
        configure();
    }

    public List<WarningCall> warnings() {
        return List.copyOf(mutableWarnings());
    }

    @Override
    protected Logger logger() {
        if (capturingLogger == null) {
            Logger delegate = super.logger();

            capturingLogger = (Logger) Proxy.newProxyInstance(
                    Logger.class.getClassLoader(), new Class<?>[] {Logger.class}, (proxy, method, arguments) -> {
                        if (method.getName().equals("warn") && arguments != null) {
                            mutableWarnings().add(WarningCall.from(arguments));
                        }

                        try {
                            return method.invoke(delegate, arguments);
                        } catch (InvocationTargetException exception) {
                            throw exception.getCause();
                        }
                    });
        }

        return capturingLogger;
    }

    private List<WarningCall> mutableWarnings() {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }

        return warnings;
    }

    public record WarningCall(String template, List<Object> arguments) {

        public static WarningCall from(Object[] invocationArguments) {
            int templateIndex = invocationArguments[0] instanceof Marker ? 1 : 0;

            String template = (String) invocationArguments[templateIndex];

            Object[] formattingArguments = extractFormattingArguments(invocationArguments, templateIndex + 1);

            List<Object> arguments = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(formattingArguments)));

            return new WarningCall(template, arguments);
        }

        public static Object[] extractFormattingArguments(Object[] invocationArguments, int startIndex) {
            if (startIndex >= invocationArguments.length) {
                return new Object[0];
            }

            if (invocationArguments.length == startIndex + 1
                    && invocationArguments[startIndex] instanceof Object[] arguments) {
                return arguments.clone();
            }

            return Arrays.copyOfRange(invocationArguments, startIndex, invocationArguments.length);
        }
    }
}
