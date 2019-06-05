import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.net.ssl.SSLSocket;

public interface Server extends Remote {
	public static final String REGISTRY_NAME = "rmi://localhost:1099/Callback Server";
	
	public String checkLicense(String client_license) throws RemoteException;
	public SSLSocket createSSLSocket() throws RemoteException;
}
