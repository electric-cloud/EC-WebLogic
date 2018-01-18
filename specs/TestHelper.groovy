import spock.lang.*
import com.electriccloud.spec.SpockTestSupport
import org.spockframework.runtime.model.SpecInfo
import org.spockframework.runtime.model.ErrorInfo
import org.spockframework.runtime.*


class TestHelper extends SpockTestSupport {


    def failureCount = 0

    def getJobProperty(String path, def jobId) {
        def result = dsl "getProperty(propertyName: '$path', jobId: '$jobId')"
        result?.property.value
    }

    def getPipelineProperty(String path, def flowRuntimeId) {
        def result = dsl "getProperty(propertyName: '$path', flowRuntimeId: '$flowRuntimeId')"
        result?.property.value
    }

    def runPipeline(String projectName, String pipelineName){
        def result = dsl "runPipeline('$projectName', '$pipelineName')";
        result?.flowRuntime?.flowRuntimeId
    }

    def jobCompleted(String jobId) {
        assert jobId
        def status = jobStatus(jobId)
        assert status.status == 'completed'
    }

    def pipelineCompleted(String flowRuntimeId) {
        assert flowRuntimeId
        def status = pipelineStatus(flowRuntimeId)
        def is_completed = status.flowRuntime[0].completed
        assert is_completed == '1'
        is_completed
    }

    def getStageRuntimeId(String flowRuntimeId) {
        assert flowRuntimeId
        def status = pipelineStatus(flowRuntimeId)
        def StageRuntimeId = status.flowRuntime[0].stages.stage[0].flowRuntimeId
        assert StageRuntimeId
        StageRuntimeId
    }

    def getPipelineSummary(String projectName, String flowRuntimeId){
        assert flowRuntimeId
        def status = pipelineStatus(flowRuntimeId)
        def summary = status.flowRuntime[0].stages.stage[0].summary.parameterDetail[0].parameterValue
        assert summary
        summary
    }

    def importProject(String projectName, String dslFilePath, def params) {
        println "Importing project $projectName from $dslFilePath file.";
        dsl """
            project "$projectName"
        """
        dslFile dslFilePath, params
        return true
    }

    def importDSL(String dslFilePath, def params) {
        println "Importing DSL from $dslFilePath file.";
        def dslList = []
        for ( item in params ) {
            dslList.add('def '+item.key+' = "'+item.value+'"')
        }
        // Get the executed script as a (java) File
        def pwd = System.getenv('PWD')
        //dslList.add(new File([pwd, dslFilePath].join('/')).getText('UTF-8'))
        dslList.add(new File([pwd, 'specs', dslFilePath].join('/')).getText('UTF-8'))

        dsl dslList.join("\n")
        return true
    }

    def runPipelineFromDSL(String dslFile, options){
        def projName = options.projName
        def pluginConfig = options.pluginConfig
        def pipeName = options.pipeName

        assert projName
        assert pipeName
        assert pluginConfig

        if (options.projectRemoval){
            if (doesProjExist(projName)){
                deleteProject(projName)
            }
        }

        importDSL(dslFile, options);
        
        if (pluginConfig){
            createConfiguration(pluginConfig, [recreate: true])
        }

        def flowRuntimeId = runPipeline(projName, pipeName)

        waitUntil {
            try {
                pipelineCompleted(flowRuntimeId)
            } catch (Exception e) {
                println e.getMessage()
            }
        }

        assert flowRuntimeId
        return flowRuntimeId
    }

    def runNWaitPipeline(String projName, pipeName){
        def projName = options.projName
        def pipeName = options.pipeName

        assert projName
        assert pipeName


        def flowRuntimeId = runPipeline(projName, pipeName)

        waitUntil {
            try {
                pipelineCompleted(flowRuntimeId)
            } catch (Exception e) {
                println e.getMessage()
            }
        }

        assert flowRuntimeId
        return flowRuntimeId
    }

    def waitNCheckLog(String flowRuntimeId, String stage, value){
        def log = getPipelineLog(flowRuntimeId, stage)
        assert log =~ /(?ms)$value/
        return true 
    }

    def checkValueFlowRuntime(String testCase, String resultFormat, String flowRuntimeId, String stage, String checkType, String resultPath, String key, compare, value){

        waitUntil {
            try {
                pipelineCompleted(flowRuntimeId)
            } catch (Exception e) {
                println e.getMessage()
            }
        }

        def StageRuntimeId = getStageRuntimeId(flowRuntimeId)
        assert StageRuntimeId
        println 'Test Case: ' + testCase
        if (checkType == 'property'){
            assert checkValue(resultFormat, StageRuntimeId, resultPath, key, compare, value)
        }
        else if (checkType == 'log'){    
            def log = getPipelineLog(flowRuntimeId, stage)
            assert log =~ /(?ms)$value/
        }
        else if (checkType == 'fstat'){
            assert value instanceof Boolean
            def file = new File(key)
            if (value){
                assert file.exists()
            }
            else{
                assert !file.exists()
            }
        }

        return true
    }

