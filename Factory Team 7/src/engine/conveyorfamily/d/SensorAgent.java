package engine.conveyorfamily.d;

import transducer.*;
import engine.agent.*;
import engine.util.*;

public class SensorAgent extends Agent implements Sensor, TReceiver{
	//Data
	public enum type {entry, exit, pre_popup};
	public enum popupStatus {raised, lowered};
	public enum conveyorStatus {ready, not_ready};
	public enum conveyorFamilyStatus {ready, not_ready};
	public enum glassStatus {none, needs_processing, does_not_need_process};
	
	public type t;
	public popupStatus pStatus;
	public conveyorStatus cStatus;//, cNextStatus;
	public conveyorFamilyStatus cfStatus;
	public glassStatus gStatus;
	
	public Glass holding;
	Conveyor conveyor;
	Popup popup;
	ConveyorFamilyInterface conveyorFamily;
	Object[] arguments = new Object[1];
	
	int IDnumber;
	
	Transducer transducer;
	//TChannel channel;
	
	//EventLog log = new EventLog();
	//entry sensor
	//has a conveyer after it
	//setup is sensor -> conveyer
//	public SensorAgent(Conveyor c, ConveyorFamilyInterface cf, Transducer tr){
//		t = type.entry;
//		conveyor = c;
//		conveyorFamily = cf;
//		holding = null;
//		transducer = tr;
//		//transducer.startTransducer();
//		cStatus = conveyorStatus.ready; //set by default
//		transducer.register(this, TChannel.SENSOR);
//		gStatus = glassStatus.none;
//	}
	public SensorAgent(String e, int n, Transducer tr){
		arguments[0] = n;
		
		if(e == "entry"){
			t = type.entry;
		}
		else{
			t = type.pre_popup;
		}
		
		IDnumber = n;
		holding = null;
		gStatus = glassStatus.none;
		
		cStatus = conveyorStatus.ready;
		
		transducer = tr;
		transducer.register(this, TChannel.SENSOR);
		
	}
	
	public void setUpEntry(Conveyor c, ConveyorFamilyInterface cf){
		//previous cf
		conveyorFamily = cf;
		
		//next conveyor
		conveyor = c;
	}
	public void setUpPrePopup(Conveyor c, Popup p){
		//previous conveyor
		conveyor = c;
		
		//next popup
		popup = p;
	}
	
	
	//prepopup sensor
	//has a conveyer before it, and a popup after it
	//setup is conveyer -> sensor -> popup
//	public SensorAgent(Conveyor c, Popup p, ConveyorFamilyInterface cf, Transducer tr){
//		t = type.pre_popup;
//		popup = p;
//		conveyorFamily = cf;
//		pStatus = popupStatus.lowered; // set by default
//		cfStatus = conveyorFamilyStatus.ready; //set by default
//		holding = null;
//		gStatus = glassStatus.none;
//		conveyor = c;
//		transducer = tr;
//		transducer.register(this, TChannel.SENSOR);
//	}
//	
	//Messages
	
	//****
	//message if popup sensor
	//****
	public void msgHereIsGlass(Glass g){
		System.out.println("Conveyor5: just received glass");
		holding = g;
		//System.out.println("I get to line 108");
		stateChanged();
	}
	
	public void msgPopupReady(){
		//give part to popup
		pStatus = popupStatus.lowered;
		stateChanged();
	}
	
	public void msgPopupNotReady(){
		pStatus = popupStatus.raised;
		stateChanged();
	}
	
	public void msgConveyorFamilyReady(){
		//give part to conveyor
		//in setup... popup->conveyor
		//assumes that you will not go conveyor -> sensor -> conveyor
		cfStatus = conveyorFamilyStatus.ready;
		stateChanged();
	}
	
	public void msgConveyorFamilyNotReady(){
		cfStatus = conveyorFamilyStatus.not_ready;
		stateChanged();
	}
	
	//*****
	//messages if entry sensor
	//*****
	public void msgConveyorReady(){
		//give part to conveyor
		//in setup... popup->conveyor
		//assumes that you will not go conveyor -> sensor -> conveyor
		cStatus = conveyorStatus.ready;
		stateChanged();
	}
	
