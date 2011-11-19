package com.xebialabs.deployit.plugins.releaseauth;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.xebialabs.deployit.test.support.TestUtils.createDeploymentPackage;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import org.hamcrest.core.Is;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.xebialabs.deployit.plugin.api.udm.Version;
import com.xebialabs.deployit.plugins.releaseauth.ConditionVerifier.VerificationResult;
import com.xebialabs.deployit.plugins.releaseauth.ConditionVerifier.ViolatedCondition;

/**
 * Unit tests for {@link ConditionVerifier}
 */
public class ConditionVerifierTest extends TestBase {
	
	@Test
	public void nullBooleanPropertyFails() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("hasReleaseNotes", null);
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("hasReleaseNotes"), deploymentPackage);
		assertThat(result.failed(), is(true));
		assertThat(result.getValidatedConditions().size(), is(0));
		assertThat(result.getViolatedConditions().size(), is(1));
		ViolatedCondition<?> violatedCondition = getOnlyElement(result.getViolatedConditions());
		assertThat(violatedCondition.name, is("hasReleaseNotes"));
		assertThat(violatedCondition.expectedValue, Is.<Object>is(TRUE));
		// 'getProperty' returns Java's default value for that type
		assertThat(violatedCondition.actualValue, Is.<Object>is(FALSE));
	}
	
	@Test
	public void falseBooleanPropertyFails() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("hasReleaseNotes", FALSE);
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("hasReleaseNotes"), deploymentPackage);
		assertThat(result.failed(), is(true));
		assertThat(result.getValidatedConditions().size(), is(0));
		assertThat(result.getViolatedConditions().size(), is(1));
		ViolatedCondition<?> violatedCondition = getOnlyElement(result.getViolatedConditions());
		assertThat(violatedCondition.name, is("hasReleaseNotes"));
		assertThat(violatedCondition.expectedValue, Is.<Object>is(TRUE));
		assertThat(violatedCondition.actualValue, Is.<Object>is(FALSE));
	}
	
	@Test
	public void trueBooleanPropertySucceeds() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("hasReleaseNotes", TRUE);
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("hasReleaseNotes"), deploymentPackage);
		assertThat(result.failed(), is(false));
		assertThat(result.getValidatedConditions().size(), is(1));
		assertThat(getOnlyElement(result.getValidatedConditions()), is("hasReleaseNotes"));
		assertThat(result.getViolatedConditions().size(), is(0));
	}

	@Test
	public void nullStringPropertyFails() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("signingOffManager", null);
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("signingOffManager"), deploymentPackage);
		assertThat(result.failed(), is(true));
		assertThat(result.getValidatedConditions().size(), is(0));
		assertThat(result.getViolatedConditions().size(), is(1));
		ViolatedCondition<?> violatedCondition = getOnlyElement(result.getViolatedConditions());
		assertThat(violatedCondition.name, is("signingOffManager"));
		assertThat(violatedCondition.expectedValue, Is.<Object>is("non-empty value"));
		assertThat(violatedCondition.actualValue, is(nullValue()));
	}
	
	@Test
	public void emptyStringPropertyFails() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("signingOffManager", "");
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("signingOffManager"), deploymentPackage);
		assertThat(result.failed(), is(true));
		assertThat(result.getValidatedConditions().size(), is(0));
		assertThat(result.getViolatedConditions().size(), is(1));
		ViolatedCondition<?> violatedCondition = getOnlyElement(result.getViolatedConditions());
		assertThat(violatedCondition.name, is("signingOffManager"));
		assertThat(violatedCondition.expectedValue, Is.<Object>is("non-empty value"));
		// 'getProperty' returns Java's default value for that type
		assertThat(violatedCondition.actualValue, is(nullValue()));
	}
	
	@Test
	public void whitespaceStringPropertyFails() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("signingOffManager", " \t");
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("signingOffManager"), deploymentPackage);
		assertThat(result.failed(), is(true));
		assertThat(result.getValidatedConditions().size(), is(0));
		assertThat(result.getViolatedConditions().size(), is(1));
		ViolatedCondition<?> violatedCondition = getOnlyElement(result.getViolatedConditions());
		assertThat(violatedCondition.name, is("signingOffManager"));
		assertThat(violatedCondition.expectedValue, Is.<Object>is("non-empty value"));
		assertThat(violatedCondition.actualValue, Is.<Object>is(" \t"));
	}
	
	@Test
	public void stringPropertyWithContentSucceeds() {
		Version deploymentPackage = createDeploymentPackage();
		deploymentPackage.setProperty("signingOffManager", "M");
		VerificationResult result = ConditionVerifier.validateReleaseConditions(
				ImmutableSet.of("signingOffManager"), deploymentPackage);
		assertThat(result.failed(), is(false));
		assertThat(result.getValidatedConditions().size(), is(1));
		assertThat(getOnlyElement(result.getValidatedConditions()), is("signingOffManager"));
		assertThat(result.getViolatedConditions().size(), is(0));
	}
}
