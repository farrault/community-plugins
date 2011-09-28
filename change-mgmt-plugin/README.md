Description
===========

A Deployit 3.5 plugin that supports validation of Change Tickets in an ITIL Change Management system before deployment.

Installation
============

Place the 'change-mgmt-plugin-&lt;version&gt;.jar' file into your SERVER_HOME/plugins directory.

Notes
=====

The change-mgmt-plugin adds the ability to include Change Requests ('chg.ChangeRequests') in deployment packages, which become Change Tickets ('chg.ChangeTicket') when deployed to change managers ('chg.ChangeManager', representing a system that tracks change tickets such as Service Desk) in an environment.

If the property 'requiresChangeTicket' is set on an environment, deployments that do *not* create or update a 'chg.ChangeTicket' will result in a validation error. Since Change Tickets are validated against 'chg.ChangeManager' containers, there needs to be at least one such container in any environment for which 'requiresChangeTicket' is set.

It is recommended to include a blank 'chg.ChangeRequest' of the same name in each deployment package, and to set or modify, as appropriate, the 'requestId' property of the 'chg.ChangeTicket' resulting from the request. 