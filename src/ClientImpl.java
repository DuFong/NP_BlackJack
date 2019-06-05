import java.rmi.server.UnicastRemoteObject;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;

import javax.swing.*;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientImpl extends UnicastRemoteObject implements Client, ActionListener{
	
	//MainFrame
	protected Frame frame;
	
	protected Server server;
	protected String host;
	
	// SSL handshaking을 위한 password
	private String mypassword;
	
	private SSLSocket sslsocketclient;
	
	// s1 = 로그인페이지   s2 = 대기방페이지   s3 = 게임화면페이지
	protected JPanel s1_panel;
	protected JLabel s1_label;
	protected JButton s1_license_button;
	protected JButton s1_name_button;
	protected TextField s1_license_input;
	protected TextArea s1_output;

	

	public ClientImpl(String host) throws RemoteException {
		this.host = host;
		
		// swing을 통한 GUI. License 검
		frame = new Frame("License Activation");
		
		// s1 페이지 영역
		s1_panel = new JPanel();
		s1_label = new JLabel("라이센스키를 입력하세요.");
		s1_license_button = new JButton("라이센스 확인");
		s1_name_button = new JButton("닉네임을 입력하세요");
		s1_license_input = new TextField(12);
		s1_output = new TextArea();
		
		s1_panel.add(s1_label);
		s1_panel.add(s1_license_input);
		s1_panel.add(s1_license_button);
		
		frame.add(s1_panel,"North");
		frame.add(s1_output,"Center");
		
		s1_license_button.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(s1_license_input.equals(""))
					s1_output.setText("라이센스키를 입력하세요");
				else s1_output.setText("라이센스를 검증합니다");
				
				try {
					mypassword = server.checkLicense(s1_license_input.getText());
					s1_license_input.setText("");
					if(mypassword == null) {
						s1_output.setText("잘못된 라이센스키입니다. 다시 확인하세요.");
					}
					else {
						System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
						System.setProperty("javax.net.ssl.trustStorePassword", mypassword);
						
						sslsocketclient = server.createSSLSocket();
									
						String[] supported = sslsocketclient.getSupportedCipherSuites();
						sslsocketclient.setEnabledCipherSuites(supported);
						//printSocketInfo(c);
						sslsocketclient.startHandshake();
						// SSL connection done.'
						s1_license_button.setVisible(false);
						s1_name_button.setVisible(true);
						
					}
				} catch (RemoteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException io) {
					System.out.println(io);
				}
				
			}
		});
		
		s1_name_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	
	public void start() throws RemoteException, NotBoundException {
		if(server == null){
	        //Registry registry = LocateRegistry.getRegistry(host);
	        //server = (Server)registry.lookup(Server.REGISTRY_NAME);
	        frame.setVisible(true);
			//s1_name_button.setVisible(false);

		}
	}

	public static void main(String[] args) throws RemoteException, NotBoundException {
		// TODO Auto-generated method stub

		ClientImpl client = new ClientImpl(args[0]);
		client.start();
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}


}
