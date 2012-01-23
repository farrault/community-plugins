Listen ${deployed.port}

<VirtualHost ${deployed.host}:${deployed.port}>
	DocumentRoot <#if deployed.documentRoot != ""> ${deployed.documentRoot}<#else> ${deployed.container.htdocsDirectory}${deployed.container.host.os.fileSeparator}${deployed.deployable.name}</#if>
	ServerName ${deployed.host}


<#list deployed.mountedContexts as ctx >
	JkMount ${ctx} ${deployed.loadbalancerName}
</#list>
<#list deployed.unmountedContexts as ctx >
	JkUnMount ${ctx} ${deployed.loadbalancerName}
</#list>

	
</VirtualHost>

