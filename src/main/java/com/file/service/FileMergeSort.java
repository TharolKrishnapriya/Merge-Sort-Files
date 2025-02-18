package com.file.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Program to Sort and Merge files of a directory in a way that is efficient and scalable 
 */

public class FileMergeSort {

	private static final int Maximum_Temp_File_Count = 1024;

	/**
	 * If the number of files in the Input Directory is greater that Maximum_Temp_File_Count,
	 * Then merge files into a single file and then split this file in to atmost 1024 temparory
	 * files. Easier to perform a K-way sort
	 * 
	 * If Number of files in input Directory is less than Maximum_Temp_File_Count then directly perform K-way sort on the files
	 * and merge them.
	 * @param inputDirectoryPath - Input Directory path
	 * @param outputFileDirectory - Output Directory path
	 * @param tempDirectoryPath  - Directory Path to where the temporary file will be stored.
	 * @param removeDiplicates - Flag to remove duplicates
	 * @throws Exception
	 */
	public void mergeAndSortFiles(String inputDirectoryPath, String outputFileDirectory, String tempDirectoryPath, boolean removeDiplicates)
			throws Exception {
		String tempSplitDirectoryPath = tempDirectoryPath + "/split";
		long start = System.currentTimeMillis();
		try {
			mergeAndSortFilesSetup(outputFileDirectory);
			List<Path> inputFilePaths = listFilesFromDirectory(inputDirectoryPath);
			if (inputFilePaths.size() > Maximum_Temp_File_Count){
				String tempMergedUnsortedFile = temproryMergeFiles(inputFilePaths, tempDirectoryPath);
				splitAndSort(tempMergedUnsortedFile, tempSplitDirectoryPath);
				mergeSortedFiles(tempSplitDirectoryPath, outputFileDirectory, removeDiplicates);
			}else {
				mergeSortedFiles(inputDirectoryPath, outputFileDirectory, removeDiplicates);
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			cleanUp(tempDirectoryPath, tempSplitDirectoryPath);
		}
		long end = System.currentTimeMillis();
		long diff = end - start;
		System.out.println("Time :: " + diff);
	}

	/**
	 * Delete the Output file if present inside the output file Directory
	 * @param outputFileDirectory
	 * @throws IOException
	 */
	private void mergeAndSortFilesSetup(String outputFileDirectory) throws IOException {
		Files.deleteIfExists(Paths.get(outputFileDirectory + "/output.dat"));
	}

	/**
	 * If Number of Files in the input directory is greater than Maximum_Temp_File_Count. Merge files
	 * into 1 file.
	 * @param inputFilePaths - Paths of files inside the input directory.
	 * @param tempDirectoryPath - Directory Path to where the temporary merged file will be stored.
	 * @return Temporary_merged_file path.
	 * @throws Exception - if input directory is empty throws Exception ("No input Files Found")
	 * 
	 */
	public String temproryMergeFiles(List<Path> inputFilePaths, String tempDirectoryPath) throws Exception {
		if (inputFilePaths.isEmpty()) {
			throw new Exception("No input Files Found");
		}
		Path tempMergedUnsortedFile = Files.createTempFile(Paths.get(tempDirectoryPath), "tempMergeFile", ".dat");
		for (Path inputFilePath : inputFilePaths) {
			try (Stream<String> lines = Files.lines(inputFilePath)) {
				Files.write(tempMergedUnsortedFile, (Iterable<String>) lines::iterator, StandardOpenOption.CREATE,
						StandardOpenOption.APPEND);
			} catch (IOException e) {
				throw new Exception("Error while merging input files");
			}
		}
		return tempMergedUnsortedFile.toString();
	}

	/**
	 * Split the Temporary_merged_file into smaller files.
	 * Maximum number of temporary files should be less than Maximum_Temp_File_Count.
	 * Estimate the size of file after split.
	 * Add string from the Temporary_merged_file to a list until the size of the list = estimated file size
	 * Sort the list and create a temporary file containing this block of sorted words and save 
	 * it to the Temporary directory
	 * 
	 * @param tempMergedUnsortedFile - Temporary merged File Path
	 * @param tempSplitDirectoryPath - Temporary Directory path to where temporary files after split must be saved.
	 * @throws Exception - if size of temporary_merged_files is 0, then throw exception as Input Directory contained files 
	 * with no content in it.
	 */
	public void splitAndSort(String tempMergedUnsortedFile, String tempSplitDirectoryPath) throws Exception {
		File intermediateMergedFile = new File(tempMergedUnsortedFile);
		long sizeOfMergedFile = intermediateMergedFile.length();
		if (sizeOfMergedFile > 0) {
			long individualTempFileSize = determineTemporaryFileSize(sizeOfMergedFile);
			BufferedReader br = new BufferedReader(new FileReader(Paths.get(tempMergedUnsortedFile).toFile()));
			try {
				Long currentTempFileSize = 0L;
				List<String> block = new ArrayList<>();
				String line = "";
				while ((line = br.readLine()) != null) {
					if (currentTempFileSize < individualTempFileSize) {
						block.add(line);
						currentTempFileSize = currentTempFileSize + line.getBytes(StandardCharsets.UTF_8).length;
					} else {
						if (!block.isEmpty()) {
							writeToTempFile(block, tempSplitDirectoryPath);
							block.clear();
							block.add(line);
							currentTempFileSize = 0L;
						}
					}
				}
				if (!block.isEmpty()) {
					writeToTempFile(block, tempSplitDirectoryPath);
				}
			} catch (IOException e) {
				throw new IOException("Error while writing to temporary files");
			} finally {
				br.close();
			}
		} else {
			throw new Exception("Empty Files - unable to merge");
		}
	}
	
	/**
	 * Estimate the size_of_files(in Bytes) after Temporary_merged_files is split
	 * If the estimated size_of_files is less than half of the Free memory size(in Bytes). Increase the size of file to be equal to (Free Memory size / 2)
	 * @param sizeOfMergedFile - Size of Temporary_Merged_File
	 * @return Long - Estimated File size
	 */
	private long determineTemporaryFileSize(long sizeOfMergedFile) {
		long tempFileSize = (sizeOfMergedFile / Maximum_Temp_File_Count) + (sizeOfMergedFile % Maximum_Temp_File_Count == 0 ? 0 : 1);
		long availableMemory = Runtime.getRuntime().freeMemory();
		if (tempFileSize < availableMemory / 2)
			tempFileSize = availableMemory / 2;
		return tempFileSize;
	}

	/**
	 * Sort and Merge the temporary files using K-Way merge sort.
	 * Stream for each file is created and added into a Priority Queue
	 * The priority queue is Custom Sorted based on the latest line read from each of the file.
	 * The first element in the queue will be the smallest and hence added to the list.
	 * Once the List has 500 elements, it is written to the Output file
	 * This is repeated until End of File in each file
	 * 
	 * @param tempSplitDirectoryPath - Temporary Directory path to where temporary files after split is saved.
	 * @param outputFileDirectory - Directory where the Output file is to be created 
	 * @param removeDiplicates 
	 * @throws IOException
	 */
	public void mergeSortedFiles(String tempSplitDirectoryPath, String outputFileDirectory, boolean removeDiplicates)
			throws IOException{
		List<Path> filePaths = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(Paths.get(tempSplitDirectoryPath))) {
			filePaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			throw new IOException("Error while getting temporary file paths");
		}
		PriorityQueue<SortedFileStreamer> mergeQueque = new PriorityQueue<>(
				(s1, s2) -> s1.getLine().compareTo(s2.getLine()));
		for (Path path : filePaths) {
			SortedFileStreamer fileStreamer = new SortedFileStreamer(path);
			mergeQueque.add(fileStreamer);
		}
		List<String> ans = new ArrayList<>();
		SortedFileStreamer line = mergeQueque.poll();
		String lastWord = null;
		while (line != null) {
			if (line.getLine() != null) {
				if (ans.size() < 500)
					ans.add(line.nextLine());
				else {
					lastWord = writeToFile(ans, outputFileDirectory, removeDiplicates, lastWord);
					ans = new ArrayList<>();
				}
				if (line.getLine() != null)
					mergeQueque.add(line);
			}
			line = mergeQueque.poll();
		}
		writeToFile(ans, outputFileDirectory, removeDiplicates, lastWord);
	}

