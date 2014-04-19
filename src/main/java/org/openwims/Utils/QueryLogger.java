package org.openwims.Utils;

import org.neo4j.helpers.collection.Visitor;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.logging.LogMarker;

/**
 * Created by jesse on 4/19/14.
 */
public class QueryLogger extends StringLogger {
    @Override
    public void logLongMessage(String s, Visitor<LineLogger, RuntimeException> lineLoggerRuntimeExceptionVisitor, boolean b) {

    }

    @Override
    public void logMessage(String s, boolean b) {

    }

    @Override
    public void logMessage(String s, LogMarker logMarker) {

    }

    @Override
    public void logMessage(String s, Throwable throwable, boolean b) {

    }

    @Override
    public void addRotationListener(Runnable runnable) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }

    @Override
    protected void logLine(String s) {

    }
}
