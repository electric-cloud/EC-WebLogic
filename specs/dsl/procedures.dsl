def projName = args.projectName
def procName = args.procedureName
def resName = args.resourceName ?: 'weblogic'
def params = args.params

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
}
