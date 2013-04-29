package engine.conveyorfamily.offline6;

import java.util.ArrayList;

import transducer.TChannel;
import transducer.TEvent;
import transducer.TReceiver;
import transducer.Transducer;
import engine.agent.Agent;
import engine.util.*;

public class ConveyorAgent extends Agent implements TReceiver{
	private Transducer transducer;
	private Integer number;
	private PopupAgent next;
	private ConveyorFamilyInterface prev;
	private Glass incomingGlass;
	private ArrayList<Glass> glass;
	private Integer capacity = 3;
	private Integer[] conArgs;
	private boolean conveyorJammed = false;
	
	private enum SendState {DEFAULT, APPROVED, WAITING, ASKED}			// --> waiting means it is actively waiting for the gui load animation to complete
	private enum SensorState {ON,OFF}
	
	private SensorState sensorOne;
	private SensorState sensorTwo;
	private SendState nextState;
	private SendState prevState;
	
	
	
	// ************************************************ //
    // ************* CLASS DECLARATIONS *************** //
    // ************************************************ //
	// by default the dest is ready to accept glass and the source is ready to give glass
	public ConveyorAgent(Transducer t, Integer num){
		transducer = t;
		transducer.register(this, TChannel.SENSOR);
		number = num;
		
		sensorOne = SensorState.OFF;
		sensorTwo = SensorState.OFF;
		nextState = SendState.APPROVED;
		prevState = SendState.DEFAULT;
		
		glass = new ArrayList<Glass>();
		conArgs = new Integer[1];
		conArgs[0] = number;
		
		printState("constructor");
	}

	// ************************************************ //
    // ***************** MESSAGES ********************* //
    // ************************************************ //
	public void msgHereIsGlass(Glass g){			// msg from source giving you glass
		incomingGlass = g;
		prevState = SendState.WAITING;
		stateChanged();
		printState("msgHereIsGlass");
	}
	
	public void msgGiveMeGlass(){					// msg from destination asking for glass --> coming from popup
		nextState = SendState.APPROVED;
		stateChanged();
		printState("msgGiveMeGlass");
	}
	
	public void setJamState(boolean jammed){		// msg from gui controls jamming conveyor
		conveyorJammed = jammed;
		if( jammed )
			stopConveyor();
		else
			stateChanged();
	}

	// ************************************************ //
    // ***************** SCHEDULER ******************** //
    // ************************************************ //
	@Override
	public boolean pickAndExecuteAnAction(){
		printState("Scheduler");
		
		if(conveyorJammed){
			return false;	// prevent agent exeution if the conveyor is jammed
		}
		
		if(!glass.isEmpty()){
			if(nextState == SendState.APPROVED){
				sendNext();
				return true;
			}
			else if(nextState == SendState.DEFAULT){
				askNext();
				return true;
			}
		}
		if(nextState == SendState.WAITING){
			startConveyor();
		}
		if(prevState == SendState.DEFAULT){
			if(glass.size()<capacity){
				if(sensorOne == SensorState.OFF){
					System.err.println("Ask for glass");
					askForGlass();
					return true;
				}
			}
		}
		return false;
	}
	
	// ************************************************ //
    // ****************** ACTIONS ********************* //
    // ************************************************ //
	public void sendNext(){
		next.msgHereIsGlass(this,glass.remove(0));
		if(sensorTwo == SensorState.ON)
			nextState = SendState.DEFAULT;
		else
			nextState = SendState.WAITING;
		
		System.err.println("starting conveyor send next");
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
		stateChanged();
		printState("send next");
	}
	
	public void startConveyor(){
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
	}
	
	public void stopConveyor(){
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conArgs);
	}
	
	public void askNext(){
		nextState = SendState.ASKED;
		next.msgCanIGive();
		stateChanged();
		printState("ask next");
	}
	
	public void askForGlass(){
		prevState = SendState.APPROVED;
		prev.msgIAmReady();
		stateChanged();
		printState("ask for glass");
	}

	//!!SCHEDULER!! --> No actions are called here
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args){
		// if the conveyor is jammed we still want to log the gui state but we need to prevent action execution
		if(channel == TChannel.SENSOR){
			if(event == TEvent.SENSOR_GUI_PRESSED){
				if((Integer)args[0] == (2*number)){
					sensorOne = SensorState.ON;
					if(prevState == SendState.WAITING){
						System.err.println("loading glass from previous conveyer");
						glass.add(incomingGlass);
						incomingGlass = null;
						prevState = SendState.DEFAULT;
						printState("s1");
					}
					else{
						System.err.println("entry pressed but send state was not expecting it");
					}
					stateChanged();
				}
				if((Integer)args[0] == ((2*number)+1)){
					System.err.println("REACHED SECOND SENSOR");
					System.err.println(nextState);
					sensorTwo = SensorState.ON;
					if(nextState == SendState.WAITING){
						nextState = SendState.DEFAULT;
						printState("s2");
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conArgs);
					}
					else{
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conArgs);
					}
					stateChanged();
				}
			}
			if(event == TEvent.SENSOR_GUI_RELEASED){
				if((Integer)args[0] == (2*number)){
					sensorOne = SensorState.OFF;
					stateChanged();
				}
				if((Integer)args[0] == ((2*number)+1)){
					sensorTwo = SensorState.OFF;
					stateChanged();
				}
			}
		}
	}
	
	// ************************************************ //
    // ***************** UTILITIES ******************** //
    // ************************************************ //
	public void setNext(PopupAgent p){
		next = p;
	}
	
	public void setPrevious(ConveyorFamilyInterface cF){
		prev = cF;
	}
	
	public String getName(){
		return "Conveyor " + number;
	}
	
	public void printState(String msg){
		System.out.println("-------CF 6---------");
		System.out.println(msg);
		System.out.println(conveyorJammed);
		System.out.println("nextState: "+nextState);
		System.out.println("prevState: "+prevState);
		System.out.println("entrySensor: "+sensorOne);
		System.out.println("exitSensor: "+sensorTwo);
		System.out.println("--------------------");
	}
}