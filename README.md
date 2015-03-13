Log4j-StaticShutdown
====================

[![Travis](https://img.shields.io/travis/DjDCH/Log4j-StaticShutdown.svg)](https://travis-ci.org/DjDCH/Log4j-StaticShutdown)
[![Maven Central](https://img.shields.io/maven-central/v/com.djdch.log4j/log4j-staticshutdown.svg)](http://mvnrepository.com/artifact/com.djdch.log4j/log4j-staticshutdown)
[![MIT License](https://img.shields.io/badge/license-MIT-8469ad.svg)](https://tldrlegal.com/license/mit-license)

Provide a ShutdownRegistrationStrategy for Log4j 2 designed to be manually invoked.

Requirements
------------

* Java and Maven installed

Build your project
------------------

Add the dependency in your `pom.xml` file:

    <dependency>
        <groupId>com.djdch.log4j</groupId>
        <artifactId>log4j-staticshutdown</artifactId>
        <version>1.0.1-SNAPSHOT</version>
    </dependency>

Add this static initialization block in your *Main* class:

    static {
        System.setProperty("log4j.shutdownCallbackRegistry", "com.djdch.log4j.StaticShutdownCallbackRegistry");
    }

> WARNING! Even if this works, there is no warranty that it always will, since there is no way to ensure that
> your Main class will be loaded before Log4j (and it won't work if that doesn't happen). To be safe, you should
> instead set the system property from the command line when launching your jar.

Then, you can invoke manually the `StaticShutdownCallbackRegistry` in your shutdown procedure:

    StaticShutdownCallbackRegistry.invoke();
