package engine.conveyorfamily.d;

//import mock.*;
import transducer.TChannel;
import transducer.TEvent;
//import interfaces.*;
//import agent.*;
//import generalClasses.*;
import engine.agent.*;
import engine.util.*;

public class SensorAgent extends Agent implements Sensor{
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
	ConveyorFamilyD conveyorFamily;
	
	Transducer transducer;
	//TChannel channel;
	
	//EventLog log = new EventLog();
	//entry sensor
	//has a conveyer after it
	//setup is sensor -> conveyer
	public SensorAgent(Conveyor c, ConveyorFamilyD cf, Transducer tr){
		t = type.entry;
		conveyor = c;
		conveyorFamily = cf;
		holding = null;
		transducer = tr;
		transducer.startTransducer();
		cStatus = conveyorStatus.ready; //set by default
		transducer.register(this, TChannel.SENSOR);
		gStatus = glassStatus.none;
	}
	
	//prepopup sensor
	//has a conveyer before it, and a popup after it
	//setup is conveyer -> sensor -> popup
	public SensorAgent(Conveyor c, Popup p, ConveyorFamilyD cf, Transducer tr){
		t = type.pre_popup;
		popup = p;
		conveyorFamily = cf;
		pStatus = popupStatus.lowered; // set by default
		cfStatus = conveyorFamilyStatus.ready; //set by default
		holding = null;
		gStatus = glassStatus.none;
		conveyor = c;
		transducer = tr;
		transducer.startTransducer();
		transducer.register(this, TChannel.SENSOR);
	}
	
	//Messages
	
	//****
	//message if popup sensor
	//****
	public void msgHereIsGlass(Glass g){
		holding = g;
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
		if(holding == null){
			gStatus = glassStatus.none;
		}
		else{
			checkProcess();
		}
		if(gStatus != glassStatus.none){
			if(t == type.entry){
				if(cStatus == conveyorStatus.ready){
					giveConveyorPart();
					return true;
				}
			}
			if(t == type.pre_popup){
				if(gStatus == glassStatus.needs_processing){
					if(pStatus == popupStatus.lowered){
					
						givePopupPart();
						return true;
					}
				}
				else{
					if(cfStatus == conveyorFamilyStatus.ready)
						giveConveyorFamPart();
					}
			}
		}
		return false;
	}
	
	//Actions
	private void checkProcess(){
		if(t == type.entry){
			conveyorFamily.msgIAmNotReady();
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
		conveyor.msgHereIsGlass(holding);
		//log.add(new LoggedEvent("Sent message msgHereIsGlass to conveyor"));
		conveyorFamily.msgIAmReady();
		//log.add(new LoggedEvent("Sent message msgIAmReady to conveyorFamily"));		
		holding = null;
	}
	
	private void givePopupPart(){
		popup.msgHereIsGlass(holding);
		//log.add(new LoggedEvent("Sent message msgHereIsGlass to popup"));
		conveyor.msgPopupSensorIsReady();
		//log.add(new LoggedEvent("Sent message msgPopupSensorIsReady to conveyor"));
		holding = null;
	}
	
	private void giveConveyorFamPart(){
		conveyorFamily.msgHereIsGlass(holding);
		//log.add(new LoggedEvent("Sent message msgHereIsGlass to conveyor family"));
		conveyor.msgPopupSensorIsReady();
		//log.add(new LoggedEvent("Sent message msgPopupSensorIsReady to conveyor"));
		holding = null;
	}

	@Override
	public void eventFired(TChannel channel, TEvent event, Object[] args) {
		// TODO Auto-generated method stub
		if(event == TEvent.SENSOR_GUI_PRESSED){
			conveyor.msgPopupSensorIsNotReady();
		}
		if(event == TEvent.SENSOR_GUI_RELEASED){
			conveyor.msgPopupSensorIsReady();
		}
	}

}
