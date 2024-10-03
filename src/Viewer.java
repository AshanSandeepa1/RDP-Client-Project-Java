import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Viewer {
    private String sharedKey;
    private String serverIP = "rdpclient.eastus.cloudapp.azure.com";
    private int port = 8080;
    private JFrame frame;
    private JLabel screenLabel;
    private Socket socket;
    private DataOutputStream out;

    public Viewer(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public void startViewing() {
        try {
            frame = new JFrame("Viewer");
            screenLabel = new JLabel();
            frame.getContentPane().add(screenLabel, BorderLayout.CENTER);
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            socket = new Socket(serverIP, port);
            out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeUTF(sharedKey);  // Send the shared key to the relay server
            System.out.println("Shared Key sent: " + sharedKey);

            // Mouse listener for remote control
            screenLabel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    sendMouseMovement(e.getX(), e.getY());
                }
            });

            screenLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    sendMouseClick(e.getButton(), true);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    sendMouseClick(e.getButton(), false);
                }
            });

            // Keyboard listener for remote control
            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    sendKeyPress(e.getKeyCode(), true);
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    sendKeyPress(e.getKeyCode(), false);
                }
            });

            // Thread for receiving images
            new Thread(() -> {
                try {
                    while (true) {
                        int len = in.readInt();  // Get the length of the image
                        if (len <= 0) continue;

                        byte[] imageBytes = new byte[len];
                        in.readFully(imageBytes);  // Read the full image data

                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));  // Convert byte array to BufferedImage

                        if (image != null) {
                            SwingUtilities.invokeLater(() -> {
                                Image scaledImage = image.getScaledInstance(frame.getWidth(), frame.getHeight(), Image.SCALE_SMOOTH);
                                screenLabel.setIcon(new ImageIcon(scaledImage));
                                frame.repaint();
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMouseMovement(int x, int y) {
        try {
            out.writeUTF("MOUSE_MOVE");
            out.writeInt(x);
            out.writeInt(y);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMouseClick(int button, boolean isPressed) {
        try {
            out.writeUTF("MOUSE_CLICK");
            out.writeInt(button);
            out.writeBoolean(isPressed);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendKeyPress(int keyCode, boolean isPressed) {
        try {
            out.writeUTF("KEY_PRESS");
            out.writeInt(keyCode);
            out.writeBoolean(isPressed);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Viewer("your_shared_key").startViewing();
    }
}
