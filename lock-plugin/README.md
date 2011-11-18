# Lock plugin #

This document describes the functionality provided by the Lock plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The Lock plugin is a Deployit plugin that adds capabilities for locking containers when deployments are in progress.

##Features##

* Lock containers for exclusive use by one deployment
* List and clear locks using a lock manager CI and control tasks.

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+
	* **Other Deployit Plugins**: None

# Performing deployments using locks #

When installed, the locks plugin will add steps to your deployment plan to exclusively lock each container involved in a deployment. If an exclusive lock can not be obtained, the deployment will fail and must be continued when the container involved is unlocked. Steps to unlock each of the containers are added to the end of your deployment plan.

Each lock is stored as a file in a directory under the Deployit installation directory. The _lock.Manager_ CI can be created in the _Infrastructure_ section of Deployit to list and clear all of the current locks.

# Configuring the locks plugin #

The locks plugin adds a few synthetic properties to all containers in Deployit:

* *allowConcurrentDeployments* (default: false): indicates whether concurrent deployments are allowed. If true, the locks plugin will *not* lock the container.
* *deploymentInProgressCheckOrder* (default: PRE_FLIGHT + 2): the order of the steps added to the deployment plan to check the container locks.
* *deploymentInProgressCheckCleanupOrder* (default: POST_FLIGHT - 2): the order of the steps added to the deployment plan to clear the container locks.
