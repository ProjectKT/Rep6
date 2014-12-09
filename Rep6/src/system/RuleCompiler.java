package system;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;

import providers.Rule;
import utils.CharArrayStreamTokenizer;

public class RuleCompiler {
	
	private CharArrayStreamTokenizer st;
	
	public Result compile(String txt) {
		Result result = new Result();
		ArrayList<RuleContainer> rules = new ArrayList<RuleContainer>();
		boolean hasError = false;
		try {
			int offset = 0;
			int token;
			st = new CharArrayStreamTokenizer(txt.toCharArray());
			while ((token = st.nextToken()) != StreamTokenizer.TT_EOF) {
				try {
					switch (token) {
					case StreamTokenizer.TT_WORD:
						String name = null;
						ArrayList<String> antecedents = null;
						String consequent = null;
						if ("rule".equalsIgnoreCase(st.sval)) {
							offset = st.getCurrentPosition() - "rule".length() - 1;
							safeNextToken();
							name = st.sval;
							safeNextToken();
							if ("if".equalsIgnoreCase(st.sval)) {
								antecedents = new ArrayList<String>();
								safeNextToken();
								while (!"then".equalsIgnoreCase(st.sval)) {
									if ("rule".equalsIgnoreCase(st.sval) || "if".equalsIgnoreCase(st.sval)) {
										throw new IllegalEndException(st.getCurrentPosition() - st.sval.length() - 1);
									}
									antecedents.add(st.sval);
									safeNextToken();
								}
								if ("then".equalsIgnoreCase(st.sval)) {
									safeNextToken();
									consequent = st.sval;
								} else {
									throw new IllegalTokenException(st.sval);
								}
							} else {
								throw new IllegalTokenException(st.sval);
							}
						} else {
							throw new IllegalTokenException(st.sval);
						}
						Rule rule = new Rule(name, antecedents, consequent);
						rules.add(new RuleContainer(offset, st.getCurrentPosition() - offset - 1, rule));
						break;
					default:
						System.out.println(token);
						throw new UnknownTokenException(token);
					}
				} catch (Exception e) {
					hasError = true;
					result.errors.add(e);
					System.out.println(e);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		} finally {
			if (st != null) {
				st.close();
			}
		}
		
		result.succeeded = !hasError;
		result.rules = rules;
		return result;
	}
	
	private int safeNextToken() throws IllegalEndException, IOException {
		int i = st.nextToken();
		if (i == StreamTokenizer.TT_EOF) {
			throw new IllegalEndException(st.getCurrentPosition() - 1);
		}
		return i;
	}
	
	
	public class Result {
		boolean succeeded = false;
		ArrayList<RuleContainer> rules = new ArrayList<RuleContainer>();
		ArrayList<Exception> errors = new ArrayList<Exception>();
		
		@Override
		public String toString() {
			return "scceeded="+succeeded+", rules="+rules;
		}
	}
	
	public static class RuleContainer {
		/** 開始位置 */
		public int offset;
		/** 長さ */
		public int count;
		/** ルール */
		public Rule rule;
		
		public RuleContainer(int offset, int count, Rule rule) {
			this.offset = offset;
			this.count = count;
			this.rule = rule;
		}
		
		@Override
		public String toString() {
			return "("+offset+"-->"+(offset+count)+"):"+rule;
		}
	}
	
	public class UnknownTokenException extends Exception {
		public int type;
		public UnknownTokenException(int n) {
			super(String.valueOf(n));
			this.type = n;
		}
	}
	public class IllegalTokenException extends Exception {
		public String token;
		public IllegalTokenException(String s) {
			super(s);
			this.token = s;
		}
	}
	public class IllegalEndException extends Exception {
		public int offset;
		public IllegalEndException(int offset) {
			super(String.valueOf(offset));
			this.offset = offset;
		}
	}
	
	public static void main(String[] args) {
		String s = ""
				+ "rule 	\"Z1\" "
				+ "if 	\"?x has hair\" "
				+ "then 	\"?x is a mammal\"";
		
		System.out.println(new RuleCompiler().compile(s));
		System.out.println(s.charAt(50));
	}
}
