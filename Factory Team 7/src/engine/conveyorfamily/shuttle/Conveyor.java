package engine.conveyorfamily.shuttle;

/*
 * 	Author: Joey Huang
 * 
 * 
 */

import java.util.ArrayList;
import java.util.List;
import engine.agent.Agent;
import engine.util.Glass;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;


public class Conveyor extends Agent{
	public int ID;
	public List<MyGlass> glassList = new ArrayList<MyGlass>();
	public enum GlassState {LOADING,WAITINGTOSEND,TOSEND,SENDING,RELEASING};
	public boolean sensorState[] = {false,false};
	public boolean receiveOK = false;
	public boolean sendOK = false;
	ConveyorFamilyShuttle conveyorFamily;
	public boolean conveyorMoving = false;
	boolean sendingGlass = false;
	Transducer t;
	public Glass incomingGlass;
	boolean moveOK = false;
	int sensorCount = 0;
	boolean shuttleFinished = false;
	
	public class MyGlass {
		Glass glass;
		GlassState state;

		public MyGlass (Glass g,GlassState gs) {
			glass = g;
			state = gs;
		}

		public MyGlass(Glass g) {
			glass = g;
			state = GlassState.LOADING;
		}
	}

	public Conveyor(int id, Transducer transducer) {
		super("conveyor"+id);
		ID = id;
		t = transducer;

		t.register(this, TChannel.SENSOR);
		t.register(this,TChannel.SHUTTLE);
	}


	public void setConveyorFamily(ConveyorFamilyShuttle cf) {
		conveyorFamily = cf;
	}

	// set ready to receive 
	public void startUp() {
		receiveOK = true;
		sendOK = true;
	}


	// sent by conveyor family, receiving glass
	public void msgHereIsGlass(Glass g) {
		updateReadyStates();
		System.out.println("CONVEYOR " + ID + ": " + "got msgHereIsGlass.");
			incomingGlass = g;
			
			if (!receiveOK && moveOK)
				if (!conveyorMoving)
					startConveyor();
			if (!glassList.isEmpty()) {
				if (glassList.get(glassList.size()-1).glass == null) {
					glassList.get(glassList.size()-1).glass = g;
					glassList.get(glassList.size()-1).state = GlassState.WAITINGTOSEND;
					incomingGlass = null;
					System.out.println("CONVEYOR " + ID + ": ready to go.glass loaded");
					 
					updateReadyStates();
					if(glassList.size() == 1 || (!sendOK && moveOK)) {

						System.out.println("CONVEYOR " + ID + ": xxxxxxxxxxxxxxxxxxxxxxxxxxxx3");
						stateChanged();
					}
				}
			}
			
	}


	public void msgIAmReady() {
		System.out.println("CONVEYOR " + ID + ": " + "next conveyor says he's ready to conveyor");
		sendOK = true;

		if (!glassList.isEmpty()) {
			System.out.println("CONVEYOR " + ID + ": " + "size: " + glassList.size() + " " + glassList.get(0).state);

			System.out.println("CONVEYOR " + ID + ": xxxxxxxxxxxxxxxxxxxxxxxxxxxx4");
			stateChanged();
		}	
	}


	public void sensorPressed(Integer sensorNum) {
		sensorState[sensorNum] = true;
		if (sensorNum == 0) {
			sensorCount++;
			MyGlass mg;
			if (incomingGlass == null) {
				 mg = new MyGlass(incomingGlass,GlassState.LOADING);
				glassList.add(mg);
			}
			else {
				mg = new MyGlass(incomingGlass,GlassState.WAITINGTOSEND);
				glassList.add(mg);
				incomingGlass = null;
				System.out.println("CONVEYOR " + ID + ": ready to go.glass loaded2");
				updateReadyStates();
				if(glassList.size() == 1 || (moveOK)) {

					System.out.println("CONVEYOR " + ID + ": xxxxxxxxxxxxxxxxxxxxxxxxxxxx1");
					stateChanged();
					
				}
			}
		}
		else if (sensorNum == 1) { // and conveyor is movingg - has to be to trigger this


			if (!sendingGlass) {
				stopConveyor();
			}
		}
	}



	// first sensor released

	public void sensorReleased(Integer sensorNum) {
		System.out.println("CONVEYOR " + ID + ": " + sensorNum + "released");
		sensorState[sensorNum] = false;

		if (sensorNum == 0) {	// check ready conditions

			updateReadyStates();
			if (receiveOK || moveOK)
				conveyorFamily.msgConveyorReady();

		} 
		else if (sensorNum == 1) {
			sensorCount--;
		}
	}

	@Override
	public boolean pickAndExecuteAnAction() {
	//	System.out.println("CONVEYOR " + ID + ": inside pick");
		updateReadyStates();
		if (glassList.isEmpty()) {
		//	System.out.println("CONVEYOR " + ID + " : Why am i here?");
			return false;
		}
		else {
			if (glassList.get(0).state == GlassState.WAITINGTOSEND) {
				if (sendOK)
					moveGlass();
				else if (moveOK) {
					if (!conveyorMoving)
						startConveyor();
				}
				return true;
			}
			
			
			if (glassList.get(0).state == GlassState.RELEASING && sendingGlass == false) {
				sendGlass();
				return true;
			}
			
		}
		return false;
	}

	public void moveGlass() {
		System.out.println("CONVEYOR " + ID + ": moveGlass");
		glassList.get(0).state = GlassState.RELEASING;
		sendingGlass = true;
		if (!conveyorMoving)
			startConveyor();
	}

	
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {

		int sensor0ID = ID*2;
		int sensor1ID = ID*2+1;
		if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED) {
			if (((Integer)args[0] == sensor0ID || (Integer)args[0] == sensor1ID) ){
				Integer[] newArgs = new Integer[1];

				newArgs[0] = (Integer)args[0] %2;
				sensorReleased((Integer)newArgs[0]);
			}
		}
		else if (channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED)
		{
			if (((Integer)args[0] == sensor0ID || (Integer)args[0] == sensor1ID)) {
				Integer[] newArgs = new Integer[1];
				newArgs[0] = (Integer)args[0] %2;
				sensorPressed((Integer)newArgs[0]);	
			}

		}
		else if (channel == TChannel.SHUTTLE && event == TEvent.SHUTTLE_FINISHED_LOADING) {
			if ((Integer)args[0] == ID) {
				sendingGlass = false;

				System.out.println("CONVEYOR " + ID + ": xxxxxxxxxxxxxxxxxxxxxxxxxxxx2");
				stateChanged();
			}
		}
	}


	public void sendGlass() {
		sendOK = false;
		System.out.println("CONVEYOR " + ID + ": sending glass");
		MyGlass mg = glassList.remove(0);
		conveyorFamily.msgReleaseGlass(mg.glass);
		updateReadyStates();

	}

	public void startConveyor() {
		conveyorMoving = true;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);

	}

	public void stopConveyor() {
		conveyorMoving = false;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);

	}


	public void updateReadyStates() {	
		moveOK = false;
		// its ok to move if:
		if(sensorState[1] == false && (sensorCount >= glassList.size())) // there is room to move at the end
			moveOK = true;
		if (!glassList.isEmpty() && glassList.get(0).state == GlassState.RELEASING)
			moveOK = true;
		
		
		
		receiveOK = false;
		
		if (sensorState[0] == false)
			receiveOK = true;
		
//		System.out.println("sendOK: " + sendOK + " receiveOK " + receiveOK + "moveOK " + moveOK);
	}

}
