encode()
{
    local  myresult=$(printf "%b" "$1" | perl -pe's/([^-_.~A-Za-z0-9])/sprintf("%%%02X", ord($1))/seg')
    echo "$myresult"
}

echo will perform inspection on '${container.name}'
echo INSPECTED:directory=/${container.name}

PERMS=`ls -ld '/${container.name}' | cut -d ' ' -f 1`
echo INSPECTED:permissions=$(encode $PERMS)
