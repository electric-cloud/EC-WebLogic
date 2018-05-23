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

    static def getAdminServerName() {
        def adminServerName = 'AdminServer'
        return adminServerName
    }

    static def getEndpoint() {
        return 't3://localhost:7001'
    }

    def createConfig(configName) {
        def endpoint = getEndpoint()
        def username = getUsername()
        def password = getPassword()
        def pluginConfig = [
            weblogic_url  : endpoint,
            enable_named_sessions: 'true',
            debug_level: '10',
            wlst_path: getWlstPath(),
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


    def runWLST(code) {
        code = code.trim()
        def resourceName = getResourceName()
        def procedureName = 'RunWLST'
        dsl """
            project '${HELPER_PROJECT}', {
                procedure '${procedureName}', {
                    step 'runCommand', {
                        resourceName = '${getResourceName()}'
                        shell = '${getWlstPath()}'
                        command = '''\$[code]'''
                    }

                    formalParameter 'code', {
                        type = 'textarea'
                    }
                }
            }
        """
        def result = runProcedure("""
            runProcedure(
                projectName: '${HELPER_PROJECT}',
                procedureName: '${procedureName}',
                actualParameter: [
                    code: '''$code'''
                ]
            )
        """, resourceName)
        result
    }

}
