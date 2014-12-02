import java.util.*;
import java.io.*;

public class RuleBaseSystem {
	static RuleBase rb;
	static FileManager fm;

	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("Usage: %java RuleBaseSystem [query strings]");
			System.out.println("Example:");
			System.out.println(" \"?x is b\" and \"?x is c\" are queries");
			System.out.println("  %java RuleBaseSystem \"?x is b,?x is c\"");
		} else {
			fm = new FileManager();
			ArrayList<Rule> rules = fm.loadRules("CarShop.data");
			// ArrayList rules = fm.loadRules("AnimalWorld.data");
			ArrayList<String> wm = fm.loadWm("CarShopWm.data");
			// ArrayList wm = fm.loadWm("AnimalWorldWm.data");
			rb = new RuleBase(rules, wm);
			StringTokenizer st = new StringTokenizer(args[0], ",");
			ArrayList<String> queries = new ArrayList<String>();
			for (int i = 0; i < st.countTokens();) {
				queries.add(st.nextToken());
			}
			rb.backwardChain(queries);
		}
	}
}

class RuleBase implements Serializable {
	String fileName;
	ArrayList<String> wm;
	ArrayList<Rule> rules;

	RuleBase(ArrayList<Rule> theRules, ArrayList<String> theWm) {
		wm = theWm;
		rules = theRules;
	}

	public void setWm(ArrayList<String> theWm) {
		wm = theWm;
	}

	public void setRules(ArrayList<Rule> theRules) {
		rules = theRules;
	}

	public void backwardChain(ArrayList<String> hypothesis) {
		System.out.println("Hypothesis:" + hypothesis);
		ArrayList<String> orgQueries = (ArrayList) hypothesis.clone();
		// HashMap<String,String> binding = new HashMap<String,String>();
		HashMap<String, String> binding = new HashMap<String, String>();
		if (matchingPatterns(hypothesis, binding)) {
			System.out.println("Yes");
			System.out.println(binding);
			// 最終的な結果を基のクェリーに代入して表示する
			for (int i = 0; i < orgQueries.size(); i++) {
				String aQuery = (String) orgQueries.get(i);
				System.out.println("binding: " + binding);
				String anAnswer = instantiate(aQuery, binding);
				System.out.println("Query: " + aQuery);
				System.out.println("Answer:" + anAnswer);
			}
		} else {
			System.out.println("No");
		}
	}

	/**
	 * マッチするワーキングメモリのアサーションとルールの後件 に対するバインディング情報を返す
	 */
	private boolean matchingPatterns(ArrayList<String> thePatterns,
			HashMap<String, String> theBinding) {
		String firstPattern;
		if (thePatterns.size() == 1) {
			firstPattern = (String) thePatterns.get(0);
			if (matchingPatternOne(firstPattern, theBinding, 0) != -1) {
				return true;
			} else {
				return false;
			}
		} else {
			firstPattern = (String) thePatterns.get(0);
			thePatterns.remove(0);

			int cPoint = 0;
			while (cPoint < wm.size() + rules.size()) {
				// 元のバインディングを取っておく
				HashMap<String, String> orgBinding = new HashMap<String, String>();
				for (Iterator<String> i = theBinding.keySet().iterator(); i
						.hasNext();) {
					String key = i.next();
					String value = (String) theBinding.get(key);
					orgBinding.put(key, value);
				}
				int tmpPoint = matchingPatternOne(firstPattern, theBinding,
						cPoint);
				System.out.println("tmpPoint: " + tmpPoint);
				if (tmpPoint != -1) {
					System.out.println("Success:" + firstPattern);
					if (matchingPatterns(thePatterns, theBinding)) {
						// 成功
						return true;
					} else {
						// 失敗
						// choiceポイントを進める
						cPoint = tmpPoint;
						// 失敗したのでバインディングを戻す
						theBinding.clear();
						for (Iterator<String> i = orgBinding.keySet()
								.iterator(); i.hasNext();) {
							String key = i.next();
							String value = orgBinding.get(key);
							theBinding.put(key, value);
						}
					}
				} else {
					// 失敗したのでバインディングを戻す
					theBinding.clear();
					for (Iterator<String> i = orgBinding.keySet().iterator(); i
							.hasNext();) {
						String key = i.next();
						String value = orgBinding.get(key);
						theBinding.put(key, value);
					}
					return false;
				}
			}
			return false;
			/*
			 * if(matchingPatternOne(firstPattern,theBinding)){ return
			 * matchingPatterns(thePatterns,theBinding); } else { return false;
			 * }
			 */
		}
	}

