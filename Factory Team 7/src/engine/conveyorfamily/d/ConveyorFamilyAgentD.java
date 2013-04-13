package engine.conveyorfamily.d;

//import mock.EventLog;
//import mock.LoggedEvent;
import transducer.TChannel;
import transducer.TEvent;
//import agent.Agent;
//import generalClasses.Glass;
//import interfaces.ConveyorFamily;
//import interfaces.Transducer;
import engine.agent.*;
import engine.util.*;

public class ConveyorFamilyAgentD extends Agent implements ConveyorFamilyD{
	ConveyorAgent conveyor;
	SensorAgent entry, prepopup;
	PopupAgent popup;
	
	Transducer transducer;
	TChannel channel;
	
	ConveyorFamilyAgentD before, after;
	
	Glass glass;
	
	public enum cfState{ready, not_ready};
	public enum nextState{ready, not_ready};
	nextState nextConveyor;
	
	
	//public EventLog log = new EventLog();
	
	ConveyorFamilyAgentD(ConveyorFamilyAgentD before, ConveyorFamilyAgentD after, Transducer t){
		transducer = t;
		this.before = before;
		this.after = after;
		
		glass = null;
		nextConveyor = nextState.ready;
		
		entry = new SensorAgent(conveyor, before, transducer);
		prepopup = new SensorAgent(conveyor, popup, after, transducer);
		conveyor = new ConveyorAgent(entry, prepopup, transducer);
		popup = new PopupAgent(prepopup, after, transducer);
		
		popup.startThread();
		//log.add(new LoggedEvent("Popup started"));
		prepopup.startThread();
		//log.add(new LoggedEvent("Prepopup sensor started"));
		conveyor.startThread();
		//log.add(new LoggedEvent("Conveyor started"));
		entry.startThread();
		//log.add(new LoggedEvent("Entry Popup started"));
	}
	
	
	public void msgHereIsGlass(Glass g) {
		// TODO Auto-generated method stub
		glass = g;
		stateChanged();
	}

	public void msgIAmReady() {
		// TODO Auto-generated method stub
		nextConveyor = nextState.ready;
		stateChanged();
	}
	
	public void msgIAmNotReady(){
		nextConveyor = nextState.not_ready;
		stateChanged();
	}

	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		if(glass != null){
			entry.msgHereIsGlass(glass);
			glass = null;
			return true;
		}
		if(entry.holding != null){
			before.msgIAmNotReady();
		}
		else{
			before.msgIAmReady();
		}
		return false;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		
	}


	/*@Override
	public void msgHereIsGlass(engine.conveyorfamily.d.Glass g) {
		// TODO Auto-generated method stub
		
	}*/

}
