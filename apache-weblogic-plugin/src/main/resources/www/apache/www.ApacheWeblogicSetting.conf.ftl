<#macro join sequenceofsequence separator>
    <#assign first="true"/>
    <#list sequenceofsequence as sequence><#list sequence.servers as entry><#if first=="true"><#assign first="false"/><#else>${separator}</#if>${entry.host.address}:${entry.port}</#list></#list>
</#macro>

#optional Listen ${deployed.port}

<VirtualHost ${deployed.host}:${deployed.port}>
	DocumentRoot <#if deployed.documentRoot != ""> ${deployed.documentRoot} <#else> ${deployed.container.htdocsDirectory}${deployed.container.host.os.fileSeparator}${deployed.deployable.name}</#if>
	ServerName ${deployed.host}

	<#assign targets> <@join deployed.targets ","/> </#assign>
	WebLogicCluster ${targets?trim}

	<#list deployed.matchExpressions as me>
	MatchExpression ${me}
	</#list>
</VirtualHost>






