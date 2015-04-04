package jmbench.benchmark;

import com.thoughtworks.xstream.XStream;
import jmbench.benchmark.runtime.RuntimeBenchmarkConfig;
import jmbench.libraries.LibraryDescription;
import jmbench.libraries.LibraryStringInfo;
import jmbench.libraries.LibraryTools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Peter Abeles
 */
public class BenchmarkTools {

	public static List<LibraryDescription> loadTestSet(  String filePath  ) {
		List<LibraryDescription> all = loadAllLibraries();
		return loadTestSet(filePath,all);
	}

	/**
	 * Parses a testset file and looks up the LibraryDescriptions
	 */
	public static List<LibraryDescription> loadTestSet(  String filePath , List<LibraryDescription> all  ) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));

			List<LibraryDescription> ret = new ArrayList<>();
			while( true ) {
				String line = reader.readLine();
				if( line == null )
					break;
				LibraryDescription lib = lookup(line,all);
				if( lib == null )
					throw new RuntimeException("Can't find '"+line+"'");
				ret.add(lib);
			}
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static LibraryDescription lookup( String name , List<LibraryDescription> all) {
		for (int i = 0; i < all.size(); i++) {
			LibraryStringInfo l = all.get(i).info;

			if( l.nameFull.compareToIgnoreCase(name) == 0 )
				return all.get(i);
			else if( l.nameShort.compareToIgnoreCase(name) == 0 )
				return all.get(i);
			else if( l.namePlot.compareToIgnoreCase(name) == 0 )
				return all.get(i);
		}
		return null;
	}

	public static List<LibraryDescription> loadAllLibraries() {
		List<LibraryDescription> all = new ArrayList<LibraryDescription>();

		File dirExternal = new File("external");

		if( !dirExternal.exists() )
			throw new RuntimeException("Can't find external directory");
		if( !dirExternal.isDirectory())
			throw new RuntimeException("external is not a directory!");

		File[] files = dirExternal.listFiles();

		for( File f : files ) {
			if( !f.isDirectory() )
				continue;

			File settings = new File(f,"TestSetInfo.txt");
			if( settings.exists() ) {
				all.addAll(parseDescription(settings));
			}
		}
		return all;
	}

	protected static List<LibraryDescription> parseDescription(File file) {
		List<LibraryStringInfo> listString = LibraryTools.loadTests(file);

		List<LibraryDescription> out = new ArrayList<LibraryDescription>();

		for( LibraryStringInfo info : listString ) {
			LibraryDescription desc = new LibraryDescription();

			desc.directory = file.getParent();
			desc.info = info;

			out.add( desc );
		}

		return out;
	}
	/**
	 * Creates the directory which will store these results.  Serializes the config object and copies over
	 * the test set file
	 * @return The directory where results should be saved to
	 */
	public static File createResultsDirectory( String testSetFile , Object config ) {
		String type;

		if( config instanceof RuntimeBenchmarkConfig) {
			type = "runtime";
		} else {
			throw new RuntimeException("Unknown config "+config.getClass().getSimpleName());
		}

		DateFormat df = new SimpleDateFormat("MM-dd-yyyy-HH:mm:ss");
		Date today = Calendar.getInstance().getTime();
		String reportDate = df.format(today);

		String directorySave = "results/"+type+"/"+reportDate;

		File dir = new File(directorySave);
		if( !dir.exists() ) {
			if(!dir.mkdirs())
				throw new RuntimeException("Failed to make directories");
		}

		try {
			saveAsXml(config, new File(dir, "config.xml"));
			Files.copy(new File(testSetFile).toPath(), new File(dir, "TestSet.txt").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
			SystemInfo info = new SystemInfo();
			info.grabCurrentInfo();
			saveAsXml(info, new File(dir, "SystemInfo.xml"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return dir;
	}

	public static void saveAsXml( Object object , File file ) throws IOException {
		XStream xstream = new XStream();
		String string = xstream.toXML(object);

		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(string);
		out.close();
	}
}
