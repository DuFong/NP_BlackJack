import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;

public interface Server extends Remote {
	public static final String REGISTRY_NAME = "rmi://localhost:9999/mainserver";
	
	public String checkLicense(String client_license) throws RemoteException;
	public SSLSocket createSSLSocket() throws RemoteException;
	public void say(String message) throws RemoteException;
	public void whisper(String message) throws RemoteException;
	public ArrayList clientList() throws RemoteException;
	public void register(Client client) throws RemoteException;
	public void exit(Client client) throws RemoteException;
	public void checkGame(String name) throws RemoteException;
	public void checkSelect(String name, int iscorrect) throws RemoteException;
}
