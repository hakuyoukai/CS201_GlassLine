package engine.conveyorfamily.shuttle;

/*
 * 	Author: Joey Huang
 *  4/14/13
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
	public enum GlassState {LOADING,WAITINGTOSEND,RELEASING};
	public boolean sensorState[] = {false,false};	// pressed = true
	public boolean receiveOK = false;
	public boolean sendOK = false;	// permission to send active until a glass is sent
	public ConveyorFamilyShuttle conveyorFamily;
	public boolean conveyorMoving = false;
	public boolean sendingGlass = false;	// from intent to send a glass to GUI release
	public Transducer t;
	public Glass incomingGlass;
	boolean moveOK = false;
	int sensorCount = 0;	// keeps track of how many glasses are between the sensors
	boolean shuttleFinished = false;
	int readyCount = 0;
	int completed = 0;

	
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
		moveOK = true;
	}


	// sent by conveyor family, receiving glass
	public void msgHereIsGlass(Glass g) {
		System.out.println("CONVEYOR " + ID + ": " + "got msgHereIsGlass.");
			incomingGlass = g;
			
			if (!glassList.isEmpty()) {
				if (glassList.get(glassList.size()-1).glass == null) {
					glassList.get(glassList.size()-1).glass = g;
					glassList.get(glassList.size()-1).state = GlassState.WAITINGTOSEND;
					incomingGlass = null;
 
					// ready to process
					if(glassList.size() == 1 ) {
						stateChanged();
					}
				}
			}
			
	}


	public void msgIAmReady() {
		readyCount++;
		System.out.println("CONVEYOR " + ID + ": msgIAmReady received =====================================" + readyCount + conveyorMoving);
		
		sendOK = true;

		
		if (!glassList.isEmpty()) {
			stateChanged();
		}	
	}


	public void sensorPressed(Integer sensorNum) {
		sensorState[sensorNum] = true;
		if (sensorNum == 0) {
			sensorCount++;
			MyGlass mg;
			if (incomingGlass == null) { // create new glass holder if message hasn't arrived
				 mg = new MyGlass(incomingGlass,GlassState.LOADING);
				glassList.add(mg);
			}
			else {
				mg = new MyGlass(incomingGlass,GlassState.WAITINGTOSEND); // add glass to list
				glassList.add(mg);
				incomingGlass = null;
				if(glassList.size() == 1){ // ready to process
					stateChanged();
					
				}
			}
		}
		else if (sensorNum == 1) { // and conveyor is movingg - has to be to trigger this
			sensorCount--;

			if (!sendingGlass) {
				stopConveyor();
			}
		}
	}


	public void sensorReleased(Integer sensorNum) {
		sensorState[sensorNum] = false;

		if (sensorNum == 0) {	

			updateReadyStates();
			if (receiveOK && moveOK) {	// cok to accept glass
				conveyorFamily.msgConveyorReady();
				System.out.println("CONVEYOR " + ID + ": READY ===============");
			}

		} 
		else if (sensorNum == 1) {
			if (!sendingGlass)
				stopConveyor();
		}
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		updateReadyStates();

		
		if (!glassList.isEmpty()) {
			// sendingGlass always true before glassstate.releasing
			
			if (glassList.get(0).state != GlassState.RELEASING && conveyorMoving)
				stopConveyor();
			
			// reaches shuttle and is ready to send
			if (glassList.get(0).state == GlassState.RELEASING && sendingGlass == false) {
				sendGlass();
				return true;
			}
			
			// new head, to be released
			if (glassList.get(0).state == GlassState.WAITINGTOSEND && sendOK && sendingGlass == false && (readyCount > completed)) {
					moveGlass();
				return true;
			}

			
		}
		return false;
	}

	// get ready to send it out
	public void moveGlass() {
		sendingGlass = true;
		glassList.get(0).state = GlassState.RELEASING;
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
				stopConveyor();	// finished loading shuttle. getting ready to send
				stateChanged();
			}
		}
	}

// send glass to next conveyor
	public void sendGlass() {
		sendOK = false;
		MyGlass mg = glassList.remove(0);
		conveyorFamily.msgReleaseGlass(mg.glass);
		completed++;
		System.out.println("CONVEYOR " + ID + ": msgHereIsGlass sent====================" + completed);
	}

// call to animation to start conveyor
	public void startConveyor() {
		conveyorMoving = true;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);

	}

	// call to animation to stop conveeyor
	public void stopConveyor() {
		conveyorMoving = false;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);

	}

// checks if it's ok to send, ok to receive, ok to move conveyor
	public void updateReadyStates() {	
		moveOK = false;
		// its ok to move if:
		if(sensorState[1] == false && (sensorCount >= glassList.size())) // there is room to move at the end
			moveOK = true;
		// theres a glass on its way out
		if (!glassList.isEmpty() && glassList.get(0).state == GlassState.RELEASING && sendingGlass==true)
			moveOK = true;
		
		receiveOK = false; // receiveOK is only positional
		if (sensorState[0] == false)
			receiveOK = true;
		}

}
