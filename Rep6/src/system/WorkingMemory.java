package system;

import java.util.ArrayList;
import java.util.HashMap;

class WorkingMemory {
	ArrayList<String> assertions;

	WorkingMemory() {
		assertions = new ArrayList<String>();
	}

	/**
	 * マッチするアサーションに対するバインディング情報を返す （再帰的）
	 * 
	 * @param 前件を示す
	 *            ArrayList
	 * @return バインディング情報が入っている ArrayList
	 */
	public ArrayList matchingAssertions(ArrayList<String> theAntecedents) {
		ArrayList bindings = new ArrayList();
		return matchable(theAntecedents, 0, bindings);
	}

	private ArrayList matchable(ArrayList<String> theAntecedents, int n,
			ArrayList bindings) {
		if (n == theAntecedents.size()) {
			return bindings;
		} else if (n == 0) {
			boolean success = false;
			for (int i = 0; i < assertions.size(); i++) {
				HashMap<String, String> binding = new HashMap<String, String>();
				if ((new Matcher()).matching((String) theAntecedents.get(n),
						(String) assertions.get(i), binding)) {
					bindings.add(binding);
					success = true;
				}
			}
			if (success) {
				return matchable(theAntecedents, n + 1, bindings);
			} else {
				return null;
			}
		} else {
			boolean success = false;
			ArrayList newBindings = new ArrayList();
			for (int i = 0; i < bindings.size(); i++) {
				for (int j = 0; j < assertions.size(); j++) {
					if ((new Matcher()).matching(
							(String) theAntecedents.get(n),
							(String) assertions.get(j),
							(HashMap) bindings.get(i))) {
						newBindings.add(bindings.get(i));
						success = true;
					}
				}
			}
			if (success) {
				return matchable(theAntecedents, n + 1, newBindings);
			} else {
				return null;
			}
		}
	}

	/**
	 * アサーションをワーキングメモリに加える．
	 * 
	 * @param アサーションを表す
	 *            String
	 */
	public void addAssertion(String theAssertion) {
		System.out.println("ADD:" + theAssertion);
		assertions.add(theAssertion);
	}

	/**
	 * 指定されたアサーションがすでに含まれているかどうかを調べる．
	 * 
	 * @param アサーションを表す
	 *            String
	 * @return 含まれていれば true，含まれていなければ false
	 */
	public boolean contains(String theAssertion) {
		return assertions.contains(theAssertion);
	}

	/**
	 * ワーキングメモリの情報をストリングとして返す．
	 * 
	 * @return ワーキングメモリの情報を表す String
	 */
	public String toString() {
		return assertions.toString();
	}

	/**
	 * 	ワーキングメモリの一部の情報をstringで返す
	 * @param i
	 * @return
	 */
	
	public String getString(int i){
		return assertions.get(i);
	}
	
	/**
	 * ワーキングメモリのサイズを返す
	 * @return
	 */
	public int size(){
		return this.assertions.size();
	}
	
}
