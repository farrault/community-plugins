Description
===========

A Deployit 3.5 plugin that supports notifications (currently, via email only).

Installation
============

Place the 'notifications-plugin-&lt;version&gt;.jar' file into your SERVER_HOME/plugins directory.

Also requires 'simple-java-mail-1.8.jar' in the SERVER_HOME/lib directory. This library is available from [Maven Central](http://search.maven.org/#search|ga|1|simple-java-mail-1.8).

Usage
=====

The notification plugin supports sending of arbitrary emails via 'notify.EmailDraft' and 'notify.TemplateEmailDraft' Deployables in packages. For template emails, a FreeMarker template with the name of the Deployed needs to be available in SERVER_HOME/ext/notify/email/<type-name>.ftl (e.g. 'notify.DeploymentEndNotification.ftl').

Two additional standard options provided by the plugin are sending of "deployment start" and/or "deployment end" notifications. These are activated by checking the property 'sendDeploymentStartNotification' (resp. 'sendDeploymentEndNotification') on the target environment.

Configuration
=============

The appropriate settings for the start/end notifications emails are set by adding the following snippet to SERVER_HOME/ext/synthetic.xml and modifying the values as appropriate:

```xml
<synthetic>
  <type-modification type="notify.FixedSentTemplateEmail">
      <property name="from" hidden="true" default="deployit@acme.com" />
      <property name="to" hidden="true" default="Stakeholders &lt;stakeholders@acme.com&gt;" />
      <property name="cc" hidden="true" required="false" default="" />
      <property name="bcc" hidden="true" required="false" default="" />
  </type-modification>
<synthetic>    
```

The above properties expect email addresses to be specified as "address" or "name <address>" - note also the XML escaping required for angle brackets. 'To', 'Cc' and 'Bcc' accept comma-separated lists of email addresses.

The contents of the start/end emails can be modified by extracting the templates 'notify/email/notify.DeploymentStartNotification.ftl' and/or 'notify/email/notify.DeploymentEndNotification.ftl' from the plugin JAR into SERVER_HOME/ext and amending them as desired.