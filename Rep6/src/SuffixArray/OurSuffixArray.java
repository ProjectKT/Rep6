package SuffixArray;

import java.util.*;
import java.io.*;



public class OurSuffixArray extends SuffixArray{
	static FileManager fm;
	ArrayList<Rule> rules;
	ArrayList<String> wm;
	private static final String[] RULE_FILES = {"AnimalWorld.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data"};
	
	public OurSuffixArray(){
		loadData();
		setupSuffixArray();
	}
	
	private void loadData() {
		fm = new FileManager();
		// ファイルから読み込む
		for (String filename : RULE_FILES) {
				rules = fm.loadRules(filename);
		}
		for (String filename : WM_FILES) {
				wm = fm.loadWm(filename);
		}
	}
	
	private void setupSuffixArray(){
		for(int i = 0; i < rules.size(); i++){
			add_Suffix_rule(rules.get(i));
		}
	}
	
	public static void main(String[] args) {
		OurSuffixArray osa = new OurSuffixArray();
		
		Iterator it = osa.get_sentences("has");
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}
}
