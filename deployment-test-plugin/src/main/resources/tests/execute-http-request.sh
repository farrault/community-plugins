#!/bin/sh
RESPONSE_FILE=${deployed.hostTemporaryDirectoryOrDefault}/http-response.$$
echo Executing "${deployed.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O $RESPONSE_FILE ${deployed.url}"

${deployed.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O "$RESPONSE_FILE" ${deployed.url}

WGET_EXIT_CODE=$?
if [ $WGET_EXIT_CODE -ne 0 ]; then
  echo FAILURE: '${deployed.url}' returned non-200 response code
  exit $WGET_EXIT_CODE
fi

<#if (deployed.showPageInConsole?? && deployed.showPageInConsole)>
  cat $RESPONSE_FILE
</#if>
grep "${deployed.expectedResponseText}" $RESPONSE_FILE

GREP_EXIT_CODE=$?
rm $RESPONSE_FILE

if [ $GREP_EXIT_CODE -ne 0 ]; then
  echo FAILURE: Response body did not contain "${deployed.expectedResponseText}"
  exit $GREP_EXIT_CODE
fi