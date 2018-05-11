import spock.lang.*
import com.electriccloud.spec.*

class WebLogicHelper extends PluginSpockTestSupport {
    static final def HELPER_PROJECT = 'EC-WebLogic Specs Helper'

    static def getWlstPath() {
        def path = System.getenv('WEBLOGIC_WLST_PATH')
        assert path
        path
    }

    static def getResourceName() {
        def resName = System.getenv('WEBLOGIC_RES_NAME')
        assert resName
        resName
    }

    static def getUsername() {
        def username = System.getenv('WEBLOGIC_USERNAME')
        assert username
        username
    }

    static def getPassword() {
        def password = System.getenv('WEBLOGIC_PASSWORD')
        assert password
        password
    }

    def createConfig(configName) {
        def endpoint = 't3://localhost:7001'
        def username = getUsername()
        def password = getPassword()
        def pluginConfig = [
            weblogic_url  : endpoint,
            enable_named_sessions: 'true',
            debug_level: '10'
        ]
        def props = [confPath: 'weblogic_cfgs']
        if (System.getenv('RECREATE_CONFIG')) {
            props.recreate = true
        }
        createPluginConfiguration(
            'EC-WebLogic',
            configName,
            pluginConfig,
            username,
            password,
            props
        )
    }


    static def runWLST(code) {
        code = code.trim()
        println "+++$code+++"
        dsl """
            project '${HELPER_PROJECT}', {
                procedure 'runWlst', {
                    step 'runCommand', {
                        resourceName = '${getResourceName()}'
                        shell = '${getWlstPath()}'
                        command = '''\$[wlst_code]'''
                    }

                    formalParameter 'wlst_code', {
                        type = 'textarea'
                    }
                }
            }
        """
        def result = runProcedureDsl """
            runProcedure(
                projectName: '${HELPER_PROJECT}',
                procedureName: 'runWlst',
                actualParameter: [
                    'wlst_code': '''$code'''
                ]
            )
        """
        println result
        result
    }

}
