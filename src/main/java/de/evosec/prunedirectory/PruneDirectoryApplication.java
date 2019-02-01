package de.evosec.prunedirectory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public class PruneDirectoryApplication implements Callable<Void> {

	private static final Logger LOG =
	        LoggerFactory.getLogger(PruneDirectoryApplication.class);

	public static void main(String[] args) throws IOException {
		CommandLine.call(new PruneDirectoryApplication(), args);
	}

	@Parameters(index = "0", description = "The directory to prune")
	private String directory;
	@Option(
	        names = {"--max-size"},
	        description = "100MiB, 512MiB, 1GiB, ...",
	        defaultValue = "100MiB",
	        showDefaultValue = Visibility.ALWAYS)
	private String maxSize;

	@Override
	public Void call() throws Exception {
		Path directory = Paths.get(this.directory);
		if (!directory.isAbsolute()) {
			directory = Paths.get(System.getProperty("user.dir"))
			    .resolve(directory)
			    .toAbsolutePath();
		}
		if (Files.notExists(directory)) {
			LOG.warn("Directory {} does not exist. Exiting.", directory);
			return null;
		}
		long maxSize = parseSize(this.maxSize);
		LOG.info("Pruning {} to maximum size of {}", directory,
		    humanReadableByteCount(maxSize, false));
		List<Path> files = new ArrayList<>();
		try (DirectoryStream<Path> directoryStream = Files
		    .newDirectoryStream(directory, f -> Files.isRegularFile(f))) {
			for (Path file : directoryStream) {
				files.add(file);
			}
		}
		Collections.sort(files, (o1, o2) -> {
			int compare =
			        getLastModifiedTime(o1).compareTo(getLastModifiedTime(o2));
			if (compare == 0) {
				// fall back to lexicographic sorting if files are
				// modified at the same time
				compare = o1.compareTo(o2);
			}
			return compare;
		});
		long currentSize = size(files);
		while (currentSize > maxSize) {
			Path file = files.remove(0);
			long size = Files.size(file);
			Files.deleteIfExists(file);
			currentSize -= size;
			LOG.info("Deleted {} with {}. {} remaining.", file,
			    humanReadableByteCount(size, false),
			    humanReadableByteCount(currentSize, false));
		}
		return null;
	}

	private static FileTime getLastModifiedTime(Path path) {
		try {
			return Files.getLastModifiedTime(path);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static long size(Collection<Path> files) {
		return files.stream().mapToLong(f -> size(f)).sum();
	}

	private static long size(Path path) {
		try {
			return Files.size(path);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit) {
			return bytes + " B";
		}
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre =
		        (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static long parseSize(String sizeToParse) {
		String size = sizeToParse.toUpperCase(Locale.ROOT);
		if (size.endsWith("KB")) {
			return Long.valueOf(size.substring(0, size.length() - 2)) * 1000;
		}
		if (size.endsWith("MB")) {
			return Long.valueOf(size.substring(0, size.length() - 2)) * 1000
			        * 1000;
		}
		if (size.endsWith("GB")) {
			return Long.valueOf(size.substring(0, size.length() - 2)) * 1000
			        * 1000 * 1000;
		}
		if (size.endsWith("KIB")) {
			return Long.valueOf(size.substring(0, size.length() - 3)) * 1024;
		}
		if (size.endsWith("MIB")) {
			return Long.valueOf(size.substring(0, size.length() - 3)) * 1024
			        * 1024;
		}
		if (size.endsWith("GIB")) {
			return Long.valueOf(size.substring(0, size.length() - 3)) * 1024
			        * 1024 * 1024;
		}
		return Long.valueOf(size);
	}

}
