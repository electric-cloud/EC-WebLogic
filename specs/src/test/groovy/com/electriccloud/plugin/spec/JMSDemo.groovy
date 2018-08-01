package com.electriccloud.plugin.spec

import spock.lang.*

@Stepwise
@Narrative("""
JMS Use-case with URL checks. First create all the required resources and deploy app,
then change JNDI names and redeploy the app.
""")
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


    @Shared def applicationName = 'JMS Demo App'
    @Shared def processName = 'Deploy'
    @Shared def tierMapName = 'WebLogic'

    def doSetupSpec() {
        setupResource()
        createConfig(CONFIG_NAME)
        deleteProject(projectName)
        publishArtifact(artifactName, '1.0.0', demoAppPath)
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
            appName: demoAppName,
            appPath: demoAppPath
        ]
    }

    def 'first deploy'() {
        when:
        def result = runProcess(projectName, 'JMS Demo App', 'Deploy', 'WebLogic')
        then:
        assert result.outcome == 'success'
        checkPage()
    }

    def 'redeploy'() {
        when:
        def result = runProcess(projectName, applicationName, processName, tierMapName)
        then:
        assert result.outcome == 'success'
        checkPage()
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
            appName: demoAppName,
            appPath: demoAppPath
        ]
        when:
        def result = runProcess(projectName, applicationName, processName, tierMapName)
        then:
        assert result.outcome == 'success'
        checkPage(names.connectionFactory, names.queue, names.topic)
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

    def checkPage(cf = null, queue = null, topic = null) {
        cf = cf ?: jndiNames.connectionFactory
        topic = topic ?: jndiNames.topic
        queue = queue ?: jndiNames.queue

        def host = getResourceHost()
        def resName = getResourceName()
        dsl """
        project '$projectName', {
            procedure 'Check Page', {
                step 'Check Queue & Topic', {
                    shell = 'ec-groovy'
                    command = '''
                        println "http://localhost:7001/\$[queueUrl]".toURL().text
                        println "http://localhost:7001/\$[topicUrl]".toURL().text
                    '''
                    resourceName = '${resName}'
                }

                formalParameter 'queueUrl', {
                    type = 'entry'
                }

                formalParameter 'topicUrl', {
                    type = 'entry'
                }
            }
        }
        """

        def queueUrl = "${demoAppName}/JMSServlet?connectionFactory=${cf}&queue=${queue}"
        def topicUrl = "${demoAppName}/JMSTopic?connectionFactory=${cf}&topic=${topic}"

        def response = runProcedure("""
            runProcedure projectName: '$projectName',
            procedureName: 'Check Page',
            actualParameter: [
                queueUrl: '$queueUrl',
                topicUrl: '$topicUrl'
            ]
            """, resName
        )

        assert response.outcome == 'success'

        assert response.logs =~ /Found Connection Factory/
        assert response.logs =~ /Subscriber is ready/
        assert response.logs =~ /Found topic/
        assert response.logs =~ /Sent message/

        assert response.logs =~ /Found connection factory/
        assert response.logs =~ /Found queue/
        assert response.logs =~ /Sent message/
        return true
    }
}
