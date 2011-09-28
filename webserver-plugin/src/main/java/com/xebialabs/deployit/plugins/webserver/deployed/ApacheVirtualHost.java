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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xebialabs.deployit.plugin.api.udm.Metadata;

@SuppressWarnings("serial")
@Metadata(virtual = true, description = "An Apache virtual host definition deployed to a www.ApacheHttpdServer")
public class ApacheVirtualHost extends ApacheConfFragment {
    static final String HOST_PROPERTY = "host";
    static final String PORT_PROPERTY = "port";
    static final String DOCROOT_PROPERTY = "documentRoot";
    
    private static final String SERVER_DOCROOT_PROPERTY = "htdocsDirectory";

    public String getDocumentRoot() {
        String documentRoot = nullToEmpty((String) getSyntheticProperty(DOCROOT_PROPERTY));
        return (!documentRoot.isEmpty() 
                ? documentRoot
                : getContainer().getSyntheticProperty(SERVER_DOCROOT_PROPERTY) 
                  + getContainer().getHost().getOs().getFileSeparator() 
                  + getDeployable().getName());
    }

    @Override
    protected String getDescription(String verb) {
        return format("%s virtual host '%s' in %s (configuration file '%s')", verb, getHostAndPort(), 
                getContainer().getName(), resolveTargetFileName());
    }

    protected HostAndPort getHostAndPort() {
        return new HostAndPort((String) getSyntheticProperty(HOST_PROPERTY), 
                (String) getSyntheticProperty(PORT_PROPERTY));
    }

    static class HostAndPort {
        private static final Pattern HOST_PORT_PATTERN = Pattern.compile("(?:(\\S+|\\*)):(?:(\\d{1,5}+|\\*))");
        private static final String HOST_PORT_FORMAT = "%s:%s";
        
        private final String host;
        private final String port;
        private final String filenameFriendlyHost;
        private final String filenameFriendlyPort;

        public HostAndPort(String host, String port) {
            this(format(HOST_PORT_FORMAT, host, port));
        }
        
        public HostAndPort(String hostAndPort) {
            checkArgument(hostAndPort != null, "Argument must be non-null");
            Matcher hostAndPortMatcher = HOST_PORT_PATTERN.matcher(hostAndPort);
            checkArgument(isValidHostAndPort(hostAndPortMatcher), 
                    "'%s' does not match the pattern <hostname>:<port>", hostAndPort);
            host = hostAndPortMatcher.group(1);
            port = hostAndPortMatcher.group(2);
            filenameFriendlyHost = toFilenameFriendlyName(host, "allhosts");
            filenameFriendlyPort = toFilenameFriendlyName(port, "allports");
        }
        
        private static String toFilenameFriendlyName(String value, String replacement) {
            return value.equals("*") ? replacement : value;
        }

        
        private static boolean isValidHostAndPort(Matcher hostAndPortMatcher) {
            return hostAndPortMatcher.matches();
        }

        @Override
        public String toString() {
            return format(HOST_PORT_FORMAT, host, port);
        }

        public String getHost() {
            return host;
        }

        public String getPort() {
            return port;
        }

        public String getFilenameFriendlyHost() {
            return filenameFriendlyHost;
        }

        public String getFilenameFriendlyPort() {
            return filenameFriendlyPort;
        }
    }
}
