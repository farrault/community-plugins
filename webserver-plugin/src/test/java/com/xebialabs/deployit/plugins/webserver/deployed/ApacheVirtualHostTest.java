package com.xebialabs.deployit.plugins.webserver.deployed;


import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugins.webserver.TestBase;
import com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost;

/**
 * Unit tests for the {@link ApacheVirtualHost}
 */
public class ApacheVirtualHostTest extends TestBase {
    
    @Test
    public void usesDefinedDocroot() {
        ApacheVirtualHost virtualHost = newInstance("www.ApacheVirtualHost2");
        virtualHost.putSyntheticProperty("documentRoot", "/secret/folder");
        assertThat(virtualHost.getDocumentRoot(), is("/secret/folder"));
    }
    
    @Test
    public void fallsBackToDerivedDocrootUnderServerDocroot() {
        assertThat(newVirtualHost().getDocumentRoot(), is("/var/httpd/www/virtualHostSpec"));
    }
    
    private static ApacheVirtualHost newVirtualHost() {
        ApacheVirtualHost virtualHost = newInstance("www.ApacheVirtualHost2");
        Container webServer = newInstance("www.ApacheHttpdServer");
        webServer.setHost(newSshHost());
        webServer.setRestartScript("/etc/init.d/httpd restart");
        webServer.putSyntheticProperty("htdocsDirectory", "/var/httpd/www");
        virtualHost.setContainer(webServer);
        Resource virtualHostSpec = (Resource) newInstance("www.ApacheVirtualHostSpec");
        virtualHostSpec.setId("virtualHostSpec");
        virtualHost.setDeployable(virtualHostSpec);
        return virtualHost;
    }
}
