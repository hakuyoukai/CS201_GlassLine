package engine.conveyorfamily.online;

import engine.agent.Agent;
import engine.conveyorfamily.shuttle.ConveyorFamilyShuttle;
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
	public enum MyGlassState{STOPLEFT,STOPRIGHT,MOVING,PROCESSING,BROKEN};
	public boolean conveyorBroken=false;

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
		this.transducer.register(this, TChannel.CONTROL_PANEL);
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
		if(conveyorIndex==11)
			System.out.println("Conveyor Index "+conveyorIndex+":Glass arrived");
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
		if(conveyorIndex==11)
		{
			System.out.println("Joey's shuttle told me its conveyor is ready");
		}
		nextCFState=NextCFState.AVAILABLE;
		stateChanged();
	}
	
	public void msgDeleteGlass(Glass g)
	{
		for(MyGlass mg : glasses)
		{
			if(mg.glass==g)
			{
				glasses.remove(g);
			}
		}
	}

	//Actions
	public void giveGlassToMachine(MyGlass g)
	{
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
		stateChanged();
	}
	
	//Scheduler
	public boolean pickAndExecuteAnAction()
	{	
			if(conveyorIndex==2)
			{
				synchronized(glasses)
				{
					for(MyGlass mg:glasses)
					{
						System.out.println(mg.state);
					}
				}
				System.out.println("--------------Beginning of the schduler-----------------");
				System.out.println("Next Conveyor State:"+nextCFState);
				System.out.println("Machine State:"+machineState);
				System.out.println("Conveyor State"+conveyorState);
				System.out.println("Conveyor Broken: "+conveyorBroken);
				System.out.println("--------------------------------------------------------");
			}
			
			
			MyGlass temp=null;
			if(!conveyorBroken)
			{
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
					if(conveyorIndex==2)
						System.out.println("execute action: giveGlassToMachine");
					giveGlassToMachine(temp);
					return true;
				}
			}
			
			
			if(machineState == MachineState.LOADED)
			{
				synchronized(glasses)
				{
					for(MyGlass mg : glasses)
					{
						if(mg.state == MyGlassState.PROCESSING)
						{
							if(mg.glass.recipe.get(conveyorIndex))
							{
//								if(conveyorIndex==11)
//									System.out.println("execute action: workstation do action");
								Object[] conveyorNum=new Object[1];
								conveyorNum[0]=new Integer(conveyorIndex);
								transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_DO_ACTION, conveyorNum);
								machineState = MachineState.DOING_ACTION;
								transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum);
								return true;
							}
							else
							{
//								if(conveyorIndex==11)
//									System.out.println("execute action: workstation release glass--glass does not need to be processed");
								Object[] conveyorNum=new Object[1];
								machineState = MachineState.DONE;
								conveyorNum[0]=new Integer(conveyorIndex);
								//transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
								transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_STOP, conveyorNum);
								return true;
							}
						}
					}
				}
			}
			
			if(machineState == MachineState.DONE)
			{
				synchronized(glasses)
				{
					for(MyGlass mg: glasses)
					{
						if(mg.state == MyGlassState.PROCESSING)
						{	
							if(conveyorIndex==2)
								System.out.println("execute action: workstation release glass");
							machineState = MachineState.RELEASING;
							Object[] conveyorNum=new Object[1];
							conveyorNum[0]=new Integer(conveyorIndex);
							transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
							return true;
						}
					}
				}
			}
			
			if(!conveyorBroken)
			{
				if(machineState == MachineState.RELEASE_FINISHED)
				{
					synchronized(glasses)
					{
						for(MyGlass mg: glasses)
						{
							if(mg.state == MyGlassState.PROCESSING)
							{
								if(conveyorIndex==2)
									System.out.println("execute action: removeglass and clean up");
								//conveyorAfter.msgHereIsGlass(mg.glass);
								glasses.remove(mg);
								machineState=MachineState.AVAILABLE;
								Object[] conveyorNum=new Object[1];
								conveyorNum[0]=new Integer(conveyorIndex);
								boolean start=true;
								for(MyGlass mg_g: glasses)
								{
									if(mg_g.state==MyGlassState.STOPRIGHT)
									{
										start=false;
									}
								}
								if(start)
									transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START, conveyorNum);
								return true;
							}
						}
					}
				}
			}
			
			if(conveyorState==ConveyorState.MOVING_TO_STOP)
			{
				if((machineState!=MachineState.LOADING)&&((machineState!=MachineState.AVAILABLE||nextCFState==NextCFState.UNAVAILABLE)))
				{
						if(conveyorIndex==2)
							System.out.println("execute action: tellGUIConveyorStopMoving");
						tellGUIConveyorStopMoving();
						return true;
				}
				else
				{
					conveyorState=ConveyorState.MOVING;
					return true;
				}
			}
		
			if(!conveyorBroken)
			{
				temp=null;
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
					if(conveyorIndex==2)
						System.out.println("execute action: tellGUIConveyorStartMoving");
					tellGUIConveyorStartMoving(temp);
					sensorAfterState=SensorAfterState.EMPTY;
					return true;
				}
			}
			
			/*
			synchronized(glasses)
			{
				for(MyGlass mg : glasses)
				{
					if(mg.state == MyGlassState.STOPRIGHT && nextCFState==NextCFState.UNAVAILABLE && machineState==MachineState.AVAILABLE)
					{
						if(conveyorAfter instanceof ConveyorFamilyShuttle)
						{
							ConveyorFamilyShuttle tempCFS = (ConveyorFamilyShuttle)conveyorAfter;
							tempCFS.conveyor.askedIfAvailable();
						}
						break;
					}
				}
			}
			*/
			if(conveyorIndex==2)
				System.out.println("execute action: Nothing");
		
		return false;
	}

	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == this.myChannel)
		{
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				machineState = MachineState.LOADED;
				stateChanged();
			}
			else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				machineState=MachineState.DONE;
				stateChanged();
			}
			else if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				machineState =MachineState.RELEASE_FINISHED;
				stateChanged();
			}
		}
		
		if(channel == TChannel.CONTROL_PANEL)
		{
			if(event == TEvent.CONVEYOR_JAM)
			{	
				if((Integer)args[0]==this.conveyorIndex)
				{
					transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_STOP,args);
					conveyorBroken = true;
					System.out.println("Conveoyr Index "+this.conveyorIndex+" is jammed");
					stateChanged();
				}
			}
			
			if(event == TEvent.CONVEYOR_UNJAM)
			{
				if((Integer)args[0]==this.conveyorIndex)
				{
					transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args);
					conveyorBroken = false;
					System.out.println("Conveoyr Index "+this.conveyorIndex+" is unjammed");
					stateChanged();
				}
			}
			
			if(event == TEvent.INLINE_WORKSTATION_BREAK)
			{
				System.out.println("Back end received that the "+(Integer)args[0]+" is broken");
				System.out.println((Integer)args[0]==conveyorIndex);
				if((Integer)args[0]==conveyorIndex)
				{
					if(machineState!=MachineState.RELEASING&&machineState!=MachineState.RELEASE_FINISHED)
					{
						synchronized(glasses)
						{
							for(MyGlass mg : glasses)
							{
								if(mg.state==MyGlassState.PROCESSING)
								{
									conveyorAfter.msgDeleteGlass(mg.glass);
									mg.state=MyGlassState.BROKEN;
									System.out.println("Inline Index "+this.conveyorIndex+" is broken");
									break;
								}
							}
							System.out.println("receive INLINE_WORKSTATION_BREAK but did not find the glass");
						}
					}
					else
					{
						System.out.println("receive INLINE_WORKSTATION_BREAK but did not process based on state ");
					}
					machineState=MachineState.LOADED;
					stateChanged();
				}
			}
			
			if(event == TEvent.INLINE_WORKSTATION_UNBREAK)
			{
				System.out.println("Back end received that the "+(Integer)args[0]+" is unbroken");
				System.out.println((Integer)args[0]==conveyorIndex);
				if((Integer)args[0]==conveyorIndex)
				{
					if(machineState!=MachineState.RELEASING&&machineState!=MachineState.RELEASE_FINISHED)
					{
						machineState=MachineState.AVAILABLE;
						synchronized(glasses)
						{
							for(MyGlass mg : glasses)
							{
								if(mg.state==MyGlassState.BROKEN)
								{
									glasses.remove(mg);
									nextCFState=NextCFState.AVAILABLE;
									System.out.println("Inline Index "+this.conveyorIndex+" is unbroken");
									transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args);
									break;
								}
							}
						}
					}
					else
					{
						System.out.println("receive INLINE_WORKSTATION_UNBREAK but did not process based on state ");
					}
					stateChanged();
				}
			}
			
			
		}
	}
}

