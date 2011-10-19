@echo off
setlocal

<#import "/sql/commonFunctions.ftl" as cmn>

set ORACLE_HOME=${deployed.container.oraHome}
set ORACLE_SID=${lookup('schema')}

# will override the declarations above if ORACLE_HOME or ORACLE_SID are present
<#include "/sql/windowsSetEnvVars.ftl">

${deployed.container.oraHome}\bin\sqlplus ${cmn.lookup('username'}/${cmn.lookup('password')}@${cmn.lookup('schema')} @${step.uploadedArtifactPath}

endlocal