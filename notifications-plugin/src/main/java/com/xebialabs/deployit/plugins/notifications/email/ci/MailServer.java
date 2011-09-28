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
package com.xebialabs.deployit.plugins.notifications.email.ci;

import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;

import com.xebialabs.deployit.plugin.api.udm.Metadata;
import com.xebialabs.deployit.plugin.generic.ci.Container;

@SuppressWarnings("serial")
@Metadata(description = "An email server")
public class MailServer extends Container {
    private static final String SMTP_HOST_PROPERTY = "smtpHost";
    private static final String SMTP_PORT_PROPERTY = "smtpPort";
    private static final String SMTP_USERNAME_PROPERTY = "smtpUsername";
    private static final String SMTP_PASSWORD_PROPERTY = "smtpPassword";
    private static final String SMTP_TRANSPORT_PROPERTY = "smtpTransport";
    
    public Mailer getMailer() {
        return new Mailer(this.<String>getSyntheticProperty(SMTP_HOST_PROPERTY),
                this.<Integer>getSyntheticProperty(SMTP_PORT_PROPERTY),
                this.<String>getSyntheticProperty(SMTP_USERNAME_PROPERTY),
                this.<String>getSyntheticProperty(SMTP_PASSWORD_PROPERTY),
                this.<TransportStrategy>getSyntheticProperty(SMTP_TRANSPORT_PROPERTY));
    }
}