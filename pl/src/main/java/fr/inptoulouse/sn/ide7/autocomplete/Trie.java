package fr.inptoulouse.sn.ide7.autocomplete;

import java.util.ArrayList;

public class Trie {
	private TrieNode root;
	
	
	public Trie() {
		root = new TrieNode();
	}
	
	public int getSize() {
		return this.root.getSize();
	}
	public void insert(String word) {
		TrieNode current = root;

	    for (char l: word.toCharArray()) {
	    	//current.setEndOfWord(false);
	        current = current.getChildren().computeIfAbsent(l, c -> new TrieNode());
	    }
	    current.setEndOfWord(true);
	    current.setFrequency(current.getFrequency() + 1);
	}
	
	public boolean isEmpty() {
		return this.root.getChildren().isEmpty();
	}
	
	public boolean find(String word) {
	    TrieNode current = root;
	    for (int i = 0; i < word.length(); i++) {
	        TrieNode node = current.getChildren().get(word.charAt(i));
	        if (node == null) {
	            return false;
	        }
	        
	        current = node;
	    }
	    return current.isEndOfWord();
	}
	
	public void delete(String word) {
	    delete(root, word, 0);
	}

	private boolean delete(TrieNode current, String word, int index) {
	    if (index == word.length()) {
	        if (!current.isEndOfWord()) {
	            return false;
	        }
	        current.setEndOfWord(false);
	        return current.getChildren().isEmpty();
	    }
	    char ch = word.charAt(index);
	    TrieNode node = current.getChildren().get(ch);
	    if (node == null) {
	        return false;
	    }
	    boolean shouldDeleteCurrentNode = delete(node, word, index + 1) && !node.isEndOfWord();

	    if (shouldDeleteCurrentNode) {
	        current.getChildren().remove(ch);
	        return current.getChildren().isEmpty();
	    }
	    return false;
	}
	public ArrayList<String> getFrequentWords(){
		if(this.isEmpty())
			return null;
		
		return this.root.getFrequentWords();
		
	}
	public int getMaxFrequency(){
		if(this.isEmpty())
			return 0;
		
		return this.root.getMaxFrequency(0);
		
	}
	
	public void printTrie() {
		System.out.println("TEST");
	}
	
}
