@echo off
setlocal

set ORACLE_HOME=${deployed.container.oraHome}
set ORACLE_SID=${deployed.usernameOrDefault}

<#assign envVars=deployed.resolvedEnvVars />
<#list envVars?keys as envVar>
# will override the declarations above if ORACLE_HOME or ORACLE_SID are present
set ${envVar}=${envVars[envVar]}
</#list>

<#if (deployed.container.workingDirectory?has_content)>
echo Changing to ${deployed.container.workingDirectory}
cd ${deployed.container.workingDirectory}
</#if>

${deployed.resolvedCommandLine}${deployed.file}

endlocal