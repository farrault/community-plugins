#!/bin/sh

<#assign envVars=deployed.envVars />
<#list envVars?keys as envVar>
export ${envVar}="${envVars[envVar]}"
</#list>

<#if deployed.file??>
REM do not remove - this actually triggers the upload
cd "${deployed.file}"
</#if>

${deployed.command}

COMMAND_EXIT_CODE=$?

exit $COMMAND_EXIT_CODE