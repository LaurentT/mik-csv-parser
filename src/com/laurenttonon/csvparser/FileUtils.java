package com.laurenttonon.csvparser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtils {

	public Map<String, List<String>> parseCSV(File csvFile, File directoryOut) {
		try {
			FileReader in = new FileReader(csvFile);
			BufferedReader bufferedReader = new BufferedReader(in);
			String line = null;
			Map<String, List<String>> musicMap = new HashMap<String, List<String>>();
			List<String> musicFiles;

			// Regexp to find the file to copy
			Pattern filePattern = Pattern.compile("^(.*\")(.*\\.mp3)(\".*)$");
			// Regexp to find the key
			Pattern keyPattern = Pattern
					.compile("^(.*\")([[0-9]|10|11|12][a-bA-B])(\".*)$");

			while ((line = bufferedReader.readLine()) != null) {
				Matcher fileMatcher = filePattern.matcher(line);
				Matcher keyMatcher = keyPattern.matcher(line);

				// If both matches add the file to the hashmap
				if (fileMatcher.find() && keyMatcher.find()) {
					String musicKey = keyMatcher.group(2).toUpperCase();
					String musicFile = fileMatcher.group(2);
					musicFiles = musicMap.get(musicKey);
					if (musicFiles == null) {
						musicFiles = new ArrayList<String>();
					}
					musicFiles.add(musicFile);
					musicMap.put(musicKey, musicFiles);
				}
			}
			return musicMap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public Boolean copyFileToDir(File fileToCopy, File directoryOut) {
		try {
			FileInputStream finStream = new FileInputStream(fileToCopy);
			File fileToCreate = new File(directoryOut, fileToCopy.getName());
			System.out.println("The file to create is: "
					+ fileToCreate.getAbsolutePath());
			FileOutputStream foutStream = new FileOutputStream(fileToCreate);

			byte[] b = new byte[1024];

			int noOfBytes = 0;

			while ((noOfBytes = finStream.read(b)) != -1) {
				foutStream.write(b, 0, noOfBytes);
			}
			finStream.close();
			foutStream.flush();
			foutStream.close();

			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
