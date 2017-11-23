package de.evosec.prunedirectory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("prune-directory")
public class PruneDirectoryProperties {

	private String directory;
	private String maxSize;

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(String maxSize) {
		this.maxSize = maxSize;
	}

}
