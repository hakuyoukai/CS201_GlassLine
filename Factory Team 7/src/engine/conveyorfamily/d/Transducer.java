package engine.conveyorfamily.d;
import transducer.*;

public interface Transducer {
	void startTransducer();
	void register(TReceiver toRegister, TChannel channel);
	void unregister(TReceiver toUnregister, TChannel channel);
	void fireEvent(TChannel channel, TEvent event, Object[] args);
}
