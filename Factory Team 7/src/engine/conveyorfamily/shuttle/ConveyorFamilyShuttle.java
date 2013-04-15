package engine.conveyorfamily.shuttle;

import java.util.ArrayList;
import java.util.List;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;
import gui.panels.DisplayPanel;

public class ConveyorFamilyShuttle implements ConveyorFamilyInterface {
	int ID;
	public Conveyor conveyor;

	public boolean receiveOK;
	public boolean sendOK;
	Transducer t;
	List<MyConveyorFamily> conveyorFamilyList = new ArrayList<MyConveyorFamily>();

	DisplayPanel display;
	public enum ConveyorFamilyType {TO,FROM};
	class MyConveyorFamily {
		ConveyorFamilyInterface conveyorFamily;
		ConveyorFamilyType type;
		
		public MyConveyorFamily(ConveyorFamilyInterface cf, ConveyorFamilyType t) {
			conveyorFamily = cf;
			type = t;
		}
	}
	

	public ConveyorFamilyShuttle(int IDnum,Transducer t) {


		ID = IDnum;
		this.t = t;

		conveyor = new Conveyor(IDnum,t);

		conveyor.setConveyorFamily(this);
		
		conveyor.startUp();

		
		conveyor.startThread();

	}
	
	public void startUp() {
		msgConveyorReady();
		msgIAmReady();
	}
	
	// adds an adjacent conveyor family, specifying to/from
	public void setNeighbor(ConveyorFamilyInterface cf, ConveyorFamilyType cft) {
		conveyorFamilyList.add(new MyConveyorFamily(cf,cft));
	}

	
	
	
	// sent from adjacent conveyor family
	@Override
	public void msgHereIsGlass(Glass g) {
		conveyor.msgHereIsGlass(g);
	}
	
	// next conveyor family is ready to accept
	@Override
	public void msgIAmReady() {
		conveyor.msgIAmReady();		
	}
	
	// alert adjacent conveyor family that this is
	// ready to receive
	public void msgConveyorReady() {
		for (MyConveyorFamily mcf:conveyorFamilyList) {
			if (mcf.type == ConveyorFamilyType.FROM) {
				mcf.conveyorFamily.msgIAmReady();
			}
		}
	}

	
	// popup request release animation
	public void msgReleaseGlass(Glass g) {
			for (MyConveyorFamily mcf: conveyorFamilyList) {
	
				if (mcf.type == ConveyorFamilyType.TO) {
					//System.out.println("CONVEYOR " + ID + ": sending msgHereIsGlass with glass != null: " + !(g==null));
					mcf.conveyorFamily.msgHereIsGlass(g);
				}
		}
	}


}