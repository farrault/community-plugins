# Apache modjk plugin #

This document describes the functionality provided by the Apache modjk plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The Apache modjk plugin is a Deployit plugin that is used to manage the configuration of the modjk plugin running in a Apache Web Server.

##Features##

* Generate the main modjk configuration file 
* Generate the workers modjk configuration file 

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+
	* **Other Deployit Plugins**: webserver-plugin-3.6.x

# Use #

The plugin works with the standard deployment package of DAR format. Please see the _Packaging Manual_ for more details about the DAR format and the ways to 
compose one. 

The following is a sample MANIFEST.MF file that can be used to create a WebLogic specific deployment package. 
It contain declarations for an Ear, a specification for a modjk worker, main configuration and the associated virtual host.

    Manifest-Version: 1.0
    Deployit-Package-Format-Version: 1.3
    CI-Application: SampleApp
    CI-Version: 1.0

    Name: SampleApp-1.0.ear
    CI-Name: SampleApp
    CI-Type: jee.Ear

	Name: PetClinic-worker-jk
	CI-Type: www.ApacheModJKWorkerSpec

	Name: PetClinic-jk
	CI-Type: www.ApacheModJKSpec

	Name: PetClinic-vh-jk
	CI-Type: www.ApacheVirtualHostModJKSpec
	CI-mountedContexts-EntryValue-1: /petclinic/*
	CI-mountedContexts-EntryValue-2: /admin


# Note #
By default, the plugin targets all the generic Containers. To manage only a specific container, modify the 'reference-type' of the 'targets' property.

Example: to target only the JBoss servers


```xml
<type type="www.ApacheModJKWorkerSetting" extends="www.ApacheConfFragment" deployable-type="www.ApacheModJKWorkerSpec">
	....
	<property name="targets" kind="set_of_ci" referenced-type="jbossas.BaseServer" required="true" />
	...
</type>
		 
```


