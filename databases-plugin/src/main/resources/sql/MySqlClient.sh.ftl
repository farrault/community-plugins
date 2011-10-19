#!/bin/sh

<#import "/sql/commonFunctions.ftl" as cmn>
<#include "/sql/linuxExportEnvVars.ftl">

${deployed.container.mySqlHome}/bin/mysql --user=${cmn.lookup('username')} --password=${cmn.lookup('password')} ${cmn.lookup('schema')} < ${step.uploadedArtifactPath}
