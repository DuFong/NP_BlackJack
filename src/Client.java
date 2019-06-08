import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
	public void start() throws RemoteException, NotBoundException;
	public String getName() throws RemoteException;
	public void setName() throws RemoteException;
	public void clientCheck() throws RemoteException;
	public void said(String m) throws RemoteException;
	public void onePlayer() throws RemoteException;
	public void twoPlayer() throws RemoteException;
	public void watchingGame() throws RemoteException;
}
