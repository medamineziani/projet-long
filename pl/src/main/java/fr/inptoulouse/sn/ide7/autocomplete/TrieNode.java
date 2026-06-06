package fr.inptoulouse.sn.ide7.autocomplete;

import java.util.ArrayList;
import java.util.HashMap;

public class TrieNode {
	private HashMap<Character, TrieNode> children;
    private String content;
    private boolean isWord = false;
    private int frequency = 0;
    
    
    public TrieNode() {
       	this.children = new HashMap<Character, TrieNode>();
    }
    
    public void setEndOfWord(boolean val) {
    	this.isWord	= val;
    }
    public void setFrequency(int val) {
    	this.frequency = val;
    }
    public int getFrequency() {
    	return this.frequency;
    }
    public boolean isEndOfWord() {
    	return this.isWord;
    }
    
    public HashMap<Character, TrieNode> getChildren() {
    	return this.children;
    }
    
    public int getSize() {
		int stack = isEndOfWord() ? 1 : 0;
		for(TrieNode n : children.values()) {
			if( n != null)
				stack += n.getSize();
		}
		return stack;
	}
    public int getMaxFrequency(int freq) {
		int maxFreq = isEndOfWord() && freq < this.frequency ? this.frequency : freq;
		for(TrieNode n : children.values()) {
			if(n != null) {
				int h = n.getMaxFrequency(maxFreq);
				if(maxFreq < h)
					maxFreq = h;
			}
		}
		return maxFreq;
	}
    
    public String getFrequentWord(int freq) {
		return null;
	}

	public ArrayList<String> getFrequentWords() {
		// TODO Auto-generated method stub
		return null;
	}
}
