package jmbench.misc;

import jmbench.interfaces.RuntimePerformanceFactory;

/**
 * Slave which is run in a new JVM.  Loads a LibraryStringInfo specified in the first arguments, creates a factory,
 * then prints its version string to standard out.
 *
 * @author Peter Abeles
 */
public class VersionSlave {
	public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		Class factoryClass = Class.forName(args[0]);
		RuntimePerformanceFactory factory = (RuntimePerformanceFactory)factoryClass.newInstance();

		System.out.println(factory.getLibraryVersion());
	}
}