	/**
	 * 
	 * @param ans - List containing a sorted words to be written to the output file
	 * @param outputFileDirectory - Directory where output file will be save
	 * @param lastWord - last word from current list, saved to check for duplicate in the upcoming list
	 * @param removeDiplicates - Flag to remove duplicates
	 * @throws IOException
	 */
	private String writeToFile(List<String> ans, String outputFileDirectory, boolean removeDuplicates, String lastWord) throws IOException {
		if(removeDuplicates) {
			List<String> distinctWords = ans.stream().distinct()
					.filter(word ->!word.equals(lastWord)).collect(Collectors.toList());
			Files.write(Paths.get(outputFileDirectory + "/output.dat"), distinctWords
					, StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
			return distinctWords.getLast();
		}else {
			Files.write(Paths.get(outputFileDirectory + "/output.dat"), ans
					, StandardOpenOption.CREATE,
					StandardOpenOption.APPEND);
			return null;
			
		}
	}

	/**
	 * Get the List input file paths inside the  input Directory.
	 * @param inputDirectoryPath - Input Directory path.
	 * @return ArrayList - List of paths of files in the input directory.
	 * @throws Exception - Exception is thrown if the Input Directory path is invalid.
	 */
	public List<Path> listFilesFromDirectory(String inputDirectoryPath) throws Exception {
		List<Path> filePaths = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(Paths.get(inputDirectoryPath))) {
			filePaths = paths.filter(Files::isRegularFile).collect(Collectors.toList());
		} catch (IOException e) {
			throw new Exception("EXCEPTION :: Input Directory " + inputDirectoryPath + " path is invalid");
		}
		return filePaths;
	}

