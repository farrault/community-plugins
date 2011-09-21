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
import com.xebialabs.deployit.ci.artifact.mapping.PlaceholderFormat;
import com.xebialabs.deployit.ci.artifact.mapping.SqlFolderMapping;
import com.xebialabs.deployit.ci.mapping.KeyValuePair;
import com.xebialabs.deployit.plugin.sql.ci.MSSQLDatabase;
import com.xebialabs.deployit.plugin.sql.ci.MySqlDatabase;
import com.xebialabs.deployit.plugin.sql.ci.OracleDatabase;
import com.xebialabs.deployit.test.support.stubs.StubChange;
import com.xebialabs.deployit.test.support.stubs.StubChangePlan;
import com.xebialabs.deployit.test.support.utils.DebugStepExecutionContext;
import com.xebialabs.deployit.test.support.utils.RunBookTestUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

public class SqlFolderRunbookItest {

	private SqlFolderRunbook runbook;

	private Deployment deployment;
	private DebugStepExecutionContext context;
	private OracleDatabase oracleDatabase;
	private MySqlDatabase mySqlDatabase;
	private Environment environment;
	private DeploymentPackage dpackage;
	private MSSQLDatabase mssqlDatabase;
	private Host windowsHost;
	private OracleDatabase oracleDatabaseWin;

	@Before
	public void setup() throws Exception {

		Host host = new Host();
		host.setAddress("ubuntu-oracle.local");
		host.setAccessMethod(HostAccessMethod.SSH_SFTP);
		host.setUsername("ubuntu");
		host.setPassword("ubuntu");
		host.setOperatingSystemFamily(OperatingSystemFamily.UNIX);


		windowsHost = new Host();
		windowsHost.setLabel("WLS 11g Windows Host");
		windowsHost.setAddress("win-4yk1f6r5qps");
		//windowsHost.setAccessMethod(HostAccessMethod.CIFS_TELNET);
		windowsHost.setAccessMethod(HostAccessMethod.SSH_SFTP);
		windowsHost.setOperatingSystemFamily(OperatingSystemFamily.WINDOWS);
		windowsHost.setUsername("Administrator");
		windowsHost.setPassword("deployit");


		oracleDatabase = new OracleDatabase();
		oracleDatabase.setLabel("Oracle");
		oracleDatabase.setHost(host);
		oracleDatabase.setUser("HR");
		oracleDatabase.setPassword("HR");
		oracleDatabase.setDatabase("XE");
		oracleDatabase.setPort(1521);
		oracleDatabase.setOracleHome("/usr/lib/oracle/xe/app/oracle/product/10.2.0/server");

		oracleDatabaseWin = new OracleDatabase();
		oracleDatabaseWin.setLabel("Oracle Windows XE");
		oracleDatabaseWin.setHost(windowsHost);
		oracleDatabaseWin.setUser("DEPLOYIT");
		oracleDatabaseWin.setPassword("DEPLOYIT");
		oracleDatabaseWin.setDatabase("XE");
		oracleDatabaseWin.setPort(1521);
		oracleDatabaseWin.setOracleHome("C:\\oraclexe\\app\\oracle\\product\\10.2.0\\server");


		mySqlDatabase = new MySqlDatabase();
		mySqlDatabase.setLabel("Mysql");
		mySqlDatabase.setHost(host);
		mySqlDatabase.setUser("deployit");
		mySqlDatabase.setPassword("xebia2010");
		mySqlDatabase.setDatabase("deployit");


		mssqlDatabase = new MSSQLDatabase();
		mssqlDatabase.setLabel("MSSQL");
		mssqlDatabase.setHost(windowsHost);
		mssqlDatabase.setDatabase("ADB");
		mssqlDatabase.setServer("AWINServer");


		environment = new Environment();
		environment.addMember(host);

		runbook = new SqlFolderRunbook();
		context = new DebugStepExecutionContext();

		dpackage = new DeploymentPackage();
		dpackage.setApplication(new Application());

		deployment = new Deployment();
		deployment.setSource(dpackage);
		deployment.setTarget(environment);

	}

	@After
	public void clean() {
		if (context != null)
			context.destroy();
	}

	@Test
	public void testOracle() throws Exception {

		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sqltest-oracle");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, oracleDatabase);

		sqlFilesMapping.setLabel("Mapping to Oracle");
		deployment.addMapping(sqlFilesMapping);
		environment.addMember(oracleDatabase);

		deployit();
	}

	@Test
	public void testOracleWithReplacementDollar() throws Exception {
		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sql-replacement/dollar");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, oracleDatabase);

		sqlFilesMapping.setLabel("Mapping to Oracle");
		sqlFilesMapping.addKeyValuePair(new KeyValuePair("BD_BASE_APPLI", "DUAL"));

		deployment.addMapping(sqlFilesMapping);
		environment.addMember(oracleDatabase);

		deployit();
	}


