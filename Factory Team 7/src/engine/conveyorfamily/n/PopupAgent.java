/*
 * Author: 		Nikhil Handyal
 * Date: 		4/1/13
 * Project:		CS201-Factory
 * Description:	
 */

package engine.conveyorfamily.n;

import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.Queue;
import engine.agent.Agent;
import engine.conveyorfamily.n.interfaces.*;
import engine.util.*;
import transducer.Transducer;
import transducer.TReceiver;
import transducer.TChannel;
import transducer.TEvent;

public class PopupAgent extends Agent implements TReceiver, Popup{
	Transducer t;
	Controller controller;
	Operator op1, op2;
	Integer[] popupIndex = new Integer[1];
	Integer cfIndex;
	Queue<Event> schedulerEvents = new LinkedList<Event>();
	Queue<Event> holdEvents = new LinkedList<Event>();
	Semaphore hold = new Semaphore(0, true);
	PopupState pState;
	private enum EventCode {
		GLASS_STAGED,
		POPUP_RAISED,
		OPERATOR_DONE,
		RELEASE_GLASS
	};
	
	public PopupAgent(Transducer t, Controller controller, TChannel channel, int cfIndex, int popupIndex){
		super("Nikhil's Popup Agent", t);
		this.t = t;
		this.controller = controller;
		this.cfIndex = cfIndex;
		this.popupIndex[0] = popupIndex;
		pState = new PopupState();
		op1 = new Operator(t, channel, this, 0);
		op2 = new Operator(t, channel, this, 1);
		t.register(this, TChannel.POPUP);
		this.startThread();
	}
	
	// ************************************************ //
    // ************* CLASS DECLARATIONS *************** //
    // ************************************************ //
	private class PopupState{
		public boolean staged = false;
		public boolean popupRaised = false;
		public boolean popupFree = true;
		public boolean op1Free = true;
		public boolean op2Free = true;
		public boolean op1Done = false;
		public boolean op2Done = false;
		public Glass pG = null;
		public Glass op1G = null;
		public Glass op2G = null;
		public boolean releaseGlass = false;
		
		public PopupState(){}
	}
	
	private class Event{
		Glass g;
		EventCode code;
		
		Event(Glass g, Operator op, EventCode code){
			this.g = g;
			this.code = code;
		}
	}
	
	// ************************************************ //
    // ***************** MESSAGES ********************* //
    // ************************************************ //	
	public void glassStaged(Glass g){
		schedulerEvents.offer(new Event(g, null, EventCode.GLASS_STAGED));
		pState.staged = true;
		stateChanged();
		System.out.println("PopupAgent: glass staged");
	}
	
	public void operatorLoadFinished(){
		System.out.println("PopupAgent: release operator load finished");
		popupRaised();
		hold.release();
	}
	
	public void operatorReleaseFinished(){
		System.out.println("PopupAgent: release opertor release finished");
		hold.release();
	}
	
	public void operatorDone(Operator op){
		schedulerEvents.offer(new Event(null, op, EventCode.OPERATOR_DONE));
		if(op == op1){
			pState.op1Done = true;
		}
		else{
			pState.op2Done = true;
		}
		
		stateChanged();
	}
	
	public void popupRaised(){
		schedulerEvents.offer(new Event(null, null, EventCode.POPUP_RAISED));
		stateChanged();
	}
	
	public void push(){
		// message from controller to release glass to next cFamily
		schedulerEvents.offer(new Event(null, null, EventCode.RELEASE_GLASS));
		pState.releaseGlass = true;
		stateChanged();	
	}
	// ************************************************ //
    // *************** MAIN SCHEDULER ***************** //
    // ************************************************ //
	
