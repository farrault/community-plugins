Description
===========

A custom Deployit 3.6 plugin to support post-deployment tests.

Installation
============

Place the 'deployment-test2-&lt;version&gt;.jar' file into your `SERVER_HOME/plugins` directory.

On Windows hosts, the plugin will by default use a version of `wget` included in the plugin. If your wish to use a _different_ `wget` that is _already present_ on the path of your target systems you can simply prevent the included version from being uploaded by modifying `SERVER_HOME/conf/deployit-defaults.properties` as follows:

```
# Classpath Resources
# tests2.ExecutedHttpRequestTest.classpathResources=tests2/runtime/wget.exe
```

to

```
# Classpath Resources
tests2.ExecutedHttpRequestTest.classpathResources=
```