    def importProject(String projectName, String dslFilePath) {
        importProject(projectName, dslFilePath, [:])
        return true
    }

    def importProject(String dslFilePath) {
        importProject("EC-Jenkins Specs", dslFilePath)
        return true
    }

    def getProcedureRunner(String input) {
        def runner = {
            def p -> p
            println p
            def updatedDsl = sprintf(input, p)
            println "DSL:" + updatedDsl
            def result =  dsl(updatedDsl)
            return result
        }
        return runner
    }

    def checkValue(String resultFormat, String StageRuntimeId, String resultPath, String key, compare, value){
        def realValue
        assert compare
        assert key
        assert StageRuntimeId
        assert resultPath

        if (resultFormat == 'xml'){
            def xml = getPropertyFromStageRuntime(resultPath, StageRuntimeId)

            println xml

            def outcome = new XmlParser().parseText(xml)

            realValue = outcome."$key".text()
        }
        else if (resultFormat == 'json'){
            def outcome = getPropertyFromStageRuntime(resultPath, StageRuntimeId)
            def jsonSlurper = new groovy.json.JsonSlurper()
            def json = jsonSlurper.parseText(outcome)

            assert json instanceof Map

            realValue = json."$key"
            
        }
        else if (resultFormat == 'propertysheet'){
            def outcome = getPropertySheetFromStageRuntime(resultPath, StageRuntimeId).property
            def index = outcome.findIndexOf { it.propertyName == key }
            realValue = outcome[index].value
        }
        else{
            assert 'Wrong resultFormat for checking value provided' == 0
            return false
        }

        println key+": "+realValue

        if (compare instanceof Boolean){
            assert realValue
            return true
        }

        if (value instanceof Integer){
            realValue = realValue.toInteger
        }

        else if (compare == '='){
            assert realValue == value
        }
        else if (compare == '>'){
            assert realValue instanceof Integer
            assert value instanceof Integer
            assert realValue > value
        }
        else if (compare == '<'){
            assert realValue instanceof Integer
            assert value instanceof Integer
            assert realValue < value
        }
        else if (compare == '~'){
            assert realValue instanceof Integer
            assert value instanceof Integer
            assert realValue =~ /(?ms)$value/
        }

        return true
    }

    def setProperty(String path, String value) {
        dsl """
             setProperty(propertyName: '$path', value: '$value')
        """;
    }



    def getPropertySheetFromStageRuntime(String path, def StageRuntimeId) {
        def result = dsl("getProperties(path: '$path', flowRuntimeId: '$StageRuntimeId')").propertySheet
        result
    }

    def getPropertyFromStageRuntime(String propertyName, def StageRuntimeId) {
        def result = dsl("getProperty(propertyName: '$propertyName', flowRuntimeId: '$StageRuntimeId')").property.value
        result
    }

    def getProperties(String path, def jobId) {
        def propertySheet = getPropertySheet(path, jobId)
        propertySheet.property
    }

    def getPropertiesRecursive(String path, String jobId) {
        def properties = getProperties(path, jobId)
        def result = [:]
        properties.each { prop ->
            if (prop.propertySheetId) {
                result[prop.propertyName] = [:]
                def children = getPropertiesRecursive("${path}/${prop.propertyName}", jobId)
                children.each {k, v ->
                    result[prop.propertyName][k] = v
                }
            }
            else {
                result[prop.propertyName] = prop.value
            }
        }
        result
    }

    def getJobProperties(String jobId) {
        getPropertiesRecursive('/myJob', jobId)
    }

    def getJobSummary(String jobId) {
        getProperty("/myJob/summary", jobId)
    }

    def jobStatus(String jobId) {
        assert jobId
        dsl "getJobStatus jobId: '$jobId'"
    }

    def pipelineStatus(String flowRuntimeId) {
        assert flowRuntimeId
        dsl "getPipelineRuntimeDetails(flowRuntimeId: '$flowRuntimeId')"
    }

    def getPipelineJobs(String flowRuntimeId, String stageName){
        assert flowRuntimeId
        assert stageName
        def jobs = []
        def job_list =  dsl "getPipelineStageRuntimeTasks(flowRuntimeId: '$flowRuntimeId', stageName: '$stageName')"
        job_list =  dsl "getPipelineStageRuntimeTasks(flowRuntimeId: '$flowRuntimeId', stageName: '$stageName')"
        println job_list

        println 'ETF'

        for (job in job_list.task) {
            jobs.add(job.jobId)
        }
        println 'jobs: '+jobs
        return jobs
    }

    def getPipelineLog(String flowRuntimeId, String stageName){
        assert flowRuntimeId
        assert stageName
        def result = ''
        for (jobId in getPipelineJobs(flowRuntimeId, stageName)){
            if (jobId){ 
                result = result + getJobLog(jobId)
                println result
            }
        }

        return result
    }


