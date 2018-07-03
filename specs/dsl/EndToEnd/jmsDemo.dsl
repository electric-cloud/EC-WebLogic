def projName = args.projectName
assert projName
def appName = 'JMS Demo App'
def artifactName = args.artifactName
assert artifactName
def resName = args.resName
assert resName
def configname = args.config
assert configname
def jmsModuleName = args.jmsModuleName
assert jmsModuleName
def cfJNDI = args.jndi.connectionFactory
def queueJNDI = args.jndi.queue
def topicJNDI = args.jndi.topic
def jmsServerName = args.jmsServerName ?: "JMSDemoServer"
def wlst = args.wlst
def envName = 'WL JMS Environment'
assert wlst

project projName, {

      // Environment
    environment envName, {
      environmentTier 'WebLogic', {
        resourceName = [
          resName,
        ]
      }
    }

    application appName, {
        applicationTier 'WebLogic', {
            component 'JMSDemo', {
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
                retrieveToDirectory = '/tmp'
                    property 'versionRange', value: '', {
                      expandable = '1'
                    }
                }

              process 'Deploy', {
                    applicationName = null
                    serviceName = null
                    processType = 'DEPLOY'

                    processStep 'Create JMS Module', {
                      actualParameter = [
                        'configname': configname,
                        'ecp_weblogic_jms_module_name': jmsModuleName,
                        'ecp_weblogic_target_list': 'AdminServer',
                        'ecp_weblogic_update_action': 'do_nothing',
                      ]
                      processStepType = 'plugin'
                      subprocedure = 'CreateOrUpdateJMSModule'
                      subproject = '/plugins/EC-WebLogic/project'
                      subcomponent = null
                      subcomponentApplicationName = null
                      subcomponentProcess = null
                      subservice = null
                      applicationTierName = null
                    }

                    processStep 'Create JMS Server', {
                      actualParameter = [
                        'configname': configname,
                        'ecp_weblogic_jms_server_name': jmsServerName,
                        'ecp_weblogic_target': 'AdminServer',
                        'ecp_weblogic_update_action': 'do_nothing',
                      ]
                      dependencyJoinType = 'and'
                      processStepType = 'plugin'
                      subprocedure = 'CreateOrUpdateJMSServer'
                      subproject = '/plugins/EC-WebLogic/project'
                      subcomponent = null
                      subcomponentApplicationName = null
                      subcomponentProcess = null
                      subservice = null
                      applicationTierName = null

                    }

                    processStep 'Create ConnectionFactory', {
                      actualParameter = [
                        'additional_options': '',
                        'cf_client_id_policy': 'restricted',
                        'cf_max_messages_per_session': '10',
                        'cf_name': 'JMSDemoConnectionFactory',
                        'cf_sharing_policy': 'exclusive',
                        'cf_xa_enabled': '1',
                        'configname': configname,
                        'jms_module_name': jmsModuleName,
                        'jms_server_list': '',
                        'jndi_name': cfJNDI,
                        'subdeployment_name': '',
                        'update_action': 'selective_update',
                        'wls_instance_list': '',
                      ]
                      dependencyJoinType = 'and'
                      processStepType = 'plugin'
                      subprocedure = 'CreateOrUpdateConnectionFactory'
                      subproject = '/plugins/EC-WebLogic/project'
                      subcomponent = null
                      subcomponentApplicationName = null
                      subcomponentProcess = null
                      subservice = null
                      applicationTierName = null
                    }

                    processStep 'Create JMS Queue', {
                      actualParameter = [
                        'configname': configname,
                        'ecp_weblogic_additional_options': '',
                        'ecp_weblogic_jms_module_name': jmsModuleName,
                        'ecp_weblogic_jms_queue_name': 'JMSDemoQueue',
                        'ecp_weblogic_jndi_name': queueJNDI,
                        'ecp_weblogic_subdeployment_name': 'JMSDemoQueueSub',
                        'ecp_weblogic_target_jms_server': jmsServerName,
                        'ecp_weblogic_update_action': 'selective_update',
                      ]
                      dependencyJoinType = 'and'
                      errorHandling = 'abortJob'
                      processStepType = 'plugin'
                      subprocedure = 'CreateOrUpdateJMSQueue'
                      subproject = '/plugins/EC-WebLogic/project'
                      subcomponent = null
                      subcomponentApplicationName = null
                      subcomponentProcess = null
                      subservice = null
                      applicationTierName = null
                    }

                    processStep 'Create JMS Topic', {
                      actualParameter = [
                        'configname': configname,
                        'ecp_weblogic_additional_options': '',
                        'ecp_weblogic_jms_module_name': jmsModuleName,
                        'ecp_weblogic_jms_topic_name': 'JMSTopicDemo',
                        'ecp_weblogic_jndi_name': topicJNDI,
                        'ecp_weblogic_subdeployment_name': 'JMSDemoTopicSub',
                        'ecp_weblogic_target_jms_server': jmsServerName,
                        'ecp_weblogic_update_action': 'selective_update',
                      ]
                      dependencyJoinType = 'and'
                      errorHandling = 'abortJob'
                      processStepType = 'plugin'
                      subprocedure = 'CreateOrUpdateJMSTopic'
                      subproject = '/plugins/EC-WebLogic/project'
                      subcomponent = null
                      subcomponentApplicationName = null
                      subcomponentProcess = null
                      subservice = null
                      applicationTierName = null
                    }

                    processStep 'Deploy App', {
                      actualParameter = [
                        'additional_options': '',
                        'appname': args.appName,
                        'apppath': '/tmp/' + args.appPath,
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

                    processDependency 'Create ConnectionFactory', targetProcessStepName: 'Create JMS Queue', {
                      branchCondition = null
                      branchConditionName = null
                      branchConditionType = null
                      branchType = 'ALWAYS'
                    }

                    processDependency 'Create JMS Queue', targetProcessStepName: 'Create JMS Topic', {
                      branchCondition = null
                      branchConditionName = null
                      branchConditionType = null
                      branchType = 'ALWAYS'
                    }

                    processDependency 'Create JMS Topic', targetProcessStepName: 'Deploy App', {
                      branchCondition = null
                      branchConditionName = null
                      branchConditionType = null
                      branchType = 'ALWAYS'
                    }

                    processDependency 'Create JMS Module', targetProcessStepName: 'Create JMS Server', {
                      branchCondition = null
                      branchConditionName = null
                      branchConditionType = null
                      branchType = 'ALWAYS'
                    }

                    processDependency 'Create JMS Server', targetProcessStepName: 'Create ConnectionFactory', {
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
              subcomponent = 'JMSDemo'
              subcomponentApplicationName = appName
              subcomponentProcess = 'Deploy'

            }

        }
    }



}
