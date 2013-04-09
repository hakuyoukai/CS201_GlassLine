
package gui.panels;


import engine.conveyorfamily.online.ConveyorFamily;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle.ConveyorFamilyType;
import engine.conveyorfamily.zero.ConveyorFamilyZero;
import engine.util.ConveyorFamilyInterface;
import gui.drivers.FactoryFrame;
import gui.test.mock.j.MockConveyorFamily;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import transducer.TChannel;
import transducer.Transducer;

/**
 * The FactoryPanel is highest level panel in the actual kitting cell. The
 * FactoryPanel makes all the back end components, connects them to the
 * GuiComponents in the DisplayPanel. It is responsible for handing
 * communication between the back and front end.
 */
@SuppressWarnings("serial")
public class FactoryPanel extends JPanel
{
	/** The frame connected to the FactoryPanel */
	private FactoryFrame parent;

	/** The control system for the factory, displayed on right */
	private ControlPanel cPanel;

	/** The graphical representation for the factory, displayed on left */
	private DisplayPanel dPanel;

	/** Allows the control panel to communicate with the back end and give commands */
	private Transducer transducer;

	ConveyorFamilyZero conveyor0;
	ConveyorFamilyShuttle conveyor1;
	ConveyorFamily conveyor2;
	ConveyorFamily conveyor3;
	ConveyorFamilyShuttle conveyor4;
	ConveyorFamilyShuttle conveyor9;
	ConveyorFamilyShuttle conveyor12;
	MockConveyorFamily conveyormock;
	
	/**
	 * Constructor links this panel to its frame
	 */
	public FactoryPanel(FactoryFrame fFrame)
	{
		parent = fFrame;

		// initialize transducer
		transducer = new Transducer();
		transducer.startTransducer();

		// use default layout
		// dPanel = new DisplayPanel(this);
		// dPanel.setDefaultLayout();
		// dPanel.setTimerListeners();

		// initialize and run
		this.initialize();
		this.initializeBackEnd();
	}

	/**
	 * Initializes all elements of the front end, including the panels, and lays
	 * them out
	 */
	private void initialize()
	{
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// initialize control panel
		cPanel = new ControlPanel(this, transducer);

		// initialize display panel
		dPanel = new DisplayPanel(this, transducer);

		// add panels in
		// JPanel tempPanel = new JPanel();
		// tempPanel.setPreferredSize(new Dimension(830, 880));
		// this.add(tempPanel);

		this.add(dPanel);
		this.add(cPanel);
	}

	/**
	 * Feel free to use this method to start all the Agent threads at the same time
	 */
	private void initializeBackEnd()
	{
		// ===========================================================================
		// TODO initialize and start Agent threads here
		// ===========================================================================
		conveyor0= new ConveyorFamilyZero(transducer);
		//conveyor1=new ConveyorFamily(null,null,transducer,1,TChannel.NO_WORKSTATION);
		conveyor1= new ConveyorFamilyShuttle(1,transducer);
		conveyor2=new ConveyorFamily(null,null,transducer,2,TChannel.BREAKOUT);
		conveyor3=new ConveyorFamily(null,null,transducer,3,TChannel.MANUAL_BREAKOUT);
		conveyor4= new ConveyorFamilyShuttle(4,transducer);
		conveyor9= new ConveyorFamilyShuttle(9,transducer);
		conveyor12= new ConveyorFamilyShuttle(12,transducer);
		conveyormock = new MockConveyorFamily(5,ConveyorFamilyType.TO,transducer);
		
		
		conveyor0.setNextConveyor(conveyor1);
		
		conveyor1.setNeighbor(conveyor0,ConveyorFamilyType.FROM);
		conveyor1.setNeighbor(conveyor2,ConveyorFamilyType.TO);
		
		conveyor2.setNextCF(conveyor3);
		conveyor2.setPreviousCF(conveyor1);
		
		conveyor3.setNextCF(conveyor4);
		conveyor3.setPreviousCF(conveyor2);
		
		conveyor4.setNeighbor(conveyor3,ConveyorFamilyType.FROM);
		conveyor4.setNeighbor(conveyormock,ConveyorFamilyType.TO);
		
		conveyormock.setNeighbor(conveyor4,ConveyorFamilyType.FROM);

		conveyor9.setNeighbor(null,ConveyorFamilyType.FROM);
		conveyor9.setNeighbor(null,ConveyorFamilyType.TO);
		
		conveyor12.setNeighbor(null,ConveyorFamilyType.FROM);
		conveyor12.setNeighbor(null,ConveyorFamilyType.TO);
		
		

		conveyor1.startUp();
		conveyor2.startAllAgentThreads();
		conveyor3.startAllAgentThreads();
		conveyor4.startUp();
		conveyormock.msgIAmReady();
		System.out.println("Back end initialization finished.");
	}

	/**
	 * Returns the parent frame of this panel
	 * 
	 * @return the parent frame
	 */
	public FactoryFrame getGuiParent()
	{
		return parent;
	}

	/**
	 * Returns the control panel
	 * 
	 * @return the control panel
	 */
	public ControlPanel getControlPanel()
	{
		return cPanel;
	}

	/**
	 * Returns the display panel
	 * 
	 * @return the display panel
	 */
	public DisplayPanel getDisplayPanel()
	{
		return dPanel;
	}
	
	public Transducer getLinkedTransducer()
	{
		return transducer;
	}
}
