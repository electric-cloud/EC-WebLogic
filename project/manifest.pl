@files = (
 ['//property[propertyName="ui_forms"]/propertySheet/property[propertyName="WebLogicCreateConfigForm"]/value'  , 'WebLogicCreateConfigForm.xml'],
 ['//property[propertyName="ui_forms"]/propertySheet/property[propertyName="WebLogicEditConfigForm"]/value'  , 'WebLogicEditConfigForm.xml'],
 
 ['//property[propertyName="stopserver_matchers"]/value', 'matchers/stopServerMatchers.pl'],
 ['//property[propertyName="startserver_matchers"]/value', 'matchers/startServerMatchers.pl'],
 ['//property[propertyName="curl_matchers"]/value', 'matchers/curlMatchers.pl'],
 ['//property[propertyName="deployer_matchers"]/value', 'matchers/deployerMatchers.pl'],
 ['//property[propertyName="weblogicserver_matchers"]/value', 'matchers/weblogicserverMatchers.pl'],
    
 ['//procedure[procedureName="StartAdminServer"]/step[stepName="createCommand"]/command' , 'server/startAdministrationServer.pl'],
 ['//procedure[procedureName="StartManagedServer"]/step[stepName="createCommand"]/command' , 'server/startManagedServer.pl'],
 ['//procedure[procedureName="StopManagedServer"]/step[stepName="createCommand"]/command' , 'server/stopManagedServer.pl'],
 ['//procedure[procedureName="StopAdminServer"]/step[stepName="createCommand"]/command' , 'server/stopAdministrationServer.pl'],
 ['//procedure[procedureName="CheckServerStatus"]/step[stepName="CreateCommand"]/command' , 'server/checkServerStatus.pl'],
 ['//procedure[procedureName="RunDeployer"]/step[stepName="CreateCommand"]/command' , 'server/runDeployer.pl'],
 ['//procedure[procedureName="RunWLST"]/step[stepName="CreateCommand"]/command' , 'server/runWlst.pl'],
 ['//procedure[procedureName="StartApp"]/step[stepName="CreateCommand"]/command' , 'server/startApp.pl'],
 ['//procedure[procedureName="StopApp"]/step[stepName="CreateCommand"]/command' , 'server/stopApp.pl'],
 ['//procedure[procedureName="DeployApp"]/step[stepName="CreateCommand"]/command' , 'server/deployApp.pl'],
 ['//procedure[procedureName="UndeployApp"]/step[stepName="CreateCommand"]/command' , 'server/undeployApp.pl'], 

 ['//procedure[procedureName="CreateConfiguration"]/step[stepName="CreateConfiguration"]/command' , 'conf/createcfg.pl'],
 ['//procedure[procedureName="CreateConfiguration"]/step[stepName="CreateAndAttachCredential"]/command' , 'conf/createAndAttachCredential.pl'],
 ['//procedure[procedureName="DeleteConfiguration"]/step[stepName="DeleteConfiguration"]/command' , 'conf/deletecfg.pl'],

 ['//property[propertyName="ec_setup"]/value', 'ec_setup.pl'],
);
