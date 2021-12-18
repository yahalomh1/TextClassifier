package com.textclassifier.search;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.textclassifier.search.files.CsvSearchEngine;
import com.textclassifier.search.files.FileSearchEngine;
import com.textclassifier.search.files.TxtSearchEngine;

import java.util.Optional;
import java.util.Set;

public class SearchEngine {
	private final static Logger logger = Logger.getLogger(SearchEngine.class.getName());
	private final static Map<String,FileSearchEngine> prefixesSearchEngine = new HashMap<>();
	// If we use Spring or Guava we can get it automatically by the platform. 
	static {
		prefixesSearchEngine.put("txt", new TxtSearchEngine());
		prefixesSearchEngine.put("csv", new CsvSearchEngine());
    }
			
	/**
	 * Recursively go over all the files (BFS) in this folder and scan them.
	 * @param file to scan. If a folder goes over all its files/sub folders
	 * @param contentsToSearch contains the domains and their indicators to look for
	 * @return a set of match domains
	 */
	public static Set<String> searchInAllRepository(File file,Map<String,List<String>> contentsToSearch){
		Set<String> foundDomains = new HashSet<>();
		// BFS scan on the paths.
		List<File> filesToScan = new ArrayList<>();
		filesToScan.add(file);
		while(!filesToScan.isEmpty()){
			file = filesToScan.remove(0); 
			if(file.isDirectory()){
				logger.log(Level.INFO,"{0} is a directory. Scan it.", file.getName());
				for(File fileInDirectory : file.listFiles()){
					filesToScan.add(fileInDirectory);
				}
			}
			else if(file.isFile()){
				Optional<String> fileType = getFileType(file);
				if(!fileType.isPresent()){
					logger.log(Level.WARNING,"Can't get file format from file {0}. Ignore it.", file.getName());
				}
				else{
					FileSearchEngine fileFormat = prefixesSearchEngine.get(fileType.get());
					if(fileFormat==null){
					   logger.log(Level.WARNING,"{0} is unsupport file format. Ignore it.", file.getName());								
					}
					else{
						foundDomains.addAll(fileFormat.searchDomainInFile(file, contentsToSearch,foundDomains));
						// If we already found the token in all the domains, no need to continue searching.
				        if(foundDomains.size() == contentsToSearch.size()){
				        	logger.log(Level.INFO,"Found all the domains. Stop searching");
				        	return foundDomains;
				        }
					}
				}
			}
			else{
				logger.log(Level.WARNING,"{0} is unsupported. Ignore it.", file.getName());
			}
		}
		return foundDomains;
	}
	
	/** Get the file type as lower case. Null if not exists. */
	private static Optional<String> getFileType(File file){
		String fileName = file.getName();
		int lastDotPosition = fileName.lastIndexOf('.');
		int lastSplashPosition = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
		 // For the case that a directory may have a '.', but the filename itself doesn't (e.q: /path/to.a/file */
		if (lastDotPosition > lastSplashPosition) {
		    return Optional.of(fileName.substring(lastDotPosition+1).toLowerCase());
		}
		return Optional.empty();
	}
}