	public void msgConveyorNotReady(){
		cStatus = conveyorStatus.not_ready;
		stateChanged();
	}	
	
	
	//Scheduler
	@Override
	public boolean pickAndExecuteAnAction() {
		// TODO Auto-generated method stub
		/*if(holding == null){
			gStatus = glassStatus.none;
		}
		else{
			//checkProcess();
			//if(gStatus != glassStatus.none){
				if(t == type.entry){
					if(cStatus == conveyorStatus.ready){
						giveConveyorPart();
						System.out.println("This should give conveyor part");
						return true;
					}
				}
				if(t == type.pre_popup){
					if(gStatus == glassStatus.needs_processing || gStatus == glassStatus.does_not_need_process){
						if(pStatus == popupStatus.lowered){
						
							givePopupPart();
							return true;
						}
					}
					/*else{
						if(cfStatus == conveyorFamilyStatus.ready)
							//giveConveyorFamPart();
							givePopupPart();
					}*/
				//}
		//}
		
		//}
		return false;
	}
	
	//Actions
	private void checkProcess(){
		if(t == type.entry){
			//conveyorFamily.msgIAmNotReady();
			//log.add(new LoggedEvent("Sent message msgIAmNotReady to conveyorFamily"));
		}
		else{
			conveyor.msgPopupSensorIsNotReady();
			//log.add(new LoggedEvent("Sent message msgPopupSensorIsNotReady to conveyor"));
		}
		
		
		/**
		 * FIX THIS v
		 */
		
		if(holding.recipe.get(1) == true){//holding.needsProcess() == true){
			gStatus = glassStatus.needs_processing;
		}
		else
			gStatus = glassStatus.does_not_need_process;
	}
	
	//entry sensor
	//if conveyor is given part...
	//conveyor fam before it will be messaged
	private void giveConveyorPart(){
		System.out.println("... here conveyor should get part");
		conveyor.msgHereIsGlass(holding);
		System.out.println("Conveyor 5 just got part");
		//log.add(new LoggedEvent("Sent message msgHereIsGlass to conveyor"));
		conveyorFamily.msgIAmReady();
		//log.add(new LoggedEvent("Sent message msgIAmReady to conveyorFamily"));		
		holding = null;
	}
	
	private void givePopupPart(){
		popup.msgHereIsGlass(holding);
		//log.add(new LoggedEvent("Sent message msgHereIsGlass to popup"));
		//conveyor.msgPopupSensorIsReady();
		//log.add(new LoggedEvent("Sent message msgPopupSensorIsReady to conveyor"));
		holding = null;
	}
	
//	private void giveConveyorFamPart(){
//		conveyorFamily.msgHereIsGlass(holding);
//		//log.add(new LoggedEvent("Sent message msgHereIsGlass to conveyor family"));
//		conveyor.msgPopupSensorIsReady();
//		//log.add(new LoggedEvent("Sent message msgPopupSensorIsReady to conveyor"));
//		holding = null;
//	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) 
	{
		// TODO Auto-generated method stub
		if(channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_PRESSED){
			//conveyor.msgPopupSensorIsNotReady();
			System.out.println("SENSOR " + args[0]);
			if((Integer)args[0] == 10){//arguments[0]){
				System.out.println("Conveyor 5: sensor" + args[0] + " pressed");
				System.out.println("Conveyor5: " + conveyor.hashCode());
				if(t == type.entry){
					conveyor.msgHereIsGlass(holding);
					//arguments[0] = 5;
					Object[] arg = new Object[1];
					arg[0] = 5;
					transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, arg);
					System.err.println("conveyor.msgHereIsGlass");
				}
				else if (t == type.pre_popup){
					popup.msgHereIsGlass(holding);
					System.err.println("popup.msgHereIsGlass");
				}
			}
			
			//conveyor.msgHereIsGlass(holding);
		}
		if(channel == TChannel.SENSOR && event == TEvent.SENSOR_GUI_RELEASED){
			if(t == type.entry){
				if((Integer)args[0] == 10){
					conveyorFamily.msgIAmReady();
				}
			}
			
		}
	}
	
	@Override
	public String getName(){
		return "dexSensor";
	}

}
