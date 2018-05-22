def projName = args.projectName
def resName = args.resourceName
def params = args.params

project projName, {
    procedure 'Retrieve', {
        params.each { k, defaultValue ->
            formalParameter k, defaultValue: defaultValue, {
                type = 'textarea'
            }
        }

        step 'RunProcedure', {
            resourceName = resName
            subproject = '/plugins/EC-Artifact/project'
            subprocedure = 'Retrieve'

            params.each { k, v ->
                actualParameter k, '$[' + k + ']'
            }
        }
    }
}
