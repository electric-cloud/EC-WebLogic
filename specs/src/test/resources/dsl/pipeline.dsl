def projName = args.projectName
def procName = args.procedureName
def resName = args.resourceName
def params = args.params

project projName, {
    pipeline procName, {
        projectName = projName

        params.each { k, defaultValue ->
            formalParameter k, defaultValue: defaultValue, {
                type = 'textarea'
            }
        }

        stage 'Stage', {
            projectName = projName


            task 'RunProcedure', {
                resourceName = resName
                subpluginKey = 'EC-WebLogic'
                subprocedure = procName
                resourceName = resName
                projectName = projName
                taskType = 'PLUGIN'

                def parameters = [:]
                params.each { k, v ->
                    parameters[k] = '$[' + k + ']'
                }
                actualParameter = parameters

            }
        }
    }
}
