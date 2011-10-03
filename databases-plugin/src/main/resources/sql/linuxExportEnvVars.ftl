<#assign envVars=deployed.container.envVars />
<#list envVars?keys as envVar>
export ${envVar}="${envVars[envVar]}"
</#list>