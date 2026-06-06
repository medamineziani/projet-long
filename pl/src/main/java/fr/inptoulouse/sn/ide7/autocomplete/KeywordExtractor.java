package fr.inptoulouse.sn.ide7.autocomplete;

import java.io.File;                  // Import the File class
import java.io.FileNotFoundException; // Import this class to handle errors
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;             // Import the Scanner class to read text files


public class KeywordExtractor {
	
	private Map<String, Integer> keywordsList;
	private Trie keywordsTrie;
	private final String splitRegex= "[,\\.\\s\\;\\:\\<\\>\\{\\}\\[\\]\\(\\)\\=\\-\\+\\/\\!\\@\\&\\|\\\"\\']";
	
	public KeywordExtractor() {
		this.keywordsList = new HashMap<String, Integer>();
		this.keywordsTrie = new Trie(); 
	}
	// Extract all occurrences of keywords (ie connected words)
	public void extractFromFile(String filename) {
	    File myObj = new File(filename);

	    // try-with-resources: Scanner will be closed automatically
	    try (Scanner myReader = new Scanner(myObj)) {
	      while (myReader.hasNextLine()) {
	        String[] keywords = myReader.nextLine().split(this.splitRegex);
	        //System.out.println(keywords.length);
    		if(keywords.length > 1)
	        for(String keyword : keywords)
	        {
	        	if(!keyword.contains("\n") && keyword.length() >2 && !keyword.isBlank() && !keyword.isEmpty())
	        	{	
	        		if(keywordsList.containsKey(keyword))
	        			keywordsList.put(keyword, keywordsList.get(keyword) + 1);
	        		else
	        		{
	        			keywordsList.put(keyword, 1);
	        			System.out.println(keyword);
	        		}
	        		keywordsTrie.insert(keyword);
	        	}
	        }
	      }
	    } catch (FileNotFoundException e) {
	      System.out.println("An error occurred.");
	      e.printStackTrace();
	    }
	  }
	
	public Map<String, Integer> getKeywordsDictionary(){
		return this.keywordsList;
	}
	public Trie getKeywordsTrie(){
		return this.keywordsTrie;
	}
}
