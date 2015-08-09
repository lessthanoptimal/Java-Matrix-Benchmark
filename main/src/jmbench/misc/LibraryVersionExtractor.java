package jmbench.misc;

import jmbench.benchmark.BenchmarkTools;
import jmbench.libraries.LibraryDescription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Gets the version of a library by launching a separate JVM to avoid corruption
 *
 * @author Peter Abeles
 */
public class LibraryVersionExtractor extends JavaRuntimeLauncher {

	PrintStream errorStream = System.err;

	LibraryDescription library;

	// string which contains the version
	String version;

	public LibraryVersionExtractor(LibraryDescription library) {
		super(library.directory);

		this.library = library;
	}

	/**
	 * Returns the version string for the library.  If it fails then an error string is returned and stuff printed
	 * to the error stream
	 * @return Version string
	 */
	public String getVersion() {
		version = "";
		if( launch(VersionSlave.class,library.info.getFactory()) ) {
			return version;
		} else {
			errorStream.println("Failed to get version information for "+library.directory);
			return "Version Failed";
		}
	}

	/**
	 * Feed standard out into a string.
	 */
	@Override
	protected void printInputBuffer(BufferedReader input) throws IOException {
		while( input.ready() ) {
			int val = input.read();
			if( val < 0 ) break;

			version += new String(Character.toChars(val));
		}
	}

	public void setErrorStream(PrintStream errorStream) {
		this.errorStream = errorStream;
	}

	public static void main(String[] args) {

		List<LibraryDescription> libs = BenchmarkTools.loadAllLibraries();
		LibraryDescription target = BenchmarkTools.lookup("ejml",libs);

		LibraryVersionExtractor app = new LibraryVersionExtractor(target);

		System.out.println(target.info.getNameFull()+": version = " + app.getVersion());
	}
}
