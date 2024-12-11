package com.extract.context;

public class ContextUtil {
    private static final ThreadLocal<RestContext> CONTEXT = new ThreadLocal<>();

    public static RestContext getOrSetCurrent() {
        RestContext restContext = CONTEXT.get();
        if (restContext == null) {
            restContext = new RestContext();
            CONTEXT.set(restContext);
        }
        return restContext;
    }

    public static RestContext initCurrent() {
        RestContext restContext = CONTEXT.get();
        if (restContext != null) {
            CONTEXT.remove();
        }
        restContext = new RestContext();
        CONTEXT.set(restContext);
        return restContext;
    }

    public static void clearCurrent() {
        CONTEXT.remove();
    }
}

