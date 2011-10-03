package com.xebialabs.deployit.plugins.database;

import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;
import com.xebialabs.deployit.plugin.api.boot.PluginBooter;
import com.xebialabs.deployit.plugin.generic.ci.Container;
import com.xebialabs.deployit.plugin.overthere.Host;
import com.xebialabs.deployit.test.deployment.DeployitTester;
import com.xebialabs.itest.ItestHost;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import static com.xebialabs.deployit.test.support.TestUtils.newInstance;
import static com.xebialabs.itest.ItestHostFactory.getItestHost;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

public class OracleSqlClientItest {

    protected static ItestHost ec2host;
    protected static DeployitTester tester;

    protected Host host;
    protected Container container;


    @BeforeClass
    public static void setupEc2Host() {
        PluginBooter.bootWithoutGlobalContext();
        tester = DeployitTester.build();

        ec2host = getItestHost("ora-unix");
        ec2host.setup();
    }

    @AfterClass
    public static void teardownEc2Host() {
        ec2host.teardown();
    }

    @Before
    public void setup() throws IOException {
        host = newInstance("overthere.SshHost");
        host.setId("Infrastructure/ora-unix");
        host.setOs(UNIX);
        host.putSyntheticProperty(CONNECTION_TYPE, SFTP);
        host.putSyntheticProperty(ADDRESS, ec2host.getHostName());
        host.putSyntheticProperty(USERNAME, "root");
        host.putSyntheticProperty("privateKeyFile", createPrivateKeyFile(privateKey).getPath());

        container = newInstance("sql.OracleSqlClient");
        container.setId("/Infrastructure/itestServer");
        container.setHost(host);
        container.putSyntheticProperty("oraHome", "/var/www/html");
    }

    static File createPrivateKeyFile(String privateKey) throws IOException {
        final File privateKeyFile = File.createTempFile("private", ".key");
        privateKeyFile.deleteOnExit();
        CharStreams.write(privateKey, new OutputSupplier<Writer>() {
            @Override
            public Writer getOutput() throws IOException {
                return new FileWriter(privateKeyFile);
            }
        });
        return privateKeyFile;
    }

    private static String privateKey = "-----BEGIN RSA PRIVATE KEY-----\r\n"
            + "MIIEowIBAAKCAQEAiUwHySbCysbvnk8fofMsfjmpaWfBa2je7sh8EtgsJPZsgeFA4KvO0fNVtTaf\r\n"
            + "kWncvGJ1kOvyPXl9ToifpqTlNyZUJX1KqqTZfBeHvFdCUvIwedWVjJKaOeCwrSWAMGhqWEOH7g2v\r\n"
            + "8GJzCCYZXQqNTRgdZHTM6P2jhTPiCjR0oI+cI6YMmbg/f7ZNAw9BiCMPWlT9MjVW3obdduVOUwwU\r\n"
            + "YrgWUweXVP9llIPqWrjmpcSWifBRy3vQpxPI0+uLoLP/bxX96YagGQ4Cd74wr27jTD7ayfi/T/Ee\r\n"
            + "vGbggaq7zsejFNWmH6ISypEeu+thLwxn6rh1XbeHsg40hYVC7Vyv4wIDAQABAoIBAEh5C7sQbM5h\r\n"
            + "CGdGWOpB1ICkq1pqXFz4NIVS6rt/xH2WXlyIrJhr2HZWvi0zsjMt8Ei4qFphUbNFh/GGiM+MRzo/\r\n"
            + "Tzei1WESN4MbYJj4bpgeI5yMM67KTAK1Kk2bd/kVhN0meIAeVXrMXPA2PDkysre5PPqj9O4fxMsx\r\n"
            + "QeYlHlMJ889IRgOyT5LcZjNTb+U1rXFUy/+kapxYgTOl0kBxLdP/dmPIGHIv8UMDkeLmNINf1z/z\r\n"
            + "3wOExOz5Gq6A2gtElGWZ0ZQs7yRytRvFQer4QXcHrxOaXfCm0cz2M+cCGDhWPY1LTHS8zwjwQv27\r\n"
            + "cbiUd6YY/2Z4CzycTZnbjTyDKoECgYEA1TPDyFYdz05MrLnMJ+JfxSUjL8m2TogV2bQAUs4d8N8j\r\n"
            + "BomT7NGGkXPLO7cqQBMGI0Y/WATjlJOyBtus4Ny0XXvM0e/kMZ8cStUXWU2IA11ARRPyDzu0aNa2\r\n"
            + "9YYbm/b4j4E2HpeUF9oS12sKLaGC36RtekE9ChxJY8UV2yjgcKECgYEApNuTyT6c+Xi0pAhdnPrt\r\n"
            + "Op2byr2rKkETkblmYcHpV2jnFPForr6NPgQ9VIgOxBpTkq/X4N8XN7JdsK+YAxuCArLBT79VHry1\r\n"
            + "kocPtBUHLjPdyWJOIcvydE3Z8bj80JWeZjW9JYYfCCSjyG7zWbzDcaugHZ1VZz8hq7DP+Ao4ngMC\r\n"
            + "gYBB5IjK/wqhiqKZ86aMYSOWS78PQvlsVhTivwYmkXuheWVa3ORyGePMSoxyfU66lOadulVTf9dS\r\n"
            + "kT9BbV2F9dBs4BlSfSD60SEuY6Oevx6dY5G8h8iVOq+sg0fypCseTftOZvHyDIkBwi12lKeFqNhJ\r\n"
            + "BImtckJKQKnSAxSZMo4DYQKBgQCjUsIb6sMbSCE2LO+JWPLzUjeI6NUNPIGFqjaq/LAOn+fnUK8U\r\n"
            + "B8XoPc0A1PZEA4zuUvU9W+clj7jQFXY1BeiMgcmQFw7eL7h23QWKtBZ0CIBeRd0AEIw3+vTDTDBy\r\n"
            + "+Pd/bRfhd/mAWMRGCt1d9uttzskG7dsxOVNDRB0VMBMdPwKBgC+Th7KlUecbKmxhuK5iFclW7s4y\r\n"
            + "l7Sdzalfk/YxDIjBZM1QDbinOBnSSfCn66uwplimhWxWKCS8oSrzPJ9+AYsxHyNP3CMD63j+bqUY\r\n"
            + "Q5+CG4695FSxGZdu4QQHFhKedfyo6yZIVmK15F/V+ivnqSpoU8buxCCpGafOl8eqnNLO\r\n" + "-----END RSA PRIVATE KEY-----";


}
