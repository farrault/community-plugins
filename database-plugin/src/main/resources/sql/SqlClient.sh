#!/bin/sh

<#assign envVars=deployed.resolvedEnvVars />
<#list envVars?keys as envVar>
export ${envVar}="${envVars[envVar]}"
</#list>

<#if (deployed.container.workingDirectory?has_content)>
echo Changing to ${deployed.container.workingDirectory}
cd ${deployed.container.workingDirectory}
</#if>

${deployed.resolvedCommandLine} ${deployed.file}