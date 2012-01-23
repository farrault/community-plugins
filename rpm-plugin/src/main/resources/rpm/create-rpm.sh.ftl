echo "Querying whether the RPM ${deployed.name} is installed on host"
rpm -q ${deployed.name}
res=$?
if [ $res != 1 ] ; then
	echo "Package is already installed"
	exit 1000
fi
echo "Installing ${deployed.name}"
rpm -i ${deployed.file}
