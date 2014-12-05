package system;

import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import providers.Rule;

	public class RuleBase {
		String text="";
		String fileName;
		FileReader f;
		StreamTokenizer st;
		WorkingMemory wm;
		ArrayList<Rule> rules;

		public RuleBase() {
			fileName = "CarShop.data";
			wm = new WorkingMemory();
			wm.addAssertion("my-car is inexpensive");
			wm.addAssertion("my-car has a VTEC engine");
			wm.addAssertion("my-car is stylish");
			wm.addAssertion("my-car has several color models");
			wm.addAssertion("my-car has several seats");
			wm.addAssertion("my-car is a wagon");
			rules = new ArrayList<Rule>();
			loadRules(fileName);
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
	}
	
