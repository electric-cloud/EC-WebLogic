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
        def result = runProcess(projectName, 'JMS Demo App', 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkQueue()
        checkTopic()
    }

    def 'redeploy'() {
        when:
        def result = runProcess(projectName, 'JMS Demo App', 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkQueue()
        checkTopic()
    }

    def 'change JNDI names'() {
        setup:
        def names = [
            connectionFactory: 'jms.sample.CF',
            queue: 'jms.sample.Queue',
            topic: 'jms.sample.Topic'
        ]
        dslFile "dsl/EndToEnd/jmsDemo.dsl", [
            projectName: projectName,
            config: CONFIG_NAME,
            resName: resName,
            artifactName: artifactName,
            jmsModuleName: jmsModuleName,
            wlst: getWlstPath(),
            jndi: names,
            appName: appName,
            appPath: 'jms-sample.war'
        ]
        when:
        def result = runProcess(projectName, 'JMS Demo App', 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkQueue(names.connectionFactory, names.queue)
        checkTopic(names.connectionFactory, names.queue)
    }

    def checkQueue(cf = null, queue = null) {
        cf = cf ?: jndiNames.connectionFactory
        queue = queue ?: jndiNames.queue
        def response = getUrl("${appName}/JMSServlet?connectionFactory=${cf}&queue=${queue}")
        logger.debug(response)
        assert response =~ /Found connection factory/
        assert response =~ /Found queue/
        assert response =~ /Sent message/
        return true
    }

    def checkTopic(cf = null, topic = null) {
        cf = cf ?: jndiNames.connectionFactory
        topic = topic ?: jndiNames.topic

        def topicResponse = getUrl("${appName}/JMSTopic?connectionFactory=${cf}&topic=${topic}")
        logger.debug(topicResponse)
        assert topicResponse =~ /Found Connection Factory/
        assert topicResponse =~ /Subscriber is ready/
        assert topicResponse =~ /Found topic/
        assert topicResponse =~ /Sent message/
        return true
    }

    def getUrl(url) {
        def host = getResourceHost()
        def fullUrl = "http://${host}:7001/${url}"
        logger.debug("Fetching ${fullUrl}")
        return fullUrl.toURL().text
    }

    def runProcess(projectName, appName, processName, tierMapName) {
        def result = runProcedure """
            runProcess(
                projectName: '$projectName',
                applicationName: '$appName',
                processName: '$processName',
                tierMapName: '$tierMapName'
            )
        """, getResourceName(), 180, 15
        return result
    }
}
