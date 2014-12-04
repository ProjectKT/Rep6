package SuffixArray;


import java.io.FileReader;
import java.io.StreamTokenizer;
import java.util.ArrayList;

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