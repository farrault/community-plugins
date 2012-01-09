@echo off
setlocal

<#assign envVars=deployed.container.envVars />
<#list envVars?keys as envVar>
set ${envVar}=${envVars[envVar]}
</#list>

set START_DELAY_SECS=${deployed.startDelay}

if not [%START_DELAY_SECS%]==[0] (
  echo Waiting %START_DELAY_SECS% seconds
  ping -w 1000 -n %START_DELAY_SECS% 127.0.0.1 > nul
)

set MAX_RETRIES=${deployed.maxRetries}
set RETRY_INTERVAL_SECS=${deployed.retryWaitInterval}
set RESPONSE_FILE_PREFIX=http-response.%RANDOM%

REM workaround for DEPLOYITPB-2907 - only needed if using a local (uploaded) executable
dir /A:-D /B . | findstr /B /C:"${deployed.container.wgetExecutable}." > nul

if not ERRORLEVEL 1 (
  mkdir DEPLOYITPB-2907-workaround
  for %%i in (${deployed.container.wgetExecutable}.*) do (
    copy /B %%i DEPLOYITPB-2907-workaround
  )
  cd DEPLOYITPB-2907-workaround
)
 
for /L %%i in (1,1,%MAX_RETRIES%) do (
  del /Q %RESPONSE_FILE_PREFIX%.%%i 2> nul

  echo Executing "${deployed.container.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O %RESPONSE_FILE_PREFIX%.%%i ${deployed.url}"
  ${deployed.container.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O %RESPONSE_FILE_PREFIX%.%%i "${deployed.url}"

  if ERRORLEVEL 1 (
    set WGET_EXIT_CODE=1
  ) else (
    set WGET_EXIT_CODE=0
    set LAST_RESPONSE_FILE=%RESPONSE_FILE_PREFIX%.%%i
    goto RequestCompleted
  )
  ping -w 1000 -n %RETRY_INTERVAL_SECS% 127.0.0.1 > nul
)

:requestCompleted
if not [%WGET_EXIT_CODE%]==[0] (
  echo ERROR: '${deployed.url}' returned non-200 response code
  exit %WGET_EXIT_CODE% 
)

<#if (deployed.showPageInConsole?? && deployed.showPageInConsole)>
type %LAST_RESPONSE_FILE%
</#if>

findstr /C:"${deployed.expectedResponseText}" %LAST_RESPONSE_FILE%

set SEARCH_EXIT_CODE=%ERRORLEVEL%

if not [%SEARCH_EXIT_CODE%]==[0] (
  echo ERROR: Response body did not contain "${deployed.expectedResponseText}"
  exit %SEARCH_EXIT_CODE%
)

endlocal