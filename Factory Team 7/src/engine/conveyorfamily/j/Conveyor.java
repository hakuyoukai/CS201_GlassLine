package engine.conveyorfamily.j;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.agent.Agent;
import engine.util.Glass;


//import interfaces.Transducer;

import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;


public class Conveyor extends Agent{
	public int ID;
	public List<MyGlass> glassList = new ArrayList<MyGlass>();
	public enum GlassState {LOADING,WAITINGTOSEND,TOSEND,SENDING,RELEASING}
	//{TOLOAD,LOADING,IDLE,TOSEND,WAITINGTOSEND,SENDING,RELEASING};
	public boolean sensorState[] = {false,false};
	public boolean receiveOK = false;
	public boolean sendOK = false;
	ConveyorFamilyJ conveyorFamily;
	public boolean conveyorMoving = true;
	boolean sendingGlass = false;
	Transducer t;
	public Glass incomingGlass;
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


	}


	public void setConveyorFamily(ConveyorFamilyJ cf) {
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
			System.out.println("CONVEYOR: " + "got glass conveyor");
			incomingGlass = g;
			//MyGlass mg = new MyGlass(g,GlassState.TOLOAD);			// receive on message reception? or when glass loaded
			//glassList.add(mg);
			//System.out.println("CONVEYOR: " + mg.state + " " + glassList.size());
			receiveOK = false;
			//conveyorFamily.display.parent.cPanel.testPanel.receiveLabel.setText("receive: " + receiveOK);
		}	
		//	stateChanged();
	}

	// popup answers conveyor's request to send part with affirmative

	public void msgIAmReady() {
		System.out.println("CONVEYOR: " + "next conveyor says he's ready to conveyor");

		sendOK = true;
		//conveyorFamily.display.parent.cPanel.testPanel.sendLabel.setText("send: " + sendOK);

		if (!glassList.isEmpty()) {
			System.out.println("CONVEYOR: " + "size: " + glassList.size() + " " + glassList.get(0).state);
			stateChanged();
		}	
	}


	public void sensorPressed(Integer sensorNum) {
		sensorState[sensorNum] = true;

		System.out.println("CONVEYOR: " + sensorNum + "pressed");
		if (sensorNum == 0) {
			startConveyor();
			if (incomingGlass != null) {
				MyGlass mg = new MyGlass(incomingGlass,GlassState.LOADING);
				glassList.add(mg);
				System.out.println("CONVEYOR: " + "glass added to list");
				incomingGlass = null;

				updateReadyStates();
				if (glassList.size() >0)
					glassList.get(glassList.size()-1).state = GlassState.WAITINGTOSEND;
				else
					System.out.println("CONVEYOR: " + "I didn't get glass");
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
			else //if (glassList.get(0).state == GlassState.TOSEND) {
			{
				glassList.get(0).state = GlassState.SENDING;
				stateChanged();
			}
		}
	}



	// first sensor released

	public void sensorReleased(Integer sensorNum) {
		System.out.println("CONVEYOR: " + sensorNum + "released");
		sensorState[sensorNum] = false;

		if (sensorNum == 0) {	// check ready conditions
			//	if (glassList.get(0).state != GlassState.SENDING) {
			updateReadyStates();
			System.out.println("CONVEYOR: " + "sensor released. send ok?: " + sendOK);

			if (sendOK == false) {
				stopConveyor();
			}	

			//	boolean oldreceive = receiveOK;
			updateReadyStates();
			//if (receiveOK && !oldreceive)
			if (receiveOK)
				conveyorFamily.msgConveyorReady();

		} 
		else if (sensorNum == 1) {
			if (glassList.get(0).state == GlassState.RELEASING) {
				glassList.remove(0);
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
			if (sendOK && glassList.get(0).state == GlassState.RELEASING) {
				resume();
			}

			if (glassList.get(0).state == GlassState.SENDING && sensorState[1] == true) {
				sendGlass(glassList.get(0));
				return true;
			}
			updateReadyStates();
			if (sendOK && (glassList.get(0).state == GlassState.WAITINGTOSEND || glassList.get(0).state == GlassState.TOSEND)) {
				//	System.out.println("CONVEYOR: " + glassList.get(0).state);
				if (sensorState[1] == false) {
					glassList.get(0).state = GlassState.TOSEND;
					if (conveyorMoving== false)
						startConveyor();
				}
				else if (sensorState[1] == true)
					glassList.get(0).state = GlassState.SENDING;
				stateChanged();
			}

			/*
			if (glassList.get(0).state == GlassState.TOSEND) {
				requestSend();
				return true;
			}
			 */
			/*	for (MyGlass mg: glassList) {
			if (mg.state == GlassState.TOLOAD) {
				System.out.println("CONVEYOR: " + "TO load pick");
				receiveGlass(mg);
				return true;
			}
		}
			 */

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
		}
		else if (channel == TChannel.SHUTTLE && event == TEvent.SHUTTLE_FINISHED_LOADING) {
			if ((Integer)args[0]== ID)
				sendingGlass = false;

		}
	}

	/*
	public void requestSend() {
	System.out.println("CONVEYOR: " + "conveyor request send");
	glassList.get(0).state = GlassState.WAITINGTOSEND;
	shuttle.msgIWantToSend();
	} */



	public void sendGlass(MyGlass mg) {
		sendingGlass = true;
		sendOK = false;
		System.out.println("CONVEYOR: " + "convetir sendglass action");
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
