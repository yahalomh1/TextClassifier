package com.textclassifier.search.files;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for scanning a generic single file.
 */
public interface FileSearchEngine {	
	/** 
	 * Does a content exist in the file dictionary.
	 * @param file to look in
	 * @param contentsToSearch contains the domains and their indicators
	 * @param excludeDomains domains to exclude from search
	 * @return set of domains that were matched.
	 */
	Set<String> searchDomainInFile(File file, Map<String,List<String>> contentsToSearch, Set<String> excludeDomains);
}
