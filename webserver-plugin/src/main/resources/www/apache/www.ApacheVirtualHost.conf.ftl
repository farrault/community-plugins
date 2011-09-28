Listen ${deployed.port}

<VirtualHost ${deployed.hostAndPort}>
	DocumentRoot ${deployed.getDocumentRoot()}
	ServerName ${deployed.host}
</VirtualHost>
