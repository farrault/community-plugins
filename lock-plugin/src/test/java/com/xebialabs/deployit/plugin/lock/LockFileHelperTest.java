package com.xebialabs.deployit.plugin.lock;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import com.xebialabs.deployit.plugin.api.udm.Container;
import com.xebialabs.deployit.plugin.api.udm.base.BaseContainer;

public class LockFileHelperTest {

	private Container container;

	@Before
	public void createContainer() {
		this.container = new BaseContainer();
		this.container.setId("Infrastructure/lock/TestContainer");
	}

	@Before
	public void clearLockDirectory() {
		LockFileHelper.clearLocks();
	}

	@Test
	public void shouldCorrectlyConvertCiIdToLockFileNameAndBack() throws FileNotFoundException {
		String lockFileName = LockFileHelper.ciIdToLockFileName(container.getId());
		assertThat(container.getId(), is(equalTo(LockFileHelper.lockFileNameToCiId(lockFileName))));
	}

	@Test
	public void shouldCorrectlyLockContainer() throws FileNotFoundException {
		assertThat(LockFileHelper.isLocked(container), is(equalTo(false)));
		
		LockFileHelper.lock(container);
		
		assertThat(LockFileHelper.isLocked(container), is(equalTo(true)));
	}
	
	@Test
	public void shouldCorrectlyClearLocks() throws FileNotFoundException {
		LockFileHelper.lock(container);
		LockFileHelper.clearLocks();
		
		assertThat(LockFileHelper.isLocked(container), is(equalTo(false)));
	}
}
