
package gui.panels.subcontrolpanels;

import gui.panels.ControlPanel;
import gui.panels.subcontrolpanels.nonnorm.ConveyorPanel;
import gui.panels.subcontrolpanels.nonnorm.InlineDoesNotProcess;
import gui.panels.subcontrolpanels.nonnorm.InlineStationPanel;
import gui.panels.subcontrolpanels.nonnorm.PopUpPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import transducer.Transducer;

/**
 * The NonNormPanel is responsible for initiating and managing non-normative
 * situations. It contains buttons for each possible non-norm.
 * 
 * The non-normative situations are:
 * 1. Conveyor Jam
 * 2.
 * 3.
 * 4.
 * 5.
 * 6.
 * 7.
 * 8.
 */
@SuppressWarnings("serial")
public class NonNormPanel extends JPanel
{
	/** The number of different havoc actions that exist */
	public static final int NUM_NON_NORMS = 8;

	/** The control panel this is linked to */
	ControlPanel parent;

	/** List of buttons for each non-norm */
	List<JButton> nonNormButtons;

	/** Title label **/
	JLabel titleLabel;
	public JPanel cardContainer;

	/**
	 * Creates a new HavocPanel and links the control panel to it
	 * 
	 * @param cp
	 *        the ControlPanel linked to it
	 */
	public NonNormPanel(ControlPanel cp)
	{
		parent = cp;
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.black);
		mainPanel.setForeground(Color.black);
		
		cardContainer = new JPanel();
		cardContainer.setLayout(new CardLayout());
		setLayout(new BorderLayout());
		
		ConveyorPanel conveyorPanel = new ConveyorPanel(this);
		PopUpPanel popUpPanel = new PopUpPanel(this);
		InlineStationPanel inlineBreakPanel = new InlineStationPanel(this);
		InlineDoesNotProcess inlineProcessPanel=new InlineDoesNotProcess(this);
		// set up layout
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		// set up button panel
		JPanel buttonPanel = new JPanel();
		GridLayout grid = new GridLayout(NUM_NON_NORMS / 2, 2);
		grid.setVgap(2);
		grid.setHgap(2);
		buttonPanel.setBackground(Color.black);
		buttonPanel.setLayout(grid);

		// make title
		titleLabel = new JLabel("NON NORMATIVES");
		titleLabel.setForeground(Color.white);
		titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
		JPanel titleLabelPanel = new JPanel();
		titleLabelPanel.add(titleLabel);
		// titleLabelPanel.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		titleLabelPanel.setBackground(Color.black);

		// make buttons
		nonNormButtons = new ArrayList<JButton>(NUM_NON_NORMS);
		nonNormButtons.add(new JButton("Conveyor Jam"));
		nonNormButtons.add(new JButton("Popup Jam"));

		nonNormButtons.add(new JButton("Disable Online"));
		nonNormButtons.add(new JButton("Online No Process"));
		nonNormButtons.add(new JButton("Disable Offline"));
		nonNormButtons.add(new JButton("Offline - Break Glass"));
		nonNormButtons.add(new JButton("Offline Malfunction"));
		nonNormButtons.add(new JButton("Broken Truck"));

		// add listeners
		nonNormButtons.get(0).addActionListener(new NonNorm1Listener());
		nonNormButtons.get(1).addActionListener(new NonNorm2Listener());
		nonNormButtons.get(2).addActionListener(new NonNorm3Listener());
		nonNormButtons.get(3).addActionListener(new NonNorm4Listener());
		nonNormButtons.get(4).addActionListener(new NonNorm5Listener());
		nonNormButtons.get(5).addActionListener(new NonNorm6Listener());
		nonNormButtons.get(6).addActionListener(new NonNorm7Listener());
		nonNormButtons.get(7).addActionListener(new NonNorm8Listener());

