import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Vector;
import java.util.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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

public class ClientImpl extends UnicastRemoteObject implements Client, ActionListener {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;
	// MainFrame
	protected JFrame frame1;
	protected JFrame frame2;
	protected Server server;
	protected String host;

	// SSL handshaking을 위한 password
	private String mypassword;
	private SSLSocket sslsocketclient;

	// s1 = 로그인페이지 s2 = 채팅 및 게임
	protected JPanel s1_panel;
	protected JLabel s1_label;
	protected JButton s1_license_button;
	protected JButton s1_name_button;
	protected JTextField s1_license_input;
	protected TextArea s1_output;
	boolean check = false; // 클라이언트 입장 확인 변수
	protected String name; // 닉네임 저장 변수

	// for GUI
	protected JTextField s2_message; // 메세지 입력창
	protected static JTextArea s2_output; // 메세지 출력창
	protected JPanel s2_mainPane;
	protected JPanel s2_messagePane;
	protected JPanel s2_sidePane;
	protected JLabel s2_lab;
	protected JList<Vector> s2_list; // 유저 목록
	protected JButton s2_send; // 메세지 보내기 버튼
	protected JButton s2_go; // 게임 참가 버튼
	protected JScrollPane scr1; // 스크롤
	protected JScrollPane scr2; // 스크롤
	ArrayList<String> clients; // client 저장 List
	boolean s2_check = false; // client가 입장했는지 체크

	// for Game
	protected JPanel s2_gamePane;
	protected JPanel s2_selPane; // 홀,짝 선택창
	protected JButton s2_odd; // 홀 버튼
	protected JButton s2_even; // 짝 버튼
	protected JTextArea s2_result; // 홀,짝 결과 출력창
	protected Font font;

	protected BufferedWriter w = null;
	protected BufferedReader r = null;

	SSLSocketFactory sslSocketFactory;

	protected int answer;
	protected int mySel;

