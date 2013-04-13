package engine.conveyorfamily.d;

//import mock.*;
import transducer.TChannel;
import transducer.TEvent;

//import interfaces.*;
//import agent.*;
//import generalClasses.*;
import engine.agent.*;
import engine.util.*;


public class PopupAgent extends Agent implements Popup{
	//Data
	public enum popupStatus {raised, lowered};
	public enum glassStatus {no_glass, waiting, working, done};
	public enum nextConveyorStatus {ready, not_ready};
	
	public popupStatus pStatus;
	public glassStatus gStatus;
	public nextConveyorStatus cStatus;
	
	public Glass workingOn;
	Sensor preSensor;
	ConveyorFamilyD nextConveyor;//next conveyor family
	
	Transducer transducer;
	TChannel channel;
	//public EventLog log = new EventLog();

	//goes from sensor -> popup -> sensor
	public PopupAgent(Sensor p, ConveyorFamilyD c, Transducer t){
		preSensor = p;
		pStatus = popupStatus.lowered; //initialize popup as lowered
		workingOn = null;
		gStatus = glassStatus.no_glass;
		nextConveyor = c;
		cStatus = nextConveyorStatus.ready;
		
		//setup transducer
		transducer = t;
		channel = TChannel.POPUP;
		transducer.register(this, channel);
		transducer.startTransducer();
	}
	
	//Messages
	public void msgHereIsGlass(Glass g) {
		raisePopup(g);	
		stateChanged();
	}
	
	public void msgNextConveyorReady(){
		cStatus = cStatus.ready;
		stateChanged();
	}
	
	public void msgNextConveyorNotReady(){
		cStatus = cStatus.not_ready;
		stateChanged();
	}
	
	//Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		if(pStatus == popupStatus.raised){
			lockPopup();
			if(gStatus == glassStatus.waiting){
				process();
				return true;
			}
			if(gStatus == glassStatus.done){
				if(cStatus == cStatus.ready){
					partExit();
					return true;
				}
			}
		}
		else{
			preSensor.msgPopupReady();
			//log.add(new LoggedEvent("Sent msgPopupReady to preSensor"));
		}
		return false;
	}
	
	//actions
	private void lockPopup(){
		preSensor.msgPopupNotReady();
		//log.add(new LoggedEvent("Sent msgPopupNotReady to preSensor"));
	}
	
	private void raisePopup(Glass g){
		pStatus = popupStatus.raised;
		gStatus = glassStatus.waiting;
		workingOn = g;
		transducer.fireEvent(channel, TEvent.POPUP_DO_MOVE_UP, new Object[0]);
		//log.add(new LoggedEvent("Sent POPUP_DO_MOVE_up to transducer"));
		stateChanged();
	}
	
	private void process(){
		gStatus = glassStatus.working;
		gStatus = glassStatus.done;
		stateChanged();
	}
	
	private void partExit(){
		pStatus = popupStatus.lowered;
		transducer.fireEvent(channel, TEvent.POPUP_DO_MOVE_DOWN, new Object[0]);
		//log.add(new LoggedEvent("Sent POPUP_DO_MOVE_DOWN to transducer"));
		nextConveyor.msgHereIsGlass(workingOn);
		//log.add(new LoggedEvent("Sent msgHereIsGlass to conveyorFamily"));
		workingOn = null;
		gStatus = glassStatus.no_glass;
		stateChanged();
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		
	}
}
