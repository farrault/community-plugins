# Preface #

This document describes the functionality provided by the database plugin.

See the **Deployit Reference Manual** for background information on Deployit and deployment concepts.

# Overview #

The database plugin is a Deployit plugin that supports deployment of SQL files and folders to a database client.

## Features

* Runs on Deployit 3.6 and up.
* Supports deployment to MySQL, Oracle and DB/2.
* Deploys and undeploys SQL files and folders.

# Requirements #

* **Deployit requirements**
	* **Deployit**: version 3.6+
	* **Other Deployit Plugins**: None
		
* **Infrastructural requirements**
	* **User credentials** for accessing the database client executables on the host running the database.

# Plugin Concepts #

## SQL Scripts ##

The [SqlScripts](#sql.SqlScripts) CI encompasses a folder containing SQL scripts that are to be executed on a database. SQL scripts come in two flavors, namely installation scripts and rollback scripts. Installation scripts are used to execute changes on the database, such as creation of a table or inserting data. Rollback scripts are associated with an installation script and undo the actions performed by the installation script. Executing an installation script followed by the accompanying rollback script should leave the database in an unchanged state.

SQL scripts are ordered alphabetically based on their filename. This is an example of ordering of several installation scripts:

* 1-create-user-table.sql
* 10-drop-user-index.sql
* 2-insert-user.sql
* ...
* 9-create-user-index.sql

Note that in this example, the tenth script, _10-drop-user-index.sql_ would be incorrectly executed after the first script.

When upgrading a SqlScripts CI, only those scripts that were not present in the previous package version are executed. For example, if the previous SqlScripts folder contained script1.sql and script2.sql, and the 
new version of SqlScripts folder contains script2.sql and script3.sql, then only script3.sql will be executed as part of the upgrade. 

When undeploying a SqlScripts CI, all rollback scripts are executed in reverse alphabetical order.

## SQL Client ##

The [SqlClient](#sql.SqlClient) CIs are containers to which [SqlScripts](#sql.SqlScripts) can be deployed. The plugin ships with SqlClient for the following databases:

* MySQL
* Oracle
* DB/2

When SQL scripts are deployed to an SQL client, each script to be executed is run against the SQL client in turn. The SQL client can be configured with a username and password that is used to connect to the database. The credentials can be overridden on each SQL script if required.

# Usage in Deployment Packages #

The following is a manifest snippet that shows how SQL file and folder CIs can be included in a deployment package. The SQL scripts CI refers to a folder, _sql_, in the deployment package.

<pre style="display:inline-block; nobreak"><code>
Manifest-Version: 1.0
Deployit-Package-Format-Version: 1.3
CI-Application: PetClinic-ear
CI-Version: 2.0

Name: PetClinic-2.0.ear
CI-Type: jee.Ear
CI-Name: PetClinic

Name: sql
CI-Type: sql.SqlScripts
CI-Name: sql
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
	<td>sql.SqlScripts</td>
	<td>sql.OracleClient,<br/>sql.MySqlClient, <br/>sql.Db2Client</td>
	<td>sql.ExecutedSqlScripts</td>
</tr>
</table>

The following table describes the effect a deployed has on it's container.

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
	<td>sql.ExecutedSqlScripts</td>
	<td>
		For each installation script in the folder (ordered alphabetically by name, ascending):
		<ul>
			<li>Run script through template engine</li>
			<li>Copy create script to container</li>
			<li>Execute script</li>
		</ul>
	</td>
	<td>
		For each rollback script in the folder (ordered alphabetically by name, descending):
		<ul>
			<li>Run script through template engine</li>
			<li>Copy destroy script to container</li>
			<li>Execute script</li>
		</ul>
	</td>
	<td>
		For each installation script in the folder that was not part of the deployment being upgraded (ordered alphabetically by name, ascending):
		<ul>
			<li>Run script through template engine</li>
			<li>Copy modify script to container</li>
			<li>Execute script</li>
		</ul>
	</td>
</tr>
</table>
