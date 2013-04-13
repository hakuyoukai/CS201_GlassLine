
package gui.panels.subcontrolpanels;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import engine.util.Glass;
import gui.panels.ControlPanel;

import javax.swing.*;

import transducer.TChannel;
import transducer.TEvent;

/**
 * The GlassSelectPanel class contains buttons allowing the user to select what
 * type of glass to produce.
 */
@SuppressWarnings("serial")
public class GlassSelectPanel extends JPanel
{
	/** The ControlPanel this is linked to */
	private ControlPanel parent;
	JButton newGlassButton;
	List<JCheckBox> steps;

	/**
	 * Creates a new GlassSelect and links it to the control panel
	 * @param cp
	 *        the ControlPanel linked to it
	 */
	public GlassSelectPanel(ControlPanel cp)
	{
		parent = cp;
		panelListener listener = new panelListener();
		newGlassButton = new JButton("Submit");
		newGlassButton.addActionListener(listener);
		JPanel panel = new JPanel(new GridLayout(0,1));
		steps = new ArrayList<JCheckBox>();
		steps.add(new JCheckBox("Cutter"));
		steps.add(new JCheckBox("Shuttle 1"));
		steps.add(new JCheckBox("Breakout"));
		steps.add(new JCheckBox("Manual Breakout"));
		steps.add(new JCheckBox("Shuttle 2"));
		steps.add(new JCheckBox("Cross Seamer"));
		steps.add(new JCheckBox("Grinder"));
		steps.add(new JCheckBox("Drill"));
		steps.add(new JCheckBox("Washer"));
		steps.add(new JCheckBox("Shuttle 3"));
		steps.add(new JCheckBox("Painter"));
		steps.add(new JCheckBox("UV Lamp"));
		steps.add(new JCheckBox("Shuttle 4"));
		steps.add(new JCheckBox("Oven"));

		// will not be added to display(cutter, shuttles)
		steps.get(0).setSelected(true);
		steps.get(1).setSelected(true);
		steps.get(4).setSelected(true);
		steps.get(9).setSelected(true);
		steps.get(12).setSelected(true);
		
		for (JCheckBox j:steps) {
			if (!j.isSelected())
				panel.add(j);
		}
		
		add(panel);
		add(newGlassButton);

	}

	/**
	 * Returns the parent panel
	 * @return the parent panel
	 */
	public ControlPanel getGuiParent()
	{
		return parent;
	}
	
	public class panelListener implements ActionListener {
		
	

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == newGlassButton) {
			StringBuilder sb = new StringBuilder();
			for (JCheckBox jcb:steps) {
				if (jcb.isSelected())
					sb.append("1 ");
				else
					sb.append("0 ");
			}
			String str = sb.toString();
			System.out.println("Recipe created: " + str);
			Glass g = new Glass(str);
			Glass[] glass = new Glass[1];
			glass[0] = g;
			parent.getTransducer().fireEvent(TChannel.BIN, TEvent.BIN_WAIT_PART,glass);
			parent.getGuiParent().getConveyor(0).msgHereIsGlass(g);
		}
		
	}
	}
}
