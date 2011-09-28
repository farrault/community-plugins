/*
 * Copyright (c) 2008-2011 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.deployit.plugins.webserver.deployed;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.Lists;
import com.xebialabs.deployit.plugins.webserver.deployed.ApacheVirtualHost.HostAndPort;

/**
 * Unit tests for {@link HostAndPort}.
 */
@RunWith(Parameterized.class)
public class HostAndPortTest {
    private final String hostAndPort;
    private final boolean validHostAndPort;
    private final String expectedHost;
    private final String expectedPort;
    private final String expectedFilenameFriendlyHost;
    private final String expectedFilenameFriendlyPort;

    @Parameters
    public static List<Object[]> parameters() {
        List<Object[]> params = Lists.newArrayList();
        params.add(new Object[] { null, false, null, null, null, null });
        params.add(new Object[] { "", false, null, null, null, null });
        params.add(new Object[] { "missing-colon", false, null, null, null, null });
        params.add(new Object[] { ":", false, null, null, null, null });
        params.add(new Object[] { "missing-port:", false, null, null, null, null });
        params.add(new Object[] { ":missing-host", false, null, null, null, null });
        params.add(new Object[] { "valid.host:invalid-port", false, null, null, null, null });
        params.add(new Object[] { "valid.host:100000", false, null, null, null, null });

        params.add(new Object[] { "valid-host:1111", true, "valid-host",
                "1111", "valid-host", "1111" });
        params.add(new Object[] { "valid-host:99999", true, "valid-host",
                "99999", "valid-host", "99999" });
        params.add(new Object[] { "valid.host:1111", true, "valid.host",
                "1111", "valid.host", "1111" });
        params.add(new Object[] { "valid.host:99999", true, "valid.host",
                "99999", "valid.host", "99999" });
        params.add(new Object[] { "valid.host:*", true, "valid.host", "*",
                "valid.host", "allports" });
        params.add(new Object[] { "*:99999", true, "*", "99999", "allhosts",
                "99999" });
        return params;
    }

    public HostAndPortTest(String hostAndPort, boolean validHostAndPort,
            String expectedHost, String expectedPort,
            String expectedFilenameFriendlyHost,
            String expectedFilenameFriendlyPort) {
        this.hostAndPort = hostAndPort;
        this.validHostAndPort = validHostAndPort;
        this.expectedHost = expectedHost;
        this.expectedPort = expectedPort;
        this.expectedFilenameFriendlyHost = expectedFilenameFriendlyHost;
        this.expectedFilenameFriendlyPort = expectedFilenameFriendlyPort;
    }

    @Test
    public void invalidHostAndPortThrowsException() {
        if (!validHostAndPort) {
            try {
                new HostAndPort(hostAndPort);
                fail(format("Expected an IllegalArgumentException to be thrown for '%s'", hostAndPort));
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    @Test
    public void validHostAndPortExtractsHost() {
        if (validHostAndPort) {
            assertEquals(expectedHost, new HostAndPort(hostAndPort).getHost());
        }
    }

    @Test
    public void validHostAndPortExtractsPort() {
        if (validHostAndPort) {
            assertEquals(expectedPort, new HostAndPort(hostAndPort).getPort());
        }
    }

    @Test
    public void wildcardHostIsConvertedToFilenameFriendlyHost() {
        if (validHostAndPort) {
            assertEquals(expectedFilenameFriendlyHost, 
                    new HostAndPort(hostAndPort).getFilenameFriendlyHost());
        }
    }

    @Test
    public void wildcardPortIsConvertedToFilenameFriendlyPort() {
        if (validHostAndPort) {
            assertEquals(expectedFilenameFriendlyPort, 
                    new HostAndPort(hostAndPort).getFilenameFriendlyPort());
        }
    }
}
