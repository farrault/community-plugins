/*
 * @(#)WebContent.java     18 Aug 2011
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.xebialabs.deployit.plugins.webserver.deployed;

import static com.xebialabs.deployit.plugin.api.deployment.specification.Operation.MODIFY;
import static com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry.getDescriptor;
import static com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost.DOCROOT_PROPERTY;
import static com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost.HOST_PROPERTY;
import static com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost.PORT_PROPERTY;
import static java.lang.String.format;

import com.xebialabs.deployit.deployment.planner.DefaultDelta;
import com.xebialabs.deployit.plugin.api.deployment.planning.Create;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.deployment.planning.Destroy;
import com.xebialabs.deployit.plugin.api.deployment.planning.Modify;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.api.udm.Property;
import com.xebialabs.deployit.plugin.generic.ci.Folder;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugin.generic.deployed.CopiedArtifact;
import com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost.HostAndPort;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "Static web content deployed to a www.ApacheHttpdServer")
public class PublishedWebContent extends CopiedArtifact<Folder> {
    private static final Resource DUMMY_APACHE_VIRTUAL_HOST_SPEC = new Resource();
    private static final char HOST_PORT_DIRECTORY_SEPARATOR = '_';
    
    @Property(category = "Virtual Host", required = false, defaultValue = "", description = "The virtual host (as 'host:port') under which to expose the content. If left blank, the content will be copied to the configured target directory")
    private String virtualHost = "";
    
    @Property(category = "Virtual Host", required = false, defaultValue = "", description = "The virtual host document root. Assumed to be a non-shared directory containing only this content. If left blank, a directory will be created under the webserver's document root. If no virtual host is specified, this property has no effect")
    private String virtualHostDocumentRoot = "";
    
    private boolean usingVirtualHost() {
        // assuming non-null
        return !virtualHost.trim().isEmpty();
    }
    
    @Create
    public void executeCreateVirtualHost(DeploymentPlanningContext ctx) {
        if (usingVirtualHost()) {
            createVirtualHost(ctx);
        }
    }

    protected void createVirtualHost(DeploymentPlanningContext ctx) {
        ApacheVirtualHost delegate = getDelegate();
        delegate.executeCreate(ctx);
        delegate.reloadServer(ctx);
    }

    private ApacheVirtualHost getDelegate() {
        ApacheVirtualHost delegate = getDescriptor("www.ApacheVirtualHost2").newInstance();
        delegate.setDeployable(DUMMY_APACHE_VIRTUAL_HOST_SPEC);
        delegate.setContainer(getContainer());
        // may be empty
        delegate.putSyntheticProperty(DOCROOT_PROPERTY, getVirtualHostDocumentRoot());
        HostAndPort virtualHostAndPort = new HostAndPort(getVirtualHost());
        // the target directory (if not specified) will be generated from this
        delegate.getDeployable().setId(getDefaultVirtualHostDocumentRoot(virtualHostAndPort));
        delegate.putSyntheticProperty(HOST_PROPERTY, virtualHostAndPort.getHost());
        delegate.putSyntheticProperty(PORT_PROPERTY, virtualHostAndPort.getPort());
        return delegate;
    }

    private static String getDefaultVirtualHostDocumentRoot(HostAndPort virtualHostAndPort) {
        return virtualHostAndPort.getFilenameFriendlyHost() + HOST_PORT_DIRECTORY_SEPARATOR 
               + virtualHostAndPort.getFilenameFriendlyPort();
    }

    @Destroy
    public void executeDestroyVirtualHost(DeploymentPlanningContext ctx) {
        if (usingVirtualHost()) {
            destroyVirtualHost(ctx);
        }
    }

    protected void destroyVirtualHost(DeploymentPlanningContext ctx) {
        ApacheVirtualHost delegate = getDelegate();
        delegate.executeDestroy(ctx);
        delegate.reloadServer(ctx);
    }
    
    @Modify
    public static void executeModifyVirtualHost(DeploymentPlanningContext ctx, Delta delta) {
        PublishedWebContent previousWebContent = (PublishedWebContent) delta.getPrevious();
        PublishedWebContent newWebContent = (PublishedWebContent) delta.getDeployed();
        String previousVirtualHost = previousWebContent.getVirtualHost();
        String newVirtualHost = newWebContent.getVirtualHost();
        // assume neither is null
        if (!previousVirtualHost.equals(newVirtualHost)) {
            if (previousVirtualHost.isEmpty()) {
                newWebContent.createVirtualHost(ctx);
                return;
            }
            if (newVirtualHost.isEmpty()) {
                previousWebContent.destroyVirtualHost(ctx);
                return;
            }
            // both non-empty
            modifyVirtualHost(ctx, previousWebContent, newWebContent);
        }
    }
    
    private static void modifyVirtualHost(DeploymentPlanningContext ctx,
            PublishedWebContent previous, PublishedWebContent www) {
        ApacheVirtualHost.executeModify(ctx, 
                new DefaultDelta(MODIFY, previous.getDelegate(), www.getDelegate()));
    }
    
    @Override
    public String getTargetDirectory() {
        return usingVirtualHost() ? getDelegate().getDocumentRoot() 
                                  : super.getTargetDirectory();
    }
    
    @Override
    public boolean isCreateTargetDirectory() {
        // static content using a virtual host gets its own directory under the server's docroot
        return usingVirtualHost();
    }

    @Override
    public boolean isTargetDirectoryShared() {
        // static content using a virtual host uses its own directory
        return !usingVirtualHost();
    }

    @Override
    protected String getDescription(String verb) {
        return format("%s in '%s'", super.getDescription(verb), getTargetDirectory());
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getVirtualHostDocumentRoot() {
        return virtualHostDocumentRoot;
    }

    public void setVirtualHostDocumentRoot(String virtualHostDocumentRoot) {
        this.virtualHostDocumentRoot = virtualHostDocumentRoot;
    }
}
