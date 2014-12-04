package SuffixArray;

import java.util.Iterator;
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
	private TreeSet<String> rules;
	// 前のSuffixとの一致数
	private int LCP;
	/**
	 * Mapで使っているならtrue Listで使っているならfalse
	 */
	private boolean mode_flag;

	/**
	 * Mapで使う場合のコンストラクタ
	 */
	SuffixData() {
		words = new TreeSet<String>();
		sentences = new TreeSet<String>();
		rules = new TreeSet<String>();
		mode_flag = true;
	}

	/**
	 * Listで使う場合のコンストラクタ
	 * @param LCP
	 */
	SuffixData(int LCP) {
		words = new TreeSet<String>();
		sentences = new TreeSet<String>();
		rules = new TreeSet<String>();
		this.LCP = LCP;
		mode_flag = false;
	}
	
	void add_word(String word){
		words.add(word);
	}
	
	void add_sentence(String sentence){
		sentences.add(sentence);
	}
	
	void add_rule(Rule rule){
		rules.add(rule.getName());
	}

	Iterator get_words() {
		return words.iterator();
	}

	Iterator get_sentences() {
		return sentences.iterator();
	}

	Iterator get_rules() {
		return rules.iterator();
	}

	int get_LCP() {
		return LCP;
	}
}
