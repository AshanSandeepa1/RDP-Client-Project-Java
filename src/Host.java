import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;

public class Host {
    private String sharedKey;
    private String serverIP = "rdpclient.eastus.cloudapp.azure.com";  // Change this to your relay server IP
    private int port = 8080;
    private Robot robot;

    public Host(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public void startHosting() {
        try {
            robot = new Robot();
            Socket socket = new Socket(serverIP, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeUTF(sharedKey);  // Send the shared key to the relay server
            System.out.println("Shared Key sent: " + sharedKey);

            // Thread for handling commands (mouse/keyboard input)
            new Thread(() -> {
                try {
                    while (true) {
                        String command = in.readUTF();  // Read command (MOUSE_MOVE, MOUSE_CLICK, KEY_PRESS)

                        if (command.equals("MOUSE_MOVE")) {
                            int x = in.readInt();
                            int y = in.readInt();
                            robot.mouseMove(x, y);  // Move the mouse
                        } else if (command.equals("MOUSE_CLICK")) {
                            int button = in.readInt();
                            boolean isPressed = in.readBoolean();
                            handleMouseClick(button, isPressed);
                        } else if (command.equals("KEY_PRESS")) {
                            int keyCode = in.readInt();
                            boolean isPressed = in.readBoolean();
                            handleKeyPress(keyCode, isPressed);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Continuously capture and send screen images
            while (true) {
                // Capture screen as a BufferedImage
                BufferedImage screenshot = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // Send image length and image data
                out.writeInt(imageBytes.length);
                out.write(imageBytes);
                out.flush();

                Thread.sleep(100);  // Adjust based on network speed
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMouseClick(int button, boolean isPressed) {
        if (isPressed) {
            robot.mousePress(InputEvent.getMaskForButton(button));
        } else {
            robot.mouseRelease(InputEvent.getMaskForButton(button));
        }
    }

    private void handleKeyPress(int keyCode, boolean isPressed) {
        if (isPressed) {
            robot.keyPress(keyCode);
        } else {
            robot.keyRelease(keyCode);
        }
    }

    public static void main(String[] args) {
        new Host("your_shared_key").startHosting();  // Replace with your actual shared key
    }
}
