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

	// SSL handshaking�� ���� password
	private String mypassword;
	private SSLSocket sslsocketclient;

	// s1 = �α��������� s2 = ä�� �� ����
	protected JPanel s1_panel;
	protected JLabel s1_label;
	protected JButton s1_license_button;
	protected JButton s1_name_button;
	protected JTextField s1_license_input;
	protected TextArea s1_output;
	boolean check = false; // Ŭ���̾�Ʈ ���� Ȯ�� ����
	protected String name; // �г��� ���� ����

	// for GUI
	protected JTextField s2_message; // �޼��� �Է�â
	protected static JTextArea s2_output; // �޼��� ���â
	protected JPanel s2_mainPane;
	protected JPanel s2_messagePane;
	protected JPanel s2_sidePane;
	protected JLabel s2_lab;
	protected JList<Vector> s2_list; // ���� ���
	protected JButton s2_send; // �޼��� ������ ��ư
	protected JButton s2_go; // ���� ���� ��ư
	protected JScrollPane scr1; // ��ũ��
	protected JScrollPane scr2; // ��ũ��
	ArrayList<String> clients; // client ���� List
	boolean s2_check = false; // client�� �����ߴ��� üũ

	// for Game
	protected JPanel s2_gamePane;
	protected JPanel s2_selPane; // Ȧ,¦ ����â
	protected JButton s2_odd; // Ȧ ��ư
	protected JButton s2_even; // ¦ ��ư
	protected JTextArea s2_result; // Ȧ,¦ ��� ���â
	protected Font font;

	protected BufferedWriter w = null;
	protected BufferedReader r = null;

	SSLSocketFactory sslSocketFactory;

	protected int answer;
	protected int mySel;

	public ClientImpl(String host) throws RemoteException {
		this.host = host;

		// swing�� ���� GUI. License ��
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

		// s1 ������ ����
		s1_panel = new JPanel();
		s1_label = new JLabel("���̼���Ű�� �Է��ϼ���.");
		s1_license_button = new JButton("���̼��� Ȯ��");
		s1_name_button = new JButton("�Է�");
		s1_license_input = new JTextField(12);
		s1_output = new TextArea();

		s1_panel.add(s1_label);
		s1_panel.add(s1_license_input);
		s1_panel.add(s1_license_button);

		frame1.add(s1_panel, "North");
		frame1.add(s1_output, "Center");

		// �г� ����
		s2_mainPane = new JPanel();
		s2_mainPane.setLayout(new GridLayout(2, 1));
		s2_sidePane = new JPanel();
		s2_messagePane = new JPanel();

		// �׵θ� ����
		Border lineBorder = BorderFactory.createLineBorder(Color.black, 1);
		Border emptyBorder = BorderFactory.createEmptyBorder();

		// text field �� area ����
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

		// ��ư ����
		s2_odd = new JButton("Ȧ");
		s2_even = new JButton("¦");
		s2_send = new JButton("������");
		s2_go = new JButton("���� ����");

		// ���� GUI ����
		s2_gamePane = new JPanel(new BorderLayout());
		s2_selPane = new JPanel();
		s2_selPane.setLayout(new FlowLayout());
		s2_odd = new JButton("Ȧ");
		s2_even = new JButton("¦");
		s2_result = new JTextArea();
		s2_result.setEditable(false);
		s2_result.setText("\n\n\n                 ������ �����Ϸ���\n          '���� ����' ��ư�� �����ּ���~~~");
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

		// side pane, message pane ����
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
					s1_output.setText("���̼���Ű�� �Է��ϼ���");
				else
					s1_output.setText("���̼����� �����մϴ�");

				try {
					mypassword = server.checkLicense(s1_license_input.getText());
					s1_license_input.setText("");
					if (mypassword == null) {
						s1_output.setText("�߸��� ���̼���Ű�Դϴ�. �ٽ� Ȯ���ϼ���.");
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

						// register ȣ��
						// SSL connection done.
						w = new BufferedWriter(new OutputStreamWriter(sslsocketclient.getOutputStream()));
						s1_license_button.setVisible(false);
						s1_panel.add(s1_name_button);
						s1_label.setText("�г����� �Է��ϼ��� : ");
						s1_output.setText("**********Cd Key �����Ǿ����ϴ�!**********");

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
						s1_output.setText("�г����� �ٽ� �Է����ּ���. �ִ� 12���� �Դϴ�.");
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
							server.say(name + "���� �����Ͽ����ϴ�.");
						} catch (RemoteException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						s2_output.append("*****�ӼӸ� ����*****\n'[/w] ����ID �޼���'�� �Է��ϼ���.\n");
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
						if (message.contains("[/w")) { // �����Ϸ��� �޼����� �ӼӸ��̸�
							try {
								message = name + message;
								// SSL�� ���ؼ� �ӼӸ� message ����
								w.write(message, 0, message.length());
								w.newLine();
								w.flush();
							} catch (IOException io) {
								// TODO Auto-generated catch block
								io.printStackTrace();
							}
						} // SSL Socket�� ���� �� ����.
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
				s2_result.setText("\n\n\n�ٸ� �÷��̾ ��ٸ��� ���Դϴ�...");
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
				s2_result.setText("\n\n\n�ٸ� �÷��̾ ��ٸ��� ���Դϴ�...");
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
				if (message.contains("[/w")) { // �����Ϸ��� �޼����� �ӼӸ��̸�
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
				} // SSL Socket�� ���� �� ����.
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
		s2_result.setText("\n\n\n�ٸ� �÷��̾ ��ٸ��� ���Դϴ�...");
		s2_go.setEnabled(false);
	}

	public void twoPlayer(int answer) throws RemoteException {
		this.answer = answer;
		s2_go.setEnabled(false);
		s2_odd.setEnabled(true);
		s2_even.setEnabled(true);
		s2_result.setText("\n\n\n                 Ȧ,¦ �������ּ���!!");
	}

	public void watchingGame() throws RemoteException {
		s2_result.setText("\n\n\n���� ���� ���Դϴ�.\n������ ���� ������ ��ٷ� �ּ���~~");
		s2_go.setEnabled(false);
	}

	public void endGame() throws RemoteException {
		s2_odd.setEnabled(false);
		s2_even.setEnabled(false);
		s2_result.setText("\n\n\n                 ������ �����Ϸ���\n          '���� ����' ��ư�� �����ּ���~~~");
		s2_go.setEnabled(true);
	}
}