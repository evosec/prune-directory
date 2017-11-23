package de.evosec.prunedirectory;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ParseSizeTest {

	@Test
	public void testParse() throws Exception {
		assertThat(PruneDirectoryApplication.parseSize("1024")).isEqualTo(1024);

		assertThat(PruneDirectoryApplication.parseSize("1KB")).isEqualTo(1000);
		assertThat(PruneDirectoryApplication.parseSize("1MB"))
		    .isEqualTo(1000 * 1000);
		assertThat(PruneDirectoryApplication.parseSize("1GB"))
		    .isEqualTo(1000 * 1000 * 1000);

		assertThat(PruneDirectoryApplication.parseSize("1KiB")).isEqualTo(1024);
		assertThat(PruneDirectoryApplication.parseSize("1MiB"))
		    .isEqualTo(1024 * 1024);
		assertThat(PruneDirectoryApplication.parseSize("1GiB"))
		    .isEqualTo(1024 * 1024 * 1024);
	}

	@Test
	public void testParseAndFormat() throws Exception {
		testParseAndFormat("1", "1 B", true);
		testParseAndFormat("1024", "1,0 kB", true);
		testParseAndFormat("1KB", "1,0 kB", true);
		testParseAndFormat("1KiB", "1,0 kB", true);

		testParseAndFormat("1024KB", "1,0 MB", true);
		testParseAndFormat("1024KiB", "1,0 MB", true);

		testParseAndFormat("1024KB", "1000,0 KiB", false);
		testParseAndFormat("1024KiB", "1,0 MiB", false);

		testParseAndFormat("100MiB", "100,0 MiB", false);
		testParseAndFormat("100MiB", "104,9 MB", true);

		testParseAndFormat("100GiB", "100,0 GiB", false);
		testParseAndFormat("100GiB", "107,4 GB", true);
	}

	@Test
	public void testFormat() throws Exception {
		testFormat(1, "1 B", true);
		testFormat(1024, "1,0 kB", true);
		// dont worry this is due to rounding
		testFormat(1024 * 1024, "1,0 MB", true);
		// dont worry this is due to rounding
		testFormat(1024 * 1024 * 1024, "1,1 GB", true);

		testFormat(1, "1 B", false);
		testFormat(1024, "1,0 KiB", false);
		testFormat(1024 * 1024, "1,0 MiB", false);
		testFormat(1024 * 1024 * 1024, "1,0 GiB", false);
	}

	private void testParseAndFormat(String input, String output, boolean si) {
		assertThat(PruneDirectoryApplication.humanReadableByteCount(
		    PruneDirectoryApplication.parseSize(input), si)).isEqualTo(output);
	}

	private void testFormat(long input, String output, boolean si) {
		assertThat(PruneDirectoryApplication.humanReadableByteCount(input, si))
		    .isEqualTo(output);
	}

}
