/*
 * @(#)WebContentTest.java     26 Aug 2011
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
import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.qrmedia.commons.reflect.ReflectionUtils;
import com.xebialabs.deployit.deployment.planner.DefaultDelta;
import com.xebialabs.deployit.plugin.api.deployment.specification.Delta;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.generic.ci.Folder;
import com.xebialabs.deployit.plugin.generic.step.ArtifactCopyStep;
import com.xebialabs.deployit.plugin.generic.step.ArtifactDeleteStep;
import com.xebialabs.deployit.plugin.generic.step.TemplateArtifactCopyStep;
import com.xebialabs.deployit.plugins.generic.ext.step.CommandExecutionStep;
import com.xebialabs.deployit.plugins.webserver.TestBase;

/**
 * Unit tests for {@link PublishedWebContent}
 */
public class PublishedWebContentTest extends TestBase {

    @Test
    public void createsTargetDirectoryForVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        www.setVirtualHost("secret.agents:007");
        assertThat(www.isCreateTargetDirectory(), is(true));
    }
    
    @Test
    public void doesNotCreateTargetDirectoryWithoutVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        assertThat(www.isCreateTargetDirectory(), is(false));
    }
    
    @Test
    public void assumesDedicatedTargetDirectoryForVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        www.setVirtualHost("secret.agents:007");
        assertThat(www.isTargetDirectoryShared(), is(false));
    }
    
    @Test
    public void assumesSharedTargetDirectoryWithoutVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        assertThat(www.isTargetDirectoryShared(), is(true));
    }
    
    @Test
    public void usesVirtualHostDocrootAsTargetDirectoryForVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        www.setVirtualHost("secret.agents:007");
        www.setTargetDirectory("/default/folder");
        www.setVirtualHostDocumentRoot("/secret/folder");
        assertThat(www.getTargetDirectory(), is("/secret/folder"));
    }
    
    @Test
    public void usesDefinedTargetDirectoryWithoutVirtualHost() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        www.setTargetDirectory("/default/folder");
        assertThat(www.getTargetDirectory(), is("/default/folder"));
    }
    
    @Test
    public void createsNewWebContent() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        // no virtual host
        PublishedWebContent www = newWebContent();
        www.setTargetDirectory("/default/folder");
        www.executeCreate(capturingContext);
        assertThat(capturingContext.steps.size(), is(1));
        assertThat(capturingContext.steps.get(0), is(ArtifactCopyStep.class));
    }

    @Test
    public void destroysDeletedWebContent() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        // no virtual host
        PublishedWebContent www = newWebContent();
        www.setTargetDirectory("/default/folder");
        www.executeDestroy(capturingContext);
        assertThat(capturingContext.steps.size(), is(1));
        assertThat(capturingContext.steps.get(0), is(ArtifactDeleteStep.class));
    }
    
    @Test
    public void destroyCreatesModifiedWebContent() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        // no virtual host
        PublishedWebContent www = newWebContent();
        www.setTargetDirectory("/default/folder");
        www.executeModify(capturingContext);
        assertThat(capturingContext.steps.size(), is(2));
        assertThat(capturingContext.steps.get(0), is(ArtifactDeleteStep.class));
        assertThat(capturingContext.steps.get(1), is(ArtifactCopyStep.class));
    }
    
    @Test
    public void createsVirtualHostOnCreateIfSpecified() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        PublishedWebContent www = newWebContent();
        www.setVirtualHost("secret.agent:007");
        www.executeCreate(capturingContext);
        www.executeCreateVirtualHost(capturingContext);
        assertThat(capturingContext.steps.size(), is(3));
        // content, vhost, reload
        assertThat(capturingContext.steps.get(0), is(ArtifactCopyStep.class));
        assertThat(capturingContext.steps.get(1), is(TemplateArtifactCopyStep.class));
        assertThat(capturingContext.steps.get(2), is(CommandExecutionStep.class));
    }
    
    @Test
    public void destroysVirtualHostOnRemoveIfSpecified() throws IllegalAccessException {
        StubPlanningContext capturingContext = new StubPlanningContext();
        PublishedWebContent www = newWebContent();
        www.setVirtualHost("secret.agent:007");
        www.setVirtualHostDocumentRoot("/secret/folder");
        www.executeDestroy(capturingContext);
        www.executeDestroyVirtualHost(capturingContext);
        assertThat(capturingContext.steps.size(), is(3));
        // content, vhost, reload
        ArtifactDeleteStep deleteContentStep = (ArtifactDeleteStep) capturingContext.steps.get(0); 
        assertThat(deleteContentStep, is(ArtifactDeleteStep.class));
        assertThat(ReflectionUtils.<String>getValue(deleteContentStep, "targetDirectory"), 
                   is("/secret/folder"));
        assertThat(capturingContext.steps.get(1), is(ArtifactDeleteStep.class));
        assertThat(capturingContext.steps.get(2), is(CommandExecutionStep.class));
    }
    
    @Test
    public void createsNewVirtualHostOnModify() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        // no virtual host
        PublishedWebContent previous = newWebContent();
        PublishedWebContent www = newWebContent();
        www.setVirtualHost("secret.agent:007");
        PublishedWebContent.executeModifyVirtualHost(capturingContext, modify(previous, www));
        assertThat(capturingContext.steps.size(), is(2));
        // vhost, reload
        assertThat(capturingContext.steps.get(0), is(TemplateArtifactCopyStep.class));
        assertThat(capturingContext.steps.get(1), is(CommandExecutionStep.class));
    }
    
    @Test
    public void destroysOldVirtualHostOnModify() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        PublishedWebContent previous = newWebContent();
        previous.setVirtualHost("secret.agent:007");
        // no virtual host
        PublishedWebContent www = newWebContent();
        PublishedWebContent.executeModifyVirtualHost(capturingContext, modify(previous, www));
        assertThat(capturingContext.steps.size(), is(2));
        // vhost, reload
        assertThat(capturingContext.steps.get(0), is(ArtifactDeleteStep.class));
        assertThat(capturingContext.steps.get(1), is(CommandExecutionStep.class));
    }
    
    @Test
    public void modifiesChangedVirtualHostOnModify() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        PublishedWebContent previous = newWebContent();
        previous.setVirtualHost("secret.agent:007");
        PublishedWebContent www = newWebContent();
        www.setVirtualHost("secret.agent:008");
        PublishedWebContent.executeModifyVirtualHost(capturingContext, modify(previous, www));
        assertThat(capturingContext.steps.size(), is(3));
        // delete old vhost, create new vhost, reload
        assertThat(capturingContext.steps.get(0), is(ArtifactDeleteStep.class));
        assertThat(capturingContext.steps.get(1), is(TemplateArtifactCopyStep.class));
        assertThat(capturingContext.steps.get(2), is(CommandExecutionStep.class));
    }
    
    @Test
    public void ignoresUnchangedVirtualHostOnModify() {
        StubPlanningContext capturingContext = new StubPlanningContext();
        PublishedWebContent previous = newWebContent();
        previous.setVirtualHost("secret.agent:007");
        PublishedWebContent www = newWebContent();
        www.setVirtualHost("secret.agent:007");
        PublishedWebContent.executeModifyVirtualHost(capturingContext, modify(previous, www));
        assertThat(capturingContext.steps.size(), is(0));
    }
    
    private static PublishedWebContent newWebContent() {
        PublishedWebContent www = newInstance("www.PublishedWebContent2");
        Container webServer = newInstance("www.ApacheHttpdServer");
        webServer.setHost(newSshHost());
        webServer.setRestartScript("/etc/init.d/httpd restart");
        webServer.putSyntheticProperty("htdocsDirectory", "/var/httpd/www");
        www.setContainer(webServer);
        www.setDeployable(new Folder());
        return www;
    }
    
    private static Delta modify(PublishedWebContent previous, PublishedWebContent www) {
        return new DefaultDelta(MODIFY, previous, www);
    }
}
