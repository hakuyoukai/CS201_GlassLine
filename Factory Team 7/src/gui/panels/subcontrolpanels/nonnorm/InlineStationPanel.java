package gui.panels.subcontrolpanels.nonnorm;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import engine.util.Glass;
import gui.panels.subcontrolpanels.NonNormPanel;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class InlineStationPanel extends JPanel implements ActionListener
{

	Transducer t;
	JComboBox<String> stationNum;
	JButton breakButton;
	JButton unBreakButton;
	JButton backButton;
	NonNormPanel parent;
	
	JComboBox<String> shuttleNum;
	JButton shuttleBreak;
	JButton shuttleFix;
	
	public InlineStationPanel(NonNormPanel p)
	{
		this.parent = p;
		breakButton = new JButton("Break");
		unBreakButton = new JButton("Unbreak");
		
		breakButton.addActionListener(this);
		unBreakButton.addActionListener(this);
		backButton = new JButton("Return to Non-Norm Menu");
		backButton.addActionListener(this);
		
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300,200));
		add(panel,BorderLayout.CENTER);
		
		String nums[] = new String[7];
		nums[0]=""+0;
		nums[1]=""+2;
		nums[2]=""+3;
		nums[3]=""+8;
		nums[4]=""+10;
		nums[5]=""+11;
		nums[6]=""+13;
		
		
		
		stationNum = new JComboBox<String>(nums);
		stationNum.addActionListener(this);
		stationNum.setPreferredSize(new Dimension(60,25));

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 1;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.insets = new Insets(0,0,15,0); 
		panel.add(new JLabel("Disable Inline"),c);


		
		c.insets = new Insets(10,5,10,5);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 2;
		
		panel.add(new JLabel("Inline "),c);
		c.gridx = 2;
		panel.add(stationNum,c);
		c.gridx = 3;
		panel.add(breakButton,c);
		
		c.gridx = 4;
		panel.add(unBreakButton,c);
		
		add(backButton,BorderLayout.SOUTH);
		
		shuttleBreak = new JButton("Break");
		shuttleFix = new JButton("Fix");
		String[] shut = new String[4];
		shut[0] = "1";
		shut[1] = "4";
		shut[2] = "9";
		shut[3] = "12";
		shuttleNum = new JComboBox<String>(shut);
		
		ShuttleListener shuttleListener = new ShuttleListener();
		shuttleBreak.addActionListener(shuttleListener);
		shuttleFix.addActionListener(shuttleListener);
		
		c.gridy = 3;
		c.gridx = 1;
		panel.add(new JLabel("Shuttle"),c);
		c.gridx = 2;
		panel.add(shuttleNum,c);
		c.gridx = 3;
		panel.add(shuttleBreak,c);
		c.gridx = 4;
		panel.add(shuttleFix,c);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == breakButton) 
		{
			Integer[] newArgs = new Integer[1];
			newArgs[0] = Integer.parseInt(stationNum.getItemAt(stationNum.getSelectedIndex()));
			System.out.println("GUI breaks the "+(Integer)newArgs[0]+" inline");
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.INLINE_WORKSTATION_BREAK,newArgs);
		}
		else if (ae.getSource()== unBreakButton)
		{
			Integer[] newArgs = new Integer[1];
			newArgs[0] = Integer.parseInt(stationNum.getItemAt(stationNum.getSelectedIndex()));
			System.out.println("GUI unbreaks the "+(Integer)newArgs[0]+" inline");
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.INLINE_WORKSTATION_UNBREAK,newArgs);
		}
		else if (ae.getSource() == backButton) 
		{
			CardLayout cl = (CardLayout)(parent.cardContainer.getLayout());
	        cl.show(parent.cardContainer, "MAINPANEL");
		}
	}
	
	public class ShuttleListener implements ActionListener {
		
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == shuttleBreak) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = shuttleNum.getSelectedIndex();
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.GUI_BREAK_SHUTTLE,newArgs);
		}
		else if (e.getSource() == shuttleFix) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = shuttleNum.getSelectedIndex();
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.GUI_FIX_SHUTTLE,newArgs);
	
		}
	}
	}
}
