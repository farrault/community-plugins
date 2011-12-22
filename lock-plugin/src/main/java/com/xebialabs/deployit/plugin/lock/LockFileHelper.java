package com.xebialabs.deployit.plugin.lock;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import com.google.common.base.Function;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;

public class LockFileHelper {

	private static final String LOCK_FILE_DIRECTORY = "locks";

	public static void lock(ConfigurationItem ci) throws FileNotFoundException {
		createLockDirectoryIfNotExists();

		PrintWriter pw = new PrintWriter(getLockFile(ci));
		pw.println("Locking " + ci.getName() + " on " + new Date());
		pw.close();

	}
	
	public static void unlock(ConfigurationItem ci) {
		createLockDirectoryIfNotExists();
		
		if (!getLockFile(ci).delete()) {
			throw new RuntimeException("Failed to unlock " + ci.getName());
		}
	}
	
	public static boolean isLocked(ConfigurationItem ci) {
		return getLockFile(ci).exists();
	}
	
	public static void clearLocks() {
		createLockDirectoryIfNotExists();
		
		for (String lockFile : getLockFileList()) {
			if (! new File(LOCK_FILE_DIRECTORY, lockFile).delete()) {
				throw new RuntimeException("Unable to delete lock file " + lockFile);
			}
		}
	}

	public static List<String> listLocks() {
		createLockDirectoryIfNotExists();
		
		return newArrayList(transform(getLockFileList(), new Function<String, String>() {
			@Override
			public String apply(String input) {
				return lockFileNameToCiId(input);
			}
		}));
	}

	private static List<String> getLockFileList() {
		return newArrayList(new File(LOCK_FILE_DIRECTORY).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".lock");
			}
		}));
	}

	private static File getLockFile(ConfigurationItem ci) {
		return new File(LOCK_FILE_DIRECTORY, ciIdToLockFileName(ci.getId()));
	}

	static String ciIdToLockFileName(String ciId) {
		return ciId.replaceAll("/", "\\$") + ".lock";
	}

	static String lockFileNameToCiId(String lockFileName) {
		return lockFileName.replaceAll("\\$", "/").replace(".lock", "");
	}
	
	private static File createLockDirectoryIfNotExists() {
		return createLockDirectoryIfNotExists(LOCK_FILE_DIRECTORY);
	}
	
	private static File createLockDirectoryIfNotExists(String directory) {
		File lockDir = new File(directory);
		if (lockDir.exists() && lockDir.isDirectory()) {
			return lockDir;
		}
		
		lockDir.mkdir();
		
		if (!(lockDir.exists() && lockDir.isDirectory())) {
			throw new RuntimeException("Unable to create lock directory");
		}
		
		return lockDir;
		
	}
}
