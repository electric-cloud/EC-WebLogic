<img src="../../plugins/EC-WebLogic/images/Config/EC-WLSConfig.png" />

**Note:** In the URL, the protocol is "t3." The
RMI communications in WebLogic Server use
the "t3" protocol to transport data between WebLogic
Server and other Java programs, including clients and
other WebLogic Server instances. A server instance
keeps track of each Java Virtual Machine (JVM) with
which it connects, and creates a single "t3" connection
to carry all traffic for a JVM. This proctocol is used
in most of the procedures of the WebLogic Plugin.