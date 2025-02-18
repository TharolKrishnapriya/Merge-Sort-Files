package com.file.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class creates a steam for each Temporary files and automatically holds the
 * next line of the file
 */
public class SortedFileStreamer {
	
	private String line;
	private BufferedReader br;

	public SortedFileStreamer(Path filePath) throws IOException {
		this.br = Files.newBufferedReader(filePath);
		line = br.readLine();
	}

	public String getLine() {
		return line;
	}
	
	public String nextLine() throws IOException {
		String currentWord = line;
		if((line = br.readLine()) == null) {
			br.close();
		}
		return currentWord;
	}
}
