package engine.conveyorfamily.m;

import engine.util.Glass;

public interface Popup
{
	public abstract void msgIAmReady();
	public abstract void msgCanIGiveGlass(Boolean action);
	public abstract void msgHereIsGlass(Conveyor c, Glass g);
}