	public ClientImpl(String host) throws RemoteException {
		this.host = host;

		// swing을 통한 GUI. License 검
		frame1 = new JFrame("BlackJack_byNL");
		frame2 = new JFrame("BlackJack_byNL");

		frame1.setLayout(new BorderLayout());
		frame1.setPreferredSize(new Dimension(800, 800));
		frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame1.pack();
		frame2.setLayout(new BorderLayout());
		frame2.setPreferredSize(new Dimension(800, 800));
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame2.pack();

		// s1 페이지 영역
		s1_panel = new JPanel();
		s1_label = new JLabel("라이센스키를 입력하세요.");
		s1_license_button = new JButton("라이센스 확인");
		s1_name_button = new JButton("입력");
		s1_license_input = new JTextField(12);
		s1_output = new TextArea();

		s1_panel.add(s1_label);
		s1_panel.add(s1_license_input);
		s1_panel.add(s1_license_button);

		frame1.add(s1_panel, "North");
		frame1.add(s1_output, "Center");

		// 패널 설정
		s2_mainPane = new JPanel();
		s2_mainPane.setLayout(new GridLayout(2, 1));
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
		s2_list = new JList();
		s2_list.setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder));
		scr1 = new JScrollPane(s2_list);
		scr2 = new JScrollPane(s2_output);

		// 버튼 설정
		s2_odd = new JButton("홀");
		s2_even = new JButton("짝");
		s2_send = new JButton("보내기");
		s2_go = new JButton("게임 참가");

		// 게임 GUI 설정
		s2_gamePane = new JPanel(new BorderLayout());
		s2_selPane = new JPanel();
		s2_selPane.setLayout(new FlowLayout());
		s2_odd = new JButton("홀");
		s2_even = new JButton("짝");
		s2_result = new JTextArea();
		s2_result.setEditable(false);
		s2_result.setText("\n\n\n                 게임을 시작하려면\n          '게임 참가' 버튼을 눌러주세요~~~");
		font = new Font("arian", Font.BOLD, 30);
		s2_result.setFont(font);
		s2_gamePane.add(s2_result, "Center");
		s2_selPane.add(s2_odd);
		s2_selPane.add(s2_even);
		s2_odd.setPreferredSize(new Dimension(150, 150));
		s2_even.setPreferredSize(new Dimension(150, 150));
		s2_odd.setEnabled(false);
		s2_even.setEnabled(false);
		s2_mainPane.add(s2_gamePane);
		s2_mainPane.add(s2_selPane);

		// side pane, message pane 설정
		s2_sidePane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		s2_sidePane.add(scr1, gbc);
		gbc.weighty = 2;
		gbc.gridx = 0;
		gbc.gridy = 1;
		s2_sidePane.add(scr2, gbc);

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
		s2_messagePane.add(s2_go, gbc);

		frame2.add(s2_mainPane, "Center");
		frame2.add(s2_sidePane, "East");
		frame2.add(s2_messagePane, "South");

		s2_message.addActionListener(this);

		s1_license_button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if (s1_license_input.equals(""))
					s1_output.setText("라이센스키를 입력하세요");
				else
					s1_output.setText("라이센스를 검증합니다");

				try {
					mypassword = server.checkLicense(s1_license_input.getText());
					s1_license_input.setText("");
					if (mypassword == null) {
						s1_output.setText("잘못된 라이센스키입니다. 다시 확인하세요.");
					} else {
						System.setProperty("javax.net.ssl.trustStore", "trustedcerts");
						System.setProperty("javax.net.ssl.trustStorePassword", mypassword);

						sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
						try {
							sslsocketclient = (SSLSocket) sslSocketFactory.createSocket(host, 8888);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						String[] supported = sslsocketclient.getSupportedCipherSuites();
						sslsocketclient.setEnabledCipherSuites(supported);
						printSocketInfo(sslsocketclient);
						sslsocketclient.startHandshake();

						// register 호출
						// SSL connection done.
						w = new BufferedWriter(new OutputStreamWriter(sslsocketclient.getOutputStream()));
						s1_license_button.setVisible(false);
						s1_panel.add(s1_name_button);
						s1_label.setText("닉네임을 입력하세요 : ");
						s1_output.setText("**********Cd Key 인증되었습니다!**********");

					}
				} catch (MalformedURLException mue) {
					System.out.println("MalformedURLException: " + mue);
				} catch (RemoteException re) {
					System.out.println("RemoteException: " + re);
				} catch (java.lang.ArithmeticException ae) {
					System.out.println("java.lang.ArithmeticException " + ae);
				} catch (IOException io) {
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
						try {
							setName();
						} catch (RemoteException er) {
							er.printStackTrace();
						}
						frame1.setVisible(false);
						frame2.setVisible(true);
						s2_message.requestFocus();

						try {
							server.say(name + "님이 입장하였습니다.");
						} catch (RemoteException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						s2_output.append("*****귓속말 사용법*****\n'[/w] 상대방ID 메세지'를 입력하세요.\n");
					}
				}
			}
		});

		s2_send.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				try {
					if (server != null) {
						String message = s2_message.getText();
						if (message.contains("[/w")) { // 전송하려는 메세지가 귓속말이면
							try {
								message = name + message;
								// SSL을 통해서 귓속말 message 전송
								w.write(message, 0, message.length());
								w.newLine();
								w.flush();
							} catch (IOException io) {
								// TODO Auto-generated catch block
								io.printStackTrace();
							}
						} // SSL Socket을 통해 값 전달.
						else
							server.say(name + " >> " + message);
						s2_message.setText("");
					}
				} catch (RemoteException ex) {
					ex.printStackTrace();
				}
			}
		});

		s2_go.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					server.checkGame(name);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		s2_odd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				s2_odd.setEnabled(false);
				s2_even.setEnabled(false);
				s2_result.setText("\n\n\n다른 플레이어를 기다리는 중입니다...");
				try {
					if(answer == 1)
						server.checkSelect(name,1);
					else
						server.checkSelect(name,0);
				}
				catch(RemoteException e1){
					e1.printStackTrace();
				}
			}
		});
		s2_even.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				s2_odd.setEnabled(false);
				s2_even.setEnabled(false);
				s2_result.setText("\n\n\n다른 플레이어를 기다리는 중입니다...");
				try {
					if(answer == 0)
						server.checkSelect(name,1);
					else
						server.checkSelect(name,0);
				}
				catch(RemoteException e1){
					e1.printStackTrace();
				}
			}
		});
	}

	public void printSocketInfo(SSLSocket s) {
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

	public synchronized void start() throws RemoteException, NotBoundException {
		if (server == null) {
			try {
				server = (Server) Naming.lookup("rmi://" + host + ":9999/mainserver");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			frame1.setVisible(true);

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
		try {
			if (server != null) {
				String message = s2_message.getText();
				if (message.contains("[/w")) { // 전송하려는 메세지가 귓속말이면
					try {
						message = name + message;
						System.out.println(message);
						w.write(message, 0, message.length());
						w.newLine();
						w.flush();
					} catch (IOException io) {
						// TODO Auto-generated catch block
						io.printStackTrace();
					}
				} // SSL Socket을 통해 값 전달.
				else
					server.say(name + " >> " + message);
				s2_message.setText("");
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
		}

	}

	public String getName() throws RemoteException {
		return this.name;
	}

	public void setName() throws RemoteException {
		server.register(this);
	}

	public void said(String m) throws RemoteException {
		s2_output.append(m + "\n");
		scr1.getVerticalScrollBar().setValue(scr2.getVerticalScrollBar().getMaximum());
		scr2.getVerticalScrollBar().setValue(scr2.getVerticalScrollBar().getMaximum());
		s2_message.setText("");
	}

	public void clientCheck() throws RemoteException {
		Vector vec = new Vector();
		try {
			clients = server.clientList();
			vec.clear();
			for (int i = 0; i < clients.size(); i++) {
				vec.addElement(clients.get(i));
			}
			s2_list.setListData(vec);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void onePlayer() throws RemoteException {
		s2_odd.setEnabled(true);
		s2_even.setEnabled(true);
		s2_result.setText("\n\n\n다른 플레이어를 기다리는 중입니다...");
		s2_go.setEnabled(false);
	}

	public void twoPlayer(int answer) throws RemoteException {
		this.answer = answer;
		s2_go.setEnabled(false);
		s2_odd.setEnabled(true);
		s2_even.setEnabled(true);
		s2_result.setText("\n\n\n                 홀,짝 선택해주세요!!");
	}

	public void watchingGame() throws RemoteException {
		s2_result.setText("\n\n\n현재 게임 중입니다.\n게임이 끝날 때까지 기다려 주세요~~");
		s2_go.setEnabled(false);
	}

	public void endGame() throws RemoteException {
		s2_odd.setEnabled(false);
		s2_even.setEnabled(false);
		s2_result.setText("\n\n\n                 게임을 시작하려면\n          '게임 참가' 버튼을 눌러주세요~~~");
		s2_go.setEnabled(true);
	}
}