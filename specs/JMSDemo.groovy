import spock.lang.*

@Stepwise
class JMSDemo extends WebLogicHelper {
    @Shared
    def projectName = "EC-WebLogic JMS Demo"

    @Shared
    def artifactName = 'weblogic:jms-demo-app'

    @Shared
    def jndiNames = [
        connectionFactory: 'com.weblogic.jmsDemoCf',
        queue: 'com.weblogic.jmsQueue',
        topic: 'com.weblogic.jmsTopic'
    ]

    @Shared
    def resName = 'WL JMS Demo Resource'

    @Shared
    def jmsModuleName = 'JMSDemoModule'

    @Shared
    def appName = 'jms-sample'

    def doSetupSpec() {
        createConfig(CONFIG_NAME)
        deleteProject(projectName)
        publishArtifact(artifactName, '1.0.0', 'jms-sample.war')
        def host = getResourceHost()
        def port = getResourcePort()

        dsl """
          resource '$resName', {
            hostName = '$host'
            port     = '$port'
          }
        """

        deleteJMSModule(jmsModuleName)
        dslFile "dsl/EndToEnd/jmsDemo.dsl", [
            projectName: projectName,
            config: CONFIG_NAME,
            resName: resName,
            artifactName: artifactName,
            jmsModuleName: jmsModuleName,
            wlst: getWlstPath(),
            jndi: jndiNames,
            appName: appName,
            appPath: 'jms-sample.war'
        ]
    }

    def 'first deploy'() {
        when:
        def result = runProcedure """
            runProcess(
                projectName: '$projectName',
                applicationName: 'JMS Demo App',
                processName: 'Deploy',
                tierMapName: 'WebLogic'
            )
        """, getResourceName()
        then:
        assert result.outcome == 'success'
        def response = getUrl("${appName}/JMSServlet?connectionFactory=${jndiNames.connectionFactory}&queue=${jndiNames.queue}")
        logger.debug(response)
        assert response =~ /Found connection factory/
        assert response =~ /Found queue/
        assert response =~ /Sent message/
    }

    def getUrl(url) {
        def host = getResourceHost()
        def fullUrl = "http://${host}:7001/${url}"
        logger.debug("Fetching ${fullUrl}")
        return fullUrl.toURL().text
    }
}
