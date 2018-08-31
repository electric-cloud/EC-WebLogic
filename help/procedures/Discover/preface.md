After the procedure runs the following are created:

* A EC-WebLogic plugin configuration based on the discovered environment
* An EF Resource and Environment, based on the provided resource/hostname data.
* An Application with components and process steps based on WebLogic resources, based on Object Names provided.
* A report describing the current state of Weblogic resources in a variety of formats (csv, JSON and HTML).

Currently the following objects are supported for discovery:

* AppDeployment
* Library
* DataSource
* JMS Module
* JMS Queue
* JMS Topic
* JMS Connection Factory
* JMS Submodule
* JMS Server
* Server
* Cluster
* User
* Group
