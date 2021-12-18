package com.textclassifier.search.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Search for domains in a single text(txt) file. 
 */
public class TxtSearchEngine  implements FileSearchEngine {
	private final static Logger logger = Logger.getLogger(TxtSearchEngine.class.getName());
	
	@Override
	public Set<String> searchDomainInFile(File file, Map<String, List<String>> contentsToSearch,Set<String> excludeDomains) {		
		Set<String> foundDomains = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))){
			logger.log(Level.INFO,"Search in {0}.", file.getName());
			String previusLine = "";
		    String currentLine;
		    // Read the current line from the file.
		    // The search will be done over the last 2 lines, 
		    // so, if the indicator is split between 2 lines it still be found.
		    while ((currentLine = br.readLine()) != null) {
		    	currentLine = currentLine.trim().toLowerCase();
		        String lineToSearch = previusLine + ' ' + currentLine;
		        foundDomains.addAll(SearchLineForIndicators(lineToSearch,contentsToSearch,excludeDomains));
		        // If we already found the token in all the domains, no need to continue searching in this file.		        		      
		        if(foundDomains.size() + + excludeDomains.size() == contentsToSearch.size()){
		        	logger.log(Level.INFO,"{0}: Found all the left domains. Stop searching", file.getName());
		        	return foundDomains;
		        }
		        previusLine = currentLine;
		    }		    
		}	
		catch(IOException exception){
			logger.log(Level.WARNING,"{0} can't be read. Ignore it.", file.getName());
		}
		return foundDomains;
	}
	
	/** Go over all the indicators and find if they exist in the given line. */
	private static Set<String> SearchLineForIndicators(String lineToSearch,
			Map<String, List<String>> contentsToSearch, Set<String> excludeDomains) {		
		Set<String> foundDomains = new HashSet<>();
		contentsToSearch.keySet().forEach(domain -> {
		  if(!excludeDomains.contains(domain)){
			  contentsToSearch.get(domain).forEach(indicator -> {
				if(lineToSearch.indexOf(indicator)!=-1){
					foundDomains.add(domain);
				}
			});
		  }
		});		
		return foundDomains;
	}
}
