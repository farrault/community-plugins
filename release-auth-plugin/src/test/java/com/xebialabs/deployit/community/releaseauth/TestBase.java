package com.xebialabs.deployit.community.releaseauth;

import org.junit.BeforeClass;

import com.xebialabs.deployit.plugin.api.boot.PluginBooter;

public abstract class TestBase {

    @BeforeClass
    public static void boot() {
        PluginBooter.bootWithoutGlobalContext();
    }
}
