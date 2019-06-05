import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ServerImpl extends UnicastRemoteObject implements Server {
	
	// SSL 연결을 위함
	private String[] license = {"20150263","20150234","1101978006"};
	private String password = "asd123";
	protected int portNumSSL = 8888;
	
	SSLSocketFactory sslSocketFactory = null;

	protected ServerImpl() throws RemoteException {
		super();
	}
	
	public String checkLicense(String client_license) throws RemoteException {
		for(int i = 0; i < license.length; i++) {
			if(license[i].equals(client_license))
				return password;
		}
		
		return null;
	}
	
	public SSLSocket createSSLSocket() throws RemoteException {
		sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		try {
			return (SSLSocket)sslSocketFactory.createSocket("localhost",portNumSSL);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws IOException, ServerNotActiveException {
		// TODO Auto-generated method stub
		ServerImpl callbackServer = new ServerImpl();
	    Registry registry = LocateRegistry.getRegistry ();
	    registry.rebind (REGISTRY_NAME, callbackServer);
	    
	    final KeyStore ks;
		final KeyManagerFactory kmf;
		final SSLContext sc;
		
		final String runRoot = "/Users/fong/NP_BlackJack/src/";  // root change : your system root
		

		SSLServerSocketFactory ssf = null;
		SSLServerSocket s = null;
		SSLSocket c = null;
		
		BufferedWriter w = null;
		BufferedReader r = null;
		
		if (args.length != 1) {
			System.out.println("Usage: Classname Port");
			System.exit(1);
		}
		
		
		String ksName = runRoot+".keystore/SSLSocketServerKey";
		
		char keyStorePass[] = "asd123".toCharArray();
		char keyPass[] = "asd123".toCharArray();
		
		try {
			ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName),keyStorePass);
			
			kmf = KeyManagerFactory.getInstance("sunX509");
			kmf.init(ks,keyPass);
			
			sc = SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(), null,null);
			
			/* SSLEngine
			sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(false);
			sslSession = sslEngine.getSession();
			
			dummy = ByteBuffer.allocate(0);
			outNetBuffer = ByteBuffer.allocate(this.getNetBufferSize());
			inAppBuffer = ByteBuffer.allocate(this.getAppBufferSize());
			*/
			
			/* SSLServerSocket */
			ssf = sc.getServerSocketFactory();
			s = (SSLServerSocket) ssf.createServerSocket(callbackServer.portNumSSL);
			
			c = (SSLSocket) s.accept();
		} catch (SSLException se) {
			System.out.println("SSL problem, exit~");
			try {
				w.close();
				r.close();
				s.close();
				c.close();
			} catch (IOException i) {
			}
		} catch (Exception e) {
			System.out.println("What?? exit~");
			try {
				System.out.println(e);
				w.close();
				r.close();
				s.close();
				c.close();
			} catch (IOException i) {
			}
		}
			

	}
}
	


