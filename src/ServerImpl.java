import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Vector;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.List;
import java.util.Random;

public class ServerImpl extends UnicastRemoteObject implements Server {

   // SSL 연결을 위함
   private String[] license = { "20150263", "20150234", "1101978006" };
   private String password = "asd123";
   protected int portNumSSL = 8888;
   protected Vector clients;
   protected int num = 0;
   protected int answer;
   protected String win;
   protected String lose;
   
   // 게임이 끝나고 초기화 해야되는 변수들
   protected ArrayList<String> ingameplayerlist;
   protected int selnum;
   

   SSLSocketFactory sslSocketFactory;

   protected ServerImpl() throws RemoteException {
      clients = new Vector();
      selnum  = 0;
   }

   public String checkLicense(String client_license) throws RemoteException {
      for (int i = 0; i < license.length; i++) {
         if (license[i].equals(client_license))
            return password;
      }

      return null;
   }

   public SSLSocket createSSLSocket() throws RemoteException {
      sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
      try {
         return (SSLSocket) sslSocketFactory.createSocket("localhost", portNumSSL);
      } catch (UnknownHostException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return null;
   }

   // tmp add
   private static void printSocketInfo(SSLSocket s) {
      System.out.println("Socket class: " + s.getClass());
      System.out.println("   Remote address = " + s.getInetAddress().toString());
      System.out.println("   Remote port = " + s.getPort());
      System.out.println("   Local socket address = " + s.getLocalSocketAddress().toString());
      System.out.println("   Local address = " + s.getLocalAddress().toString());
      System.out.println("   Local port = " + s.getLocalPort());
      System.out.println("   Need client authentication = " + s.getNeedClientAuth());
      SSLSession ss = s.getSession();
      System.out.println("   Cipher suite = " + ss.getCipherSuite());
      System.out.println("   Protocol = " + ss.getProtocol());
   }

   private static void printServerSocketInfo(SSLServerSocket s) {
      System.out.println("Server socket class: " + s.getClass());
      System.out.println("   Server address = " + s.getInetAddress().toString());
      System.out.println("   Server port = " + s.getLocalPort());
      System.out.println("   Need client authentication = " + s.getNeedClientAuth());
      System.out.println("   Want client authentication = " + s.getWantClientAuth());
      System.out.println("   Use client mode = " + s.getUseClientMode());
   }

   public static void main(String[] args) throws IOException, ServerNotActiveException {
      // TODO Auto-generated method stub
      Server callbackServer = new ServerImpl();
      Naming.rebind("rmi://" + args[0] + ":9999/mainserver", callbackServer);

      final KeyStore ks;
      final KeyManagerFactory kmf;
      final SSLContext sc;

      final String runRoot = "C://Users//eunch//Desktop//GIT//NP_BlackJack//src//"; // root change : your system root

      SSLServerSocketFactory ssf = null;
      SSLServerSocket s = null;

      BufferedWriter w = null;
      BufferedReader r = null;

      String ksName = runRoot + ".keystore/SSLSocketServerKey";

      char keyStorePass[] = "asd123".toCharArray();
      char keyPass[] = "asd123".toCharArray();

      try {
         ks = KeyStore.getInstance("JKS");
         ks.load(new FileInputStream(ksName), keyStorePass);

         kmf = KeyManagerFactory.getInstance("sunX509");
         kmf.init(ks, keyPass);

         sc = SSLContext.getInstance("TLS");
         sc.init(kmf.getKeyManagers(), null, null);

         /* SSLServerSocket */
         ssf = sc.getServerSocketFactory();
         s = (SSLServerSocket) ssf.createServerSocket(8888);
         printServerSocketInfo(s);

         while (true) {
            SSLSocket socket = (SSLSocket) s.accept();

            ThreadServer thread = new ThreadServer(socket, callbackServer);
            thread.start();
         }
      } catch (SSLException se) {
         System.out.println("SSL problem, exit~");
         try {
            w.close();
            r.close();
            s.close();
         } catch (IOException i) {
         }
      } catch (Exception e) {
         System.out.println("What?? exit~");
         try {
            System.out.println(e);
            w.close();
            r.close();
            s.close();
         } catch (IOException i) {
         }
      }
   }

   public void register(Client client) throws RemoteException {
      clients.addElement(client);
      sendCheck();
      num++;
   }

   public void exit(Client client) throws RemoteException {
      clients.removeElement(client);
      sendCheck();
   }

   public ArrayList clientList() throws RemoteException {
      ArrayList<String> list = new ArrayList<>();
      for (int i = 0; i < this.clients.size(); i++) {
         Client client = (Client) clients.elementAt(i);
         list.add(client.getName());
      }
      return list;
   }

   public void sendCheck() {
      Vector clients = (Vector) this.clients.clone();
      for (int i = 0; i < clients.size(); ++i) {
         Client c = (Client) clients.elementAt(i);
         try {
            c.clientCheck();
         } catch (RemoteException e) {
            e.printStackTrace();
         }
      }
   }

   public void whisper(String message) throws RemoteException {
      int first, second, third;
      first = message.indexOf("[");
      second = message.indexOf("/");
      third = message.indexOf("]");
      // select sender
      String sender_name = message.substring(0, first);
      // select receiver
      String receiver_name = message.substring(second + 2, third);
      // parsing message
      String msg = message.substring(third + 1);

      Client sender = null, receiver = null;

      Vector clients = (Vector) this.clients.clone();
      for (int i = 0; i < clients.size(); ++i) {
         Client client = (Client) clients.elementAt(i);
         if (client.getName().equals(sender_name)) { // 귓속말을 보내는 client 구분
            sender = client;
            break;
         }
      }

      for (int i = 0; i < clients.size(); ++i) {
         Client client = (Client) clients.elementAt(i);
         if (client.getName().equals(receiver_name)) { // 귓속말을 받는 client 구분
            receiver = client;
            break;
         }
      }

      if (sender != null && receiver != null) {
         sender.said("[귓속말] " + sender_name + " >> " + receiver_name + " )) " + msg);
         receiver.said("[귓속말] " + sender_name + " >> " + receiver_name + " )) " + msg);
      } else
         sender.said("정확히 입력해주세요");

   }

   public void say(String message) throws RemoteException {
      Vector clients = (Vector) this.clients.clone();
      for (int i = 0; i < clients.size(); ++i) {
         Client client = (Client) clients.elementAt(i);
         try {
               client.said(message);
         } catch (RemoteException ex) {
            this.clients.removeElement(client);
         }
      }
   }

	 public void checkGame(String name) throws RemoteException {
		 if(ingameplayerlist == null) {
		      ingameplayerlist = new ArrayList<String>();
		 }
	      if (ingameplayerlist.size() < 2) {
	         ingameplayerlist.add(name);
	         if (ingameplayerlist.size() == 2) {
	        	 Random rand = new Random();
	        	 answer = rand.nextInt(2);
	            for (int i = 0; i < 2; i++) {
	               for (int j = 0; j < clients.size(); j++) {
	                  Client client = (Client) clients.elementAt(j);
	                  if (client.getName().equals(ingameplayerlist.get(i))) {
	                	  client.twoPlayer(answer);
	                	  
	                  }
	               }
	            }
	            for (int j = 0; j < clients.size(); j++) {
	               Client client = (Client) clients.elementAt(j);
	               if (!ingameplayerlist.contains(client.getName())) {
	                  client.watchingGame();
	               }
	            }
	         }

	         else {
	            for (int j = 0; j < clients.size(); j++) {
	               Client client = (Client) clients.elementAt(j);
	               if (client.getName().equals(ingameplayerlist.get(0))) {
	                  client.onePlayer();
	               }

	            }
	         }
	      }
	   }
	 
	 public void checkSelect(String name, int iscorrect) throws RemoteException{
		 if(selnum == 0) {
			 win = "이긴사람은 :";
			 lose = "진사람은 :";
		 }
		 selnum++;
		 if(iscorrect == 1) {
			 win = win +" "+ name;
		 }
		 else {
			 lose = lose + " " + name;
		 }
		 
		 if(selnum == 2) {
			 for (int i = 0; i < clients.size(); ++i) {
		         Client client = (Client) clients.elementAt(i);
		         client.said("\n*****게임 결과*****\n" + win + "\n" + lose+ "\n");
		         client.endGame();
			 }
			 ingameplayerlist.clear();
			 selnum = 0;
		 }
	 }
}