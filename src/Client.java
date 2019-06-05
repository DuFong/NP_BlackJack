import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public void start() throws RemoteException, NotBoundException;
}
