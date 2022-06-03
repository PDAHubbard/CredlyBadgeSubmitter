package org.peterh.credly.util;

public class MyLog {
	
	public void error(String f, Object... a) {
		System.out.printf("ERROR: " + f + "\n", a);
	}
	
	public void warn(String f, Object... a) {
		System.out.printf("WARN: " + f + "\n", a);
	}
	
	public void info(String f, Object... a) {
		System.out.printf("INFO: " + f + "\n", a);
	}
	
	public void debug(String f, Object... a) {
		System.out.printf("DEBUG: " +f + "\n", a);
	}
}
