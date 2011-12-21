#!/bin/sh
CONFIRMATION_STATUS_FILE="/tmp/awaiting-${deployed.name}"

if ! [ -a $CONFIRMATION_STATUS_FILE ]; then
  echo Awaiting confirmation of '${deployed.name}'. Please retry this step once confirmation has been received.
  echo Confirmation file for ${deployed.name} > $CONFIRMATION_STATUS_FILE
  exit 1
fi

echo Confirmation of '${deployed.name}' received
rm -f $CONFIRMATION_STATUS_FILE