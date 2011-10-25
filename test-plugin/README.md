Description
===========

A Deployit 3.5 plugin that supports execution of post-deployment application tests.

Installation
============

Place the 'tests-plugin-&lt;version&gt;.jar' file into your SERVER_HOME/plugins directory.

TODO

Usage
=====

Create 'tests.HttpRequestTest' CIs in deployment packages and map them to 'tests.TestStation' CIs in the target environment. The URL configured in the test will be requested from the test station's host at the end of a deployment, in the POST_FLIGHT phase.

The default implementation supports only UNIX test stations.

Configuration
=============

The default implementation of the request test can be overridden by placing a modified 'execute-http-request.sh' file in SERVER_HOME/conf/tests, e.g. to use curl in order to check for a specific response code (rather than a response in the 200 range).