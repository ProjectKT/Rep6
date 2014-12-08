package system;


import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import providers.*;

	public class RuleBase {
		String text="";
		String fileName;
		String wmFileName;
		FileReader f;
		FileManager fm;
		StreamTokenizer st;
		WorkingMemory wm;
		ArrayList<String> wmTemp;
		ArrayList<Rule> rules;

		public RuleBase() {
			fileName = "CarShop.data";
			wmFileName = "CarShopWm.data";
			fm = new FileManager();
			wm = new WorkingMemory();
			wmTemp = new ArrayList<String>();
			/*
			wm.addAssertion("my-car is inexpensive");
			wm.addAssertion("my-car has a VTEC engine");
			wm.addAssertion("my-car is stylish");
			wm.addAssertion("my-car has several color models");
			wm.addAssertion("my-car has several seats");
			wm.addAssertion("my-car is a wagon");
			*/
			wmTemp = fm.loadWm(wmFileName);
			
			for(String temp:wmTemp){
				wm.addAssertion(temp);
			}
			
			rules = new ArrayList<Rule>();
			loadRules(fileName);
		}

		
		/**
		 *  ルールのセット
		 */
		public void setRules(ArrayList<Rule> rule){
			this.rules = rule;
		}
			
		
		/**
		 * 前向き推論を行うためのメソッド
		 * 
		 */
		public void forwardChain() {
			text="";
			boolean newAssertionCreated;
			// 新しいアサーションが生成されなくなるまで続ける．
			do {
				newAssertionCreated = false;
				for (int i = 0; i < rules.size(); i++) {
					Rule aRule = (Rule) rules.get(i);
					System.out.println("apply rule:" + aRule.getName());
					ArrayList<String> antecedents = aRule.getAntecedents();
					String consequent = aRule.getConsequent();
					// HashMap bindings = wm.matchingAssertions(antecedents);
					ArrayList bindings = wm.matchingAssertions(antecedents);
					if (bindings != null) {
						for (int j = 0; j < bindings.size(); j++) {
							// 後件をインスタンシエーション
							String newAssertion = instantiate((String) consequent,
									(HashMap) bindings.get(j));
							// ワーキングメモリーになければ成功
							if (!wm.contains(newAssertion)) {
								System.out.println("Success: " + newAssertion);
								text += "Success: " + newAssertion+"\n";
								wm.addAssertion(newAssertion);
								newAssertionCreated = true;
							}
						}
					}
				}
				System.out.println("Working Memory" + wm);
				text += "Working Memory" + wm+"\n";
			} while (newAssertionCreated);
			System.out.println("No rule produces a new assertion");
			text += "No rule produces a new assertion"+"\n";
		}

		public String get_answer(){
			return text;
		}
		
		private String instantiate(String thePattern, HashMap theBindings) {
			String result = new String();
			StringTokenizer st = new StringTokenizer(thePattern);
			for (int i = 0; i < st.countTokens();) {
				String tmp = st.nextToken();
				if (var(tmp)) {
					result = result + " " + (String) theBindings.get(tmp);
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

		private void loadRules(String theFileName) {
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
							// if(st.nextToken() == '"'){
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
							// }
						}
						// ルールの生成
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
			for (int i = 0; i < rules.size(); i++) {
				System.out.println(((Rule) rules.get(i)).toString());
			}
		}
		/**
			後ろ向き推論
		**/
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
					if ((new Unifier()).unify(thePattern,wm.getString(i),
							theBinding)) {
						System.out.println("Success WM");
						System.out.println(wm.getString(i) + " <=> "
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
		
		
	}
	
