package engine.conveyorfamily.d;

import transducer.*;
import engine.agent.*;
import engine.util.*;

public class ConveyorFamilyAgentD extends Agent implements ConveyorFamilyInterface{
	ConveyorAgent conveyor;
	SensorAgent entry, prepopup;
	PopupAgent popup;
	
	Transducer transducer;
	TChannel channel;
	
	ConveyorFamilyInterface before, after;
	
	Glass glass;
	
	public enum cfState{ready, not_ready};
	public enum nextState{ready, not_ready};
	nextState nextConveyor;
	
	
	//public EventLog log = new EventLog();
	
	public ConveyorFamilyAgentD(/*ConveyorFamilyInterface before, ConveyorFamilyInterface after, */Transducer t, String WS){
		transducer = t;
		//this.before = before;
		//this.after = after;
		
		glass = null;
		nextConveyor = nextState.ready;
		
//		entry = new SensorAgent(conveyor, before, transducer);
//		prepopup = new SensorAgent(conveyor, popup, after, transducer);
//		conveyor = new ConveyorAgent(entry, prepopup, transducer);
//		popup = new PopupAgent(prepopup, after, transducer, WS);
		
		entry = new SensorAgent("entry", 10, transducer);
		prepopup = new SensorAgent("popup", 11, transducer);
		conveyor = new ConveyorAgent(5, transducer);
		popup = new PopupAgent(transducer, WS, 0);
		
	}
	
	public void startALLthreads(){
		entry.setUpEntry(conveyor, before);
		prepopup.setUpPrePopup(conveyor, popup);
		conveyor.setUpConveyor(entry, prepopup);
		popup.setUp(prepopup, after);
		
		this.startThread();
		popup.startThread();
		prepopup.startThread();
		conveyor.startThread();
		entry.startThread();
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
	
//	public void msgIAmNotReady(){
//		nextConveyor = nextState.not_ready;
//		stateChanged();
//	}

	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		if(glass != null){
			entry.msgHereIsGlass(glass);
			glass = null;
			return true;
		}
		if(entry.holding == null){
			before.msgIAmReady();
		}
		return false;
	}
	
	public void setNeighbors(ConveyorFamilyInterface before, ConveyorFamilyInterface after){
		this.before = before;
		this.after = after;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		
	}


	/*@Override
	public void msgHereIsGlass(engine.conveyorfamily.d.Glass g) {
		// TODO Auto-generated method stub
		
	}*/
	
	@Override
	public String getName(){
		return "DexConveyorFam";
	}

}