	@Test
	public void testOracleWithReplacementPercent() throws Exception {
		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sql-replacement/percent");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, oracleDatabase);
		sqlFilesMapping.setPlaceholderFormat(PlaceholderFormat.WINDOWS_SHELL);

		sqlFilesMapping.setLabel("Mapping to Oracle");
		sqlFilesMapping.addKeyValuePair(new KeyValuePair("BD_BASE_APPLI", "DUAL"));

		deployment.addMapping(sqlFilesMapping);
		environment.addMember(oracleDatabase);

		deployit();
	}


	@Test
	public void testOracleWithReplacementPercentOnWindows() throws Exception {
		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sql-replacement/percent");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, oracleDatabaseWin);
		sqlFilesMapping.setPlaceholderFormat(PlaceholderFormat.WINDOWS_SHELL);

		sqlFilesMapping.setLabel("Mapping to Oracle");
		sqlFilesMapping.addKeyValuePair(new KeyValuePair("BD_BASE_APPLI", "DUAL"));

		deployment.addMapping(sqlFilesMapping);
		environment.addMember(oracleDatabaseWin);

		deployit();
	}


	@Test
	@Ignore
	public void testMSSQL() throws Exception {

		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sqltest-oracle");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, mssqlDatabase);
		sqlFilesMapping.setLabel("Mapping to MSSQL");
		deployment.addMapping(sqlFilesMapping);
		environment.addMember(mssqlDatabase);

		deployit();
	}


	@Test
	public void testMySql() throws Exception {

		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sqltest-mysql");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, mySqlDatabase);
		sqlFilesMapping.setLabel("Mapping to MySql");
		deployment.addMapping(sqlFilesMapping);
		environment.addMember(mySqlDatabase);

		deployit();
	}


	@Test
	public void testMySqAndOracle() throws Exception {

		SqlFolder mysqlSQLFiles = new SqlFolder();
		mysqlSQLFiles.setLabel("SQLFiles");
		mysqlSQLFiles.setLocation("src/test/resources/sqltest-mysql");

		SqlFolder oracleSqlFiles = new SqlFolder();
		oracleSqlFiles.setLabel("Oracle");
		oracleSqlFiles.setLocation("src/test/resources/sqltest-oracle");


		dpackage.addDeployableArtifact(mysqlSQLFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(mysqlSQLFiles, mySqlDatabase);
		sqlFilesMapping.setLabel("Mapping to MySql");
		deployment.addMapping(sqlFilesMapping);


		dpackage.addDeployableArtifact(oracleSqlFiles);
		final SqlFolderMapping sqlFilesOracleMapping = new SqlFolderMapping(oracleSqlFiles, oracleDatabase);
		sqlFilesOracleMapping.setLabel("Mapping to Oracle");
		deployment.addMapping(sqlFilesOracleMapping);

		environment.addMember(mySqlDatabase);
		environment.addMember(oracleDatabase);

		deployit();
	}

	@Test(expected = RuntimeException.class)
	public void testFailOracle() throws Exception {
		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sqltest-oracle-errors");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, oracleDatabase);
		sqlFilesMapping.setLabel("Mapping to Oracle");
		deployment.addMapping(sqlFilesMapping);
		environment.addMember(oracleDatabase);

		deployit();
	}

	@Test(expected = RuntimeException.class)
	public void testFailMySql() throws Exception {

		SqlFolder sqlFiles = new SqlFolder();
		sqlFiles.setLabel("SQLFiles");
		sqlFiles.setLocation("src/test/resources/sqltest-mysql-errors");
		dpackage.addDeployableArtifact(sqlFiles);
		final SqlFolderMapping sqlFilesMapping = new SqlFolderMapping(sqlFiles, mySqlDatabase);
		sqlFilesMapping.setLabel("Mapping to MySql");
		deployment.addMapping(sqlFilesMapping);
		environment.addMember(mySqlDatabase);

		deployit();

	}

	private void deployit() {
		Change<Deployment> deploymentChange = new StubChange<Deployment>(null, deployment);

		ChangePlan cp = new StubChangePlan(deploymentChange);
		Collection<ChangeResolution> crs = runbook.resolve(cp);
		List<Step> steps = RunBookTestUtils.assertOneResolutionAndGetItsSteps(crs);


		long l = System.currentTimeMillis();
		for (Step s : steps) {
			String description = s.getDescription();
			logger.info(description);
			if (description.contains(".svn"))
				continue;

			s.execute(context);
		}
		long ll = System.currentTimeMillis();

		System.out.println("Total --- " + (ll - l));
	}

	private static Logger logger = Logger.getLogger(SqlFolderRunbookItest.class);

}