	private int matchingPatternOne(String thePattern,
			HashMap<String, String> theBinding, int cPoint) {
		if (cPoint < wm.size()) {
			// WME(Working Memory Elements) と Unify してみる．
			for (int i = cPoint; i < wm.size(); i++) {
				if ((new Unifier()).unify(thePattern, (String) wm.get(i),
						theBinding)) {
					System.out.println("Success WM");
					System.out.println((String) wm.get(i) + " <=> "
							+ thePattern);
					return i + 1;
				}
			}
		}
		if (cPoint < wm.size() + rules.size()) {
			// Ruleと Unify してみる．
			for (int i = cPoint; i < rules.size(); i++) {
				Rule aRule = rename((Rule) rules.get(i));
				// 元のバインディングを取っておく．
				HashMap<String, String> orgBinding = new HashMap<String, String>();
				for (Iterator<String> itr = theBinding.keySet().iterator(); itr
						.hasNext();) {
					String key = itr.next();
					String value = theBinding.get(key);
					orgBinding.put(key, value);
				}
				if ((new Unifier()).unify(thePattern,
						(String) aRule.getConsequent(), theBinding)) {
					System.out.println("Success RULE");
					System.out.println("Rule:" + aRule + " <=> " + thePattern);
					// さらにbackwardChaining
					ArrayList<String> newPatterns = aRule.getAntecedents();
					if (matchingPatterns(newPatterns, theBinding)) {
						return wm.size() + i + 1;
					} else {
						// 失敗したら元に戻す．
						theBinding.clear();
						for (Iterator<String> itr = orgBinding.keySet()
								.iterator(); itr.hasNext();) {
							String key = itr.next();
							String value = orgBinding.get(key);
							theBinding.put(key, value);
						}
					}
				}
			}
		}
		return -1;
	}

	/**
	 * 与えられたルールの変数をリネームしたルールのコピーを返す．
	 * 
	 * @param 変数をリネームしたいルール
	 * @return 変数がリネームされたルールのコピーを返す．
	 */
	int uniqueNum = 0;

	private Rule rename(Rule theRule) {
		Rule newRule = theRule.getRenamedRule(uniqueNum);
		uniqueNum = uniqueNum + 1;
		return newRule;
	}

	private String instantiate(String thePattern,
			HashMap<String, String> theBindings) {
		String result = new String();
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				result = result + " " + (String) theBindings.get(tmp);
				System.out.println("tmp: " + tmp + ", result: " + result);
			} else {
				result = result + " " + tmp;
			}
		}
		return result.trim();
	}

	private boolean var(String str1) {
		// 先頭が ? なら変数
		return str1.startsWith("?");
	}
}

class FileManager {
	FileReader f;
	StreamTokenizer st;

	public ArrayList<Rule> loadRules(String theFileName) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		String line;
		try {
			int token;
			f = new FileReader(theFileName);
			st = new StreamTokenizer(f);
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
				switch (token) {
				case StreamTokenizer.TT_WORD:
					String name = null;
					ArrayList<String> antecedents = null;
					String consequent = null;
					if ("rule".equals(st.sval)) {
						st.nextToken();
						name = st.sval;
						st.nextToken();
						if ("if".equals(st.sval)) {
							antecedents = new ArrayList<String>();
							st.nextToken();
							while (!"then".equals(st.sval)) {
								antecedents.add(st.sval);
								st.nextToken();
							}
							if ("then".equals(st.sval)) {
								st.nextToken();
								consequent = st.sval;
							}
						}
					}
					rules.add(new Rule(name, antecedents, consequent));
					break;
				default:
					System.out.println(token);
					break;
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return rules;
	}

	public ArrayList<String> loadWm(String theFileName) {
		ArrayList<String> wm = new ArrayList<String>();
		String line;
		try {
			int token;
			f = new FileReader(theFileName);
			st = new StreamTokenizer(f);
			st.eolIsSignificant(true);
			st.wordChars('\'', '\'');
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
				line = new String();
				while (token != StreamTokenizer.TT_EOL) {
					line = line + st.sval + " ";
					token = st.nextToken();
				}
				wm.add(line.trim());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return wm;
	}
}

/**
 * ルールを表すクラス．
 */
class Rule implements Serializable {
	String name;
	ArrayList<String> antecedents;
	String consequent;

	Rule(String theName, ArrayList<String> theAntecedents, String theConsequent) {
		this.name = theName;
		this.antecedents = theAntecedents;
		this.consequent = theConsequent;
	}

	public Rule getRenamedRule(int uniqueNum) {
		ArrayList<String> vars = new ArrayList<String>();
		for (int i = 0; i < antecedents.size(); i++) {
			String antecedent = (String) this.antecedents.get(i);
			vars = getVars(antecedent, vars);
		}
		vars = getVars(this.consequent, vars);
		HashMap<String, String> renamedVarsTable = makeRenamedVarsTable(vars,
				uniqueNum);

		ArrayList<String> newAntecedents = new ArrayList<String>();
		for (int i = 0; i < antecedents.size(); i++) {
			String newAntecedent = renameVars((String) antecedents.get(i),
					renamedVarsTable);
			newAntecedents.add(newAntecedent);
		}
		String newConsequent = renameVars(consequent, renamedVarsTable);

		Rule newRule = new Rule(name, newAntecedents, newConsequent);
		return newRule;
	}

	private ArrayList<String> getVars(String thePattern, ArrayList<String> vars) {
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				vars.add(tmp);
			}
		}
		return vars;
	}

