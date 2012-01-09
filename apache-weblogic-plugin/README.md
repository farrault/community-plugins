# Apache Weblogic plugin #

This document describes the functionality provided by the Apache Weblogic plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The Apache Weblogic plugin is a Deployit plugin that is used to manage the configuration of the Weblogic plugin running in a Apache Web Server.

##Features##

* Generate the apacheWebServer configuration file

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+
	* **Other Deployit Plugins**: wls-plugin-3.6.x

# Use #

The plugin works with the standard deployment package of DAR format. Please see the _Packaging Manual_ for more details about the DAR format and the ways to 
compose one. 

The following is a sample MANIFEST.MF file that can be used to create a WebLogic specific deployment package. 
It contain declarations for an Ear, a specification for a Weblogic configuration.

    Manifest-Version: 1.0
    Deployit-Package-Format-Version: 1.3
    CI-Application: SampleApp
    CI-Version: 1.0

    Name: SampleApp-1.0.ear
    CI-Name: SampleApp
    CI-Type: jee.Ear

	Name: LoadBalancer
	CI-Type: www.ApacheWeblogicSettingSpec
	CI-mountedContexts-EntryValue-1: *.jsp


# Oracle documentation #

* [General Parameters for Web Server Plug-In](http://docs.oracle.com/cd/E11035_01/wls100/plugins/plugin_params.html#wp1143055)
* [Installing and Configuring the Apache HTTP Server Plug-In](http://docs.oracle.com/cd/E15051_01/wls/docs103/plugins/apacheWebServer.html)




