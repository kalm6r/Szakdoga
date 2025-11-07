package ui;

import dao.UserDao;
import dao.jdbc.JdbcUserDao;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private final UserDao userDao = new JdbcUserDao();

    public LoginFrame() {
        setTitle("Bejelentkezés");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 835, 430);
        getContentPane().setLayout(new BorderLayout(0, 0));
        initComponents();
        setLocationRelativeTo(null);
        setResizable(false);   
    }

    private void initComponents() {
        // Bal oldal (űrlap)
        JPanel panelLeft = new JPanel();
        panelLeft.setPreferredSize(new Dimension(400, 10));
        getContentPane().add(panelLeft, BorderLayout.WEST);

        // Cím sáv
        JPanel headerPanel = new JPanel();
        headerPanel.setPreferredSize(new Dimension(10, 100));
        headerPanel.setLayout(null);
        JLabel lblTitle = new JLabel("Bejelentkezés");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setBounds(129, 45, 180, 30);
        headerPanel.add(lblTitle);

        // Űrlap sáv
        JPanel formPanel = new JPanel();
        formPanel.setPreferredSize(new Dimension(10, 250));
        formPanel.setLayout(null);

        // Felhasználónév blokk
        JPanel userBlock = new JPanel();
        userBlock.setBounds(89, 35, 230, 47);
        userBlock.setLayout(null);
        formPanel.add(userBlock);

        JLabel lblUser = new JLabel("Felhasználónév");
        lblUser.setBounds(0, 0, 120, 14);
        userBlock.add(lblUser);

        txtUsername = new JTextField();
        txtUsername.setBounds(0, 17, 230, 30);
        txtUsername.setColumns(10);
        userBlock.add(txtUsername);

        // Jelszó blokk
        JPanel passBlock = new JPanel();
        passBlock.setBounds(89, 113, 230, 48);
        passBlock.setLayout(null);
        formPanel.add(passBlock);

        JLabel lblPass = new JLabel("Jelszó");
        lblPass.setBounds(0, 0, 74, 14);
        passBlock.add(lblPass);

        txtPassword = new JPasswordField();
        txtPassword.setBounds(0, 18, 230, 30);
        passBlock.add(txtPassword);

        // Gombok / link
        JButton btnLogin = new JButton("Belépés");
        btnLogin.setBounds(89, 175, 100, 28);
        formPanel.add(btnLogin);

        JLabel lblForgot = new JLabel("Elfelejtett jelszó");
        lblForgot.setForeground(new Color(0, 0, 204));
        lblForgot.setBounds(220, 181, 120, 16);
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        formPanel.add(lblForgot);

        // Bal oldal elrendezés (GroupLayout)
        GroupLayout gl_left = new GroupLayout(panelLeft);
        gl_left.setHorizontalGroup(
            gl_left.createParallelGroup(Alignment.LEADING)
                  .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
                  .addComponent(formPanel, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
        );
        gl_left.setVerticalGroup(
            gl_left.createParallelGroup(Alignment.LEADING)
                  .addGroup(gl_left.createSequentialGroup()
                        .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(formPanel, GroupLayout.PREFERRED_SIZE, 223, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(66, Short.MAX_VALUE))
        );
        panelLeft.setLayout(gl_left);

        // Jobb oldal (illusztrációs üres panel)
        JPanel panelRight = new JPanel();
        panelRight.setBackground(Color.WHITE);
        panelRight.setPreferredSize(new Dimension(450, 10));
        getContentPane().add(panelRight, BorderLayout.EAST);

        // Események
        btnLogin.addActionListener(e -> onLogin());
        lblForgot.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "Jelszó-visszaállítás: fordulj a rendszergazdához.");
            }
        });
    }

    private void onLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kérlek töltsd ki a mezőket.",
                    "Hiányzó adat", JOptionPane.WARNING_MESSAGE);
            return;
        }

        userDao.authenticate(username, password).ifPresentOrElse(
            user -> {
                // Siker: főablak
            	MainFrame mf = new MainFrame(user);
                mf.setLocationRelativeTo(null);
                mf.setVisible(true);
                dispose();
            },
            () -> JOptionPane.showMessageDialog(this, "Hibás felhasználónév vagy jelszó.",
                    "Sikertelen bejelentkezés", JOptionPane.ERROR_MESSAGE)
        );
    }

    // Opcionális önálló indítás
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
