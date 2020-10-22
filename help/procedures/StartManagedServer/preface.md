This procedure starts a WebLogic Managed Server. A WebLogic Server
administration domain is a logically related group of
WebLogic Server resources. Usually, you configure a
domain to include additional WebLogic Server instances
called Managed Servers. You deploy Web applications,
EJBs, and other resources onto the Managed Servers and
use the Administration Server for configuration and
management purposes only. The Node Manager
must be running before you run this procedure.
In order to start the managed server, a file called
"boot.properties" must be created in the path
"$DOMAIN_DIR$/servers/myserver/security", and this file
must have the credentials needed to start the server.
For example:
username=weblogic
password=w3blogic
