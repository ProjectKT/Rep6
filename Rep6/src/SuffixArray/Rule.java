package SuffixArray;

import java.util.ArrayList;

/**
 * ルールを表すクラス．
 * 
 * 
 */
class Rule {
	String name;
	ArrayList<String> antecedents;
	String consequent;

	Rule(String theName, ArrayList<String> theAntecedents, String theConsequent) {
		this.name = theName;
		this.antecedents = theAntecedents;
		this.consequent = theConsequent;
	}

	/**
	 * ルールの名前を返す．
	 * 
	 * @return 名前を表す String
	 */
	public String getName() {
		return name;
	}

	/**
	 * ルールをString形式で返す
	 * 
	 * @return ルールを整形したString
	 */
	public String toString() {
		return name + " " + antecedents.toString() + "->" + consequent;
	}

	/**
	 * ルールの前件を返す．
	 * 
	 * @return 前件を表す ArrayList
	 */
	public ArrayList<String> getAntecedents() {
		return antecedents;
	}

	/**
	 * ルールの後件を返す．
	 * 
	 * @return 後件を表す String
	 */
	public String getConsequent() {
		return consequent;
	}

}