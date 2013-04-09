package gui.test.mock.j;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import engine.conveyorfamily.j.ConveyorFamilyJ;
import engine.conveyorfamily.j.ConveyorFamilyJ.ConveyorFamilyType;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;


import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;


public class MockConveyorFamily implements ConveyorFamilyInterface, TReceiver{

	public Transducer t;
	ConveyorFamilyType type;
	MyConveyorFamily mcf;
	Map<Integer,Integer> recipe;
	
	Glass g = null;
	boolean ready = true;
	int ID;
	boolean sensorPressed = false;
	ArrayList<Glass> glassList = new ArrayList<Glass>();
	
	public class MyConveyorFamily {
		ConveyorFamilyJ conveyorFamily;
		ConveyorFamilyType type;
		
		public MyConveyorFamily(ConveyorFamilyInterface cf,ConveyorFamilyType ty) {
			conveyorFamily = (ConveyorFamilyJ) cf;
			type = ty;
		}
	}

	public MockConveyorFamily(int id, ConveyorFamilyType typ,Transducer trans) {
		t = trans;
		type = typ;
		t.register(this, TChannel.SENSOR);
		ID = id;
		recipe = new HashMap<Integer,Integer>();
		recipe.put(0, 1);
		recipe.put(1,1);
		recipe.put(2, 1);
		recipe.put(3, 1);
		recipe.put(4,1);
		recipe.put(5, 1);
		recipe.put(6, 1);
		recipe.put(7,1);
		recipe.put(8, 1);
		recipe.put(9, 1);
		recipe.put(10,1);
	}
	
	@Override
	public void msgHereIsGlass(Glass g) {
		if (type == ConveyorFamilyType.TO) {
			glassList.add(g);
			System.out.println("glass added mock" + ID);
			/*if (ready) {
				mcf.conveyorFamily.msgIAmReady();
				System.out.println("TO is ready");
			}
			else
				System.out.println("TO not ready");
			*/
			mcf.conveyorFamily.msgIAmReady();
			
		}
		else {
			glassList.add(g);
			System.out.println("glass added mock" + ID);
		}



	}

	@Override
	public void msgIAmReady() {
		
		System.out.println("READY " + type);
		ready = true;
		Integer[] newArgs = new Integer[1];
		newArgs[0] = ID;
		if (sensorPressed == true) {
	///		Glass g = new Glass(null);
	//		mcf.conveyorFamily.msgHereIsGlass(g);	
			mcf.conveyorFamily.msgHereIsGlass(glassList.remove(0));
		}
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
	}


	public void setNeighbor(ConveyorFamilyJ fam, ConveyorFamilyType ty) {
		mcf = new MyConveyorFamily(fam,ty);
		
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		int sensor0ID = ID*2;
		int sensor1ID = ID*2+1;

		if (channel == TChannel.SENSOR && event==TEvent.SENSOR_GUI_PRESSED) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = (Integer)args[0] / 2;
			if ((Integer)args[0] == sensor0ID) {
		/*		if (mcf.type == ConveyorFamilyType.FROM) {
				g = new Glass(null);
				msgHereIsGlass(g);
				}*/
			/*	if (sensorPressed)
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
				else {
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
				} */
				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
			}
			else if ((Integer)args[0]== sensor1ID) {
				sensorPressed = true;
			//	if (ready == false) {
			//		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);	
			//	} else {
					ready = false;
					Glass g = new Glass(null);
					mcf.conveyorFamily.msgHereIsGlass(g);
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
			//	}
			}
		}
		else if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = (Integer)args[0] / 2;
			if ((Integer)args[0] == sensor1ID) {
				sensorPressed = false;
			}

		}

		
	}


	
}











