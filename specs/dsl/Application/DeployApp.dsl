def projName = args.projectName
def resName = args.resourceName

def appName = projName
def envName = projName

def params = [
    'configname'               : 'EC-Specs WebLogic Config',
    'additional_options'       : '',
    'appname'                  : '',
    'apppath'                  : '',
    'archive_version'          : '',
    'deployment_plan'          : '',
    'is_library'               : '',
    'overwrite_deployment_plan': '',
    'plan_path'                : '',
    'plan_version'             : '',
    'remote'                   : '',
    'retire_gracefully'        : '',
    'retire_timeout'           : '',
    'stage_mode'               : '',
    'targets'                  : '',
    'upload'                   : '',
    'version_identifier'       : '',
    'wlstabspath'              : '',
]

def actualParam = [:]
params.each { key, value ->
    actualParam[key] = '$[' + key + ']'
}


project projName, {

    environment envName, {
        environmentEnabled = '1'

        environmentTier 'Tier 1', {
            batchSize = null
            batchSizeType = null
            resourceName = [
                resName,
            ]
        }
    }

    application appName, {
        description = ''

        applicationTier 'Tier 1', {
        }

        process 'MainProcess', {
            processType = 'OTHER'

            processStep 'Procedure', {
                actualParameter = actualParam
                applicationTierName = 'Tier 1'
                processStepType = 'plugin'
                subprocedure = 'DeployApp'
                subproject = '/plugins/EC-WebLogic/project'
            }

            params.each { key, value ->
                formalParameter key, defaultValue: value, {
                    type = 'entry'
                }
            }
        }

        tierMap '542c35d9-7471-11e8-9738-0242ac120003', {
            applicationName = appName
            environmentName = envName
            environmentProjectName = projName

            tierMapping '5452f82e-7471-11e8-a01a-0242ac120003', {
                applicationTierName = 'Tier 1'
                environmentTierName = 'Tier 1'
                tierMapName = '542c35d9-7471-11e8-9738-0242ac120003'
            }
        }

    }
}
