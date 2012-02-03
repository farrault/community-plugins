#!/bin/bash

<#assign envVars=deployed.container.envVars />
<#list envVars?keys as envVar>
export ${envVar}="${envVars[envVar]}"
</#list>

START_DELAY_SECS=${deployed.startDelay}

if [ $START_DELAY_SECS -ne 0 ]; then
  echo Waiting $START_DELAY_SECS seconds
  sleep $START_DELAY_SECS
fi

MAX_RETRIES=${deployed.maxRetries}
RETRY_INTERVAL_SECS=${deployed.retryWaitInterval}

for (( i=1; i<=$MAX_RETRIES; i++ )); do
  RESPONSE_FILE=http-response.$$
  rm -f $RESPONSE_FILE
  
  echo Executing "${deployed.container.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O $RESPONSE_FILE ${deployed.url}"
  ${deployed.container.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O "$RESPONSE_FILE" ${deployed.url}

  WGET_EXIT_CODE=$?
  if [ $WGET_EXIT_CODE -eq 0 ]; then
    break
  fi
  sleep $RETRY_INTERVAL_SECS
done

if [ $WGET_EXIT_CODE -ne 0 ]; then
  echo ERROR: '${deployed.url}' returned non-200 response code
  exit $WGET_EXIT_CODE
fi

<#if (deployed.showPageInConsole?? && deployed.showPageInConsole)>
cat $RESPONSE_FILE
</#if>

grep "${deployed.expectedResponseText}" $RESPONSE_FILE

SEARCH_EXIT_CODE=$?

if [ $SEARCH_EXIT_CODE -ne 0 ]; then
  echo ERROR: Response body did not contain "${deployed.expectedResponseText}"
  exit $SEARCH_EXIT_CODE
fi