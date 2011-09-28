@echo off
setlocal

<#assign envVars=deployed.resolvedEnvVars />
<#list envVars?keys as envVar>
set ${envVar}=${envVars[envVar]}
</#list>

<#if (deployed.container.workingDirectory?has_content)>
echo Changing to ${deployed.container.workingDirectory}
cd ${deployed.container.workingDirectory}
</#if>

${deployed.resolvedCommandLine} ${deployed.file}

endlocal