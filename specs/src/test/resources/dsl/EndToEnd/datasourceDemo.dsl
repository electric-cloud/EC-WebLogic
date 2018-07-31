def projName = args.projectName
assert projName
def appName = args.appName
def artifactName = args.artifactName
assert artifactName
def resName = args.resName
assert resName
def configname = args.config
assert configname
def wlst = args.wlst
def envName = 'WL Datasource Environment'
def updateAction = args.updateAction ?: 'selective_update'
assert wlst
def componentName = 'DatasourceDemo'

def driverName = args.driverName
def dbName = args.dbName
def dbUrl = args.dbUrl
def dsName = args.dsName
def jndiName = args.jndi

project projName, {

    // Environment
    environment envName, {
        environmentTier 'WebLogic', {
            resourceName = [
                resName,
            ]
        }
    }

    credential 'dsCred', {
        userName = args.userName
        password = args.password
    }


    application appName, {
        applicationTier 'WebLogic', {
            component componentName, {
                pluginKey = 'EC-Artifact'
                property 'ec_content_details', {

                    // Custom properties
                    property 'artifactName', value: artifactName, {
                        expandable = '1'
                    }
                    artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
                    filterList = ''
                    overwrite = 'update'
                    pluginProcedure = 'Retrieve'
                    property 'pluginProjectName', value: 'EC-Artifact', {
                        expandable = '1'
                    }
                    retrieveToDirectory = 'ds-demo'
                    property 'versionRange', value: '', {
                        expandable = '1'
                    }
                }

                process 'Deploy', {
                    applicationName = null
                    serviceName = null
                    processType = 'DEPLOY'

                    processStep 'Create Datasource', {
                        actualParameter = [
                            configname: configname,
                            ecp_weblogic_dataSourceName       : dsName,
                            ecp_weblogic_dataSourceDriverClass: driverName,
                            ecp_weblogic_databaseUrl          : dbUrl,
                            ecp_weblogic_jndiName             : jndiName,
                            ecp_weblogic_targets              : 'AdminServer',
                            ecp_weblogic_additionalOptions    : '',
                            ecp_weblogic_updateAction         : updateAction,
                            ecp_weblogic_databaseName         : dbName,
                            ecp_weblogic_dataSourceCredentials: '/projects/' + projName + '/credentials/dsCred'
                        ]
                        dependencyJoinType = 'and'
                        errorHandling = 'abortJob'
                        processStepType = 'plugin'
                        subprocedure = 'CreateOrUpdateDatasource'
                        subproject = '/plugins/EC-WebLogic/project'
                        subcomponent = null
                        subcomponentApplicationName = null
                        subcomponentProcess = null
                        subservice = null
                        applicationTierName = null
                    }


                    attachCredential credentialName: 'dsCred',
                        projectName: projName,
                        applicationName: appName,
                        processName: 'Deploy',
                        processStepName: 'Create Datasource',
                        componentName: componentName


                    processStep 'DeployApp', {
                        actualParameter = [
                            'additional_options': '',
                            'appname': args.appName,
                            'apppath': 'ds-demo/' + args.appPath,
                            'archive_version': '',
                            'configname': configname,
                            'deployment_plan': '',
                            'is_library': '0',
                            'overwrite_deployment_plan': '0',
                            'plan_path': '',
                            'plan_version': '',
                            'remote': '0',
                            'retire_gracefully': '0',
                            'retire_timeout': '',
                            'stage_mode': '',
                            'targets': 'AdminServer',
                            'upload': '0',
                            'version_identifier': '',
                            'wlstabspath': wlst,
                        ]
                        dependencyJoinType = 'and'
                        errorHandling = 'abortJob'
                        processStepType = 'plugin'
                        subprocedure = 'DeployApp'
                        subproject = '/plugins/EC-WebLogic/project'

                        subcomponent = null
                        subcomponentApplicationName = null
                        subcomponentProcess = null
                        subservice = null
                        applicationTierName = null

                    }

                    processDependency 'Create Datasource', targetProcessStepName: 'DeployApp', {
                        branchCondition = null
                        branchConditionName = null
                        branchConditionType = null
                        branchType = 'ALWAYS'
                    }

                }


            }
        }

        // Tier map
        tierMap 'WebLogic', {
            environmentName = envName
            environmentProjectName = projName

            tierMapping 'WebLogic', {
                applicationTierName = 'WebLogic'
                environmentTierName = 'WebLogic'
            }
        }

        process 'Deploy', {
            processType = 'DEPLOY'


            formalParameter 'ec_enforceDependencies', defaultValue: '0', {
                expansionDeferred = '1'
                label = null
                orderIndex = null
                required = '0'
                type = 'checkbox'
            }

            formalParameter 'ec_smartDeployOption', defaultValue: '0', {
                expansionDeferred = '1'
                label = null
                orderIndex = null
                required = '0'
                type = 'checkbox'
            }

            formalParameter 'ec_stageArtifacts', defaultValue: '1', {
                expansionDeferred = '1'
                label = null
                orderIndex = null
                required = '0'
                type = 'checkbox'
            }

            processStep 'Deploy', {
                alwaysRun = '0'
                applicationTierName = 'WebLogic'
                dependencyJoinType = 'and'
                errorHandling = 'abortJob'
                processStepType = 'process'
                subcomponent = componentName
                subcomponentApplicationName = appName
                subcomponentProcess = 'Deploy'

            }

        }
    }

}
