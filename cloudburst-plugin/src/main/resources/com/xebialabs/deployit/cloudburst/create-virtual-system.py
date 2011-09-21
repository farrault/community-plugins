#!/usr/bin/env cloudburst

from time import sleep
from socket import gethostbyaddr

sys.argv.pop(0)
patternName = sys.argv.pop(0)
cloudName = sys.argv.pop(0)
systemName = sys.argv.pop(0)
systemPassword = sys.argv.pop(0)

print "Deploying pattern", patternName, "to cloud", cloudName, "with name", systemName

myPattern = cloudburst.patterns[patternName][0]
myCloud = cloudburst.clouds[cloudName][0]
mySystem = cloudburst.virtualsystems.create({'name': systemName, 'cloud': myCloud, 'pattern': myPattern, '*.*.password': systemPassword})

mySystem = cloudburst.virtualsystems[systemName][0]
mySystem.waitFor()

print "Deployed pattern", patternName, "to cloud", cloudName, "with name", systemName

mySystem = cloudburst.virtualsystems[systemName][0]
print "state:", mySystem.currentstatus_text
