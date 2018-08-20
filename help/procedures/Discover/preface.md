This procedure will discover WebLogic resources and create a configuration, resource and EF model based on your current WebLogic resources.

Upon run, the procedure will create:

* A configuration for EC-WebLogic plugin, based on provided and discovered data.
* An EF Resource and Environment, based on the provided resource/hostname data.
* An Application with the components and process steps based on WebLogic resources, if Object Names are provided.

Every run will also create several reports in csv, JSON and HTML format, which will describe the current state of WebLogic resources.


The following resources can be discovered:

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


User
Group
