echo "Querying whether the RPM ${deployed.name} is installed on host"
rpm -q ${deployed.name}
res=$?
if [ $res != 0 ] ; then
	echo "Package is not already installed, Cannot perform upgrade."
	exit $res
fi
echo "Upgrading ${deployed.name}"
rpm -U ${deployed.file}