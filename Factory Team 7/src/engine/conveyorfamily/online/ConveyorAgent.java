package engine.conveyorfamily.online;

import engine.agent.Agent;
import engine.util.ConveyorFamilyInterface;
import engine.util.Glass;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import transducer.TChannel;
import transducer.TEvent;
import transducer.Transducer;

public class ConveyorAgent extends Agent
{
	public List<MyGlass> glasses= Collections.synchronizedList(new ArrayList<MyGlass>());
	public enum MyGlassState{STOPLEFT,STOPRIGHT,MOVING,PROCESSING};

	public enum ConveyorState{MOVING,STATIC,MOVING_TO_STOP};
	public ConveyorState conveyorState=ConveyorState.STATIC;

	public enum SensorAfterState{PRESSED,RELEASED};
	public SensorAfterState sensorAfterState=SensorAfterState.RELEASED;
	
	public enum NextCFState{AVAILABLE,UNAVAILABLE};
	public NextCFState nextCFState=NextCFState.AVAILABLE;

	ConveyorFamilyInterface conveyorAfter;
	int conveyorIndex;
	//Semaphore animationDone=new Semaphore(0,true);
	
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
		this.transducer.register(this, TChannel.PAINTER);
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
		glasses.get(0).state=MyGlassState.STOPRIGHT;
		sensorAfterState=SensorAfterState.PRESSED;
		stateChanged();
	}
	
	public void msgNextCFRready()
	{
		nextCFState=NextCFState.AVAILABLE;
	}

	
	//Actions
	public void giveGlassToNextCF(MyGlass g)
	{
		g.state=MyGlassState.PROCESSING;
		Object[] args=new Object[1];
		args[0]=new Integer(conveyorIndex);
		transducer.fireEvent(TChannel.CONVEYOR,TEvent.CONVEYOR_DO_START,args);
		//for testing
		if(conveyorIndex==1)
		{
			conveyorAfter.msgHereIsGlass(g.glass); 
		}
	}

	public void tellGUIConveyorStartMoving()
	{
		Object[] args=new Object[1];
		args[0]=new Integer(conveyorIndex);
		transducer.fireEvent(TChannel.CONVEYOR, TEvent.CONVEYOR_DO_START,args);
		conveyorState=ConveyorState.MOVING;
		glasses.get(0).state=MyGlassState.MOVING;
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
		if(glasses.size()==0)
		{
			if(conveyorState!=ConveyorState.STATIC)
			{
				tellGUIConveyorStopMoving();
				return true;
			}	
		}
		else
		{
			if(sensorAfterState==SensorAfterState.PRESSED)
			{
				if(conveyorState!=ConveyorState.STATIC)
				{
					tellGUIConveyorStopMoving();
					return true;
				}
			}
			
			synchronized(glasses)
			{
				for(MyGlass mg : glasses)
				{
					if(mg.state == MyGlassState.STOPRIGHT && nextCFState==NextCFState.AVAILABLE)
					{
						giveGlassToNextCF(mg);
						return true;
					}
				}
			}
			
			synchronized(glasses)
			{
				for(MyGlass mg : glasses)
				{
					if(mg.state==MyGlassState.STOPLEFT&&sensorAfterState==SensorAfterState.RELEASED)
					{
						tellGUIConveyorStartMoving();
						return true;
					}
				}
			}
		}
		return false;
	}

	public void eventFired(TChannel channel, TEvent event, Object[] args)
	{
		if(channel == this.myChannel)
		{
			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
			{
				synchronized(glasses)
				{
					for(MyGlass mg : glasses)
					{
						if(mg.state == MyGlassState.PROCESSING)
						{
							if(mg.glass.recipe.get(conveyorIndex))
							{
								Object[] conveyorNum=new Object[1];
								conveyorNum[0]=new Integer(conveyorIndex);
								transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_DO_ACTION, conveyorNum);
								break;
							}
							else
							{
								Object[] conveyorNum=new Object[1];
								conveyorNum[0]=new Integer(conveyorIndex);
								transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
								break;
							}
						}
					}
				}
			}
			else if(event == TEvent.WORKSTATION_GUI_ACTION_FINISHED)
			{
				synchronized(glasses)
				{
					for(MyGlass mg: glasses)
					{
						if(mg.state == MyGlassState.PROCESSING)
						{
							Object[] conveyorNum=new Object[1];
							conveyorNum[0]=new Integer(conveyorIndex);
							transducer.fireEvent(this.myChannel, TEvent.WORKSTATION_RELEASE_GLASS, conveyorNum);
							break;
						}
					}
				}
			}
			else if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
			{
				synchronized(glasses)
				{
					for(MyGlass mg: glasses)
					{
						if(mg.state == MyGlassState.PROCESSING)
						{
							System.out.println(conveyorIndex+ " is giveing glass to "+(conveyorIndex+1));
							conveyorAfter.msgHereIsGlass(mg.glass);
							glasses.remove(mg);
							nextCFState = NextCFState.UNAVAILABLE;
							break;
						}
					}
				}
			}
		}
	}
}

