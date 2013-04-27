package engine.conveyorfamily.shuttle;

import java.util.ArrayList;
import java.util.List;

import transducer.Transducer;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;


public class ConveyorFamilyShuttle implements ConveyorFamilyInterface {
	int ID;
	public Conveyor conveyor;
	Transducer t;
	List<MyConveyorFamily> conveyorFamilyList = new ArrayList<MyConveyorFamily>(); //neighbors
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
		
		//TODO: change later
		if (ID != 4)
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

	
	// request release - send glass to next conveyor
	public void msgReleaseGlass(Glass g) {
			for (MyConveyorFamily mcf: conveyorFamilyList) {
	
				if (mcf.type == ConveyorFamilyType.TO) {
					mcf.conveyorFamily.msgHereIsGlass(g);
				}
		}
	}
	
	public void tellPreviousIAmReady()
	{
		for (MyConveyorFamily mcf: conveyorFamilyList) 
		{
			
			if (mcf.type == ConveyorFamilyType.FROM) 
			{
				mcf.conveyorFamily.msgIAmReady();
			}
		}
	}

	@Override
	public void msgDeleteGlass(Glass g)
	{
		// TODO Auto-generated method stub
		
	}


}