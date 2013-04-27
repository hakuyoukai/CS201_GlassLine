/*
 * Author: 		Nikhil Handyal
 * Date: 		4/1/13
 * Project:		CS201-Factory
 * Description:	Main agent controller for conveyer family grouping
 */

package engine.conveyorfamily.n;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import engine.agent.Agent;
import engine.conveyorfamily.n.interfaces.*;
import transducer.Transducer;
import transducer.TEvent;
import transducer.TChannel;
import engine.util.*; 

public class ControllerAgent extends Agent implements ConveyorFamilyInterface, Controller{
	// Class data
	Transducer t;
	ConveyerState cState;
	ConveyorFamilyInterface source, destination;
	Conveyor conveyor;
	Popup popup;
	int ei, exi, cfIndex, popupIndex;
	Semaphore hold = new Semaphore(0, true);
	LinkedBlockingQueue<Glass> conveyerQueue = new LinkedBlockingQueue<Glass>(3);
	Queue<Event> controllerEvents = new LinkedList<Event>();
	
	private enum EventCode {
		ENTRY_PRESSED,
		ENTRY_RELEASED,
		EXIT_PRESSED,
		EXIT_RELEASED,
		RELEASE_GLASS
	}; 
	
	
	// Constructor
	public ControllerAgent(Transducer t, int cfIndex, TChannel channel){
		super("Nikhil's Agent", t);				// call Agent base class constructor
		this.t = t;								// set the transducer
		this.cfIndex = cfIndex;
		ei = cfIndex*2;
		exi = ei+1;
		new Sensor(t, this, ei);
		new Sensor(t, this, exi);
		popupIndex = cfIndex - 5;
		conveyor = new Conveyor(t, this, cfIndex);
		popup = new PopupAgent(t, this, channel, cfIndex, popupIndex);
		cState = new ConveyerState();
		this.startThread();
		// debug statements
	}
	
	// ************************************************ //
    // ************* CLASS DECLARATIONS *************** //
    // ************************************************ //
	
	private class ConveyerState{
		public boolean exitPressed = false;
		public boolean loadingPopup = false;
		
		public ConveyerState(){}
	}
	
	private class Event{
		EventCode code;
		
		Event(Glass g, EventCode code){
			this.code = code;
		}
	}
	
	// ************************************************ //
    // ***************** MESSAGES ********************* //
    // ************************************************ //
	@Override
	public void msgHereIsGlass(Glass g){
		// message from source pushing glass
		// blocks till there is space to accept glass within the queue
		// this should never be the case but is a fail-safe for corner cases
		System.out.println("Controller Agent: Received Glass");
		try {
			conveyerQueue.put(g); 				// ----> Might need to start conveyer at this point
			stateChanged();
		} catch (InterruptedException e){}
	}

	@Override
	public void msgIAmReady() {
		// message from destination saying they're ready to accept glass
		controllerEvents.offer(new Event(null, EventCode.RELEASE_GLASS));
		stateChanged();
	}
	
	// ------------------ INTERNAL COMMUNICATIONS ---------------- //
	// ------------------- SENSOR COMMUNICATIONS ----------------- //
	public void sensorPressed(int si){
		System.out.println("Controller Agent: "+"sp "+si);
		if(si == ei){
			controllerEvents.offer(new Event(null, EventCode.ENTRY_PRESSED));
		}
		else{
			controllerEvents.offer(new Event(null, EventCode.EXIT_PRESSED));
			cState.exitPressed = true;
		}
		stateChanged();
	}
	
	public void sensorReleased(int si){
		if(si == ei){
			controllerEvents.offer(new Event(null, EventCode.ENTRY_RELEASED));
		}
		else{
			controllerEvents.offer(new Event(null, EventCode.EXIT_RELEASED));
			cState.exitPressed = false;
		}
		stateChanged();
	}
	
	// ------------------- POPUP COMMUNICATIONS ----------------- //
	public void releaseToPopup(){
		hold.release();
	}
	
	public void releasingGlass(Glass g){
		destination.msgHereIsGlass(g);
	}
	
	// ************************************************ //
    // ***************** SCHEDULER ******************** //
    // ************************************************ //
	@Override
	public boolean pickAndExecuteAnAction() {
		System.out.print("ControllerAgent scheduler: ");
		if(controllerEvents.size() == 0){
			System.out.println("nothing to do");
			return false;
		}
		else{
			System.out.println("something to do");
		}
		
		Event currentEvent = controllerEvents.remove();
		if(currentEvent.code == EventCode.ENTRY_PRESSED){
			if(!cState.exitPressed)
				startConveyor();
			else{
				controllerEvents.offer(currentEvent);
				stateChanged();
			}
			return true;
		}
		if(currentEvent.code == EventCode.ENTRY_RELEASED){
			if(!cState.exitPressed){ 
				// if there isn't anything on the exit sensor right now, we can accept a new glass
				acceptNewGlass();
			}
			else{
				controllerEvents.offer(currentEvent);
				stateChanged();
			}
			return true;
		}
		if(currentEvent.code == EventCode.EXIT_PRESSED){
			holdForPopup();
			return true;
		}
		if(currentEvent.code == EventCode.EXIT_RELEASED){
			if(conveyerQueue.size() == 0)
				stopConveyor();
			return true;
		}
		if(currentEvent.code == EventCode.RELEASE_GLASS){
			// notify the popup to release the next sheet to the destination
			release();
		}
					
		
		return false;
	}
	// ************************************************ //
    // ****************** ACTIONS ********************* //
    // ************************************************ //
	public void holdForPopup(){
		// stop the conveyer and wait till the popup can accept glass
		conveyor.stopConveyor();
		try {
			popup.glassStaged(conveyerQueue.remove());
			hold.acquire();
			conveyor.startConveyor();					// this will effectively load the glass on the popup
			hold.acquire();								// block till the popup load animation completes
		}catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void startConveyor(){
		conveyor.startConveyor();
	}
	
	public void stopConveyor(){
		System.out.println("Controller Agent: Conveyer Stopped");
		conveyor.stopConveyor();
	}
	
	public void acceptNewGlass(){
		source.msgIAmReady();
	}
	
	public void release(){
		// this is only informing the popup to release, it does not guarantee that the popup
		// actually releaed the glass to the next CF
		popup.push();
	}
	// ************************************************ //
    // ******************* EXTRA ********************** //
    // ************************************************ //
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if( (Integer)args[0] == (Integer)popupIndex ){
			if(event == TEvent.POPUP_GUI_LOAD_FINISHED){
				hold.release();
			}
		}
	}
	
	@Override
	public void bindNeighbors(ConveyorFamilyInterface source, ConveyorFamilyInterface destination){
		this.source = source;
		this.destination = destination;
	}
	
	public void init(){
		source.msgIAmReady();
	}

	@Override
	public void msgDeleteGlass(Glass g)
	{
		// TODO Auto-generated method stub
		
	}
}