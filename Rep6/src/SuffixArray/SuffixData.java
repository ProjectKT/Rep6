package SuffixArray;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Suffixのクラス
 * 
 * @author cjh15110
 */
public class SuffixData {

	// 対応する単語
	private TreeSet<String> words;
	// 対応する文
	private TreeSet<String> sentences;
	// 対応するルール
	private TreeMap<String,Rule> rules;
	// 前のSuffixとの一致数
	private int LCP;
	/**
	 * Mapで使っているならtrue Listで使っているならfalse
	 */
	private boolean modeFlag;

	/**
	 * Mapで使う場合のコンストラクタ
	 */
	SuffixData() {
		words = new TreeSet<String>();
		sentences = new TreeSet<String>();
		rules = new TreeMap<String,Rule>();
		modeFlag = true;
	}

	/**
	 * Listで使う場合のコンストラクタ
	 * @param LCP
	 */
	SuffixData(int LCP) {
		words = new TreeSet<String>();
		sentences = new TreeSet<String>();
		rules = new TreeMap<String,Rule>();
		this.LCP = LCP;
		modeFlag = false;
	}
	
	void addWord(String word){
		words.add(word);
	}
	
	void addSentence(String sentence){
		sentences.add(sentence);
	}
	
	void addRule(Rule rule){
		if(rule != null)
		rules.put(rule.getName(),rule);
	}

	Iterator<String> getWords() {
		return words.iterator();
	}

	Iterator<String> getSentences() {
		return sentences.iterator();
	}

	Iterator<Entry<String, Rule>> getRules() {
		return rules.entrySet().iterator();
	}

	int getLCP() {
		return LCP;
	}
}
