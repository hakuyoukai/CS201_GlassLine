package engine.conveyorfamily.d;

import java.util.*;

import engine.agent.Agent;
import engine.util.*;


import transducer.*;

public class ConveyorAgent extends Agent implements Conveyor {
	//Data
	public ArrayList<Glass> glass;
	public enum conveyorStatus{ready, not_ready};
	public enum pSensorState{empty, holding};
	public int hi = 30;
	public pSensorState pStatus;
	public conveyorStatus cStatus;
	
	
	ConveyorFamilyInterface conveyorFamily;	
	Sensor enterSensor, prePopupSensor;
	
	Transducer transducer;
	TChannel channel;// = TChannel.CONVEYOR;
	
	Object[] arguments = new Object[1];

	
	//public EventLog log = new EventLog();
	
	public ConveyorAgent(Sensor enter, Sensor prepopup, Transducer t){//, ConveyorFamily CF){
		//initialize arraylist to hold glass
		glass = new ArrayList<Glass>();
		
		//set status
		cStatus = conveyorStatus.ready; //default initialization
		pStatus = pSensorState.empty; //default initialization
		
		//setup transducer
		channel = TChannel.CONVEYOR;
		transducer = t;
		//transducer.startTransducer();
		transducer.register(this, TChannel.CONVEYOR);
		
		
		//conveyorFamily = CF;
		enterSensor = enter;
		prePopupSensor = prepopup;
	}
	
	public ConveyorAgent(int n, Transducer t){
		//initialize arraylist to hold glass
		glass = new ArrayList<Glass>();
		
		//set status
		cStatus = conveyorStatus.ready; //default initialization
		pStatus = pSensorState.empty; //default initialization
		
		//setup transducer
		channel = TChannel.CONVEYOR;
		transducer = t;
		//transducer.startTransducer();
		transducer.register(this, TChannel.CONVEYOR);
		
		arguments[0] = n;
	}
	
	public void setUpConveyor(Sensor e, Sensor p){
		enterSensor = e;
		prePopupSensor = p;
	}
	
	//Messages
	public void msgHereIsGlass(Glass g){
		//action add part glass g... statechanged
		System.out.println("Glass g: " + g.hashCode());
		addGlass(g);
		stateChanged();
	}
	
	public void msgPopupSensorIsReady(){
		//action give to popupsensor
		pStatus = pSensorState.empty;
		stateChanged();
	}
	
	public void msgPopupSensorIsNotReady(){
		pStatus = pSensorState.holding;
		stateChanged();
	}
	
	//Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		if(glass.size() < 5){
			moveConveyor();
			if(pStatus == pSensorState.empty){
				givePartToPrePopupSensor();
				return true;
			}
			return true;
		}
		if (glass.size() >= 5){
			stopConveyor();
			if(pStatus == pSensorState.empty){
				givePartToPrePopupSensor();
				return true;
			}
			return true;
		}
		
		
		return false;
	}

	//Actions	
	private void addGlass(Glass g){
		glass.add(g);
	}
	
	private void moveConveyor(){
		cStatus = conveyorStatus.ready;
		enterSensor.msgConveyorReady();
		//log.add(new LoggedEvent("Sent mesage msgConveyorReady to enter sensor"));
	}
	
	private void stopConveyor(){
		cStatus = conveyorStatus.not_ready;
		enterSensor.msgConveyorNotReady();
		//log.add(new LoggedEvent("Sent message msgConveyorNotReady to enter sensor"));
		transducer.fireEvent(channel,  TEvent.CONVEYOR_DO_STOP, arguments);
	}
	
	private void givePartToPrePopupSensor(){
		if(glass.size() > 0){
			//assumes that all glass needs treatment
			prePopupSensor.msgHereIsGlass(glass.get(0));
			//log.add(new LoggedEvent("Sent message msgHereIsGlass to prePopupSensor"));
			glass.remove(0);
			if(glass.size()<5){
				cStatus = conveyorStatus.ready;
			}
		}
	}
	
	@Override
	public String getName(){
		return "DexConveyor";
	}


	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		
	}
	
}
