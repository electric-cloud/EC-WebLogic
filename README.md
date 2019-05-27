EC-WebLogic
============

The CloudBees Flow WebLogic integration

## Compile ##

Run gradlew to compile the plugin\
`./gradlew`

## Compile And Upload ##
1. Install git\
   `sudo apt-get install git`
2. Get this plugin\
   `git clone https://github.com/electric-cloud/EC-WebLogic.git`
3. Run gradlew to compile the plugin\
   `./gradlew jar (in EC-Weblogic directory)`
4. Upload the plugin to EC server
5. Create a configuration for the EC-Weblogic plugin.

#### Prerequisite installation: ####
1. Installation of WebLogic Server:
  * [Download](http://www.oracle.com/technetwork/middleware/weblogic/downloads/wls-main-097127.html)
  * [Windows Installation](https://docs.oracle.com/cd/E24329_01/doc.1211/e24492/install_screens.htm#WLSIG213)
  * [Linux Installation](https://oracle-base.com/articles/12c/weblogic-installation-on-oracle-linux-5-and-6-1212)
2. [Create and Configure domain](https://docs.oracle.com/cd/E13222_01/wls/docs90/config_scripting/domains.html#1001190)

#### Required files: ####
1. Create a file called ecplugin.properties inside EC-Weblogic directory with the below mentioned contents.

#### Contents of ecplugin.properties: ####
```
    COMMANDER_SERVER=<COMMANDER_SERVER>(Commander server IP)
    COMMANDER_USER=<COMMANDER_USER>
    COMMANDER_PASSWORD=<COMMANDER_PASSWORD>
    WEBLOGIC_AGENT_IP=<WEBLOGIC_AGENT_IP>(IP of the Machine where Weblogic server is installed)
    
    WEBLOGIC_USERNAME=<NEW_WEBLOGIC_USERNAME>(Username of the new Weblogic user)
    WEBLOGIC_PASSWORD=<NEW_WEBLOGIC_PASSWORD>(Password of the new Weblogic user)
```

#### Contents of Configurations.json: ####
1. Configurations.json is a configurable file.
2. Refer to the sample Configurations.json file, `/src/test/java/ecplugins/weblogic/Configurations.json`. It has to be updated with the user specific inputs.
3. Inputs should be valid and specific to the environment on which the tests are run.
4. Unit tests are ordered and will execute in a sequential manner as programmed. Same **resource name** should be used in the respective delete and create procedures to make the Unit Tests residueless.
5. Example: 
```
"CreateCluster": [{
    "wlst_abs_path": "C:/Oracle/Middleware/Oracle_Home/Oracle_Common/common/bin/wlst.cmd",
	"configname": "newConfig",
	"cluster_name" : "newCluster",
	"multicast_address": "239.192.0.0",
	"multicast_port": "7040"
}],
          
"DeleteCluster": [{
    "wlst_abs_path": "C:/Oracle/Middleware/Oracle_Home/Oracle_Common/common/bin/wlst.cmd",
    "configname": "newConfig",
    "cluster_name" : "newCluster"
}]
```
#### Run the tests: #####
    ./gradlew test

