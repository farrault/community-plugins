#!/bin/sh

<#import "/sql/commonFunctions.ftl" as cmn>

export ORACLE_HOME="${deployed.container.oraHome}"
export ORACLE_SID="${cmn.lookup('schema')}"

# will override the declarations above if ORACLE_HOME or ORACLE_SID are present
<#include "/sql/linuxExportEnvVars.ftl">

${deployed.container.oraHome}/bin/sqlplus ${cmn.lookup('username')}/${cmn.lookup('password')}@${cmn.lookup('schema')} @${step.uploadedArtifactPath}
