package engine.conveyorfamily.online;

import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class ConveyorAgent extends Agent
{
	public List<MyGlass> glasses= Collections.synchronizedList(new ArrayList<MyGlass>());
	public enum MyGlassState{STOPLEFT,STOPRIGHT,MOVING,PROCESSING};

	public enum ConveyorState{MOVING,STATIC,MOVING_TO_STOP};
	public ConveyorState conveyorState=ConveyorState.STATIC;

	public enum SensorAfterState{PRESSED,RELEASED,EMPTY};
	public SensorAfterState sensorAfterState=SensorAfterState.RELEASED;
	
	public enum NextCFState{AVAILABLE,UNAVAILABLE};
	public NextCFState nextCFState=NextCFState.AVAILABLE;
	
	public enum MachineState{AVAILABLE,LOADING,LOADED,DOING_ACTION,DONE,RELEASING,RELEASE_FINISHED};
	public MachineState machineState=MachineState.AVAILABLE;

	ConveyorFamilyInterface conveyorAfter;
	int conveyorIndex;
	
	public TChannel myChannel;
	
	public class MyGlass
	{
		public Glass glass;
		public MyGlassState state;
		
		public MyGlass(Glass glass,MyGlassState state)
		{
			this.glass=glass;
			this.state=state;
		}
	}
	
	public ConveyorAgent(ConveyorFamilyInterface conveyorAfter,Transducer t, int index, TChannel myChannel)
	{
		super("Conveyor Belt " + index);
		this.transducer=t;
		this.transducer.register(this, myChannel);
		this.conveyorIndex=index;
		this.conveyorAfter=conveyorAfter;
		this.myChannel=myChannel;
	}
	
	public void setNextCF(ConveyorFamilyInterface conveyorAfter)
	{
		this.conveyorAfter=conveyorAfter;
	}
	
	public void msgPreviousCFGaveGlass(Glass g)
	{
		glasses.add(new MyGlass(g,MyGlassState.STOPLEFT));
		stateChanged();
	}

	public void msgSensorAfterReleased()
	{
		sensorAfterState=SensorAfterState.RELEASED;
		stateChanged();
	}

	public void msgGlassArrived()
	{
		//System.out.println("Conveyor Index "+conveyorIndex+":Glass arrived");
		for(MyGlass mg : glasses)
		{
			if(mg.state==MyGlassState.MOVING)
			{
				mg.state=MyGlassState.STOPRIGHT;
				break;
			}
		}
		sensorAfterState=SensorAfterState.PRESSED;
		conveyorState=ConveyorState.MOVING_TO_STOP;
		stateChanged();
	}
	
	public void msgNextCFRready()
	{
		//System.out.println("Next CF, index:"+(conveyorIndex+1)+" says it is ready");
		nextCFState=NextCFState.AVAILABLE;
//		for(MyGlass mg : glasses)
//		{
//			System.out.println("Glass State:" + mg.state);
//		}
//		System.out.println();
//		System.out.println("NextCFState:"+nextCFState);
//		System.out.println("MachineState:"+machineState);
		stateChanged();
	}

	//Actions
	public void giveGlassToMachine(MyGlass g)
	{
		if(conveyorIndex==2)
			System.err.println("giveGlassToNextCF is called");
		machineState=MachineState.LOADING;
		g.state=MyGlassState.PROCESSING;
		Object[] args=new Object[1];
		args[0]=new Integer(conveyorIndex);
		conveyorAfter.msgHereIsGlass(g.glass);
		nextCFState = NextCFState.UNAVAILABLE;
		transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args);
		stateChanged();
	}

	public void tellGUIConveyorStartMoving(MyGlass mg)
	{
		Object[] args=new Object[1];
		args[0]=new Integer(conveyorIndex);
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,args);
		conveyorState=ConveyorState.MOVING;
		mg.state=MyGlassState.MOVING;
		stateChanged();
	}

	public void tellGUIConveyorStopMoving()
	{
		Object[] args=new Object[1];
		args[0]=new Integer(conveyorIndex);
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP,args);
		conveyorState=ConveyorState.STATIC;
		//System.err.println("The Conveyor is stopped");
		stateChanged();
	}
	
	//Scheduler
	public boolean pickAndExecuteAnAction()
	{		
		if(conveyorIndex==2)
		{
			System.err.println("MachineState:"+machineState);
			System.err.println("glasses.size():"+glasses.size());
			for(MyGlass mg : glasses)
			{
				System.err.println(mg.state);
			}
			System.err.println(" ");
		}
		
		
		if(machineState == MachineState.LOADED){
			
//			if(conveyorIndex==13)
//				System.err.println("TEvent.WORKSTATION_LOAD_FINISHED");
			
			synchronized(glasses)
			{
				for(MyGlass mg : glasses)
				{
					//System.err.println(mg.state);
					
					if(mg.state == MyGlassState.PROCESSING)
					{
						if(mg.glass.recipe.get(conveyorIndex))
						{
							Object[] conveyorNum=new Object[1];
							conveyorNum[0]=new Integer(conveyorIndex);
							transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_DO_ACTION, conveyorNum);
							machineState = MachineState.DOING_ACTION;
							transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum);
							break;
						}
						else
						{
							Object[] conveyorNum=new Object[1];
							machineState = MachineState.RELEASING;
							conveyorNum[0]=new Integer(conveyorIndex);
							transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
							break;
						}
					}
				}
			}
		}
		if(machineState == MachineState.DONE){
			synchronized(glasses)
			{
				for(MyGlass mg: glasses)
				{
					if(mg.state == MyGlassState.PROCESSING)
					{			
						//System.err.println("TEvent.WORKSTATION_GUI_ACTION_FINISHED - inside PROCESSING");
						Object[] conveyorNum=new Object[1];
						conveyorNum[0]=new Integer(conveyorIndex);
						transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
						break;
					}
				}
			}
		}
		
		if(machineState ==MachineState.RELEASE_FINISHED)
		{
			synchronized(glasses)
			{
				for(MyGlass mg: glasses)
				{
					if(mg.state == MyGlassState.PROCESSING)
					{
						if(conveyorIndex==13)
							System.err.println("TEvent.WORKSTATION_RELEASE_FINISHED - delete");
						glasses.remove(mg);
						machineState=MachineState.AVAILABLE;
						Object[] conveyorNum=new Object[1];
						conveyorNum[0]=new Integer(conveyorIndex);
						transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum);
						break;
					}
				}
			}
		}
		
		if(conveyorState==ConveyorState.MOVING_TO_STOP&&(machineState!=MachineState.AVAILABLE&&machineState!=MachineState.LOADING))
		{
			tellGUIConveyorStopMoving();
			return true;
		}
		
		MyGlass temp=null;
		synchronized(glasses)
		{
			for(MyGlass mg : glasses)
			{
				if(mg.state == MyGlassState.STOPRIGHT && nextCFState==NextCFState.AVAILABLE && machineState==MachineState.AVAILABLE)
				{
					temp=mg;
					break;
				}
			}
		}
		if(temp!=null)
		{
			giveGlassToMachine(temp);
			//System.err.println("giveGlassToNextCF(temp)");
			return true;
		}
		
		
		synchronized(glasses)
		{
			for(MyGlass mg : glasses)
			{
				if(mg.state==MyGlassState.STOPLEFT&&sensorAfterState==SensorAfterState.RELEASED)
				{
					temp=mg;
					break;
				}
			}
		}
		
		
		if(temp!=null)
		{
			tellGUIConveyorStartMoving(temp);
			sensorAfterState=SensorAfterState.EMPTY;
			return true;
		}
		return false;
	}

	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == this.myChannel)
		{
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				machineState = MachineState.LOADED;
				if(conveyorIndex==2)
					System.err.println("Animation tells me TEvent.WORKSTATION_LOAD_FINISHED");
				stateChanged();
			}
			else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				//System.err.println("TEvent.WORKSTATION_GUI_ACTION_FINISHED");
				machineState=MachineState.DONE;
				stateChanged();
			}
			else if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				machineState =MachineState.RELEASE_FINISHED;
				stateChanged();
			}
		}
	}
}

