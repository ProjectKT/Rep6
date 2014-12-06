package providers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

public class SuffixArray {

	// TreeMapでSuffixArrayを表現
	// キーは各Suffix、Suffixのリンクなどの情報はクラスSuffixに格納
	private TreeMap<String, SuffixData> SuffixArray = new TreeMap<String, SuffixData>();

	// 投げられた文字列で始まるsuffixsを格納するTreeSet
	private TreeSet<String> suffixs = new TreeSet<String>();
	
	/**
	 * ワーキングメモリを受け取りSuffixArrayに追加処理　新しいワーキングメモリを追加する時用
	 * @param wm
	 */
	public void addSuffixWm(String wm){
		// 受け取った文を単語に分割
		String[] wordlist = wm.split(" ");
		String sentence= "?x";
		for (int i = 1; i < wordlist.length; i++) {
			// 分割した単語のそれぞれを追加(先頭は固有名詞なので無視)
			addSuffixWord(wordlist[i],null);
			sentence = sentence + " " + wordlist[i];
		}
		//wmにある知識の固有名詞部を?xに変えてSuffixArrayに追加
		addSuffixSentence(sentence,null);
	}

	/**
	 * ルールを受け取りSuffixArrayに追加処理 新しいルールを追加する時用
	 * 
	 * @param rule
	 */
	public void addSuffixRule(Rule rule) {
		ArrayList<String> antecedents = rule.getAntecedents();
		for (int i = 0; i < antecedents.size(); i++) {
			// 前件を登録
			addSuffixSentence(antecedents.get(i), rule);
		}
		// 後件を登録
		addSuffixSentence(rule.getConsequent(), rule);
	}

	/**
	 * 文を受け取りSuffixArrayを追加処理
	 * 
	 * @param sentence
	 *            「?x is a foreign car」のような文章
	 * @param rule
	 */
	private void addSuffixSentence(String sentence, Rule rule) {

		// まず文をSuffixArrayに追加する
		for (int i = 0; i < sentence.length(); i++) {
			putSuffixSentence(sentence.substring(i), sentence);
			putSuffixRule(sentence.substring(i), rule);
		}

		// 受け取った文を単語に分割
		String[] wordlist = sentence.split(" ");
		for (int i2 = 0; i2 < wordlist.length; i2++) {
			// 分割した単語のそれぞれを追加
			addSuffixWord(wordlist[i2], rule);
		}
	}

	/**
	 * 単語を受け取りSuffixArrayを追加処理
	 * 
	 * @param word
	 * @param rule
	 */
	private void addSuffixWord(String word, Rule rule) {
		for (int i = 0; i < word.length(); i++) {
			putSuffixWord(word.substring(i), word);
			putSuffixRule(word.substring(i), rule);
		}
	}

	/**
	 * SuffixDataに新しいwordを追加する
	 * 
	 * @param suffix
	 * @param string
	 */
	private void putSuffixWord(String suffix, String word) {
		// 新しいSuffixならSuffixArrayにput
		if (!SuffixArray.containsKey(suffix)) {
			SuffixData data = new SuffixData();
			SuffixArray.put(suffix, data);
		}

		SuffixArray.get(suffix).addWord(word);
	}
	
	/**
	 * SuffixDataに新しいはsentenceを追加する
	 * 
	 * @param suffix
	 * @param string
	 */
	private void putSuffixSentence(String suffix, String sentence) {
		// 新しいSuffixならSuffixArrayにput
		if (!SuffixArray.containsKey(suffix)) {
			SuffixData data = new SuffixData();
			SuffixArray.put(suffix, data);
		}

		SuffixArray.get(suffix).addSentence(sentence);
	}

	/**
	 * SuffixDataに新しいルールを追加する
	 * 
	 * @param suffix
	 * @param rule
	 */
	private void putSuffixRule(String suffix, Rule rule) {
		SuffixArray.get(suffix).addRule(rule);
	}

	private Iterator<String> setSuffixTree(String word) {
		int wordl = word.length();
		suffixs.clear();
		boolean end = false;
		for (Iterator<Entry<String,SuffixData>> it = SuffixArray.entrySet().iterator(); it.hasNext();) {
			Entry<String,SuffixData> entry = it.next();
			String key = (String) entry.getKey();
			if (key.length() >= word.length()) {
				if (key.substring(0, wordl).equalsIgnoreCase(word)) {
					end = true;
					suffixs.add(key);
				} else {
					if (end) {
						continue;
					}
				}
			}
		}
		return suffixs.iterator();
	}

	/**
	 * 受け取ったsuffixを含むルールを返す
	 * 
	 * @param suffix
	 * @return
	 */
	public Iterator<Entry<String,Rule>> getRules(String word) {
		TreeMap<String,Rule> rules = new TreeMap<String,Rule>();
		Iterator<String> it = setSuffixTree(word);
		while (it.hasNext()) {
			Iterator<Entry<String, Rule>> it2 = SuffixArray.get(it.next()).getRules();
			while (it2.hasNext()) {
				Entry<String, Rule> entry = it2.next();
				String key = (String) entry.getKey();
			    Rule value = (Rule) entry.getValue();
			    rules.put(key,value);
			}
		}
		return rules.entrySet().iterator();

	}

	/**
	 * 受け取ったsuffixを含む文を返す
	 * 
	 * @param suffix
	 * @return
	 */
	public Iterator<String> getSentences(String word) {
		TreeSet<String> sentences = new TreeSet<String>();
		Iterator<String> it = setSuffixTree(word);
		while (it.hasNext()) {
			Iterator<String> it2 = SuffixArray.get(it.next()).getSentences();
			while (it2.hasNext()) {
				sentences.add(it2.next());
			}
		}
		return sentences.iterator();

	}

	/**
	 * 受け取ったsuffixを含む単語を返す
	 * 
	 * @param suffix
	 * @return
	 */
	public Iterator<String> getWords(String word) {
		TreeSet<String> words = new TreeSet<String>();
		Iterator<String> it = setSuffixTree(word);
		while (it.hasNext()) {
			Iterator<String> it2 = SuffixArray.get(it.next()).getWords();
			while (it2.hasNext()) {
				words.add(it2.next());
			}
		}
		return words.iterator();

	}

}