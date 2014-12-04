import javax.swing.JFrame;

import components.HighlightedTextPane;


public class RuleBaseSystem extends JFrame {
	
	public RuleBaseSystem() {
		initialize();
	}
	
	private void initialize() {
		setBounds(100, 100, 400, 600);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		HighlightedTextPane ruleTextPane = new HighlightedTextPane();
		add(ruleTextPane);
	}

	public static void main(String[] args) {
		new RuleBaseSystem().setVisible(true);
	}
}
