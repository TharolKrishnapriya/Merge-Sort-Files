package com.file.service;

import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * @author Tharol Krishnapriya
 * Sort and Merge files of a directory in a way that is efficient and scalable 
 */
public class FileMergeAndSortService {
		
	/**
	 * User Input:
	 * Input Directory Path
	 * Output Directory Path
	 * Temporary Directory Path
	 * Remove Duplicates - true/false
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner scanner = new Scanner(new InputStreamReader(System.in));
		System.out.println("Please enter Input directory Path : ");
		String inputDirectoryPath = scanner.nextLine();
		System.out.println("Please enter Output directory Path : ");
		String outputFilePath = scanner.nextLine();
		System.out.println("Please enter Temporary directory Path : ");
		String tempDirectoryPath = scanner.nextLine();
		System.out.println("Flag Remove Duplicates");
		boolean removeDiplicates = scanner.nextBoolean();
		FileMergeSort mergeSorter = new FileMergeSort();
		System.out.println("MERGING...");
		try {
			mergeSorter.mergeAndSortFiles(inputDirectoryPath, outputFilePath, tempDirectoryPath, removeDiplicates);
			System.out.println("MERGE COMPLETED");
		} catch (Exception e) {
			System.out.println("MERGE FAILED -- " + e.getMessage());
		}
	}
}

