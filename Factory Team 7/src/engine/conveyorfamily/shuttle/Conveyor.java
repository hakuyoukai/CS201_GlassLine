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

	public boolean receiveOK = false;	// space w/o moving
	public boolean sendOK = false;	// permission to send active until a glass is sent
	boolean allowed;
	boolean moveOK = false;
	
	public ConveyorFamilyShuttle conveyorFamily;
	public boolean conveyorMoving = false;
	public boolean sendingGlass = false;	// from intent to send a glass to GUI release
	public Transducer t;
	public Glass incomingGlass;
	int sensorCount = 0;	// keeps track of how many glasses are between the sensors - handle GUI issues
	boolean shuttleFinished = false;
	
	// make sure number of messages match
	int readyCount = 0; // next conveyor ready
	int completed = 0; // sent glass
	int receiveCount = 0; // received glass
	int conveyorReadyCount = 0; // i am ready sent
	
	// for non-norms
	public boolean conveyorJam = false;
	public boolean conveyorQuiet = true;
	public boolean shuttleBroken = false;
	
	boolean[] outerGlassLoc = {false,false};
	
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
		t.register(this, TChannel.CONTROL_PANEL);
	}


	public void setConveyorFamily(ConveyorFamilyShuttle cf) {
		conveyorFamily = cf;
	}

	// set ready to receive 
	public void startUp() {
		receiveOK = true;
		sendOK = true;
		moveOK = true;
		allowed = true;
		conveyorReadyCount = 1;
		// readycount set in conveyorfamily sending msgiamready initially
	}

	public void msgDeleteGlass() {
		if (incomingGlass != null) {
			System.out.println("CONVEYOR: " + ID + ": deleted glass");
			incomingGlass = null;
			receiveCount--;
		}
	}

	// sent by conveyor family, receiving glass
	public void msgHereIsGlass(Glass g) {
		conveyorQuiet = false;
		receiveCount++;
		System.out.println("CONVEYOR " + ID + ": " + "got msgHereIsGlass. rec red cred com " + receiveCount + readyCount + conveyorReadyCount + completed);
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
		System.out.println("CONVEYOR " + ID + ": msgIAmReady received ============= ready received completed" + readyCount +receiveCount + completed+ "size: " + glassList.size());
		allowed = true;
		if (!conveyorJam && !shuttleBroken)
			sendOK = true;
		
		
		if (!glassList.isEmpty()) {
			System.out.println("CCONVEYOR " + ID + ": state" + glassList.get(glassList.size()-1).state);
			stateChanged();
		
		}	
	}
	
	public void askedIfAvailable()
	{
		if(receiveOK)
		{
			conveyorFamily.tellPreviousIAmReady();
		}
	}


	public void sensorPressed(Integer sensorNum) {
		System.out.println("CONVEYOR " + ID + ": sensor " + sensorNum + " pressed " + (incomingGlass != null));
		sensorState[sensorNum] = true;
		
		if (sensorNum == 0) {
			conveyorQuiet = false;
			sensorCount++;
			MyGlass mg;
			if (incomingGlass == null) { // create new glass holder if message hasn't arrived
				 mg = new MyGlass(incomingGlass,GlassState.LOADING);
				glassList.add(mg);
				System.out.println("CONVEYOR " + ID + ": null glass added");
			}
			else {
				mg = new MyGlass(incomingGlass,GlassState.WAITINGTOSEND); // add glass to list
				glassList.add(mg);
				System.out.println("CONVEYOR " + ID + ": regular glass added");
				incomingGlass = null;
				if(glassList.size() == 1){ // ready to process
					stateChanged();
				}
			}
		}
		else if (sensorNum == 1) { // and conveyor is movingg - has to be to trigger this
			sensorCount--;

			if (!sendingGlass || shuttleBroken) {
				System.out.println("STOP HERE");
				stopConveyor();
			}
			
//TODO:
		//	if ((receiveCount == conveyorReadyCount) && glassList.size() == 1 && !conveyorJam && (incomingGlass == null) && !shuttleBroken) {	// cok to accept glass
			if ((receiveCount == conveyorReadyCount) && glassList.size() == 1 && (incomingGlass == null)) {
				conveyorFamily.msgConveyorReady();
				conveyorReadyCount++;
				System.out.println("CONVEYOR " + ID + ": Iam ready sent ===============");
			}
		}
	}


	public void sensorReleased(Integer sensorNum) {
		System.out.println("CONVEYOR " + ID + ": sensor " + sensorNum + " released");
		sensorState[sensorNum] = false;
		
		if (sensorNum == 0) {	

			updateReadyStates();
			// TODO:
			//if (receiveOK && moveOK && !conveyorJam && (receiveCount == conveyorReadyCount) && !shuttleBroken) {	// cok to accept glass
			if (receiveOK && (receiveCount == conveyorReadyCount))	{
				conveyorFamily.msgConveyorReady();
				conveyorReadyCount++;
				System.out.println("CONVEYOR " + ID + ": Iam ready sent ===============");
			}

		} 
		else if (sensorNum == 1) {
			if (!sendingGlass || shuttleBroken)
				stopConveyor();
			
		}
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		
		updateReadyStates();

		
		if (!conveyorQuiet && !glassList.isEmpty()) {
			// sendingGlass always true before glassstate.releasing
			
			if (glassList.get(0).state != GlassState.RELEASING && conveyorMoving)
				stopConveyor();
			
			// reaches shuttle and is ready to send
			if (glassList.get(0).state == GlassState.RELEASING && sendingGlass == false) {
				sendGlass();
				return true;
			}
			
			// new head, to be released
			if (glassList.get(0).state == GlassState.WAITINGTOSEND && sendOK && sendingGlass == false && (readyCount == completed+1)) {
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
		if (!conveyorMoving && !conveyorJam)
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
		else if (channel == TChannel.CONTROL_PANEL && event == TEvent.CONVEYOR_JAM) {
			if ((Integer)args[0] == ID) {
				conveyorJam = true;
				stopConveyor();
			}
		}
		else if (channel == TChannel.CONTROL_PANEL && event == TEvent.CONVEYOR_UNJAM) {
			if ((Integer)args[0] == ID) {
				conveyorJam = false;
				updateReadyStates();
			//TODO
				
//				if (receiveOK && moveOK && !conveyorJam && (receiveCount == conveyorReadyCount)) {	// cok to accept glass
				if (receiveOK && (receiveCount == conveyorReadyCount)) {
					conveyorReadyCount++;
					conveyorFamily.msgConveyorReady();
					System.out.println("CONVEYOR " + ID + ": i am ready sent ===============");
				}
				if (allowed == true) {
					sendOK = true;
					startConveyor();
					stateChanged();
				}
			}
		}
		else if (channel == TChannel.CONTROL_PANEL && event == TEvent.GUI_BREAK_SHUTTLE) {
			int indexNum = (Integer) args[0];
			
			if ((indexNum == 0 && ID == 1) || (indexNum == 1 && ID == 4) || (indexNum == 2 && ID == 9) || (indexNum == 3 && ID == 12)) {
				shuttleBroken = true;
				//stopConveyor();
				if (!glassList.isEmpty() && glassList.get(0).state == GlassState.RELEASING && glassList.size() > sensorCount || sensorState[1] == true) {	// there is a glass on the shuttle
					stopConveyor();
				}
				
			}
		}
		else if (channel == TChannel.CONTROL_PANEL && event == TEvent.GUI_FIX_SHUTTLE) {
			int indexNum = (Integer) args[0];
			if ((indexNum == 0 && ID == 1) || (indexNum == 1 && ID == 4) || (indexNum == 2 && ID == 9) || (indexNum == 3 && ID == 12)) {
				shuttleBroken = false;
			

			updateReadyStates();
			if (receiveOK && moveOK && !conveyorJam && (receiveCount == conveyorReadyCount)) {	// cok to accept glass
				conveyorReadyCount++;
				conveyorFamily.msgConveyorReady();
				System.out.println("CONVEYOR " + ID + ": i am ready sent ===============");
			}
			if (allowed == true) {
				sendOK = true;
				startConveyor();
				stateChanged();
			}
			}
		}
	}

// send glass to next conveyor
	public void sendGlass() {
		allowed = false;
		sendOK = false;
		MyGlass mg = glassList.remove(0);

		System.out.println("CONVEYOR " + ID + ": glass released " + glassList.size());
		conveyorFamily.msgReleaseGlass(mg.glass);
		completed++;
		//System.out.println("CONVEYOR " + ID + ": msgHereIsGlass sent====================" + completed);
		if (glassList.size() == 0) {
			conveyorQuiet = true;
			stopConveyor();
		}
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
		
		
		// there is room to move at the end

		if(sensorState[1] == false && (sensorCount >= glassList.size())) 
			moveOK = true;
		// theres a glass on its way out
		if (!glassList.isEmpty() && glassList.get(0).state == GlassState.RELEASING)// && sendingGlass==true)
			moveOK = true;
		
		receiveOK = false; // receiveOK is only positional
		
		//TODO
		//if (sensorState[0] == false && !conveyorJam && !shuttleBroken)
		if (sensorState[0] == false)
			receiveOK = true;
		}

}
