package engine.conveyorfamily.m;

import engine.util.Glass;

public interface Conveyor
{
	public abstract void msgHereIsGlass(Glass g);
	public abstract void msgGiveMeGlass();
}