Description
===========

A Deployit 3.5 plugin that supports deployment of web content to web servers and management of Apache virtual hosts. The plugin is easily extensible to arbitrary Apache configuration fragments (see section 'Extending the plugin').

Installation
============

Place the 'webserver-plugin-&lt;version&gt;.jar' file into your SERVER_HOME/plugins directory.

If you are running a version of the Deployit server that does not yet address [DEPLOYITPB-2252](http://tech.xebialabs.com/jira/browse/DEPLOYITPB-2252), you will also need to copy the hotfix-DEPLOYITPB-2252-1.0.jar to your SERVER_HOME/hotfix directory. Create the JAR by running 'gradle build' from this project's 'hotfix-DEPLOYITPB-2252' directory.

Notes
=====

The plugin offers two modes of support for Apache virtual hosts: 'content-scoped' and 'infrastructure-scoped'.

Infrastructure-scoped (i-s) virtual hosts are meant to persist across the lifecycle of various items of web content, and may even serve as virtual hosts for multiple applications. 
To deploy an i-s virtual host, include appropriate 'www.ApacheVirtualHostSpec' deployables in your deployment packages and map them to 'www.ApacheHttpdServer' containers.
When deploying web content ('www.WebContent' deployeds) to an i-s virtual host, ignore the web content's 'Virtual Host' attributes and ensure the 'targetDirectory' property of the web content matches the 'documentRoot' property of the virtual host.

Content-scoped virtual hosts share the lifecycle of a single piece of web content and are designed to expose only the web content of the application currently being deployed. To create a c-s virtual host for your web content, simply set the 'virtualHost' and, optionally, 'virtualHostDocumentRoot' properties of the web content's being deployed. You do not need to include a 'www.ApacheVirtualHostSpec' deployable to create c-s virtual hosts.

If you will *only* be deploying i-s virtual hosts, you may wish to hide the 'Virtual Host' attributes of the 'www.WebContent' deployed to prevent c-s virtual hosts being created. Similarly, if you will *only* be using c-s hosts, the 'targetDirectory' property of the 'www.WebContent' deployed should be hidden.

The relevent XML snippets for your DEPLOYIT_HOME/ext/synthetic.xml file are:

```xml
<!-- infrastructure-scoped virtual hosts only -->
<type-modification type="www.PublishedWebContent">
  <property name="virtualHost" required="false" hidden="true" default="" />
  <property name="virtualHostDocumentRoot" required="false" hidden="true" default="" />
</type-modification>

<!-- content-scoped virtual hosts only -->
<type-modification type="www.PublishedWebContent">
  <!-- overridden at runtime for virtual hosts -->
  <property name="targetDirectory" hidden="true" default="${deployed.container.htdocsDirectory}" />
</type-modification>
```

Extending the plugin
====================

The webserver plugin is designed to support deployment of arbitrary types of Apache configuration fragments. Adding support for a new fragment type takes only two steps:

1) Defining the type of configuration fragment and its properties  
2) Supplying a template for the configuration fragment implementation

Example:

1) Define an 'ApacheProxyPassSetting' type in DEPLOYIT_HOME/ext/synthetic.xml

```xml
<!-- see Deployit extender documentation for details -->
<type type="www.ApacheProxyPassSetting" extends="www.ApacheConfFragment"
    deployable-type="www.ApacheProxyPassSpec">
  <generate-deployable type="www.ApacheProxyPassSpec" extends="www.ApacheConfFragmentSpec" />
  
  <!-- before of SQL injection-style attacks! -->
  <property name="from" />
  <property name="to" />
  <property name="options" required="false" default="" />
  <property name="reverse" kind="boolean" required="false" default="false" />
</type>
```

2) Create a www.ApacheProxyPassSetting.conf.ftl in DEPLOYIT_HOME/ext/www/apache

<pre>
--- start www.ApacheProxyPassSetting.conf.ftl ---
ProxyPass ${deployed.from} ${deployed.to} &lt;#if (deployed.options?has_content)&gt;${deployed.options}&lt;/#if&gt;
&lt;#if (deployed.reverse)&gt;
ProxyPassReverse ${deployed.from} ${deployed.to}
&lt;/#if&gt;
--- end www.ApacheProxyPassSetting.conf.ftl ---
</pre>