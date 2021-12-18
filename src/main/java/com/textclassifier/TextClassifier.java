package com.textclassifier;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.textclassifier.search.SearchEngine;

public class TextClassifier {
	private final static Logger logger = Logger.getLogger(SearchEngine.class.getName());
	
	private static final String CLASSIFICATION_RULE_KEY = "classification_rules";
	private static final String DOMAIN_KEY = "domain";
	private static final String INDICATORS_KEY = "indicators";

	public static void main(String[] args) {		
		if(args.length<2){
			System.out.println("Too few parameters.");
			System.out.println("TextClassifier filePath classificationFile");
			System.exit(1);
		}
		String repositoryFilePath = args[0];
		String classificationFilePath = args[1];
		
		File repositoryFile = new File(repositoryFilePath);
		if(!repositoryFile.exists()){
			System.out.println("File " + repositoryFilePath + " doesn't exist.");
			System.exit(1);
		}
		
		File classificationFile = new File(classificationFilePath);
		if(!classificationFile.exists()){
			System.out.println("Classification file " + classificationFilePath + " doesn't exist.");
			System.exit(1);
		}
		if(!classificationFile.isFile()){
			System.out.println(classificationFilePath + " isn't a normal file.");
			System.exit(1);
		}
		
		JSONParser parser = new JSONParser();
		JSONObject classification = null;
		try (FileReader fileReader = new FileReader(classificationFile)){
			 classification = (JSONObject)parser.parse(fileReader);			 			 
		} catch (IOException | ParseException e) {
			System.out.println(classificationFilePath + " isn't a valid json file.");
			System.exit(1);
		}
		
		if(!classification.containsKey(CLASSIFICATION_RULE_KEY)){
			 System.out.println(classificationFilePath + " doesn't contain " + CLASSIFICATION_RULE_KEY);
			 System.exit(1);
		}
		
			
		Set<String> foundDomains = searchClassificationInRepository(repositoryFile,(JSONArray)classification.get(CLASSIFICATION_RULE_KEY));
		foundDomains.forEach(foundDomain -> {System.out.println(foundDomain);});		
	}
	
	private static Set<String> searchClassificationInRepository(File repositoryFile,JSONArray classificationArray){
		// Convert classification Json object to Java map.
		// This way SearchEngine won't be depend on the user api.
		Map<String,List<String>> contentsToSearch = new HashMap<>();
		classificationArray.forEach(classificationObject -> {
			JSONObject classificationEntry = (JSONObject)classificationObject;
			if(!classificationEntry.containsKey(DOMAIN_KEY) || !classificationEntry.containsKey(INDICATORS_KEY)){
				logger.log(Level.WARNING,"Invalid classification rules entry. Ignore it. ({0})",classificationEntry);
			}
			else{					
				String domain = (String)classificationEntry.get(DOMAIN_KEY);
				List<String> indicators = new ArrayList<>();
				contentsToSearch.put(domain, indicators);
				((JSONArray)classificationEntry.get(INDICATORS_KEY)).forEach(indicator ->{
					indicators.add(((String)indicator).toLowerCase());					
				});				
			}
		});		
		
		return SearchEngine.searchInAllRepository(repositoryFile, contentsToSearch);
	}	
}
