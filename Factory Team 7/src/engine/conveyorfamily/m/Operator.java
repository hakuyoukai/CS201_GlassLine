package engine.conveyorfamily.m;

import engine.util.Glass;

public interface Operator
{
	public abstract void msgHereIsAPart(Glass glass);
	public abstract void msgSendPart();
}