	@Override
	public boolean pickAndExecuteAnAction() {
		if(schedulerEvents.size() == 0){
			return false;
		}
		
		Event currentEvent = schedulerEvents.remove();
		if(currentEvent.code == EventCode.GLASS_STAGED){
			// check if the popup is down
			System.out.println("laskfjalsjfsa");
			if(pState.popupRaised){
				schedulerEvents.offer(currentEvent);
				stateChanged();
				return true;
			}
			// check if the popup is free
			else if(pState.popupFree){
				// check if the glass needs processing at this station
				Glass g = currentEvent.g;
				if((g.recipe.get(cfIndex)).booleanValue()){
					// glass needs processing, check if there is an opertor free
					if(pState.op1Free || pState.op2Free){
						// there is an operator to accept the glass, load the glass, raise popup
						pState.pG = g;
						load();								// load is a blocking function
						raisePopup();						// raise popup is blocking function
						return true;
					}
					else{
						// there is no space to process the glass right now
						schedulerEvents.offer(currentEvent);
						stateChanged();
						return true;
					}
				}
				else{
					// glass doesn't need processing, load through
					pState.pG = g;
					load();
					try {
						System.out.println("PopupAgent: hold popup load through "+hold.availablePermits());
						hold.acquire();						// wait till release glass message comes in from controller
						releaseToNextCF();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return true;
				}
			}
			else{
				// popup is down but it is occupied
				schedulerEvents.offer(currentEvent);
				stateChanged();
				return true;
			}
		}
		if(currentEvent.code == EventCode.POPUP_RAISED){
			// 3 cases
			// 1 --> offload the current glass
			// 2 --> load glass from operator
			// 3 --> lower to pick up next glass, glass has been staged
			if(!pState.popupFree){
				// case 1
				if(pState.op1Free)
					loadOperator(1);
				else
					loadOperator(2);
			}
			/*
			else{
				// the popup is free so it is either case 2 or 3
				if(pState.op1Done || pState.op2Done){
					// case 2
					/*
					if(pState.op1Done)
						releaseOperator(1);
					else
						releaseOperator(2);
					
					lowerPopup();
					return true;
				}
				else{
					// check if there is glass staged
					if(pState.staged){
						lowerPopup();
						return true;
					}
				}
			}
			*/
		}
		if(currentEvent.code == EventCode.OPERATOR_DONE){
			// 2 cases
			// 1 --> popup is down and must be raised
			// 2 --> popup is already raised
			System.out.println("PopupAgent: scheduler operator done");
			if(pState.popupRaised){
				// case 2
				System.out.println("PopupAgent: scheduer --> operator done popup raised");
				if(pState.op1Done)
					releaseOperator(1);
				else
					releaseOperator(2);
				
				try {
					System.out.println("PopupAgent: hold operator release "+hold.availablePermits());
					hold.acquire();						// block for operator to release glass
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				lowerPopup();
				return true;
			}
			else{
				// case 1
				raisePopup();
				return true;
			}
		}
		if(currentEvent.code == EventCode.RELEASE_GLASS){
			if(pState.releaseGlass){
				schedulerEvents.offer(new Event(null, null, EventCode.RELEASE_GLASS));
			}
			if(!pState.popupRaised){
				if(!pState.popupFree){
					// popup is not free aka there is glass on the popup
					releaseToNextCF();
					return true;
				}
				else{
					// there isn't currently any glass to offer
					//schedulerEvents.offer(currentEvent);
					//stateChanged();
					return true;
				}
			}
			else{
				// popup is not down right now
				//schedulerEvents.offer(currentEvent);
				//stateChanged();
				return true;
			}
		}
		return false;
	}
	// ************************************************ //
    // ****************** ACTIONS ********************* //
    // ************************************************ //
	public void load(){							// load onto popup
		pState.popupFree = false;
		controller.releaseToPopup();
		try {
			System.out.println("PopupAgent: hold load "+hold.availablePermits());
			hold.acquire();						// block for popup load animation to complete
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void raisePopup(){
		pState.popupRaised = true;
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, popupIndex);
		try {
			System.out.println("PopupAgent: hold raise popup "+hold.availablePermits());
			hold.acquire();						// block for animation to complete
			popupRaised();						// pass internal message notifying the animation is complete
			pState.popupRaised = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void lowerPopup(){
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, popupIndex);
		try{
			System.out.println("PopupAgent: hold lower popup "+hold.availablePermits());
			hold.acquire();						// block for animation to complete
			pState.popupRaised = false;
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public void loadOperator(int op){
		if(op == 1){
			op1.loadGlass();
			pState.op1Free = false;
			pState.op1G = pState.pG;
			pState.pG = null;
			pState.popupFree = true;
		}
		else{
			op2.loadGlass();
			pState.op2Free = false;
			pState.op2G = pState.pG;
			pState.pG = null;
			pState.popupFree = true;
		}
		try {
			System.out.println("PopupAgent: hold load operator "+hold.availablePermits());
			hold.acquire();						// wait for operator load animation to complete
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void releaseOperator(int op){
		if(op == 1){
			op1.releaseGlass();
			pState.popupFree = false;
			pState.pG = pState.op1G;
			pState.op1G = null;
			pState.op1Done = false;
		}
		else{
			op2.releaseGlass();
			pState.popupFree = false;
			pState.pG = pState.op2G;
			pState.op2G = null;
			pState.op2Done = false;
		}
		try{
			System.out.println("PopupAgent: hold release operator "+hold.availablePermits());
			hold.acquire();						// block till the relesae animation is complete
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	public void releaseToNextCF(){
		t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, popupIndex);
		controller.releasingGlass(pState.pG);
		pState.pG = null;
		pState.releaseGlass = false;
		try{
			System.out.println("PopupAgent: hold release to next cf "+hold.availablePermits());
			hold.acquire();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	
	// ************************************************ //
    // ******************* EXTRA ********************** //
    // ************************************************ //
	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		if( (Integer)args[0] == (Integer)popupIndex[0] ){
			if( event == TEvent.POPUP_GUI_LOAD_FINISHED){
				System.out.println("PopupAgent: release gui load finished");
				hold.release();
			}
			else if(event == TEvent.POPUP_GUI_MOVED_UP){
				System.out.println("PopupAgent: release gui moved up");
				hold.release();
			}
			else if(event == TEvent.POPUP_GUI_MOVED_DOWN){
				System.out.println("PopupAgent: release gui moved down");
				pState.popupRaised = false;
				hold.release();
			}
			else if(event == TEvent.POPUP_GUI_RELEASE_FINISHED){
				System.out.println("PopupAgent: release gui release finished");
				pState.popupFree = true;
				hold.release();
			}
		}
	}
}