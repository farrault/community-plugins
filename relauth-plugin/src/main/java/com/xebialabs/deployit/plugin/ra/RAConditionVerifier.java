package com.xebialabs.deployit.plugin.ra;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.xebialabs.deployit.plugin.api.udm.Environment;
import com.xebialabs.deployit.plugin.api.udm.Version;

/**
 * Helper class that performs the validation of the release authorization conditions.
 */
public class RAConditionVerifier {

	private final Version version;
	private final Environment environment;

	public RAConditionVerifier(Version version, Environment environment) {
		this.version = version;
		this.environment = environment;
	}
	
	@SuppressWarnings("rawtypes")
	public VerificationResult verify() {
		VerificationResult result = new VerificationResult();
		
		Set<String> conditions = environment.getSyntheticProperty("conditions");
		
		for (String condition : conditions) {
			Object conditionProperty = version.getSyntheticProperty(condition);

			// Handles non-existent property as well as null object properties
			if (conditionProperty == null) {
				result.logFailedValidation(condition);
				continue;
			}
			
			if (conditionProperty instanceof String && StringUtils.isBlank((String) conditionProperty)) {
				result.logFailedValidation(condition);
				continue;
			}
			
			if (conditionProperty instanceof Boolean && ! (Boolean) conditionProperty) {
				result.logFailedValidation(condition);
				continue;
			}

			if (conditionProperty instanceof Collection && ((Collection) conditionProperty).isEmpty()) {
				result.logFailedValidation(condition);
				continue;
			}

			result.logSuccessfulValidation(condition);
		}

		return result;
	}
	
	class VerificationResult {
		private StringBuffer log = new StringBuffer();
		private boolean success = true;

		public String getLog() {
			return log.toString();
		}

		public boolean isSuccess() {
			return success;
		}

		public void logFailedValidation(String condition) {
			log.append("Package " + version + " failed to meet release condition '" + condition + "'\n");
			success = false;
		}

		public void logSuccessfulValidation(String condition) {
			log.append("Release condition '" + condition + "': OK\n");
		}
	}
}
