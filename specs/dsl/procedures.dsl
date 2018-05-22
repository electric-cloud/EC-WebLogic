def projName = args.projectName
def procName = args.procedureName
def resName = args.resourceName
def params = args.params
def deleteProcName = args.deleteProcedureName
def deleteParams = args.deleteParams

project projName, {
    procedure procName, {
        params.each { k, defaultValue ->
            formalParameter k, defaultValue: defaultValue, {
                type = 'textarea'
            }
        }

        step 'RunProcedure', {
            resourceName = resName
            subproject = '/plugins/EC-WebLogic/project'
            subprocedure = procName

            params.each { k, v ->
                actualParameter k, '$[' + k + ']'
            }
        }
    }

    procedure deleteProcName, {
        deleteParams.each { k, defaultValue ->
            formalParameter k, defaultValue: defaultValue, {
                type = 'textarea'
            }
        }

        step 'RunProcedure', {
            resourceName = resName
            subproject = '/plugins/EC-WebLogic/project'
            subprocedure = deleteProcName

            deleteParams.each {k, v ->
                actualParameter k, '$[' + k + ']'
            }
        }
    }
}
