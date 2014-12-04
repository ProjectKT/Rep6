package SuffixArray;

import java.util.*;
import java.io.*;



public class OurSuffixArray extends SuffixArray{
	static FileManager fm;
	ArrayList<Rule> rules = new ArrayList<Rule>();
	ArrayList<String> wm = new ArrayList<String>();
	private static final String[] RULE_FILES = {"AnimalWorld.data","CarShop.data"};
	private static final String[] WM_FILES = {"AnimalWorldWm.data","CarShopWm.data"};
	
	public OurSuffixArray(){
		loadData();
		setupSuffixArray();
	}
	
	private void loadData() {
		fm = new FileManager();
		// ファイルから読み込む
		for (String filename : RULE_FILES) {
				rules.addAll(fm.loadRules(filename));
		}
		for (String filename : WM_FILES) {
				wm.addAll(fm.loadWm(filename));
		}
	}
	
	private void setupSuffixArray(){
		for(int i = 0; i < rules.size(); i++){
			addSuffixRule(rules.get(i));
		}
		for(int i2 = 0; i2 < wm.size(); i2++){
			addSuffixWm(wm.get(i2));
		}
	}
	
	public static void main(String[] args) {
		OurSuffixArray osa = new OurSuffixArray();
		
		Iterator it = osa.getSentences("has");
		while(it.hasNext()){
			System.out.println(it.next());
		}
	}
}
