

import javax.swing.JFrame;

import components.RuleTextPane;


public class RuleBaseSystem extends JFrame {
	
	public RuleBaseSystem() {
		initialize();
	}
	
	private void initialize() {
		setBounds(100, 100, 400, 600);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		RuleTextPane ruleTextPane = new RuleTextPane();
		add(ruleTextPane);
	}

	public static void main(String[] args) {
		new RuleBaseSystem().setVisible(true);
	}
}
