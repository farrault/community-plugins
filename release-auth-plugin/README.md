# Release Authorization plugin #

This document describes the functionality provided by the Release Authorization (RA) plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

Deployments usually happen in the context of a software release management process, which tries to safeguard when, how and by whom deployments can be performed. Using the RA plugin, it is possible to automatically enforce your organization's conditions, such as mandatory change tickets or test requirements. Set conditions per environment and Deployit verifies these conditions before any changes can be made.

##Features##

* Define conditions per environment that a deployment package must meet
* Verify that deployment package conditions are satisfied when performing a deployment

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+
	* **Other Deployit Plugins**: None

# Defining deployment package properties #

Release authorization is concerned with the conditions which permit a particular deployment package to be deployed to an environment. When performing a deployment, the package in question is checked to ensure it meets the required prerequisites for deployment. These prerequisites are defined as _synthetic properties_ on the deployment package. For more information about synthetic properties, see the **Customization Manual**.

To define release authorization properties on the deployment package, create a _synthetic.xml_ file that modifies the _udm.DeploymentPackage_ CI to add the properties to be checked. The following XML snippet shows an example:

	<type-modification type="udm.DeploymentPackage">
	        <property name="hasReleaseNotes" description="Indicates the package contains release notes" required="false" kind="boolean" category="Release Auth"/>
	        <property name="isPerformanceTested" description="Indicates the package has been performance tested" required="false" kind="boolean" category="Release Auth"/>
	        <property name="hasFinalOk" description="Indicates the package has been given the final OK to be deployed on production" required="false" kind="boolean" category="Release Auth"/>
	</type-modification>

Release authorization condition properties can be any allowed type. Boolean properties must have the value _true_ to pass validation, other properties must not be empty.

# Defining environment conditions #

The conditions for allowing a deployment to an environment are defined on the environment itself. It is possible to specify different conditions for different environments. For instance, the test environment may have less stringent requirements than the production environment. Conditions are defined in a synthetic property on the environment.

To define conditions on the environment, create a _synthetic.xml_ file that modifies the _udm.Environment_ CI to add _conditions_ property. The following XML snippet shows how:

	<type-modification type="udm.Environment">
	        <property name="conditions" kind="set_of_string" required="false" category="Release Auth"/>
	</type-modification>

To specify which conditions must be met for deployment to the environment, open the environment in the Deployit GUI's Repository tab and switch to the **Release Auth** tab. The conditions property contains the exact name (case-sensitive) of the deployment package property that will be validated on a deployment to this environment.

For instance, if the deployment package has the _hasReleaseNotes_ property as defined in the example in the previous section, adding _hasReleaseNotes_ to the _conditions_ property on the test environment means the packages being deployed to the test environment must have the _hasReleaseNotes_ property set to _true_ to be deployed.

# Verifying release authorization conditions #

When preparing a deployment of a particular package to an environment, Deployit will validate the release authorization conditions if:

* the target environment has a property _conditions_ defined
* the deployment is an _initial_ deployment or an _upgrade_ (release authorization is not performed for undeployment)

If this is the case, Deployit will verify whether the target environment's conditions are met by the deployment package when the deployment has been fully configured and the deployment plan is generated. If any conditions are unmet, an error message will be shown, explaining the missing conditions.

In addition, the generated deployment plan will contain a step that validates the release authorization. This serves to obtain an audit trail in the deployment log, detailing exactly which conditions have been checked.

# Approving release authorization conditions #

Release authorization properties are stored on the deployment package. To set or unset these properties, locate the deployment package in the Repository browser. Users that have _repo#edit_ permission as well as _read_ and _write_ permission on the deployment package will be able to set the release authorization properties.