	private HashMap<String, String> makeRenamedVarsTable(
			ArrayList<String> vars, int uniqueNum) {
		HashMap<String, String> result = new HashMap<String, String>();
		for (int i = 0; i < vars.size(); i++) {
			String newVar = (String) vars.get(i) + uniqueNum;
			result.put((String) vars.get(i), newVar);
		}
		return result;
	}

	private String renameVars(String thePattern,
			HashMap<String, String> renamedVarsTable) {
		String result = new String();
		StringTokenizer st = new StringTokenizer(thePattern);
		for (int i = 0; i < st.countTokens();) {
			String tmp = st.nextToken();
			if (var(tmp)) {
				result = result + " " + renamedVarsTable.get(tmp);
			} else {
				result = result + " " + tmp;
			}
		}
		return result.trim();
	}

	private boolean var(String str) {
		// 先頭が ? なら変数
		return str.startsWith("?");
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name + " " + antecedents.toString() + "->" + consequent;
	}

	public ArrayList<String> getAntecedents() {
		return antecedents;
	}

	public String getConsequent() {
		return consequent;
	}
}

class Unifier {
	StringTokenizer st1;
	String buffer1[];
	StringTokenizer st2;
	String buffer2[];
	HashMap<String, String> vars;

	Unifier() {
		// vars = new HashMap();
	}

	public boolean unify(String string1, String string2,
			HashMap<String, String> theBindings) {
		HashMap<String, String> orgBindings = new HashMap<String, String>();
		for (Iterator<String> i = theBindings.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			String value = theBindings.get(key);
			orgBindings.put(key, value);
		}
		this.vars = theBindings;
		if (unify(string1, string2)) {
			return true;
		} else {
			// 失敗したら元に戻す．
			theBindings.clear();
			for (Iterator<String> i = orgBindings.keySet().iterator(); i
					.hasNext();) {
				String key = i.next();
				String value = orgBindings.get(key);
				theBindings.put(key, value);
			}
			return false;
		}
	}

	public boolean unify(String string1, String string2) {
		// 同じなら成功
		if (string1.equals(string2))
			return true;

		// 各々トークンに分ける
		st1 = new StringTokenizer(string1);
		st2 = new StringTokenizer(string2);

		// 数が異なったら失敗
		if (st1.countTokens() != st2.countTokens())
			return false;

		// 定数同士
		int length = st1.countTokens();
		buffer1 = new String[length];
		buffer2 = new String[length];
		for (int i = 0; i < length; i++) {
			buffer1[i] = st1.nextToken();
			buffer2[i] = st2.nextToken();
		}

		// 初期値としてバインディングが与えられていたら
		if (this.vars.size() != 0) {
			for (Iterator<String> i = vars.keySet().iterator(); i.hasNext();) {
				String key = i.next();
				String value = vars.get(key);
				replaceBuffer(key, value);
			}
		}

		for (int i = 0; i < length; i++) {
			if (!tokenMatching(buffer1[i], buffer2[i])) {
				return false;
			}
		}

		return true;
	}

	boolean tokenMatching(String token1, String token2) {
		if (token1.equals(token2))
			return true;
		if (var(token1) && !var(token2))
			return varMatching(token1, token2);
		if (!var(token1) && var(token2))
			return varMatching(token2, token1);
		if (var(token1) && var(token2))
			return varMatching(token1, token2);
		return false;
	}

	boolean varMatching(String vartoken, String token) {
		if (vars.containsKey(vartoken)) {
			if (token.equals(vars.get(vartoken))) {
				return true;
			} else {
				return false;
			}
		} else {
			replaceBuffer(vartoken, token);
			if (vars.containsValue(vartoken)) {
				replaceBindings(vartoken, token);
			}
			vars.put(vartoken, token);
		}
		return true;
	}

	void replaceBuffer(String preString, String postString) {
		for (int i = 0; i < buffer1.length; i++) {
			if (preString.equals(buffer1[i])) {
				buffer1[i] = postString;
			}
			if (preString.equals(buffer2[i])) {
				buffer2[i] = postString;
			}
		}
	}

	void replaceBindings(String preString, String postString) {
		for (Iterator<String> i = vars.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			if (preString.equals(vars.get(key))) {
				vars.put(key, postString);
			}
		}
	}

	boolean var(String str1) {
		// 先頭が ? なら変数
		return str1.startsWith("?");
	}
}