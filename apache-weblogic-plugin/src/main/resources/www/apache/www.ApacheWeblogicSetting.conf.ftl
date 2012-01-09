
#optional Listen ${deployed.port}

<VirtualHost ${deployed.host}:${deployed.port}>
	DocumentRoot <#if deployed.documentRoot != ""> ${deployed.documentRoot}<#else> ${deployed.container.htdocsDirectory}${deployed.container.host.os.fileSeparator}${deployed.deployable.name}</#if>
	ServerName ${deployed.host}

	<#assign targets="">
	<#list deployed.targets as target>
		<#assign targets="${targets},${target.host.address}:${target.host.port}">
	</#list>
	WebLogicCluster ${targets}
	<#list deployed.matchExpressions as me>
	MatchExpression ${me}
	</#list>
</VirtualHost>