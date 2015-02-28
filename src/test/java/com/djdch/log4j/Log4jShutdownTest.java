package com.djdch.log4j;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Log4jShutdownTest {
    private static final Logger logger = LogManager.getLogger();

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    public File file;

    @Before
    public void setUpAppender() throws IOException {
        file = folder.newFile();

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        final LoggerConfig root = ((AbstractConfiguration) config).getRootLogger();

        PatternLayout layout = PatternLayout.createLayout("[%p] %m%n", config, null, null, true, false, null, null);
        FileAppender appender = FileAppender.createAppender(file.getAbsolutePath(), "true", "false", "TestLogFile", "true", "false", "false", "8192", layout, null, "false", null, config);

        appender.start();
        config.addAppender(appender);

        root.addAppender(appender, null, null);
        ctx.updateLoggers();
    }

    @Test
    public void testLogger() throws IOException {
        // Used to randomize message
        UUID uuid = UUID.randomUUID();

        // This message should always be logged
        logger.debug("Before shutdown " + uuid);

        // Shutdown Log4j
        StaticShutdownCallbackRegistry.invoke();

        // This message should never be logged
        logger.debug("After shutdown " + uuid);

        // Get logfile content
        String content = FileUtils.readFileToString(file);

        // Assert content
        assertEquals("Logfile should contain only the first message", "[DEBUG] Before shutdown " + uuid + LINE_SEPARATOR, content);
    }
}
