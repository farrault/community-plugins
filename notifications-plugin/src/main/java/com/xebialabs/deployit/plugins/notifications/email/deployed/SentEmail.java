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
package com.xebialabs.deployit.plugins.notifications.email.deployed;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.xebialabs.deployit.plugin.api.deployment.planning.DeploymentPlanningContext;
import com.xebialabs.deployit.plugin.api.udm.DeployedApplication;
import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Resource;
import com.xebialabs.deployit.plugins.generic.ext.deployed.TemplatePropertyResolvingProcessedTemplate;
import com.xebialabs.deployit.plugins.notifications.email.ci.MailServer;
import com.xebialabs.deployit.plugins.notifications.email.step.LiteralEmailSendStep;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An email sent via a notify.MailServer")
public class SentEmail extends TemplatePropertyResolvingProcessedTemplate<Resource> {
    private static final String SUBJECT_PROPERTY = "Subject";
    private static final String FROM_PROPERTY = "From";
    private static final String TO_PROPERTY = "To";
    private static final String CC_PROPERTY = "Cc";
    private static final String BCC_PROPERTY = "Bcc";
    private static final String BODY_PROPERTY = "Body";
    private static final String ADDRESS_SEPARATOR = ",";
    
    private DeployedApplication deployedApplication;
    
    @Override
    public void executeCreate(DeploymentPlanningContext ctx) {
        ctx.addStep(new LiteralEmailSendStep(getCreateOrder(), getDescription(getCreateVerb()), 
                (MailServer) getContainer(), getFromAddress(), getToAddresses(), 
                getCcAddresses(), getBccAddresses(), getSubject(), 
                resolveExpression(this.<String>getSyntheticProperty(BODY_PROPERTY))));
    }

    @Override
    public void executeDestroy(DeploymentPlanningContext ctx) {
        // not supported
    }
    
    protected String getSubject() {
        return nullToEmpty(this.<String>getSyntheticProperty(SUBJECT_PROPERTY));
    }
    
    protected String getFromAddress() {
        return getSyntheticProperty(FROM_PROPERTY);
    }
    
    protected List<String> getToAddresses() {
        return splitAddresses(this.<String>getSyntheticProperty(TO_PROPERTY));
    }

    protected List<String> getCcAddresses() {
        return splitAddresses(this.<String>getSyntheticProperty(CC_PROPERTY));
    }
    
    protected List<String> getBccAddresses() {
        return splitAddresses(this.<String>getSyntheticProperty(BCC_PROPERTY));
    }
    
    private static List<String> splitAddresses(String commaSeparatedAddresses) {
        return ((commaSeparatedAddresses != null)
                ? ImmutableList.copyOf(commaSeparatedAddresses.split(ADDRESS_SEPARATOR))
                : ImmutableList.<String>of());
    }

    @Override
    protected String getDescription(String verb) {
        return format("%s email '%s' to: %s (cc: %s, bcc: %s)", verb, 
                getSyntheticProperty(SUBJECT_PROPERTY), 
                nullToEmpty(this.<String>getSyntheticProperty(TO_PROPERTY)), 
                nullToEmpty(this.<String>getSyntheticProperty(CC_PROPERTY)), 
                nullToEmpty(this.<String>getSyntheticProperty(BCC_PROPERTY)));
    }

    // short name for user convenience
    public DeployedApplication getApp() {
        checkState(deployedApplication != null, "'getApp' should not be called before the application has been set");
        return deployedApplication;
    }

    public void setDeployedApplication(DeployedApplication deployedApplication) {
        this.deployedApplication = deployedApplication;
    }
}
