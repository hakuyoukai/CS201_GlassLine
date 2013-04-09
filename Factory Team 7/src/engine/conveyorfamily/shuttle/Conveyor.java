package engine.conveyorfamily.shuttle;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import engine.agent.Agent;
import engine.util.Glass;


//import interfaces.Transducer;

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
	public boolean allowed = false;
	ConveyorFamilyShuttle conveyorFamily;
	public boolean conveyorMoving = true;
	boolean sendingGlass = false;
	Transducer t;
	public Glass incomingGlass;
	long startTime = 0;
	long endTime = 0;
	int intervalset = 0;
	double timeInterval = 0;
	double defaultInterval = 1000/24;
	double currTimeInterval = 0;

	Timer timer = new Timer();
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
		sendOK = false;
		receiveOK = false;
		conveyorMoving = false;
		t = transducer;

		t.register(this, TChannel.SENSOR);
		t.register(this,TChannel.SHUTTLE);
		t.register(this,TChannel.CONTROL_PANEL);


	}


	public void setConveyorFamily(ConveyorFamilyShuttle cf) {
		conveyorFamily = cf;
	}

	// set ready to receive 
	public void startUp() {
		receiveOK = true;
		//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + receiveOK);	// TODO: for testing, need to make members public
		sendOK = false;
		//conveyorFamily.display.parent.cPanel.testPanel.sendLabel.setText("send: " + sendOK);
		conveyorMoving = true;
	}


	// sent by conveyor family, receiving glass
	public void msgHereIsGlass(Glass g) {
		if (incomingGlass == null) {
			System.out.println("CONVEYOR " + ID + ": " + "got glass conveyor.");
			incomingGlass = g;
			
			if (sensorState[0] == true && !conveyorMoving) {
			MyGlass mg = new MyGlass(g,GlassState.WAITINGTOSEND);			// receive on message reception? or when glass loaded
			glassList.add(mg);
			startConveyor();
	
				System.out.println("CONVEYOR " + ID + ": " + mg.state + " num glasses: " + glassList.size());
			}		
				receiveOK = false;
			//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + receiveOK);
		}	
		//	stateChanged();

	}


	public void msgIAmReady() {
		System.out.println("CONVEYOR " + ID + ": " + "next conveyor says he's ready to conveyor");
		allowed = true;
		sendOK = true;
		//conveyorFamily.display.parent.cPanel.testPanel.sendLabel.setText("send: " + sendOK);

		if (!glassList.isEmpty()) {
			System.out.println("CONVEYOR " + ID + ": " + "size: " + glassList.size() + " " + glassList.get(0).state);
			stateChanged();
		}	
	}


	public void sensorPressed(Integer sensorNum) {
		sensorState[sensorNum] = true;

		System.out.println("CONVEYOR " + ID + ": " + sensorNum + "pressed");
		if (sensorNum == 0) {
			if (incomingGlass == null)
				stopConveyor();
			else
			startConveyor();
			if (incomingGlass != null) {
				MyGlass mg = new MyGlass(incomingGlass,GlassState.WAITINGTOSEND);
				glassList.add(mg);
				System.out.println("CONVEYOR " + ID + ": " + "glass added to list");
				incomingGlass = null;
				System.out.println("CONVEYOR " + ID + ": " + mg.state + " num glasses: " + glassList.size());
				updateReadyStates();
				if (glassList.size() >0)
					glassList.get(glassList.size()-1).state = GlassState.WAITINGTOSEND;
			}
		}
		else if (sensorNum == 1) { // and conveyor is movingg - has to be to trigger this


			if (!sendingGlass)
				stopConveyor();

			updateReadyStates();
			if (sendOK == false) {
				receiveOK = false;
				glassList.get(0).state = GlassState.WAITINGTOSEND;
				//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + receiveOK);
			}
			else 
			{
				glassList.get(0).state = GlassState.SENDING;
				stateChanged();
			}
		}
	}



	// first sensor released

	public void sensorReleased(Integer sensorNum) {
		System.out.println("CONVEYOR " + ID + ": " + sensorNum + "released");
		sensorState[sensorNum] = false;

		if (sensorNum == 0) {	// check ready conditions

			updateReadyStates();
			
			System.out.println("CONVEYOR " + ID + ": " + "sensor released. send ok?: " + sendOK + " state: " + glassList.get(0).state);
			updateReadyStates();
			if (receiveOK)
				conveyorFamily.msgConveyorReady();

			if (sendOK == false) {
				timer = new Timer();
				timer.schedule(new TimerTask() {
		    		@Override
		    		public void run() {
		    			stopConveyor();
		    			timer.cancel();
		    		}
		    	},(long) currTimeInterval);
			}	
		} 
		else if (sensorNum == 1) {
			if (glassList.get(0).state == GlassState.RELEASING) {
				sendOK = false;
				if (allowed)
					sendOK = allowed;
				glassList.remove(0);
				System.out.println("CONVEYOR " + ID + ": "  + " num glasses: " + glassList.size());
				//conveyorFamily.display.parent.cPanel.testPanel.sendLabel.setText("send: " + sendOK);
			}
			else {
				System.out.println("CONVEYOR: releasing not ready: " + glassList.get(0).state);
			}
		}
	}

	@Override
	public boolean pickAndExecuteAnAction() {
		if (!glassList.isEmpty()) {
			if (sendOK && glassList.get(0).state == GlassState.RELEASING && sensorState[1] == false) {
				resume();
				return true;
			}

			if (glassList.get(0).state == GlassState.SENDING && sensorState[1] == true) {
				sendGlass(glassList.get(0));
				return true;
			}
			updateReadyStates();
			if (sendOK && (glassList.get(0).state == GlassState.WAITINGTOSEND || glassList.get(0).state == GlassState.TOSEND)) {
				if (sensorState[1] == false) {
					glassList.get(0).state = GlassState.TOSEND;
					if (conveyorMoving== false)
						startConveyor();
				}
				else if (sensorState[1] == true)
					glassList.get(0).state = GlassState.SENDING;
				stateChanged();
				return true;
			}

		}
		return false;
	}

	public void resume() {
		startConveyor();
		conveyorFamily.msgConveyorReady();
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
			else if ((Integer)args[0] == 0) {
				if (intervalset== 0) {
					Calendar cal = Calendar.getInstance();
					startTime = cal.getTimeInMillis();
					intervalset++;
				}
			}
			else if ((Integer)args[0] == 1) {
				if (intervalset == 1) {
					Calendar cal = Calendar.getInstance();
					endTime = cal.getTimeInMillis();
					timeInterval = 0.2*(endTime-startTime);
					currTimeInterval = timeInterval;
					intervalset++;
				}
			}
		}
		else if (channel == TChannel.SHUTTLE && event == TEvent.SHUTTLE_FINISHED_LOADING) {
			if ((Integer)args[0]== ID)
				sendingGlass = false;
		}
		else if (channel == TChannel.CONTROL_PANEL && event == TEvent.CONVEYOR_SPEED_CHANGE) {
			double newInterval = 1000/(Integer)args[0];
			currTimeInterval = (newInterval/defaultInterval)*timeInterval;
		}
	}


	public void sendGlass(MyGlass mg) {
		allowed = false;
		sendingGlass = true;
		System.out.println("CONVEYOR " + ID + ": " + "convetir sendglass action. sendOK = false");
		conveyorFamily.msgReleaseGlass(mg.glass);
		startConveyor();
		receiveOK = true;
		//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + sendOK);
		glassList.get(0).state = GlassState.RELEASING;


	}

	public void startConveyor() {
		conveyorMoving = true;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, args);
		//if (receiveOK == true)
		//	conveyorFamily.msgConveyorReady();	// if this is true, it should already know ready

	}

	public void stopConveyor() {
		conveyorMoving = false;
		Integer[] args = new Integer[1];
		args[0] = ID;
		t.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, args);

	}


	public boolean isMoving() {
		return conveyorMoving;
	}

	public void updateReadyStates() {
		receiveOK = false;
		if (sensorState[1] == false)	// there is room for the conveyor to move
			receiveOK = true;
		if (!glassList.isEmpty()) {
			if (glassList.get(0).state == GlassState.SENDING || sendOK == true || glassList.get(0).state == GlassState.TOSEND)	// conveyor is//will be in motion. there will be space
				receiveOK = true;
		}

		//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + receiveOK);

	}

}
