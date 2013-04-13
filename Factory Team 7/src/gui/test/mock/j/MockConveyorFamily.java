package gui.test.mock.j;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle.ConveyorFamilyType;
//TODO
//import engine.conveyorfamily.j.ConveyorFamilyJ;
//import engine.conveyorfamily.j.ConveyorFamilyJ.ConveyorFamilyType;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;


import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;


public class MockConveyorFamily implements ConveyorFamilyInterface, TReceiver{

	public Transducer t;
	MyConveyorFamily mcfTO;
	MyConveyorFamily mcfFROM;
	TChannel myChannel;
	Glass g = null;
	boolean ready = true;
	int ID;
	boolean sensorPressed = false;
	ArrayList<Glass> glassList = new ArrayList<Glass>();
	boolean popupReady = true;
	
	public class MyConveyorFamily {
		//TODO
		//		ConveyorFamilyJ conveyorFamily;
		ConveyorFamilyInterface conveyorFamily;
		ConveyorFamilyType type;

		public MyConveyorFamily(ConveyorFamilyInterface cf,ConveyorFamilyType ty) {
			//TODO
			//conveyorFamily = (ConveyorFamilyJ) cf;
			conveyorFamily = cf;
			type = ty;
		}
	}

	public MockConveyorFamily(int id,Transducer trans,TChannel chan) {
		t = trans;

		
		t.register(this, TChannel.SENSOR);
		t.register(this, TChannel.POPUP);
		t.register(this,chan);
		t.register(this, TChannel.TRUCK);
		ID = id;
		myChannel = chan;
	
	}

	@Override
	public void msgHereIsGlass(Glass g) {
			glassList.add(g);
			System.out.println("glass added mock" + ID);
			mcfFROM.conveyorFamily.msgIAmReady();
	}

	@Override
	public void msgIAmReady() {
		ready = true;
		Integer[] newArgs = new Integer[1];
		newArgs[0] = ID;
		/*if (sensorPressed == true) {
			mcfTO.conveyorFamily.msgHereIsGlass(glassList.remove(0)); 
		} */
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
	}

	//TODO
	//public void setNeighbor(ConveyorFamilyJ fam, ConveyorFamilyType ty) {
	public void setNeighbor(ConveyorFamilyInterface fam,ConveyorFamilyType ty) {
		if (ty == ConveyorFamilyType.TO)
		mcfTO = new MyConveyorFamily(fam,ty);
		else
			mcfFROM = new MyConveyorFamily(fam,ty);
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		int sensor0ID = ID*2;
		int sensor1ID = ID*2+1;

		if (channel == TChannel.SENSOR && event==TEvent.SENSOR_GUI_PRESSED) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = (Integer)args[0] / 2;
			if ((Integer)args[0] == sensor0ID) {
				t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
			}
			else if ((Integer)args[0]== sensor1ID) {
				sensorPressed = true;
				if (ready && (ID <5 || ID > 7)) {
					if (mcfTO.conveyorFamily != null) {
					mcfTO.conveyorFamily.msgHereIsGlass(glassList.remove(0));
					}
					else
						glassList.remove(0);
					
					
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
					ready = false;
				}
				else if (ID >=5 || ID <= 7){
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
				}
				else {
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, newArgs);
				}
				}
	/*		else if (((Integer)args[0] % 2) == 0 && (Integer)args[0] > 9)
				{
					newArgs[0] = (Integer)args[0] / 2;
					t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, newArgs);
				}*/
		}
		else if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			Integer[] newArgs = new Integer[1];
			newArgs[0] = (Integer)args[0] / 2;
			if ((Integer)args[0] == sensor1ID) {
				sensorPressed = false;
			}

		}
		else if (channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED)
		{
			if ((Integer)args[0] == 0 && ID == 5) {
				if (ready) {
					ready = false;
					mcfTO.conveyorFamily.msgHereIsGlass(glassList.remove(0));
					t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);					
				}
			}
			if ((Integer)args[0] == 1 && ID == 6){
				if (ready) {
					ready = false;
					mcfTO.conveyorFamily.msgHereIsGlass(glassList.remove(0));
					t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);					
				}
			}
			if ((Integer)args[0] == 2 && ID == 7){
				if (ready) {
					ready = false;
					mcfTO.conveyorFamily.msgHereIsGlass(glassList.remove(0));
					t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);					
				}
			}
			
			
		}
		else if(channel==TChannel.TRUCK && event == TEvent.TRUCK_GUI_LOAD_FINISHED){//added by monroe
			t.fireEvent(TChannel.TRUCK, TEvent.TRUCK_DO_EMPTY, null);
			mcfFROM.conveyorFamily.msgIAmReady();
		}



	}



}