		for (int i = 0; i < NUM_NON_NORMS; i++)
		{
			nonNormButtons.get(i).setBackground(Color.white);
			nonNormButtons.get(i).setForeground(Color.black);
			nonNormButtons.get(i).setFont(new Font("SansSerif", Font.BOLD, 14));
			nonNormButtons.get(i).setOpaque(true);
			nonNormButtons.get(i).setBorderPainted(false);
			nonNormButtons.get(i).setSize(20, 30);
			nonNormButtons.get(i).setMinimumSize(new Dimension(20, 40));
			nonNormButtons.get(i).setMaximumSize(new Dimension(20, 40));
			nonNormButtons.get(i).setPreferredSize(new Dimension(20, 40));
		}
		// add to panel
		mainPanel.add(titleLabelPanel);

		JPanel colorLinesPanel1 = new JPanel();
		colorLinesPanel1.setPreferredSize(new Dimension(350, 2));
		colorLinesPanel1.setBackground(Color.black);
		ImageIcon cl = new ImageIcon("imageicons/singleColoredLine.png");
		JLabel clLabel1 = new JLabel(cl);
		colorLinesPanel1.add(clLabel1);
		mainPanel.add(colorLinesPanel1);

		for (JButton j : nonNormButtons)
		{
			buttonPanel.add(j);
		}
		buttonPanel.setAlignmentY(JPanel.CENTER_ALIGNMENT);
		mainPanel.add(buttonPanel);

		JPanel colorLinesPanel2 = new JPanel();
		colorLinesPanel2.setPreferredSize(new Dimension(350, 40));
		colorLinesPanel2.setBackground(Color.black);
		JLabel clLabel2 = new JLabel();
		colorLinesPanel2.add(clLabel2);
		mainPanel.add(colorLinesPanel2);
		
		
		cardContainer.add(mainPanel,"MAINPANEL");
		cardContainer.add(conveyorPanel,"conveyor");
		cardContainer.add(popUpPanel,"popupworkstations");
		cardContainer.add(inlineProcessPanel,"inlineProcessPanel");
		cardContainer.add(inlineBreakPanel,"inlineStation");
		
		this.add(cardContainer,BorderLayout.CENTER);
	}

	/**
	 * Returns the parent panel
	 * 
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent()
	{
		return parent;
	}

	/**
	 * Non-norm 1
	 */
	public class NonNorm1Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		
		// Conveyor Jam
		public void actionPerformed(ActionEvent ae)
		{
			 CardLayout cl = (CardLayout)(cardContainer.getLayout());
		        cl.show(cardContainer, "conveyor");
		}
	}

	/**
	 * Non-norm 2
	 */
	public class NonNorm2Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{
			 CardLayout cl = (CardLayout)(cardContainer.getLayout());
		        cl.show(cardContainer, "popupworkstations");
		}
	}

	/**
	 * Non-norm 3
	 */
	public class NonNorm3Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{
			CardLayout cl = (CardLayout)(cardContainer.getLayout());
	        cl.show(cardContainer, "inlineStation");
		}
	}

	/**
	 * Non-norm 4
	 */
	public class NonNorm4Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{
			CardLayout cl = (CardLayout)(cardContainer.getLayout());
	        cl.show(cardContainer, "inlineProcessPanel");
		}
	}

	/**
	 * Non-norm 5
	 */
	public class NonNorm5Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{
			 CardLayout cl = (CardLayout)(cardContainer.getLayout());
		        cl.show(cardContainer, "popupworkstations");
	
		}
	}

	/**
	 * Non-norm 6
	 */
	public class NonNorm6Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{
			 CardLayout cl = (CardLayout)(cardContainer.getLayout());
		        cl.show(cardContainer, "popupworkstations");
		}
	}

	/**
	 * Non-norm 7
	 */
	public class NonNorm7Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{

		}
	}

	/**
	 * Non-norm 8
	 */
	public class NonNorm8Listener implements ActionListener
	{
		/**
		 * Invoked whenever the button is pressed
		 */
		public void actionPerformed(ActionEvent ae)
		{

		}
	}
	
	public Transducer getTransducer() {
		return parent.getTransducer();
	}

}
