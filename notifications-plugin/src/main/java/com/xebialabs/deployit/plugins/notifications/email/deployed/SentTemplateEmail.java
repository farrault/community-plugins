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

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugins.notifications.email.ci.MailServer;
import com.xebialabs.deployit.plugins.notifications.email.step.EmailSendStep;
import com.xebialabs.deployit.plugins.notifications.email.step.TemplateEmailSendStep;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "A template email sent via a notify.MailServer")
public class SentTemplateEmail extends SentEmail {

    @Override
    protected EmailSendStep getEmailSendStep() {
         return new TemplateEmailSendStep(getCreateOrder(), getDescription(getCreateVerb()), 
                (MailServer) getContainer(), getFromAddress(), getToAddresses(), 
                getCcAddresses(), getBccAddresses(), getSubject(), 
                getDeployedAsFreeMarkerContext(), getTemplate());
    }
}
