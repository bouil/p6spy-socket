#Introduction

This project is to be used with the [P6Spy module](https://github.com/p6spy/p6spy).

It is a socket listener that will send JDBC statements to any connected client.
It listens to the port is 4564, on interface 127.0.0.1. This is not configurable yet.  

The dependency for p6spy is already included in this project.

# How-To

Change your jdbc url from `jdbc:whatever` to `jdbc:p6spy:whatever`.
Set the driver to `com.p6spy.engine.spy.P6SpyDriver`.

In your classpath (usually in `src/main/resources`), create a file name spy.properties.
To configure the socket appender, place the following configuration. 

``
appender=net.bouillon.p6spy.socket.SocketLogger
``

You may also have to set the `driverlist` property to specify your real JDBC driver.
Please consult [P6Spy Documentation](https://p6spy.readthedocs.io/en/latest/configandusage.html).

Once your project is up and running, you may want to connect to the socket using my *p6spy-ui* project.

