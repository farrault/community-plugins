TODO: Fix http://tech.xebialabs.com/jira/browse/DEPLOYITPB-2255!

Description
===========

A Deployit 3.5 plugin that supports execution of SQL and other database commands against database clients such as Oracle's SQL*Plus.

Installation
============

Place the 'database-plugin-&lt;version&gt;.jar' file into your SERVER_HOME/plugins directory.

If you are running a version of the Deployit server that does not yet address [DEPLOYITPB-2235](http://tech.xebialabs.com/jira/browse/DEPLOYITPB-2235), you will also need to copy the hotfix-DEPLOYITPB-2235-1.0.jar to your SERVER_HOME/hotfix directory. Create the JAR by running 'gradle build' from this project's 'hotfix-DEPLOYITPB-2235' directory.


Usage
=====

The database plugin supports execution of SQL scripts ('sql.SqlScript') against database clients ('sql.SqlClient' and subtypes) defined in Deployit. The SQL scripts are first processed to replace any '{{...}}' placeholders present, then copied to the target machine on which the database client is installed, and then run against the database client.

Ordering
--------

The 'order' property of 'sql.SqlScript' CIs defines the order in which the SQL scripts are run against a target database, with lower numbers executing before higher numbers. Ordering applies only to scripts executed against *the same* database; if multiple databases are targeted during a single deployment, *all* scripts to be run against the database client with the (lexicographically) smaller ID will be run, in order, before clients with lexicographically greater IDs. 
If ordering *across* databases is required, it is recommended to give the 'sql.SqlClient' CIs appropriate IDs, e.g. '1-RunHereFirst', '2-RunHereSecond' etc.

All 'sql.SqlScript' CIs in a deployment package must have different orders, multiple 'sql.SqlScript' CIs with the *same* order are not allowed.

Rollback
--------

Deployit supports rollback of database changes via rollback scripts specified using the 'rollbackScript' property of 'sql.SqlScript' CIs. Rollback scripts should be included alongside 'regular' scripts in the same package, and may use the same set of placeholder ('{{...}}') expressions as their corresponding 'regular' script.

Rollback scripts will be executed in *reverse* order of the corresponding 'regular' scripts, so the latest changes are rolled back first.

Recommendations
---------------

In order to make maximum use of the functionality offered by the database-plugin, it is recommended to deliver database changes as a set of *incremental* files that grows from application version to application version, so that each version includes all the database changes necessary to bring a 'greenfield' database to the desired state.

When updating from one version to the next, Deployit's UDE will automatically detect only the *new* changes not included in the previous version, and run these in the correct order. In this way, only the database changes that have not yet been applied as part of the previous version will be executed.

When downgrading to an older version, Deployit's UDE will detect which database changes were not required for the older version, and will run the appropriate rollback scripts to undo them.

Configuration
=============

The generic 'sql.SqlClient' is designed to allow you to represent an arbitrary database client with its specific command line syntax for executing SQL scripts. The database plugin also allows you to define environment variables that need to be set before the command to run the SQL script is executed.

The command and the environment variables are inserted into a 'runner script' (specified by the 'runSqlScript' property of the 'sql.SqlClient' CI) which by default is the sql/SqlClient.sh (or sql/SqlClient.bat, for Windows) file in the database plugin JAR. You can specify your own FreeMarker template via the SERVER_HOME/conf/synthetic.xml file:

```xml
<synthetic>
  ...
  <type-modification type="sql.SqlClient">
    <property name="runSqlScript" hidden="true" default="my/sql/run/script" />
  </type-modification>
</synthetic>
```

Since Deployit aims to remove unnecessary complexity and the associated risk of errors from the deployment process, it is *strongly* recommended not to work with 'sql.SqlClient' CIs directly, but to define subtypes (again, via SERVER_HOME/conf/synthetic.xml) that reflect the database clients in use in your environment. The 'sql.OracleClient', 'sql.MySqlClient' and 'sql.Db2Client' defined in the plugin are examples.

Only if there are so many variants in command line syntax across your environment that management of all the subtypes would become difficult (e.g. if the command line depended on the machine hosting the database client, to take an extreme example), should one consider using the generic 'sql.SqlClient' type or a custom subtype (e.g. 'sql.MyOracleClient') with a visible 'command' property that can be set individually for each instance:

```xml
<synthetic>
  ...
  <type type="sql.MyOracleClient" extends="sql.OracleClient" 
      description="An Oracle SQL*Plus client with custom settings">
    <property name="command" default="${deployed.container.oraHome}/bin/sqlplus ${deployed.usernameOrDefault}/${deployed.passwordOrDefault}@${deployed.schemaOrDefault} @" />
    ...
  </type>
</synthetic>
```