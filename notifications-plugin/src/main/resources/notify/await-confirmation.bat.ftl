@echo off
setlocal
set CONFIRMATION_STATUS_FILE="%TMP%\awaiting-${deployed.name}"

if not exist %CONFIRMATION_STATUS_FILE% (
  echo Awaiting confirmation of '${deployed.name}'. Please retry this step once confirmation has been received.
  echo Confirmation file for ${deployed.name} > %CONFIRMATION_STATUS_FILE%
  exit 1
)

echo Confirmation of '${deployed.name}' received
del /Q %CONFIRMATION_STATUS_FILE% > nul
endlocal