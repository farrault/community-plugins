<#macro join sequence separator>
    <#assign first="true"/>
    <#list sequence as entry><#if first=="true"><#assign first="false"/><#else>${separator}</#if>worker-${entry.name}</#list>
</#macro>

ps=/

<#if deployed.jkstatus >
worker.list=jkstatus,${deployed.loadbalancerName}
worker.jkstatus.type=status
<#else>
worker.list=${deployed.loadbalancerName}
</#if>

<#assign workers> <@join deployed.targets ","/></#assign>

worker.${deployed.loadbalancerName}.type=lb
worker.${deployed.loadbalancerName}.balance_workers=${workers?trim}
worker.${deployed.loadbalancerName}.sticky_session=${deployed.stickySession?string}
worker.${deployed.loadbalancerName}.sticky_session_force=${deployed.stickySessionForce?string}

<#list deployed.targets as target>
 <#assign worker="worker-${target.name}"/>
worker.${worker}.port=${target.ajpPort}
worker.${worker}.host=${target.host.address}
worker.${worker}.type=ajp13
worker.${worker}.lbfactor=1
worker.${worker}.retries=${deployed.retries}
worker.${worker}.socket_keepalive=${deployed.socketKeepAlive}
worker.${worker}.socket_timeout=${deployed.socketTimeout}

</#list>



