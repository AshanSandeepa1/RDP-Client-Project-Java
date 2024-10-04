import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainForm {
    private JFrame frame;
    private JTextField keyField;
    private JComboBox<String> roleCombo;
    private JButton connectButton;
    private JButton disconnectButton;
    private Host host;
    private Viewer viewer;

    public MainForm() {
        frame = new JFrame("Remote Desktop Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        keyField = new JTextField(10);
        roleCombo = new JComboBox<>(new String[]{"Host", "Viewer"});
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");

        JPanel panel = new JPanel();
        panel.add(new JLabel("Shared Key:"));
        panel.add(keyField);
        panel.add(new JLabel("Role:"));
        panel.add(roleCombo);
        panel.add(connectButton);
        panel.add(disconnectButton);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String sharedKey = keyField.getText();
                String role = (String) roleCombo.getSelectedItem();

                try {
                    if ("Host".equals(role)) {
                        host = new Host(sharedKey);
                        host.startHosting();
                    } else if ("Viewer".equals(role)) {
                        viewer = new Viewer(sharedKey);
                        viewer.startViewing();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        disconnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (host != null) {
                    System.out.println("Host disconnected.");
                } else if (viewer != null) {
                    System.out.println("Viewer disconnected.");
                }
                System.exit(0);
            }
        });

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new MainForm();
    }
}
