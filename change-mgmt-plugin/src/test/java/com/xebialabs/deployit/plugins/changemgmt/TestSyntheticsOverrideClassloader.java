package com.xebialabs.deployit.plugins.changemgmt;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

public class TestSyntheticsOverrideClassloader extends ClassLoader {
	private static final String TEST_SYNTHETIC_RESOURCE_NAME = "synthetic-test.xml";
	
	private final Enumeration<URL> testSynthetics;
	
	public TestSyntheticsOverrideClassloader(ClassLoader parent, String... syntheticsFiles) {
		super(parent);
		testSynthetics = toUrlEnumeration(syntheticsFiles);
	}
	
	private static Enumeration<URL> toUrlEnumeration(String[] syntheticsFiles) {
		// don't you just love Enumerations...
		Vector<URL> synthetics = new Vector<URL>(syntheticsFiles.length);
		for (String syntheticsFile : syntheticsFiles) {
			try {
				synthetics.add(new File(syntheticsFile).toURI().toURL());
			} catch (MalformedURLException exception) {
				throw new IllegalArgumentException(
						format("Unable to convert '%s' to URL", syntheticsFile), exception);
			}
		}
		return synthetics.elements();
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return (name.equals(TEST_SYNTHETIC_RESOURCE_NAME) ? testSynthetics : super.getResources(name));
	}
}
