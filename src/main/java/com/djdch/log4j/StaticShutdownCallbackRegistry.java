package com.djdch.log4j;

import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * ShutdownRegistrationStrategy designed to be manually invoked. If not invoked, log4j will not shutdown properly.
 *
 * @see org.apache.logging.log4j.core.util.DefaultShutdownCallbackRegistry
 */
public class StaticShutdownCallbackRegistry implements ShutdownCallbackRegistry, LifeCycle, Runnable, Serializable {

    private static final long serialVersionUID = 1L;
    protected static final Logger LOGGER = StatusLogger.getLogger();

    private final AtomicReference<State> state = new AtomicReference<State>(State.INITIALIZED);
    private final Collection<Cancellable> hooks = new CopyOnWriteArrayList<Cancellable>();

    private static final Collection<StaticShutdownCallbackRegistry> instances = new CopyOnWriteArrayList<StaticShutdownCallbackRegistry>();

    /**
     * Constructs a DelayedShutdownCallbackRegistry.
     */
    public StaticShutdownCallbackRegistry() {
        instances.add(this);
    }

    /**
     * Invoke all ShutdownCallbackRegistry instances.
     */
    public static void invoke() {
        for (final Runnable instance : instances) {
            try {
                instance.run();
            } catch (final Throwable t) {
                LOGGER.error(SHUTDOWN_HOOK_MARKER, "Caught exception executing shutdown hook {}", instance, t);
            }
        }
    }

    /**
     * Executes the registered shutdown callbacks.
     */
    @Override
    public void run() {
        if (state.compareAndSet(State.STARTED, State.STOPPING)) {
            for (final Runnable hook : hooks) {
                try {
                    hook.run();
                } catch (final Throwable t) {
                    LOGGER.error(SHUTDOWN_HOOK_MARKER, "Caught exception executing shutdown hook {}", hook, t);
                }
            }
            state.set(State.STOPPED);
        }
    }

    @Override
    public Cancellable addShutdownCallback(final Runnable callback) {
        if (isStarted()) {
            final Cancellable receipt = new Cancellable() {
                // use a reference to prevent memory leaks
                private final Reference<Runnable> hook = new SoftReference<Runnable>(callback);

                @Override
                public void cancel() {
                    hook.clear();
                    hooks.remove(this);
                }

                @Override
                public void run() {
                    final Runnable hook = this.hook.get();
                    if (hook != null) {
                        hook.run();
                        this.hook.clear();
                    }
                }

                @Override
                public String toString() {
                    return String.valueOf(hook.get());
                }
            };
            hooks.add(receipt);
            return receipt;
        }
        throw new IllegalStateException("Cannot add new shutdown hook as this is not started. Current state: " +
                state.get().name());
    }

    /**
     * Registers the shutdown thread only if this is initialized.
     */
    @Override
    public void start() {
        state.set(State.STARTED);
    }

    /**
     * Cancels the shutdown thread only if this is started.
     */
    @Override
    public void stop() {
        state.set(State.STOPPED);
    }

    public State getState() {
        return state.get();
    }

    /**
     * Indicates if this can accept shutdown hooks.
     *
     * @return true if this can accept shutdown hooks
     */
    @Override
    public boolean isStarted() {
        return state.get() == State.STARTED;
    }

    @Override
    public boolean isStopped() {
        return state.get() == State.STOPPED;
    }

}
