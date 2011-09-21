#!/usr/bin/env cloudburst

from time import sleep
from socket import gethostbyaddr

sys.argv.pop(0)
systemName = sys.argv.pop(0)

print "Destroying virtual system", systemName

mySystem = cloudburst.virtualsystems[systemName][0]
mySystem.delete()

print "Destroyed virtual system", systemName
