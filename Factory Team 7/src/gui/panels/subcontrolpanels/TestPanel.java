package gui.panels.subcontrolpanels;

import engine.util.Glass;
import gui.panels.ControlPanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import transducer.TChannel;
import transducer.TEvent;
public class TestPanel extends JPanel implements ActionListener {
	private ControlPanel parent;
	JButton newGlass;
	JButton stopButton;
	JButton startButton;
	JComboBox conveyorNum;
	public JLabel receiveLabel;
	public JLabel sendLabel;
	JButton receiveToggle;
	JButton sendToggle;
	
	public TestPanel(ControlPanel cp) {
		setLayout(new BoxLayout(this,BoxLayout.PAGE_AXIS));
		//JPanel oneLine = new JPanel();
		Box oneLine = Box.createHorizontalBox();
		
		parent = cp;
		newGlass = new JButton("Add Glass");
		newGlass.addActionListener(this);
		add(newGlass);
		
		stopButton = new JButton("Stop");
		stopButton.addActionListener(this);
	//	add(stopButton);
		
		startButton = new JButton("Start");
		startButton.addActionListener(this);
	//	add(startButton);
		
		Integer[] nums = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
		conveyorNum = new JComboBox(nums);
		conveyorNum.addActionListener(this);
		conveyorNum.setMaximumSize(new Dimension(40,25));
		oneLine.add(conveyorNum);
		oneLine.add(startButton);
		oneLine.add(stopButton);
		add(oneLine);
		
		receiveLabel = new JLabel("receive: false");
		sendLabel = new JLabel("send: false");
		receiveToggle = new JButton("Toggle");
		sendToggle = new JButton("Toggle");
		receiveToggle.addActionListener(this);
		sendToggle.addActionListener(this);
		

		Box secondLine = Box.createHorizontalBox();
		secondLine.add(receiveLabel);
		secondLine.add(receiveToggle);
		secondLine.add(sendLabel);
		secondLine.add(sendToggle);
		add(secondLine);
	}
//TODO: remove
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Returns the parent panel
	 * @return the parent panel
	 */
	
	/*
	public ControlPanel getGuiParent()
	{
		return parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newGlass) {
			parent.transducer.fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
			Glass g = new Glass(null);
			parent.parent.dPanel.JTest.mockCF1.msgHereIsGlass(g);
			
		}
		else if (e.getSource() == stopButton) {
			Integer[] newArgs = new Integer[1];
				newArgs[0] = conveyorNum.getSelectedIndex();
				parent.transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);

		}
		else if (e.getSource() == startButton) {
			Integer[] newArgs = new Integer[1];
				newArgs[0] = conveyorNum.getSelectedIndex();
				parent.transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		}
		else if (e.getSource() == receiveToggle) {
			boolean a =	parent.parent.dPanel.JTest.conveyorFamily.receiveOK;
			a = !a;
			parent.parent.dPanel.JTest.conveyorFamily.receiveOK = a;
			receiveLabel.setText("receive: " + a);
		}
		else if (e.getSource() == sendToggle) {
			boolean a =	parent.parent.dPanel.JTest.conveyorFamily.sendOK;
			a = !a;
			parent.parent.dPanel.JTest.conveyorFamily.sendOK = a;
			if (a == true) {
				parent.parent.dPanel.JTest.conveyorFamily.msgIAmReady();
			}
			sendLabel.setText("send: " + a);
		}

		
	}

	*/
	
}