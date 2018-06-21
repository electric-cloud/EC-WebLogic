def projName = args.projectName
def resName = args.resourceName
def appName = projName
def envName = projName

def params = [
    'configname'               : 'EC-Specs WebLogic Config',
    cf_name                    : '',
    jndi_name                  : '',
    cf_sharing_policy          : '',
    cf_client_id_policy        : '',
    jms_module_name            : '',
    wls_instance_list          : '',
    cf_max_messages_per_session: '',
    cf_xa_enabled              : '',
    subdeployment_name         : '',
    jms_server_list            : '',
    update_action              : '',
    additional_options         : '',
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
                subprocedure = 'CreateOrUpdateConnectionFactory'
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
