def projName = args.projectName
def resName = args.resourceName
def appName = projName
def envName = projName

def params = [
    'configname'      : 'EC-Specs WebLogic Config',
    wlstabspath       : '',
    appname           : '',
    additional_options: '',
    retire_gracefully : '',
    version_identifier: '',
    give_up           : ''
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
                subprocedure = 'UndeployApp'
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

