package com.test;

import java.lang.instrument.Instrumentation;

/**
 * 
 * @author hengyunabc 2020-07-28
 *
 */
public class DemoAgent {

	public static void premain(String args, Instrumentation inst) {
		main(true, args, inst);
	}

	public static void agentmain(String args, Instrumentation inst) {
		main(false, args, inst);
	}

	private static synchronized void main(boolean premain, String args, Instrumentation inst) {
		System.out.println("DemoAgent started.");
	}

}
