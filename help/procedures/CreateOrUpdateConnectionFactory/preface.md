WebLogic Server defines two default connection factories, which can be looked up using the JNDI names weblogic.jms.ConnectionFactory and weblogic.jms.XAConnectionFactory.

This procedure is used to create and configure a new connection factory for an existing JMS module if the pre-configured settings of the default factories are not suitable for your application.

It is recommended to use default targeting for the Connection Factory, i.e. do not use Subdeployments.
