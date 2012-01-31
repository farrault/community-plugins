# RPM plugin #

This document describes the functionality provided by the RPM plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The RPM plugin is a Deployit plugin allows to package RPM files and deploy them, either on a Host, either on a rpm.Container.

##Features##

* Install the RPM package
* Upgrade the RPM package
* Remove the RPM package

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+

# Use #

The plugin works with the standard deployment package of DAR format. Please see the _Packaging Manual_ for more details about the DAR format and the ways to 
compose one. 

The following is a sample MANIFEST.MF file that can be used to create a WebLogic specific deployment package. 
It contain declarations for an RPM package.

    Manifest-Version: 1.0
    Deployit-Package-Format-Version: 1.3
    CI-Application: SampleApp
    CI-Version: 1.0

    Name: toto-0.1-1.i386.rpm
    CI-Name: myRpmPackage
    CI-Type: rpm.Package

Note: leave the CI-Name without the '.rpm' extension to allow to perform query before the initial installation or the update.

If you want to apply commands before and after the RPM installation, package a rpm.ContainerPackage and target a rpm.Container

Exemple:

```

<type type="acme.MyRpmContainer extends="rpm.Container">
	<property name="stopScript" default="acme/before"/>
	<property name="startScript" default="acme/after"/>
</type>

```

and define yours command in acme/before.sh.ftl and acme.after.sh.ftl files