	/**
	 * Create and write temporary files after split from the Temporary merged file.
	 * @param block - List of words split from the temporary merged file and written to a file.
	 * @param tempSplitDirectoryPath - Temporary Directory path to where temporary files after split is saved.
	 * @throws IOException
	 */
	private void writeToTempFile(List<String> block, String tempSplitDirectoryPath) throws IOException {
		if (Files.notExists(Path.of(tempSplitDirectoryPath))) {
			Files.createDirectory(Path.of(tempSplitDirectoryPath));
		}
		Path tempFilePath = Files.createTempFile(Paths.get(tempSplitDirectoryPath), "temp", ".dat");
		Stream<String> sorted = block.stream().sorted(Comparator.naturalOrder());
		Files.write(tempFilePath, (Iterable<String>) sorted::iterator, StandardOpenOption.APPEND);
	}

	/**
	 * Delete the temporary files and Directory at the end of the program.
	 * @param tempDirectoryPath - Directory Path to where the temporary file will be stored.
	 * @param tempSplitDirectoryPath - Temporary Directory path to where temporary files after split is saved.
	 */
	private void cleanUp(String tempDirectoryPath, String tempSplitDirectoryPath) {
		try {
			List<Path> tempFilePaths = Files.walk(Paths.get(tempDirectoryPath))
					.filter(path -> Files.isRegularFile(path)).collect(Collectors.toList());
			for (Path path : tempFilePaths) {
				Files.deleteIfExists(path);
			}
			Files.deleteIfExists(Path.of(tempSplitDirectoryPath));
		} catch (IOException e) {
			System.out.println("Error while Clean up");
		}
	}
}
