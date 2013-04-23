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
	JComboBox conveyorNumJam;
	
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
		conveyorNum.setPreferredSize(new Dimension(40,26));
		conveyorNum.setMaximumSize(new Dimension(40,26));
		oneLine.add(new JLabel("Animation Only"));
		oneLine.add(Box.createRigidArea(new Dimension(5,0)));
		oneLine.add(conveyorNum);
		oneLine.add(Box.createRigidArea(new Dimension(5,0)));
		oneLine.add(startButton);
		oneLine.add(Box.createRigidArea(new Dimension(5,0)));
		oneLine.add(stopButton);
		add(oneLine);
	
		conveyorNumJam = new JComboBox(nums);
		conveyorNumJam.setMaximumSize(new Dimension(40,26));
		Box box = Box.createHorizontalBox();
		box.add(new JLabel("Conveyor Jam"));
		box.add(Box.createRigidArea(new Dimension(5,0)));
		box.add(conveyorNumJam);
		box.add(Box.createRigidArea(new Dimension(5,0)));
		
		JButton button = new JButton("Jam");
		button.addActionListener(this);
		box.add(button);
		box.add(Box.createRigidArea(new Dimension(5,0)));
		button = new JButton("Unjam");
		button.addActionListener(this);
		box.add(button);
		
		add(box);
	}

	
	/**
	 * Returns the parent panel
	 * @return the parent panel
	 */
	
	
	public ControlPanel getGuiParent()
	{
		return parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newGlass) {
			parent.getTransducer().fireEvent(TChannel.BIN, TEvent.BIN_CREATE_PART, null);
			Glass g = new Glass(null);
			parent.getGuiParent().getConveyor(0).msgHereIsGlass(g);
			
		}
		else if (e.getSource() == stopButton) {
			Integer[] newArgs = new Integer[1];
				newArgs[0] = conveyorNum.getSelectedIndex();
				parent.getTransducer().fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);

		}
		else if (e.getSource() == startButton) {
			Integer[] newArgs = new Integer[1];
				newArgs[0] = conveyorNum.getSelectedIndex();
				parent.getTransducer().fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
		}
		else if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("Jam")) {

				Integer[] newArgs = new Integer[1];
					newArgs[0] = conveyorNumJam.getSelectedIndex();

				parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.CONVEYOR_JAM,newArgs);
			}
			else if (button.getText().equals("Unjam")) {

				Integer[] newArgs = new Integer[1];
					newArgs[0] = conveyorNumJam.getSelectedIndex();
					parent.getTransducer().fireEvent(TChannel.CONTROL_PANEL,TEvent.CONVEYOR_UNJAM,newArgs);

			}
		}
		
	}

	
	
}