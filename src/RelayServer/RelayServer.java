package RelayServer;
import java.io.*;
import java.net.*;
import java.util.HashMap;


public class RelayServer {
    private static final HashMap<String, ClientHandler> clients = new HashMap<>();


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Relay Server started...");


        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(new ClientHandler(socket)).start();
        }
    }


    static class ClientHandler implements Runnable {
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        private String sharedKey;


        public ClientHandler(Socket socket) {
            this.socket = socket;
        }


        @Override
        public void run() {
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                sharedKey = in.readUTF();  // Read the shared key
                System.out.println("Received Shared Key: " + sharedKey);


                synchronized (clients) {
                    if (clients.containsKey(sharedKey)) {
                        // Pair the clients
                        ClientHandler host = clients.remove(sharedKey);
                        System.out.println("Pairing clients with shared key: " + sharedKey);


                        // Forward data between host and viewer
                        new Thread(new DataForwarder(host.in, out)).start();
                        new Thread(new DataForwarder(in, host.out)).start();
                    } else {
                        clients.put(sharedKey, this);  // Register the client
                        System.out.println("Client registered with shared key: " + sharedKey);
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection error with client: " + e.getMessage());
            }
        }
    }


    static class DataForwarder implements Runnable {
        private DataInputStream in;
        private DataOutputStream out;

        public DataForwarder(DataInputStream in, DataOutputStream out) {
            this.in = in;
            this.out = out;
        }


        @Override
        public void run() {
            try {
                byte[] buffer = new byte[8192];  // Larger buffer size for efficiency
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
            } catch (IOException e) {
                System.out.println("Connection closed.");
            }
        }
    }
}