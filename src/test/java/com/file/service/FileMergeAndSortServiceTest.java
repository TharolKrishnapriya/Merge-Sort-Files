package com.file.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileMergeAndSortServiceTest {
	
	private String inputDirectory = "src/test/resources/inputDir";
	private String inputDirectory_WithEmptyFiles = "src/test/resources/inputDir_EmptyFiles";
	private String tempMergeFile = "src/test/resources/tempMerge/UnsortedMergedFile.txt";
	private String empty_tempMergeFile = "src/test/resources/tempMerge/EmptyUnsortedMergedFile.txt";
	private String outputDirectory = "src/test/resources";
	private String subDirectory = "";
	private String subDirectory_Split = "";
	private long tempMergedFileSize_Expected = 0L;
	private List<String> expected_output = Arrays.asList(new String[] {
			"accidentals", "accuse", "acquaints", "adhesion", "affiliates", "airways",
			"apprising", "approbation", "assails", "autopsied", "babysit", "backlogged",
			"banjoes", "banjoes", "bikes", "blanche","burgher", "bypass", "camacho", "capstans",
			"cello", "cephalic", "characteristically", "characteristically", "characteristically", "cheered"
	});
	private List<String> expected_NoDuplicates_output = Arrays.asList(new String[] {
			"accidentals", "accuse", "acquaints", "adhesion", "affiliates", "airways",
			"apprising", "approbation", "assails", "autopsied", "babysit", "backlogged",
			"banjoes", "bikes", "blanche","burgher", "bypass", "camacho", "capstans",
			"cello", "cephalic", "characteristically", "cheered"
	});
	private List<Path> inputFilePaths = new ArrayList<>();
	private List<Path> empty_InputFilePaths = new ArrayList<>();
	private boolean removeDuplicates = false;
	
	@BeforeEach
	public void setup(@TempDir Path tempDir) throws IOException {
		Path subdirPath = tempDir.resolve("MergeService");
		Files.createDirectory(subdirPath);
		subDirectory = subdirPath.toString();
		subDirectory_Split = subDirectory + "\\split";
		tempMergedFileSize_Expected = Files.size(Paths.get(tempMergeFile));
		inputFilePaths = Files.walk(Paths.get(inputDirectory)).filter(Files::isRegularFile).collect(Collectors.toList());
		empty_InputFilePaths = Files.walk(Paths.get(inputDirectory_WithEmptyFiles)).filter(Files::isRegularFile).collect(Collectors.toList());
	}

	@Test
	@DisplayName("Check if the Temporary merged file contains all data")
	void test_TemporaryMergedFileSize() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.temproryMergeFiles(inputFilePaths, subDirectory);
		Path tempUnmergedFilePath = Files.walk(Paths.get(subDirectory), 1).filter(Files::isRegularFile).findFirst().get();
		assertEquals(tempMergedFileSize_Expected, Files.size(tempUnmergedFilePath));
	}
	
	@Test
	@DisplayName("Check for Exception when Input directory path is invalid")
	void test_InvalidInputDirecttoryPath() {
		String invalidInputDir = "src/test/resources/inputDirectory";
		assertThrows(Exception.class, () -> {
			FileMergeSort merge = new FileMergeSort();
			merge.listFilesFromDirectory(invalidInputDir);
		});
	}
	
	@Test
	@DisplayName("Check if temporary merged file size is 0 when input files are empty")
	void test_InputDirectoryWithEmptyFiles() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		String tempMergedFilePath = merge.temproryMergeFiles(empty_InputFilePaths, subDirectory);
		assertEquals(0, Files.size(Paths.get(tempMergedFilePath)));
	}
	
	@Test
	@DisplayName("Check if the Temporary Split directory is created")
	void test_CheckIfSortedTempSplitDirectoryCreated() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		assertTrue(Files.exists(Paths.get(subDirectory_Split)));
	}
	
	@Test
	@DisplayName("Check for Exception when Input files are empty")
	void test_SplitAndSortEmptyTempMergedFile() throws Exception {
		assertThrows(Exception.class, ()->{
			FileMergeSort merge = new FileMergeSort();
			merge.splitAndSort(empty_tempMergeFile, subDirectory_Split);
		});
	}
	
	@Test
	@DisplayName("Check if sorted temp files created after split")
	void test_CheckIfTempSortedFilesCreated() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		List<Path> tempFilePaths = Files.walk(Paths.get(subDirectory_Split)).filter(Files::isRegularFile).collect(Collectors.toList());
		assertTrue(tempFilePaths.size() >= 1);
	}
	
	@Test
	@DisplayName("Check output file is generated")
	void test_checkIfOutFileIsGenerated() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		merge.mergeSortedFiles(subDirectory_Split, outputDirectory,removeDuplicates);
		assertTrue(Files.exists(Paths.get(outputDirectory + "/output.dat")));
	}
	
	@Test
	@DisplayName("Check output file contains all data")
	void test_CheckIfOutputFileSizeMatchedTempMergedFileSize() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		merge.mergeSortedFiles(subDirectory_Split, outputDirectory,removeDuplicates);
		assertEquals(tempMergedFileSize_Expected, Files.size(Paths.get(outputDirectory+"/output.dat")));
	}
	
	@Test
	@DisplayName("Check if output file content is sorted")
	void test_CheckIfTheOutputIsSorted() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		merge.mergeSortedFiles(subDirectory_Split, outputDirectory,removeDuplicates);
		List<String> actualOutput = Files.lines(Paths.get(outputDirectory + "/output.dat"))
				.collect(Collectors.toList());
		assertTrue(actualOutput.equals(expected_output));
	}
	
	@Test
	@DisplayName("Check if output file content has duplicates")
	void test_CheckIfOutputFileHasDuplicates() throws Exception {
		FileMergeSort merge = new FileMergeSort();
		merge.splitAndSort(tempMergeFile, subDirectory_Split);
		merge.mergeSortedFiles(subDirectory_Split, outputDirectory,true);
		List<String> actualOutput = Files.lines(Paths.get(outputDirectory + "/output.dat"))
				.collect(Collectors.toList());
		assertTrue(actualOutput.equals(expected_NoDuplicates_output));
	}

	@AfterEach
	void deleteOutPutFile() throws IOException {
		Files.deleteIfExists(Paths.get("src/test/resources/output.dat"));
	}
}
