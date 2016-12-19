package login;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Login extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Login() {
		setTitle("Chat System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnNewUser = new JButton("New User");
		btnNewUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				NewUser user=new NewUser();
				user.setVisible(true);
				dispose();
			}
		});
		btnNewUser.setBounds(163, 96, 117, 29);
		contentPane.add(btnNewUser);
		
		JButton btnExistingUser = new JButton("Existing User");
		btnExistingUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ExistingUser user=new ExistingUser();
				user.setVisible(true);
				dispose();
			}
		});
		btnExistingUser.setBounds(163, 154, 117, 29);
		contentPane.add(btnExistingUser);
	}
}
