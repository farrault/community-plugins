package com.xebialabs.deployit.community.releaseauth;

import static com.google.common.base.Strings.nullToEmpty;
import static com.xebialabs.deployit.plugin.api.reflect.PropertyKind.BOOLEAN;
import static com.xebialabs.deployit.plugin.api.reflect.PropertyKind.STRING;
import static java.lang.Boolean.TRUE;
import static java.lang.String.format;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.xebialabs.deployit.plugin.api.reflect.Descriptor;
import com.xebialabs.deployit.plugin.api.reflect.DescriptorRegistry;
import com.xebialabs.deployit.plugin.api.reflect.PropertyKind;
import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.DeploymentPackage;
import com.xebialabs.deployit.plugin.api.udm.Version;

public class ConditionVerifier {
	private static final Descriptor DEPLOYMENT_PACKAGE_DESCRIPTOR = 
		DescriptorRegistry.getDescriptor(Type.valueOf(DeploymentPackage.class));
	
	public static VerificationResult validateReleaseConditions(Set<String> conditions, 
			Version deploymentPackage) {
		VerificationResult result = new VerificationResult();
		
        for (String conditionName : conditions) {
        	PropertyKind conditionKind = 
        		DEPLOYMENT_PACKAGE_DESCRIPTOR.getPropertyDescriptor(conditionName).getKind();
        	Object conditionValue = deploymentPackage.getProperty(conditionName);
			switch (conditionKind) {
        		case BOOLEAN:
        			verifyBooleanCondition(conditionName, (Boolean) conditionValue, result);
        			break;
        		case STRING:
        			verifyStringCondition(conditionName, (String) conditionValue, result);
        			break;
    			default:
    				throw new IllegalArgumentException(format("Only release conditions of kind '%s' or '%s' are supported, but condition '%s' was of kind '%s'",
    						BOOLEAN, STRING, conditionKind));
        	}
        }
        return result;
    }
    
	private static void verifyBooleanCondition(String conditionName, Boolean conditionValue, VerificationResult result) {
		if (!TRUE.equals(conditionValue)) {
			result.logViolatedCondition(conditionName, TRUE, conditionValue);
		} else {
			result.logValidatedCondition(conditionName);
		}
	}

	private static void verifyStringCondition(String conditionName, String conditionValue, VerificationResult result) {
		if (isNullOrBlank(conditionValue)) {
			result.logViolatedCondition(conditionName, "non-empty value", conditionValue);
		} else {
			result.logValidatedCondition(conditionName);
		}
	}
	
	// TODO: move to Strings2 or whatever
	private static boolean isNullOrBlank(String string) {
		return nullToEmpty(string).trim().isEmpty();
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
