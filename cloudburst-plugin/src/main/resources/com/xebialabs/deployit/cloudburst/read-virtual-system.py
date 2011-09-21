#!/usr/bin/env cloudburst

from time import sleep
from socket import gethostbyaddr

sys.argv.pop(0)
systemName = sys.argv.pop(0)

print "Reading information about virtual system", systemName

mySystem = cloudburst.virtualsystems[systemName][0]
print "state:", mySystem.currentstatus_text
ipaddr = mySystem.virtualmachines[0].ip.ipaddress
print "ipaddr:", ipaddr
hostname = gethostbyaddr(ipaddr)[0]
print "hostname:",hostname
