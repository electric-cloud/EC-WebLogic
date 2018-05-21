def projName = args.projectName
def procName = args.procedureName
def resName = args.resourceName
def params = args.params
def subProject = args.subProjectName ?: '/plugins/EC-WebLogic/project'


project projName, {
    procedure procName, {
        params.each { k, defaultValue ->
            formalParameter k, defaultValue: defaultValue, {
                type = 'textarea'
            }
        }

        step 'RunProcedure', {
            resourceName = resName
            subproject = subProject
            subprocedure = procName

            params.each { k, v ->
                actualParameter k, '$[' + k + ']'
            }
        }
    }
}
