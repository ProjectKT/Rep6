package SuffixArray;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
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
	public void add_Suffix_wm(String wm){
		// 受け取った文を単語に分割
		String[] wordlist = wm.split(" ");
		String sentence= "?x";
		for (int i = 1; i < wordlist.length; i++) {
			// 分割した単語のそれぞれを追加(先頭は固有名詞なので無視)
			add_Suffix_word(wordlist[i],null);
			sentence = sentence + " " + wordlist[i];
		}
		//wmにある知識の固有名詞部を?xに変えてSuffixArrayに追加
		add_Suffix_sentence(sentence,null);
	}

	/**
	 * ルールを受け取りSuffixArrayに追加処理 新しいルールを追加する時用
	 * 
	 * @param rule
	 */
	public void add_Suffix_rule(Rule rule) {
		ArrayList<String> antecedents = rule.getAntecedents();
		for (int i = 0; i < antecedents.size(); i++) {
			// 前件を登録
			add_Suffix_sentence(antecedents.get(i), rule);
		}
		// 後件を登録
		add_Suffix_sentence(rule.getConsequent(), rule);
	}

	/**
	 * 文を受け取りSuffixArrayを追加処理
	 * 
	 * @param sentence
	 *            「?x is a foreign car」のような文章
	 * @param rule
	 */
	private void add_Suffix_sentence(String sentence, Rule rule) {

		// まず文をSuffixArrayに追加する
		for (int i = 0; i < sentence.length(); i++) {
			put_Suffix_sentence(sentence.substring(i), sentence);
			put_Suffix_rule(sentence.substring(i), rule);
		}

		// 受け取った文を単語に分割
		String[] wordlist = sentence.split(" ");
		for (int i2 = 0; i2 < wordlist.length; i2++) {
			// 分割した単語のそれぞれを追加
			add_Suffix_word(wordlist[i2], rule);
		}
	}

	/**
	 * 単語を受け取りSuffixArrayを追加処理
	 * 
	 * @param word
	 * @param rule
	 */
	private void add_Suffix_word(String word, Rule rule) {
		for (int i = 0; i < word.length(); i++) {
			put_Suffix_word(word.substring(i), word);
			put_Suffix_rule(word.substring(i), rule);
		}
	}

	/**
	 * SuffixDataに新しいwordを追加する
	 * 
	 * @param suffix
	 * @param string
	 */
	private void put_Suffix_word(String suffix, String word) {
		// 新しいSuffixならSuffixArrayにput
		if (!SuffixArray.containsKey(suffix)) {
			SuffixData data = new SuffixData();
			SuffixArray.put(suffix, data);
		}

		SuffixArray.get(suffix).add_word(word);
	}
	
	/**
	 * SuffixDataに新しいはsentenceを追加する
	 * 
	 * @param suffix
	 * @param string
	 */
	private void put_Suffix_sentence(String suffix, String sentence) {
		// 新しいSuffixならSuffixArrayにput
		if (!SuffixArray.containsKey(suffix)) {
			SuffixData data = new SuffixData();
			SuffixArray.put(suffix, data);
		}

		SuffixArray.get(suffix).add_sentence(sentence);
	}

	/**
	 * SuffixDataに新しいルールを追加する
	 * 
	 * @param suffix
	 * @param rule
	 */
	private void put_Suffix_rule(String suffix, Rule rule) {
		SuffixArray.get(suffix).add_rule(rule);
	}

	private Iterator set_suffix_tree(String word) {
		int wordl = word.length();
		suffixs.clear();
		boolean end = false;
		for (Iterator it = SuffixArray.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
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
	public Iterator get_rules(String word) {
		TreeMap<String,Rule> rules = new TreeMap<String,Rule>();
		Iterator it = set_suffix_tree(word);
		while (it.hasNext()) {
			Iterator it2 = SuffixArray.get(it.next()).get_rules();
			while (it2.hasNext()) {
				Map.Entry entry = (Map.Entry)it2.next();
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
	public Iterator get_sentences(String word) {
		TreeSet<String> sentences = new TreeSet<String>();
		Iterator it = set_suffix_tree(word);
		while (it.hasNext()) {
			Iterator it2 = SuffixArray.get(it.next()).get_sentences();
			while (it2.hasNext()) {
				sentences.add((String) it2.next());
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
	public Iterator get_words(String word) {
		TreeSet<String> words = new TreeSet<String>();
		Iterator it = set_suffix_tree(word);
		while (it.hasNext()) {
			Iterator it2 = SuffixArray.get(it.next()).get_words();
			while (it2.hasNext()) {
				words.add((String) it2.next());
			}
		}
		return words.iterator();

	}

}
