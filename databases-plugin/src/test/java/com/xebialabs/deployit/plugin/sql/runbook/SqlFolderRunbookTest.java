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

package com.xebialabs.deployit.plugin.sql.runbook;

import com.xebialabs.deployit.Change;
import com.xebialabs.deployit.ChangePlan;
import com.xebialabs.deployit.ChangeResolution;
import com.xebialabs.deployit.Step;
import com.xebialabs.deployit.ci.*;
import com.xebialabs.deployit.ci.artifact.SqlFolder;
import com.xebialabs.deployit.ci.artifact.mapping.SqlFolderMapping;
import com.xebialabs.deployit.plugin.sql.ci.MSSQLDatabase;
import com.xebialabs.deployit.plugin.sql.ci.MySqlDatabase;
import com.xebialabs.deployit.plugin.sql.ci.OracleDatabase;
import com.xebialabs.deployit.steps.RunSqlScriptWithUploadedScriptOnDatabaseStep;
import com.xebialabs.deployit.test.support.stubs.StubChange;
import com.xebialabs.deployit.test.support.stubs.StubChangePlan;
import com.xebialabs.deployit.test.support.utils.RunBookTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class SqlFolderRunbookTest {

	private SqlFolderRunbook runbook;

	private Deployment deployment;
	private SqlFolderMapping mappingSqlFiles2Oracle;
	private MySqlDatabase mySqlDatabase;
	private OracleDatabase oracleDatabase;
	private Environment environment;
	private SqlFolderMapping mappingSqlFiles2MySql;
	private SqlFolder sqlFilesMock;
	private DeploymentPackage deploymentPackage;
	private SqlFolder sqlFilesComplex;
	private SqlFolderMapping mappingSqlFileComplex;
	private MSSQLDatabase mssqlDatabase;
	private Host windowsHost;

	private Host remoteHost;

	@Before
	public void setup() throws Exception {


		Host h = new Host();
		h.setAddress("10.10.14.11");
		h.setOperatingSystemFamily(OperatingSystemFamily.UNIX);

		windowsHost = new Host();
		windowsHost.setAddress("10.10.14.11");
		windowsHost.setOperatingSystemFamily(OperatingSystemFamily.WINDOWS);

		oracleDatabase = new OracleDatabase();
		oracleDatabase.setLabel("ORACLE");
		oracleDatabase.setHost(h);
		oracleDatabase.setUser("pcap_form");
		oracleDatabase.setPassword("pcap");
		oracleDatabase.setDatabase("HM");
		oracleDatabase.setPort(1522);
		oracleDatabase.setOracleHome("/usr/bin/oracle");

		mySqlDatabase = new MySqlDatabase();
		mySqlDatabase.setLabel("MYSQL");
		mySqlDatabase.setHost(h);
		mySqlDatabase.setUser("deployit");
		mySqlDatabase.setPassword("xebia2010");
		mySqlDatabase.setDatabase("deployit");

		mssqlDatabase = new MSSQLDatabase();
		mssqlDatabase.setLabel("MSSQL");
		mssqlDatabase.setHost(windowsHost);
		mssqlDatabase.setDatabase("ADB");
		mssqlDatabase.setServer("AWINServer");
		mssqlDatabase.setSqlcmdPath("c:\\MSSQL\\Binn");

		sqlFilesMock = new SqlFolder() {
			@Override
			public List<File> getFiles() {
				List<File> files = new ArrayList<File>();
				files.add(new File("01/01_a.sql"));
				files.add(new File("01/01_b.sql"));
				files.add(new File("01/02_a.sql"));
				files.add(new File("01/02_b.sql"));
				return files;
			}

		};
		sqlFilesMock.setLocation("sqlfiles");


		sqlFilesComplex = new SqlFolder();
		sqlFilesComplex.setLabel("SQLFiles");
		sqlFilesComplex.setLocation("src/test/resources/complex");

		deploymentPackage = new DeploymentPackage();

		deploymentPackage.setApplication(new Application());

		environment = new Environment();

		environment.addMember(h);

		deployment = new Deployment();
		deployment.setSource(deploymentPackage);
		deployment.setTarget(environment);

		mappingSqlFiles2Oracle = new SqlFolderMapping();
		mappingSqlFiles2Oracle.setLabel("Mapping to Oracle");
		mappingSqlFiles2Oracle.setSource(sqlFilesMock);
		mappingSqlFiles2Oracle.setTarget(oracleDatabase);


		mappingSqlFiles2MySql = new SqlFolderMapping();
		mappingSqlFiles2MySql.setLabel("Mappgin to MySQL");
		mappingSqlFiles2MySql.setSource(sqlFilesMock);
		mappingSqlFiles2MySql.setTarget(mySqlDatabase);

		mappingSqlFileComplex = new SqlFolderMapping();
		mappingSqlFileComplex.setLabel("Mappgin to MySQL");
		mappingSqlFileComplex.setSource(sqlFilesComplex);
		mappingSqlFileComplex.setTarget(mySqlDatabase);


		remoteHost = new Host();
		remoteHost.setLabel("Remote Host for MySQL");
		remoteHost.setAddress("remoteHost.local");
		remoteHost.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
		remoteHost.setAccessMethod(HostAccessMethod.LOCAL);
		runbook = new SqlFolderRunbook();

	}

	@Test
	public void testMSSQLCommand() throws Exception {

		final String errorLevel = "EXIT %ERRORLEVEL%";

		assertContains(mssqlDatabase.getCommand(), "c:\\MSSQL\\Binn\\SQLCMD.exe -S AWINServer -d ADB   -i %1");
		assertContains(mssqlDatabase.getCommand(), errorLevel);

		mssqlDatabase.setUser("scott");
		assertContains(mssqlDatabase.getCommand(), "c:\\MSSQL\\Binn\\SQLCMD.exe -S AWINServer -d ADB -U scott  -i %1");
		assertContains(mssqlDatabase.getCommand(), errorLevel);

		mssqlDatabase.setPassword("tiger");
		assertContains(mssqlDatabase.getCommand(), "c:\\MSSQL\\Binn\\SQLCMD.exe -S AWINServer -d ADB -U scott -P tiger -i %1");
		assertContains(mssqlDatabase.getCommand(), errorLevel);

		mssqlDatabase.setSqlcmdPath(null);
		assertContains(mssqlDatabase.getCommand(), "SQLCMD.exe -S AWINServer -d ADB -U scott -P tiger -i %1");
		assertContains(mssqlDatabase.getCommand(), errorLevel);
	}

	@Test
	public void testOracleDatabaseCommandUnix() throws Exception {
		assertContains(oracleDatabase.getCommand(), "export ORACLE_HOME=/usr/bin/oracle");
        assertContains(oracleDatabase.getCommand(), "export ORACLE_SID=HM");
		assertContains(oracleDatabase.getCommand(), "/usr/bin/oracle/bin/sqlplus pcap_form/pcap@HM @$1");
	}

	@Test
	public void testOracleDatabaseCommandWindows() throws Exception {
		oracleDatabase.setHost(windowsHost);
		oracleDatabase.setOracleHome("d:\\program\\oracle");
		assertContains(oracleDatabase.getCommand(), "set ORACLE_HOME=d:\\program\\oracle");
        assertContains(oracleDatabase.getCommand(), "set ORACLE_SID=HM");
        assertContains(oracleDatabase.getCommand(), "d:\\program\\oracle\\bin\\sqlplus pcap_form/pcap@HM @%1");

    }
    
	@Test
	public void testMySqlDatabaseCommandUnix() throws Exception {
		assertContains(mySqlDatabase.getCommand(), "mysql deployit -udeployit -pxebia2010 -P3306 < $1");
		mySqlDatabase.setMysqlPath("/usr/bin");
		assertContains(mySqlDatabase.getCommand(), "/usr/bin/mysql deployit -udeployit -pxebia2010 -P3306 < $1");
	}

	@Test
	public void testMySqlDatabaseWithHostCommandUnix() throws Exception {
		assertContains(mySqlDatabase.getCommand(), "mysql deployit -udeployit -pxebia2010 -P3306 < $1");
		mySqlDatabase.setMysqlPath("/usr/bin");

		mySqlDatabase.setTargetHost(remoteHost);
		assertContains(mySqlDatabase.getCommand(), "/usr/bin/mysql deployit -hremoteHost.local -udeployit -pxebia2010 -P3306 < $1");
	}

	@Test
	public void testMySqlDatabaseCommandWindows() throws Exception {
		mySqlDatabase.setHost(windowsHost);
		assertContains(mySqlDatabase.getCommand(), "mysql deployit -udeployit -pxebia2010 -P3306 < %1");
		final String errorLevel = "EXIT %ERRORLEVEL%";
		assertContains(mssqlDatabase.getCommand(), errorLevel);

		mySqlDatabase.setMysqlPath("c:\\mysql\\bin");
		assertContains(mySqlDatabase.getCommand(), "c:\\mysql\\bin\\mysql deployit -udeployit -pxebia2010 -P3306 < %1");
		assertContains(mssqlDatabase.getCommand(), errorLevel);


		mySqlDatabase.setTargetHost(remoteHost);
		assertContains(mySqlDatabase.getCommand(), "c:\\mysql\\bin\\mysql deployit -hremoteHost.local -udeployit -pxebia2010 -P3306 < %1");

		mySqlDatabase.setPort(1234);
		assertContains(mySqlDatabase.getCommand(), "c:\\mysql\\bin\\mysql deployit -hremoteHost.local -udeployit -pxebia2010 -P1234 < %1");
	}

	private void assertContains(final String command, String expected) {
        assertThat(command, containsString(expected));
	}

	private List<Step> deploy() {
		Change<Deployment> deploymentChange = new StubChange<Deployment>(null, deployment);
		ChangePlan cp = new StubChangePlan(deploymentChange);
		Collection<ChangeResolution> crs = runbook.resolve(cp);
		List<Step> steps = RunBookTestUtils.assertOneResolutionAndGetItsSteps(crs);
		return steps;
	}

	@Test
	public void testOracle() throws Exception {
		environment.addMember(oracleDatabase);
		deployment.addMapping(mappingSqlFiles2Oracle);
		deploymentPackage.addDeployableArtifact(sqlFilesMock);

		List<Step> steps = deploy();
		RunBookTestUtils.assertTypeSequence(steps, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class);
	}


	@Test
	public void testMySql() throws Exception {
		environment.addMember(mySqlDatabase);
		deployment.addMapping(mappingSqlFiles2MySql);
		deploymentPackage.addDeployableArtifact(sqlFilesMock);

		List<Step> steps = deploy();
		RunBookTestUtils.assertTypeSequence(steps, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class);
	}

	@Test
	public void testMySqlAndOracle() throws Exception {

		environment.addMember(mySqlDatabase);
		environment.addMember(oracleDatabase);

		deploymentPackage.addDeployableArtifact(sqlFilesMock);

		deployment.addMapping(mappingSqlFiles2MySql);
		deployment.addMapping(mappingSqlFiles2Oracle);

		List<Step> steps = deploy();
		RunBookTestUtils.assertTypeSequence(steps, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class, RunSqlScriptWithUploadedScriptOnDatabaseStep.class);

	}

	@Test
	public void testComplex() throws Exception {
		environment.addMember(mySqlDatabase);
		deployment.addMapping(mappingSqlFileComplex);
		deploymentPackage.addDeployableArtifact(sqlFilesComplex);

		List<Step> steps = deploy();
		final List<RunSqlScriptWithUploadedScriptOnDatabaseStep> stepsOfClass = RunBookTestUtils.getStepsOfClass(steps, RunSqlScriptWithUploadedScriptOnDatabaseStep.class);
		Assert.assertEquals(6, stepsOfClass.size());
		for (RunSqlScriptWithUploadedScriptOnDatabaseStep step : stepsOfClass)
			System.out.println("step " + step.getDescription());
		final List<RunSqlScriptWithUploadedScriptOnDatabaseStep> withUploadedScriptOnDatabaseSteps = RunBookTestUtils.getStepsOfClass(steps, RunSqlScriptWithUploadedScriptOnDatabaseStep.class);
		assertEquals(6, withUploadedScriptOnDatabaseSteps.size());
	}


}