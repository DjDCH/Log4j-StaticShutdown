package com.djdch.log4j;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class StaticShutdownCallbackRegistryTest {

    @Test
    public void testStaticShutdownCallbackRegistry() {
        // Instantiate a new registry
        StaticShutdownCallbackRegistry registry = new StaticShutdownCallbackRegistry();

        // Mark the registry as started
        registry.start();

        // Define a boolean to false
        final AtomicBoolean bool = new AtomicBoolean();

        // Add a callback that will set the boolean to true
        registry.addShutdownCallback(new Runnable() {
            @Override
            public void run() {
                bool.set(true);
            }
        });

        // This should run the callback and set the boolean to true
        registry.run();

        // Assert boolean
        assertTrue("ShutdownCallback should have ran", bool.get());
    }
}
