
package gui.panels;


import engine.conveyorfamily.last.ConveyorFamilyLast;
import engine.conveyorfamily.offline.ConveyorFamilyOffline;
import engine.conveyorfamily.offline5.ConveyorFamilyOffline5;
import engine.conveyorfamily.online.ConveyorFamily;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle.ConveyorFamilyType;
import engine.conveyorfamily.zero.ConveyorFamilyZero;
import engine.util.ConveyorFamilyInterface;
import gui.drivers.FactoryFrame;
import gui.test.mock.j.MockConveyorFamily;
import engine.conveyorfamily.d.*;


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
	ConveyorFamily conveyor8;
	ConveyorFamily conveyor10;
	ConveyorFamily conveyor11;
	ConveyorFamily conveyor13;
	ConveyorFamilyShuttle conveyor4;
	ConveyorFamilyShuttle conveyor9;
	ConveyorFamilyShuttle conveyor12;
	
	ConveyorFamilyOffline5 conveyor5;
	ConveyorFamilyOffline conveyor6;
	ConveyorFamilyOffline conveyor7;
	
	ConveyorFamilyLast conveyor14;
	
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
		conveyor2=new ConveyorFamily(null,null,transducer,2,TChannel.BREAKOUT,cPanel);
		conveyor3=new ConveyorFamily(null,null,transducer,3,TChannel.MANUAL_BREAKOUT,cPanel);
		conveyor8=new ConveyorFamily(null,null,transducer,8,TChannel.WASHER,cPanel);
		conveyor10=new ConveyorFamily(null,null,transducer,10,TChannel.PAINTER,cPanel);
		conveyor11=new ConveyorFamily(null,null,transducer,11,TChannel.UV_LAMP,cPanel);
		conveyor13=new ConveyorFamily(null,null,transducer,13,TChannel.OVEN,cPanel);
		conveyor4= new ConveyorFamilyShuttle(4,transducer);
		conveyor9= new ConveyorFamilyShuttle(9,transducer);
		conveyor12= new ConveyorFamilyShuttle(12,transducer);
		
		conveyor5 = new ConveyorFamilyOffline5(transducer,conveyor4,5);
		conveyor6 = new ConveyorFamilyOffline(transducer,conveyor5,6);
		conveyor7 = new ConveyorFamilyOffline(transducer,conveyor6,7);
		
		conveyor14 = new ConveyorFamilyLast(transducer,conveyor13);
		
		conveyor0.setNextConveyor(conveyor1);
		
		conveyor1.setNeighbor(conveyor0,ConveyorFamilyType.FROM);
		conveyor1.setNeighbor(conveyor2,ConveyorFamilyType.TO);
		
		conveyor2.setNextCF(conveyor3);
		conveyor2.setPreviousCF(conveyor1);
		
		conveyor3.setNextCF(conveyor4);
		conveyor3.setPreviousCF(conveyor2);
		
		conveyor4.setNeighbor(conveyor3,ConveyorFamilyType.FROM);
		conveyor4.setNeighbor(conveyor5,ConveyorFamilyType.TO);
		
		conveyor9.setNeighbor(conveyor8,ConveyorFamilyType.FROM);
		conveyor9.setNeighbor(conveyor10,ConveyorFamilyType.TO);
		
		conveyor12.setNeighbor(conveyor11,ConveyorFamilyType.FROM);
		conveyor12.setNeighbor(conveyor13,ConveyorFamilyType.TO);
		

		conveyor5.setNext(conveyor6);
		//conveyor5.disableBot();
		
		conveyor6.setNext(conveyor7);
		conveyor7.setNext(conveyor8);
		
		conveyor8.setNextCF(conveyor9);
		conveyor8.setPreviousCF(conveyor7);
		
		conveyor10.setNextCF(conveyor11);
		conveyor10.setPreviousCF(conveyor9);
		
		conveyor11.setNextCF(conveyor12);
		conveyor11.setPreviousCF(conveyor10);
		
		conveyor13.setNextCF(conveyor14);
		conveyor13.setPreviousCF(conveyor12);

		conveyor1.startUp();
		conveyor2.startAllAgentThreads();
		conveyor3.startAllAgentThreads();
		
		conveyor5.starThread();
		conveyor6.starThread();
		conveyor7.starThread();
		conveyor8.startAllAgentThreads();
		conveyor10.startAllAgentThreads();
		conveyor11.startAllAgentThreads();
		conveyor13.startAllAgentThreads();
		 
		conveyor4.startUp();
		conveyor9.startUp();
		conveyor12.startUp();
		conveyor5.msgIAmReady();
		conveyor6.msgIAmReady();
		conveyor7.msgIAmReady();
		System.out.println("Back end initialization finished.");
		
//		conveyor5.disableBoth();
//		conveyor5.jam();
//		conveyor5.turnOn();
		conveyor5.breakGlassTop();
		
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
	
	public ConveyorFamilyInterface getConveyor(int num) {
		if (num == 0)
			return conveyor0;
		else
			return null;
	}
}
