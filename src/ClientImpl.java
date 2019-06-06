import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class ClientImpl extends UnicastRemoteObject implements Client, ActionListener{
	
		// MainFrame
	   protected JFrame frame;
	   protected Server server;
	   protected String host;

	   // SSL handshaking을 위한 password
	   private String mypassword;
	   private SSLSocket sslsocketclient;
	   
	   // s1 = 로그인페이지 s2 = 대기방페이지 s3 = 게임화면페이지
	   protected JPanel s1_panel;
	   protected JLabel s1_label;
	   protected JButton s1_license_button;
	   protected JButton s1_name_button;
	   protected TextField s1_license_input;
	   protected TextArea s1_output;
	   boolean check = false; // 클라이언트 입장 확인 변수
	   String name; // 닉네임 저장 변수

	   // for GUI
	   protected JTextField s2_message; // 메세지 입력창
	   protected static JTextArea s2_output; // 메세지 출력창
	   protected JPanel s2_mainPane;
	   protected JPanel s2_messagePane;
	   protected JPanel s2_sidePane;
	   protected JLabel s2_lab;
	   protected JList<String> s2_list; // 유저 목록
	   protected JButton s2_send; // 메세지 보내기 버튼
	   protected JButton s2_make; // 방 만들기 버튼
	   protected JButton s3_exit; // 나가기 버튼
	   protected JButton s3_hit; // Hit 버튼
	   protected JButton s3_stop; // Stop 버튼
	   ArrayList<String> clients; // client 저장 List
	   boolean s2_check = false; // client가 입장했는지 체크

	   protected int myPort;
	   protected static int min = 9000;
	   protected static int max = 9999;
	   protected Vector<String> c;
	
	   SSLSocketFactory sslSocketFactory;

	

	public ClientImpl(String host) throws RemoteException {
		this.host = host;
		
		// swing을 통한 GUI. License 검
		frame = new JFrame("BlackJack_byNL");
		
		 // swing을 통한 GUI. License 검사
	      frame = new JFrame("License Activation");
	      frame.setLayout(new BorderLayout());
	      frame.setPreferredSize(new Dimension(800, 800));
	      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      frame.pack();

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

	      frame.add(s1_panel, "North");
	      frame.add(s1_output, "Center");

	      // s2 부분
	      c = new Vector<String>();
	      s2_list = new JList<String>();
	      s2_list.setListData(c);

	      // 패널 설정
	      s2_mainPane = new JPanel();
	      s2_sidePane = new JPanel();
	      s2_messagePane = new JPanel();

	      // 테두리 설정
	      Border lineBorder = BorderFactory.createLineBorder(Color.black, 1);
	      Border emptyBorder = BorderFactory.createEmptyBorder();

	      // text field 및 area 설정
	      s2_message = new JTextField(100);
	      s2_message.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
	      s2_output = new JTextArea();
	      s2_output.setEditable(false);
	      s2_output.setLineWrap(true);
	      s2_output.setSize(218, 800);
	      s2_output.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
	      s2_list.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));

	      // 버튼 설정
	      s2_send = new JButton("보내기");
	      s2_make = new JButton("방 만들기");
	      s3_exit = new JButton("나가기");
	      s3_hit = new JButton("Hit");
	      s3_stop = new JButton("Stop");

	      // side pane, message pane 설정
	      s2_sidePane.setLayout(new GridBagLayout());
	      GridBagConstraints gbc = new GridBagConstraints();
	      gbc.fill = GridBagConstraints.BOTH;
	      gbc.gridwidth = 1;
	      gbc.gridheight = 1;
	      gbc.weighty = 1;
	      gbc.gridx = 0;
	      gbc.gridy = 0;
	      s2_sidePane.add(s2_list, gbc);
	      gbc.weighty = 2;
	      gbc.gridx = 0;
	      gbc.gridy = 1;
	      s2_sidePane.add(s2_output, gbc);

	      gbc.fill = GridBagConstraints.BOTH;
	      s2_messagePane.setLayout(new GridBagLayout());
	      gbc.weightx = 2;
	      gbc.gridx = 0;
	      gbc.gridy = 0;
	      s2_messagePane.add(s2_message, gbc);
	      gbc.weightx = 0.1;
	      gbc.gridx = 1;
	      gbc.gridy = 0;
	      s2_messagePane.add(s2_send, gbc);
	      gbc.weightx = 0.1;
	      gbc.gridx = 2;
	      gbc.gridy = 0;
	      s2_messagePane.add(s2_make, gbc);

		
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
						s1_output.setText(mypassword);
						System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
						System.setProperty("javax.net.ssl.trustStorePassword", mypassword);
						
						sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
						try {
							sslsocketclient = (SSLSocket)sslSocketFactory.createSocket(host,8888);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						} catch (IOException e1){
							e1.printStackTrace();
						}
									
						String[] supported = sslsocketclient.getSupportedCipherSuites();
						sslsocketclient.setEnabledCipherSuites(supported);
						printSocketInfo(sslsocketclient);
						s1_output.setText("hand hand");
						sslsocketclient.startHandshake();
						s1_output.setText("hand hand2");
						// SSL connection done.
						s1_license_button.setVisible(false);
						s1_panel.add(s1_name_button);
						
					}
				} catch(MalformedURLException mue)
				{
					System.out.println("MalformedURLException: " + mue);
				}
				catch(RemoteException re)
				{
					System.out.println("RemoteException: " + re);
				}
				catch(java.lang.ArithmeticException ae)
				{
					System.out.println("java.lang.ArithmeticException " + ae);
				}
				catch(IOException io) {
					System.out.println(io);
					
				}
				
			}
		});
		
		s1_name_button.addActionListener(new ActionListener() {
	         @Override
	         public void actionPerformed(ActionEvent e) {
	            if (check == false) {
	               check = true;
	               name = s1_license_input.getText();
	               if (name.equals("") || name.length() > 12) {
	                  s1_output.setText("닉네임을 다시 입력해주세요. 최대 12글자 입니다.");
	                  name = null;
	                  check = false;
	               } else {
	                  s1_panel.setVisible(false);
	                  s2_sidePane.setVisible(true);
	                  s2_mainPane.setVisible(true);
	                  frame.add(s2_mainPane, BorderLayout.CENTER);
	                  frame.add(s2_messagePane, "South");
	                  frame.add(s2_sidePane, BorderLayout.EAST);
	                  s2_sidePane.setVisible(false);
	                  s2_mainPane.setVisible(false);
	                  frame.pack();
	                  frame.setVisible(true);
	                  s2_message.requestFocus();

	                  s2_output.append("입장하였습니다.\n");
	                  s2_output.append("*****귓속말 사용법*****\n상대방 ID를 클릭 후 메세지를 입력하거나,\n'/w 상대방ID 메세지'를 입력하세요.\n");
	              
	               }
	            }	         
	         }	     
		});	   
	}

	
	public void printSocketInfo(SSLSocket s) {
		System.out.println("Socket class: "+s.getClass());
		System.out.println("   Remote address = "
				+s.getInetAddress().toString());
		System.out.println("   Remote port = "+s.getPort());
		System.out.println("   Local socket address = "
				+s.getLocalSocketAddress().toString());
		System.out.println("   Local address = "
				+s.getLocalAddress().toString());
		System.out.println("   Local port = "+s.getLocalPort());
		System.out.println("   Need client authentication = "
				+s.getNeedClientAuth());
		SSLSession ss = s.getSession();
		System.out.println("   Cipher suite = "+ss.getCipherSuite());
		System.out.println("   Protocol = "+ss.getProtocol());
	}
	
	
	public void start() throws RemoteException, NotBoundException {
		if(server == null){
	        try {
				server = (Server)Naming.lookup("rmi://"+host+"/CallbackServer");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        frame.setVisible(true);

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
