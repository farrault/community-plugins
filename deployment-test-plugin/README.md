# Deployment Tests Plugin #

This document describes the functionality provided by the deployment tests plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The deployment-tests-plugin is a Deployit plugin that supports execution of post-deployment application tests. These post deployment tests may includes tests such as testing whether the deployed application is accessible 
or whether the created datasource is functioning properly or not. The tests-plugin is intended to provide such functionality to the Deployit server.

##Features##

* Ability to check whether the deployed application is accessible through a URL

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.5+
	* **Other Deployit Plugins**: None
		
* **Infrastructural requirements**
	* **User credentials** for accessing the Host running the JBoss application Server.
	* **User credentials** for accessing the Hosts on which the application accessibility test has to be performed (called the TestStation)
	* **wget** installed on the Hosts (Unix/Windows) on which the application accessibility test has to be performed if it's not same as the Deployit server Host.

# Usage in Deployment Packages #

The plugin works with the standard deployment package of DAR format. Please see the _Packaging Manual_ for more details about the DAR format and the ways to 
compose one. 

The following is a MANIFEST.MF file snippet that shows how a tester can be included in the deployment package which can be used to run a sanity check to verify that the petclinic application is accessible after the
deployment

<pre style="display:inline-block; nobreak"><code>
Manifest-Version: 1.0
Deployit-Package-Format-Version: 1.3
CI-Application: PetClinic-ear
CI-Version: 2.0

Name: PetClinic-2.0.ear
CI-Type: jee.Ear
CI-Name: PetClinic

Name: PetclinicTest
CI-Type: tests.HttpRequestTest
CI-url: http://jboss-51:8080/petclinic
CI-expectedResponseText: Display all veterinarians
CI-startDelay: 10
CI-noOfRetries: 5
CI-retryWaitInterval: 3
</code></pre>

# Using the deployables and deployeds #

The following table describes which deployable/container combinations are possible.

## Deployable vs. Container table ##
<table class="deployed-matrix">
<tr>
	<th>Deployable</th>
	<th>Container</th>
	<th>Generated deployed</th>
</tr>
<tr>
	<td>test.HttpRequestTest</td>
	<td>tests.TestStation</td>
	<td>test.HttpRequestTester</td>
</tr>
</table>


The following table describes the effect a deployed has on it's container

## Deployed Actions Table ##

<table class="deployed-matrix">
<tr>
	<th class="borderless-bottom">Deployed</th>
	<th colspan="3">Actions performed for operations</th>
</tr>
<tr>
	<th class="borderless-top">&nbsp;</th>
	<th align="center">Create</th>
	<th align="center">Destroy</th>
	<th align="center">Modify</th>
</tr>
<tr>
	<td>test.HttpRequestTester</td>
	<td>
		<ul>
			<li>invoke the application URL to check if application is running</li>
		</ul>
      </td>
	<td>
		<ul>
			<li>no action</li>
		</ul>
	</td>
	<td>
		<ul>
		<ul>
			<li>invoke the application URL to check if application is running</li>
		</ul>
		</ul>
</tr>
</table>

# Test Station #
A test station is a generic.Container type on which a particular test is run. For the httpRequestTest, the type of Host used for creating the test station determines which version of the test should
be run. There are two kind of tests possible for http request test.

* `Local test`: If the http test has be performed from the same Host on which the Deployit server is running, use a test station built on overthere.LocalHost. This will 
              use Java to access the URL, means it doesn't require any agent to be installed on the machine. 
* `Remote test`: If the http test has to be performed from a remote host (different from Deployit server Host), use a test station built on a Host other than 
			overthere.LocalHost. This expects `wget` to be present on the Host since the script uses `wget` utility to invoke the URL.

# Using a HttpRequestTester #
Let's say if petclinic application is deployed on the server _production-host_, and the http request test has to be tested from host _test-host_ (that has access to the petclinic application), then the 
following steps can be followed to add this application testing feature as part of the standard deployment:

* Create a host corresponding to test-host in the repository browser (ID: Infrastructure/test-host)
* Create a test station (let's say testStation) under the previously created testHost (ID: Infrastructure/test-host/testStation)
* Add the testStation in the environment to which deployment will be performed
* If the application test (let's say petclinicTest of type test.HttpRequestTest) was not part of the package which was imported for the application version, create it under the application 
  version (so the ID of the test will look something like Applications/PetClinic-ear/1.0/PetclinicTest)
* Deploy the package to the enviromment, and you should see a extra step for testing the application at the bottom of the step list.
