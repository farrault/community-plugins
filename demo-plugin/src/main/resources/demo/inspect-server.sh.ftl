encode()
{
    local  myresult=$(printf "%b" "$1" | perl -pe's/([^-_.~A-Za-z0-9])/sprintf("%%%02X", ord($1))/seg')
    echo "$myresult"
}

#
# Inspects the demo server by setting the home property and discovering the /tmp folder. 
#

echo will perform inspection on ${container.name}
echo INSPECTED:home=$(encode '/')

IFS=$'\n'
for i in '/tmp'
do
    echo discovered $i as CI with id $(encode "${container.id}$i")
    echo DISCOVERED:$(encode "${container.id}$i")=demo.Folder
done
