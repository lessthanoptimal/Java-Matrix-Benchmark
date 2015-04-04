package jmbench.benchmark.runtime;

import jmbench.benchmark.BenchmarkConstants;
import jmbench.benchmark.BenchmarkTools;
import jmbench.libraries.LibraryDescription;

import java.io.File;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class BencharkRuntimeApp {

	RuntimeBenchmarkConfig config;
	List<LibraryDescription> libraries;
	File resultsDir;

	public BencharkRuntimeApp( String testSetFile , RuntimeBenchmarkConfig config ) {
		this.config = config;
		libraries = BenchmarkTools.loadTestSet(testSetFile);
		resultsDir = BenchmarkTools.createResultsDirectory(testSetFile,config);
	}

	public void perform() {
		long before = System.currentTimeMillis();
		for (int i = 0; i < libraries.size(); i++) {
			process(libraries.get(i));
		}
		long after = System.currentTimeMillis();

		double hours = (after-before)/(1000*60*60.0);

		System.out.println("DONE! elapsed time = "+hours+"  hours");
	}

	protected void process( LibraryDescription desc ) {
		File d = new File(desc.directory);
		File testDir = new File(resultsDir,d.getName());
		if( !testDir.mkdir() )
			throw new RuntimeException("Failed to create directory for test. "+testDir);

		// TODO save version info.  Must invoke a new JRE to avoid contamination

		// TODO start computing the results
	}

	public static void main(String[] args) {
		String testSetFile = BenchmarkConstants.DEFAULT_TEST_SET;
		RuntimeBenchmarkConfig config = RuntimeBenchmarkConfig.createAllConfig();

		BencharkRuntimeApp app = new BencharkRuntimeApp(testSetFile,config);

		app.perform();
	}
}
