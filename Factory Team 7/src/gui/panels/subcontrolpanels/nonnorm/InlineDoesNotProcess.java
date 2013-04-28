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

import gui.panels.subcontrolpanels.NonNormPanel;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class InlineDoesNotProcess extends JPanel implements ActionListener
{

	Transducer t;
	JComboBox<String> stationNum;
	JButton forgetButton;
	JButton unforgetButton;
	JButton backButton;
	NonNormPanel parent;
	
	public InlineDoesNotProcess(NonNormPanel p)
	{
		this.parent = p;
		forgetButton = new JButton("Not Process");
		unforgetButton = new JButton("Process");
		
		forgetButton.addActionListener(this);
		unforgetButton.addActionListener(this);
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
		panel.add(new JLabel("Inline Forgets"),c);


		
		c.insets = new Insets(10,5,10,5);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 2;
		
		panel.add(new JLabel("Inline "),c);
		c.gridx = 2;
		panel.add(stationNum,c);
		c.gridx = 3;
		panel.add(forgetButton,c);
		
		c.gridx = 4;
		panel.add(unforgetButton,c);
		
		add(backButton,BorderLayout.SOUTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == forgetButton) 
		{
			Integer[] newArgs = new Integer[1];
			newArgs[0] = Integer.parseInt(stationNum.getItemAt(stationNum.getSelectedIndex()));
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.INLINE_DOES_NOT_PROCESS,newArgs);
		}
		else if (ae.getSource()== unforgetButton)
		{
			Integer[] newArgs = new Integer[1];
			newArgs[0] = Integer.parseInt(stationNum.getItemAt(stationNum.getSelectedIndex()));
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.INLINE_DOES_PROCESS,newArgs);
		}
		else if (ae.getSource() == backButton) 
		{
			CardLayout cl = (CardLayout)(parent.cardContainer.getLayout());
	        cl.show(parent.cardContainer, "MAINPANEL");
		}
	}

}
