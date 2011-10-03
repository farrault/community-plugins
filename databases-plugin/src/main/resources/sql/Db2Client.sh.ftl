#!/bin/sh

<#include "/sql/linuxExportEnvVars.ftl">

${deployed.container.db2Home}/bin/db2 -tvf ${step.uploadedArtifactPath}
