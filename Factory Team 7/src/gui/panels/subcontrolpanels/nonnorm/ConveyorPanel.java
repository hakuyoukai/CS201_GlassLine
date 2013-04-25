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

public class ConveyorPanel extends JPanel implements ActionListener{

	Transducer t;
	JComboBox<String> conveyorNum;
	JButton jamButton;
	JButton unJamButton;
	JButton backButton;
	NonNormPanel parent;

// constructor
	public ConveyorPanel(NonNormPanel p){
		this.parent = p;
		jamButton = new JButton("Jam");
		unJamButton = new JButton("Unjam");
		
		jamButton.addActionListener(this);
		unJamButton.addActionListener(this);
		backButton = new JButton("Return to Non-Norm Menu");
		backButton.addActionListener(this);
		
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(300,200));
		add(panel,BorderLayout.CENTER);
		
		String nums[] = new String[15];
		for (int i = 0; i < 15;i++) {
			nums[i] = i + "";
		}
		
		
		conveyorNum = new JComboBox<String>(nums);
		conveyorNum.addActionListener(this);
		conveyorNum.setPreferredSize(new Dimension(60,25));

		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridy = 1;
		c.gridx = 1;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.insets = new Insets(0,0,15,0); 
		panel.add(new JLabel("Conveyor Jam"),c);


		
		c.insets = new Insets(10,5,10,5);
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = 2;
		
		panel.add(new JLabel("Conveyor "),c);
		c.gridx = 2;
		panel.add(conveyorNum,c);
		c.gridx = 3;
		panel.add(jamButton,c);
		
		c.gridx = 4;
		panel.add(unJamButton,c);
		
		
		/*
		c.gridy = 3;
		c.gridx = 2;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
*/
		add(backButton,BorderLayout.SOUTH);
}

	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == jamButton) {

				Integer[] newArgs = new Integer[1];
					newArgs[0] = conveyorNum.getSelectedIndex();

				parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.CONVEYOR_JAM,newArgs);
		}
		else if (ae.getSource()== unJamButton){
		

			Integer[] newArgs = new Integer[1];
			newArgs[0] = conveyorNum.getSelectedIndex();
			parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.CONVEYOR_UNJAM,newArgs);

		}
		else if (ae.getSource() == backButton) {
			 CardLayout cl = (CardLayout)(parent.cardContainer.getLayout());
		        cl.show(parent.cardContainer, "MAINPANEL");
		}
	}
}