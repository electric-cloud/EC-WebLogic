
def targetUrl = 'http://'+webLogicHost+':7001/sample/'


println targetUrl

project projName, {
  description = 'Third-party auto-created project'
  resourceName = null
  workspaceName = null

  environment 'WebLogic Demo', {
    environmentEnabled = '1'
    projectName = projName
    reservationRequired = '0'
    rollingDeployEnabled = null
    rollingDeployType = null

    environmentTier 'Tier 1', {
      batchSize = null
      batchSizeType = null
      resourceName = [
        resName,
      ]
    }
  }
}


application 'WebLogic DemoApp', {
  description = ''
  projectName = projName

  applicationTier 'Tier 1', {
    applicationName = 'WebLogic DemoApp'
    
    component 'WebLogic Sample', pluginName: null, {
      applicationName = 'WebLogic DemoApp'
      pluginKey = 'EC-Artifact'
      reference = '0'
      sourceComponentName = null
      sourceProjectName = null

      process 'Deploy', {
        applicationName = null
        processType = 'DEPLOY'
        smartUndeployEnabled = null
        timeLimitUnits = null
        workspaceName = null

        processStep 'Deploy', {
          applicationTierName = null
          componentRollback = null
          dependencyJoinType = 'and'
          errorHandling = 'abortJob'
          instruction = null
          notificationTemplate = null
          processStepType = 'plugin'
          rollbackSnapshot = null
          rollbackType = null
          rollbackUndeployProcess = null
          skipRollbackIfUndeployFails = null
          smartRollback = null
          subcomponent = null
          subcomponentApplicationName = null
          subcomponentProcess = null
          subprocedure = 'DeployApp'
          subproject = '/plugins/EC-WebLogic/project'
          subservice = null
          timeLimitUnits = null
          workspaceName = null
          actualParameter 'additional_options', ''
          actualParameter 'appname', 'hello-world'
          actualParameter 'apppath', '/tmp/weblogic12/sample.war '
          actualParameter 'archive_version', ''
          actualParameter 'configname', pluginConfig
          actualParameter 'deployment_plan', ''
          actualParameter 'is_library', '0'
          actualParameter 'overwrite_deployment_plan', '0'
          actualParameter 'plan_path', ''
          actualParameter 'plan_version', ''
          actualParameter 'remote', '0'
          actualParameter 'retire_gracefully', '0'
          actualParameter 'retire_timeout', ''
          actualParameter 'stage_mode', ''
          actualParameter 'targets', 'AdminServer'
          actualParameter 'upload', '0'
          actualParameter 'version_identifier', ''
          actualParameter 'wlstabspath', '/u01/oracle/oracle_common/common/bin/wlst.sh'

          // Custom properties
          afterLastRetry = ''
          retryCount = ''
          retryInterval = ''
          retryType = ''
        }
      }

      process 'Undeploy', {
        applicationName = null
        processType = 'UNDEPLOY'
        smartUndeployEnabled = '1'
        timeLimitUnits = null
        workspaceName = null

        processStep 'Undeploy App', {
          applicationTierName = null
          componentRollback = null
          dependencyJoinType = 'and'
          errorHandling = 'failProcedure'
          instruction = null
          notificationTemplate = null
          processStepType = 'plugin'
          rollbackSnapshot = null
          rollbackType = null
          rollbackUndeployProcess = null
          skipRollbackIfUndeployFails = null
          smartRollback = null
          subcomponent = null
          subcomponentApplicationName = null
          subcomponentProcess = null
          subprocedure = 'UndeployApp'
          subproject = '/plugins/EC-WebLogic/project'
          subservice = null
          timeLimitUnits = null
          workspaceName = null
          actualParameter 'additional_options', ''
          actualParameter 'appname', 'hello-world'
          actualParameter 'configname', pluginConfig
          actualParameter 'give_up', '0'
          actualParameter 'retire_gracefully', '0'
          actualParameter 'version_identifier', ''
          actualParameter 'wlstabspath', '/u01/oracle/oracle_common/common/bin/wlst.sh'
        }
      }

      // Custom properties

      property 'ec_content_details', {

        // Custom properties

        property 'artifactName', value: 'weblogic:sample', {
          expandable = '1'
        }
        artifactVersionLocationProperty = '/myJob/retrievedArtifactVersions/$[assignedResourceName]'
        filterList = ''
        overwrite = 'update'
        pluginProcedure = 'Retrieve'

        property 'pluginProjectName', value: 'EC-Artifact', {
          expandable = '1'
        }
        retrieveToDirectory = '/tmp/weblogic12'

        property 'versionRange', value: '', {
          expandable = '1'
        }
      }
    }
  }

  process 'Deploy', {
    applicationName = 'WebLogic DemoApp'
    processType = 'OTHER'
    smartUndeployEnabled = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'ec_enforceDependencies', defaultValue: '0', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_smartDeployOption', defaultValue: '1', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_stageArtifacts', defaultValue: '0', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_WebLogic Sample-run', defaultValue: '1', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_WebLogic Sample-version', defaultValue: '$[/projects/3rdPartyIntegrations/applications/WebLogic DemoApp/components/WebLogic Sample/ec_content_details/versionRange]', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    processStep 'Deploy', {
      applicationTierName = 'Tier 1'
      componentRollback = null
      dependencyJoinType = 'and'
      errorHandling = 'abortJob'
      instruction = null
      notificationTemplate = null
      processStepType = 'process'
      rollbackSnapshot = null
      rollbackType = null
      rollbackUndeployProcess = null
      skipRollbackIfUndeployFails = null
      smartRollback = null
      subcomponent = 'WebLogic Sample'
      subcomponentApplicationName = 'WebLogic DemoApp'
      subcomponentProcess = 'Deploy'
      subprocedure = null
      subproject = null
      subservice = null
      timeLimitUnits = null
      workspaceName = null

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
      afterLastRetry = ''
      retryCount = ''
      retryInterval = ''
      retryType = ''
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }
  }

  process 'Undeploy', {
    applicationName = 'WebLogic DemoApp'
    processType = 'OTHER'
    smartUndeployEnabled = null
    timeLimitUnits = null
    workspaceName = null

    formalParameter 'ec_enforceDependencies', defaultValue: '0', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_smartDeployOption', defaultValue: '1', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_stageArtifacts', defaultValue: '0', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_WebLogic Sample-run', defaultValue: '1', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'checkbox'
    }

    formalParameter 'ec_WebLogic Sample-version', defaultValue: '$[/projects/3rdPartyIntegrations/applications/WebLogic DemoApp/components/WebLogic Sample/ec_content_details/versionRange]', {
      expansionDeferred = '1'
      label = null
      orderIndex = null
      required = '0'
      type = 'entry'
    }

    processStep 'Undeploy', {
      applicationTierName = 'Tier 1'
      componentRollback = null
      dependencyJoinType = 'and'
      errorHandling = 'failProcedure'
      instruction = null
      notificationTemplate = null
      processStepType = 'process'
      rollbackSnapshot = null
      rollbackType = null
      rollbackUndeployProcess = null
      skipRollbackIfUndeployFails = null
      smartRollback = null
      subcomponent = 'WebLogic Sample'
      subcomponentApplicationName = 'WebLogic DemoApp'
      subcomponentProcess = 'Undeploy'
      subprocedure = null
      subproject = null
      subservice = null
      timeLimitUnits = null
      workspaceName = null

      // Custom properties

      property 'ec_deploy', {

        // Custom properties
        ec_notifierStatus = '0'
      }
    }

    // Custom properties

    property 'ec_deploy', {

      // Custom properties
      ec_notifierStatus = '0'
    }
  }

  tierMap '8384db8a-e18d-11e7-b7a8-0242faa257ad', {
      applicationName = 'WebLogic DemoApp'
      environmentName = 'WebLogic Demo'
      environmentProjectName = projName
      projectName = projName

      tierMapping '83927041-e18d-11e7-bfaf-0242faa257ad', {
        applicationTierName = 'Tier 1'
        environmentTierName = 'Tier 1'
        tierMapName = '8384db8a-e18d-11e7-b7a8-0242faa257ad'
      }
    }

  // Custom properties

  property 'ec_deploy', {

    // Custom properties
    ec_notifierStatus = '0'
  }
  jobCounter = '76'
}


