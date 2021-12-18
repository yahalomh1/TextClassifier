package com.textclassifier.search.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Search for domains in a single csv file. 
 */
public class CsvSearchEngine implements FileSearchEngine{

	private final static Logger logger = Logger.getLogger(CsvSearchEngine.class.getName());
	private final String COMMA_DELIMITER = ",";

	@Override
	public Set<String> searchDomainInFile(File file, Map<String,List<String>> contentsToSearch, Set<String> excludeDomains){
		Set<String> foundDomains = new HashSet<>();
		try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsolutePath()))){
			logger.log(Level.INFO,"Search in {0}.", file.getName());
		    String line;
		    while ((line = br.readLine()) != null) {
		        String[] tokens = line.split(COMMA_DELIMITER);
		        for(String token: tokens){
		        	foundDomains.addAll(SearchTokenInIndicators(normalizeToken(token),contentsToSearch,excludeDomains));
		        	// If we already found the token in all the domains, no need to continue searching in this file.
			        if(foundDomains.size() + excludeDomains.size() == contentsToSearch.size()){
			        	logger.log(Level.INFO,"{0}: Found all the left domains. Stop searching",file.getName());
			        	return foundDomains;
			        }
		        }		        
		    }		    
		}	
		catch(IOException exception){
			logger.log(Level.WARNING,"{0} can't be read. Ignore it.", file.getName());
		}
		return foundDomains;
	}
	
	/** Go over all the indicators and find the match domain of this token. */
	private static Set<String> SearchTokenInIndicators(String normalizeToken,
			Map<String, List<String>> contentsToSearch,Set<String> excludeDomains) {
		Set<String> foundDomains = new HashSet<>();
		contentsToSearch.keySet().forEach(domain -> {
			if(!excludeDomains.contains(domain)){
			  contentsToSearch.get(domain).forEach(indicator -> {
				 if(normalizeToken.equals(indicator)){
					foundDomains.add(domain);
				}
			  });
		   }
		});		
		return foundDomains;
	}

	/** 
	 * Normalized the token. 
	 * Translate it to lower case, remove trailing blanks and wrapped quotes.
	 */
	private static String normalizeToken(String token){
		token = token.trim().toLowerCase();
		if(token.length()>1 && token.charAt(0)=='"' && token.charAt(token.length()-1)=='"'){
			token = token.substring(1,token.length()-1).trim();
		}
		return token;
	}	
}

