package com.xebialabs.deployit.plugins.releaseauth;

import static java.lang.Boolean.TRUE;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class ConditionVerifier {

	public static VerificationResult validateReleaseConditions(Set<String> conditions, 
			Version deploymentPackage) {
		VerificationResult result = new VerificationResult();
		
        for (String conditionName : conditions) {
            Boolean conditionValue = deploymentPackage.getProperty(conditionName);
			if (!TRUE.equals(conditionValue)) {
            	result.logViolatedCondition(conditionName, TRUE, conditionValue);
            } else {
            	result.logValidatedCondition(conditionName);
            }
        }
        return result;
    }
    
	public static class VerificationResult {
		private final ImmutableSet.Builder<ViolatedCondition<?>> violatedConditions = 
			new Builder<ViolatedCondition<?>>();
		private final ImmutableSet.Builder<String> validatedConditions = new Builder<String>();
		private boolean failed = false;

		private <T> void logViolatedCondition(String condition, T expectedValue, T actualValue) {
			violatedConditions.add(new ViolatedCondition<T>(condition, expectedValue, actualValue));
			failed = true;
		}

		private void logValidatedCondition(String condition) {
			validatedConditions.add(condition);
		}

		public boolean failed() {
			return failed;
		}

		public Set<ViolatedCondition<?>> getViolatedConditions() {
			return violatedConditions.build();
		}
		
		public Set<String> getValidatedConditions() {
			return validatedConditions.build();
		}
	}
	
	public static class ViolatedCondition<T> {
		public final String name;
		public final T expectedValue;
		public final T actualValue;
		
		private ViolatedCondition(String name, T expectedValue, T actualValue) {
			this.name = name;
			this.expectedValue = expectedValue;
			this.actualValue = actualValue;
		}
	}
}
