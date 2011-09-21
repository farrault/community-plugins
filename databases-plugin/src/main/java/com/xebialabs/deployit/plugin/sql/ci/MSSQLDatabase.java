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

package com.xebialabs.deployit.plugin.sql.ci;

import com.xebialabs.deployit.ConfigurationItem;
import com.xebialabs.deployit.ConfigurationItemProperty;
import com.xebialabs.deployit.ci.Database;
import com.xebialabs.deployit.plugin.sql.support.VelocityUtils;
import org.apache.commons.lang.StringUtils;

@SuppressWarnings("serial")
@ConfigurationItem(description = "SQL Oracle database instance")
public class MSSQLDatabase extends Database {

	static final String SQLCMD_EXE = "sqlcmd.exe";

	@ConfigurationItemProperty(required = true, description = "–S SERVER\\instance")
	private String server;

	@ConfigurationItemProperty(required = false, label = "sqlcmd Path", description = "sqlcmd.exe Path, ex c:\\path\\to")
	private String sqlcmdPath;


	public String getSqlcmdPath() {
		return sqlcmdPath;
	}

	public void setSqlcmdPath(String sqlcmdPath) {
		this.sqlcmdPath = sqlcmdPath;
	}


	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	//sqlcmd –S SERVER\instance –U ${username} –P ${password} –d ${database} –i
	//SQLCMD.EXE -S %BD_SERVEUR_APPLI% -d %BD_BASE_APPLI% -i %SQL% -o %LOGSQL%
	@Override
	public String getCommand() {

		if (StringUtils.isNotBlank(super.getCommand())) {
			return super.getCommand();
		}

		final String os = getHost().getOperatingSystemFamily().toString().toLowerCase();
		return VelocityUtils.evaluateTemplate(this, "com/xebialabs/deployit/plugin/sql/mssql/" + os + "_template.vm");
	}


}
