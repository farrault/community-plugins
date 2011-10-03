@echo off
setlocal

<#include "/sql/windowsSetEnvVars.ftl">

${deployed.container.db2Home}\bin\db2 -tvf ${step.uploadedArtifactPath}

endlocal