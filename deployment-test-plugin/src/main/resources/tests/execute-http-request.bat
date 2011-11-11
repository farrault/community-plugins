@echo off

set tempDirectory = <#if deployed.container.host.temporaryDirectoryPath??>${deployed.container.host.temporaryDirectoryPath}<#else>${deployed.host.os.defaultTemporaryDirectoryPath}</#if>
echo tempDirectory is %tempDirectory%

set RESPONSE_FILE=%tempDirectory%\http-response.txt

echo Executing "${deployed.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O %RESPONSE_FILE% ${deployed.url}"

${deployed.wgetExecutable} <#if (deployed.ignoreCertificateWarnings?? && deployed.ignoreCertificateWarnings)>--no-check-certificate</#if> -O %RESPONSE_FILE% "${deployed.url}"

set WGET_EXIT_CODE=%errorlevel%
echo WGET_EXIT_CODE is %WGET_EXIT_CODE%

if not %WGET_EXIT_CODE% == 0 (
  echo FAILURE: '${deployed.url}' returned non-200 response code
  exit %WGET_EXIT_CODE% 
)

<#if (deployed.showPageInConsole?? && deployed.showPageInConsole)>
  more %RESPONSE_FILE%
</#if>

findstr /C:"${deployed.expectedResponseText}" %RESPONSE_FILE%

set GREP_EXIT_CODE=%errorlevel%

echo GREP_EXIT_CODE is %GREP_EXIT_CODE%

if not %GREP_EXIT_CODE% == 0 (
  echo FAILURE: Response body did not contain "${deployed.expectedResponseText}":
  del %RESPONSE_FILE%
  exit %GREP_EXIT_CODE%
) else (
  echo test string was found! TEST SUCCESSFULL
)
del %RESPONSE_FILE%