pipeline 'WebLogic - DemoApp - Deploy', {
  description = ''
  enabled = '1'
  projectName = projName
  type = null

  formalParameter 'ec_stagesToRun', defaultValue: null, {
    expansionDeferred = '1'
    label = null
    orderIndex = null
    required = '0'
    type = null
  }

  stage 'Stage 1', {
    condition = null
    parallelToPrevious = null
    pipelineName = 'WebLogic - DemoApp - Deploy'
    precondition = null
    
    gate 'PRE', {
      condition = null
      precondition = null
      }

    gate 'POST', {
      condition = null
      precondition = null
      }

    task 'Deploy SampleApp', {
      description = ''
      actualParameter = [
        'ec_smartDeployOption': '0',
        'ec_stageArtifacts': '1',
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = 'WebLogic Demo'
      environmentProjectName = projName
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      rollingDeployEnabled = '0'
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = 'WebLogic DemoApp'
      subpluginKey = null
      subprocedure = null
      subprocess = 'Deploy'
      subproject = projName
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PROCESS'
    }

    task 'Check If App works fine', {
      description = ''
      actualParameter = [
        'successcriteria': 'pagefound',
        'targeturl': targetUrl,
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = null
      environmentProjectName = null
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      rollingDeployEnabled = null
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = null
      subpluginKey = 'EC-WebLogic'
      subprocedure = 'CheckPageStatus'
      subprocess = null
      subproject = null
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PLUGIN'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '1'
  }
}


pipeline 'WebLogic - DemoApp - Undeploy', {
  description = ''
  enabled = '1'
  projectName = projName
  type = null

  formalParameter 'ec_stagesToRun', defaultValue: null, {
    expansionDeferred = '1'
    label = null
    orderIndex = null
    required = '0'
    type = null
  }

  stage 'Stage 1', {
    condition = null
    parallelToPrevious = null
    pipelineName = 'WebLogic - DemoApp - Undeploy'
    precondition = null
    
    gate 'PRE', {
      condition = null
      precondition = null
      }

    gate 'POST', {
      condition = null
      precondition = null
      }

    task 'Undeploy SampleApp', {
      description = ''
      actualParameter = [
        'ec_smartDeployOption': '0',
        'ec_stageArtifacts': '1',
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = 'WebLogic Demo'
      environmentProjectName = projName
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      rollingDeployEnabled = '0'
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = 'WebLogic DemoApp'
      subpluginKey = null
      subprocedure = null
      subprocess = 'Undeploy'
      subproject = projName
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PROCESS'
    }

    task 'Check if App stopped', {
      description = ''
      actualParameter = [
        'successcriteria': 'pagenotfound',
        'targeturl': targetUrl,
      ]
      advancedMode = '0'
      condition = null
      deployerExpression = null
      deployerRunType = null
      enabled = '1'
      environmentName = null
      environmentProjectName = null
      environmentTemplateName = null
      environmentTemplateProjectName = null
      errorHandling = 'stopOnError'
      gateCondition = null
      gateType = null
      groupName = null
      insertRollingDeployManualStep = '0'
      instruction = null
      notificationTemplate = null
      parallelToPrevious = null
      precondition = null
      rollingDeployEnabled = null
      rollingDeployManualStepCondition = null
      skippable = '0'
      snapshotName = null
      startTime = null
      subapplication = null
      subpluginKey = 'EC-WebLogic'
      subprocedure = 'CheckPageStatus'
      subprocess = null
      subproject = null
      subworkflowDefinition = null
      subworkflowStartingState = null
      taskType = 'PLUGIN'
    }
  }

  // Custom properties

  property 'ec_counters', {

    // Custom properties
    pipelineCounter = '1'
  }
}

