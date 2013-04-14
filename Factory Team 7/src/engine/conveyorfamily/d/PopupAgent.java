package engine.conveyorfamily.d;

//import mock.*;
import transducer.*;
import engine.agent.*;
import engine.util.*;


public class PopupAgent extends Agent implements Popup{
	//Data
	public enum popupStatus {raised, lowered};
	public enum glassStatus {no_glass, waiting, working, done};
	public enum nextConveyorStatus {ready, not_ready};
	public enum conveyorType {DRILL, CROSS_SEAMER, GRINDER};
	
	public conveyorType cType;
	public popupStatus pStatus;
	public glassStatus gStatus;
	public nextConveyorStatus cStatus;
	public glassStatus workOneStatus;
	public glassStatus workTwoStatus;
	
	String workstation;
	
	public Glass workingOn;
	Sensor preSensor;
	ConveyorFamilyInterface nextConveyor;//next conveyor family
	
	Transducer transducer;
	TChannel channel;
	//public EventLog log = new EventLog();
	
	Object[] arguments = new Object[1];
	
	public PopupAgent(Transducer t, String ws, int n){
		arguments[0] = n;
		//preSensor = p;
		pStatus = popupStatus.lowered; //initialize popup as lowered
		workingOn = null;
		gStatus = glassStatus.no_glass;
		//nextConveyor = c;
		cStatus = nextConveyorStatus.ready;
		workOneStatus = glassStatus.no_glass;
		workTwoStatus = glassStatus.no_glass;
		
		workstation = ws;
		if(ws == "DRILL")
			cType = conveyorType.DRILL;
		else if (ws == "CROSS_SEAMER")
			cType = conveyorType.CROSS_SEAMER;
		else if (ws == "GRINDER")
			cType = conveyorType.GRINDER;
		
		//setup transducer
		transducer = t;
		channel = TChannel.POPUP;
		transducer.register(this, TChannel.POPUP);
		transducer.register(this, TChannel.CROSS_SEAMER);
		transducer.register(this, TChannel.DRILL);
		transducer.register(this, TChannel.GRINDER);
	}
	
	public void setUp(Sensor p, ConveyorFamilyInterface c){
		preSensor = p;
		nextConveyor = c;
	}
	
	//Messages
	public void msgHereIsGlass(Glass g) {
		System.out.println("Hi");
		workingOn = g;
		//raisePopup();	
		//stateChanged();
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
		if(gStatus == glassStatus.done){
			if(cStatus == cStatus.ready){
				Integer ar[] = new Integer[1];
				ar[0] = 0;
				transducer.fireEvent(channel,  TEvent.POPUP_RELEASE_GLASS, ar);
				System.out.println("popup_release_glass");
				gStatus = glassStatus.no_glass;
				return true;
			}
		}
		return false;
	}
		
	
	//actions
	private void raisePopup(){
		pStatus = popupStatus.raised;
		gStatus = glassStatus.waiting;
		System.err.println("I'M GONNNA RAISE ZE ROOF");
		
		transducer.fireEvent(channel, TEvent.POPUP_DO_MOVE_UP, arguments);
	}
	
	private void lowerPopup()
	{
		pStatus = popupStatus.lowered;
		gStatus = glassStatus.waiting;
		transducer.fireEvent(channel, TEvent.POPUP_DO_MOVE_DOWN,arguments);
	}
	

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		System.err.println(event + " EVENT LOG");
		if(channel == TChannel.POPUP && event == TEvent.POPUP_GUI_LOAD_FINISHED){
			if(workstation == "CROSS_SEAMER"){// && workingOn.recipe.get(5) == true){
				raisePopup();
			}
			else if(workstation == "DRILL"){
				if(workOneStatus == glassStatus.waiting)
				{
					lowerPopup();
					workOneStatus = glassStatus.no_glass;
				}
				else
				{
					raisePopup();
				}
			}
			else if(workstation == "GRINDER"){
				raisePopup();
				
			}
			else{
				gStatus = glassStatus.done;
				stateChanged();
			}
			
		}
		else if(channel == TChannel.POPUP && event == TEvent.POPUP_GUI_RELEASE_FINISHED){
			nextConveyor.msgHereIsGlass(workingOn);
			workingOn = null;
			gStatus = glassStatus.no_glass;
			workOneStatus = glassStatus.no_glass;
			System.out.println("Finished with part");
		}
		else if(channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_UP){
			System.out.println("popup raised");
			if(workstation == "DRILL"){
				Integer ar[] = new Integer[1];
				ar[0] = 0;
				transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_LOAD_GLASS, ar);
				System.out.println("doloadglass");
			}
			else if(workstation == "CROSS_SEAMER"){
				transducer.fireEvent(TChannel.CROSS_SEAMER, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
			}
			else if(workstation == "GRINDER"){
				transducer.fireEvent(TChannel.GRINDER, TEvent.WORKSTATION_DO_LOAD_GLASS, args);
			}
		}
		else if(channel == TChannel.CROSS_SEAMER && event == TEvent.WORKSTATION_LOAD_FINISHED){
			transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, args);
		}
		//daf
		else if(channel == TChannel.DRILL && event == TEvent.WORKSTATION_LOAD_FINISHED){
			Integer ar[] = new Integer[1];
			ar[0] = 0;
			System.err.println("load finished");
			workOneStatus = glassStatus.working;
			transducer.fireEvent(TChannel.DRILL, TEvent.WORKSTATION_DO_ACTION, ar);
		}
		else if(channel == TChannel.GRINDER && event == TEvent.WORKSTATION_LOAD_FINISHED){
			transducer.fireEvent(channel, TEvent.WORKSTATION_DO_ACTION, args);
		}
	
		else if(channel == TChannel.CROSS_SEAMER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
			Integer ar[] = new Integer[1];
			ar[0] = 0;
			transducer.fireEvent(channel,  TEvent.WORKSTATION_RELEASE_GLASS, ar);
			
		}
		else if(channel == TChannel.DRILL && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
			Integer ar[] = new Integer[1];
			ar[0] = 0;
			System.out.println("workstation Action finished");
			workOneStatus = glassStatus.done;
			transducer.fireEvent(TChannel.DRILL,  TEvent.WORKSTATION_RELEASE_GLASS, ar);
		}
		else if(channel == TChannel.GRINDER && event == TEvent.WORKSTATION_GUI_ACTION_FINISHED){
			transducer.fireEvent(channel,  TEvent.WORKSTATION_RELEASE_GLASS, args);
		}
		
		else if(channel == TChannel.CROSS_SEAMER && event == TEvent.WORKSTATION_RELEASE_FINISHED){
			//transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
		}
		else if(channel == TChannel.DRILL && event == TEvent.WORKSTATION_RELEASE_FINISHED){
			Integer ar[] = new Integer[1];
			ar[0] = 0;
			workOneStatus = glassStatus.waiting;
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, ar);
			
		}
		else if(channel == TChannel.GRINDER && event == TEvent.WORKSTATION_RELEASE_FINISHED){
			transducer.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, new Object[0]);
		}
		
		else if(channel == TChannel.POPUP && event == TEvent.POPUP_GUI_MOVED_DOWN){
			//transduer.fireEvent(channel, TEvent.POPUP_)
			gStatus = glassStatus.done;
			System.out.println("Glass status = done");
			
			stateChanged();
		}
		
	}
	
	@Override
	public String getName(){
		return "dexPopup";
	}
}