    def getJobLog(String jobId){
        assert jobId
        def plugin_name = System.getenv('PLUGIN_NAME')
        //only Unix for now

        def jobData = dsl "getJobDetails(jobId: '$jobId')"


        def logFileDir 
        if (jobData.job.workspace){
            logFileDir = jobData.job.workspace[0].unix
        }
        else{
            logFileDir = '/opt/electriccloud/electriccommander/workspace/'
        }

        //should be loop for all steps here
        def logFileName = jobData.job.jobStep[0].logFileName
        def result = dsl """
        runProcedure(
            projectName: '/plugins/EC-FileOps/project',
            procedureName: 'SaveFileContent',
            actualParameter: [
                Path: '$logFileDir/$logFileName',
                Content_outpp: '/plugins/$plugin_name/project/log'
            ]
        )
        """
        waitUntil {
            jobCompleted(result.jobId)
        }

        def log_data = dsl "getProperty(propertyName:'/plugins/$plugin_name/project/log')"

        return log_data.property.value
    }



    //link/workspaceFile/workspaces/default?jobStepId=5d737943-c87f-11e7-bf49-024289db233d&fileName=initiate%2520scanner%2520job.5d737943-c87f-11e7-bf49-024289db233d.log&jobName=job_7808_20171113173141&jobId=5d6a03c2-c87f-11e7-9fd7-024289db233d&diagCharEncoding=&resourceName=local&completed=1
    def getProperty(String property, String jobId) {
        def result = dsl "getProperty(propertyName: '$property', jobId: '$jobId')"
        result?.property.value
    }

    def host_url(String protocol, String host, String port, String path){
        return protocol+'://+'+host+port?':'+port:''+path?path:'/'
    }

    def deleteConfiguration(String configName) {
        def plugin_name = System.getenv('PLUGIN_NAME')
        deletePluginConfiguration(configName, plugin_name)
    }

    def deletePluginConfiguration(String configName, String pluginName) {
        def result = dsl """
            runProcedure(
                projectName: '/plugins/$pluginName/project',
                procedureName: 'DeleteConfiguration',
                actualParameter: [
                    config: '$configName'
                ]
            )
        """

        assert result.jobId
        waitUntil {
            jobCompleted(result.jobId)
        }

    }


    def createConfiguration(String configName, options = [:]) {
        def plugin_name = System.getenv('PLUGIN_NAME')
        if (!options.recreate) {
            if (doesConfExist(configName)) {
                println "Configuration $configName exists"
                return
            }
        }

        if (doesConfExist(configName, plugin_name)) {
            deleteConfiguration(configName)
        }

        def weblogic_url = System.getenv('SPECS_configuration_connection')
        def username = System.getenv('SQPECS_credential_username')
        def password = System.getenv('SQPECS_credential_password')

        
        def debugLevel  = 3
        if (configName =~ /^info/){
            debugLevel = 1
        }
        else if (configName =~ /wrong-config/){
            return true
        }
        

        def result = dsl """
        runProcedure(
            projectName: '/plugins/$plugin_name/project',
            procedureName: 'CreateConfiguration',
            credential: [
                credentialName: '$configName',
                userName: '$username',
                password: '$password'
            ],
            actualParameter: [
                config: '$configName',
                credential: '$configName',
                weblogic_url: '$weblogic_url',
                debug_level: '$debugLevel'
            ]
        )
        """

        
        assert result.jobId
        waitUntil {
            jobCompleted(result.jobId)
        }
    }

    def waitForJob(String jobId) {
        def completed = jobStatus(jobId).status == "completed"
        def delay = 1000
        def seconds = 120
        def timeout = seconds * 1000
        def time = 0
        while(!completed && time < timeout) {
            sleep(delay)
            completed = jobStatus(jobId).status == "completed"
            println jobStatus(jobId).status;
            time += delay
            delay *= 1.3
        }
        if (time >= timeout) {
            throw new RuntimeException("Timeout: the job is running for more than $seconds seconds")
        }
        return completed
    }

    def doesConfExist(String confName, String plugin_name) {
        def result = dsl "getProperty('/plugins/$plugin_name/project/ec_plugin_cfgs/$confName')"
        return result?.property
    }

    def doesProjExist (String projectName){
        def res = dsl """try{
              def proj = getProject '$projectName'
              return proj != null
            } catch (all) {
              return false
            }"""
        return res.value.toBoolean()
    } 


    def deleteProject(String projectName) {
        def result = dsl "deleteProject('$projectName')"
        return true
    }


    def getLogs(def jobId) {
        def logs = getProperty("/myJob/debug_logs", jobId)
        logs
    }

    // @Override
    // void visitSpec(SpecInfo specInfo) {
    //     println "Spec visited"
    //     specInfo.addListener(new HelperListener())
    // }


    class HelperListener extends AbstractRunListener {

        def void error(ErrorInfo error) {
            println "Actual on error logic"
            failureCount ++
        }
    }
}
