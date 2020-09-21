This procedure stops the deployment unit and removes staged files
from target servers.

Contrary to the weblogic documentation in section 'Undeploying a Retiring Application' at [docs.oracle.com](https://docs.oracle.com/cd/E13222_01/wls/docs103/deployment/redeploy.html), we found that in Weblogic 11, only retired version of application is undeployed if no application version is specified.
