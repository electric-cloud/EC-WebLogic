def resName = args.resourceName
def projName = 'EC-WebLogic Specs Helper'
def appName = 'EC-WebLogic Specs Application'
def envName = 'EC-Weblogic Specs Env'
def procName = 'CreateOrUpdateJMSModuleSubdeployment'

def params = [
    'configname'                          : 'EC-Specs WebLogic Config',
    ecp_weblogic_jms_module_name          : '',
    ecp_weblogic_update_action            : 'do_nothing',
    ecp_weblogic_subdeployment_target_list: '',
    ecp_weblogic_subdeployment_name       : ''
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

        process procName, {
            processType = 'OTHER'

            processStep 'Procedure', {
                actualParameter = actualParam
                applicationTierName = 'Tier 1'
                processStepType = 'plugin'
                subprocedure = 'CreateOrUpdateJMSModuleSubdeployment'
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
            environmentProjectName = 'EC-WebLogic Specs Helper'

            tierMapping '5452f82e-7471-11e8-a01a-0242ac120003', {
                applicationTierName = 'Tier 1'
                environmentTierName = 'Tier 1'
                tierMapName = '542c35d9-7471-11e8-9738-0242ac120003'
            }
        }

    }
}
