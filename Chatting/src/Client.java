import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    static String ServerIP = "localhost";
    static Socket client;

    public static void main(String[] args) throws IOException {
        Socket client = new Socket(ServerIP,25525);
        Thread recv = new Thread(new RecvThread(client));
        Thread send = new Thread(new SendThread(client));
        recv.start();
        send.start();
    }
}

class RecvThread implements Runnable{
    Socket client;
    //msg read from server(your friends)
    BufferedReader inServer;

    public RecvThread(Socket client) throws IOException {
        this.client = client;
        inServer = new BufferedReader(
                new InputStreamReader(client.getInputStream(),StandardCharsets.UTF_8));
    }
    @Override
    public void run() {
        try {
            receiving();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiving() throws IOException {
        String line = null;
        while((line = inServer.readLine())!= null){
            System.out.println(line);
        }
    }
}

class SendThread implements Runnable{
    PrintWriter out;
    BufferedReader inTerminal;
    Socket client;

    public SendThread(Socket client) throws IOException {
        this.client = client;
        out = new PrintWriter(client.getOutputStream(), true);
        //msg read from terminal
        inTerminal = new BufferedReader(new InputStreamReader(System.in));
    }
    @Override
    public void run() {
        try {
            sending();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sending() throws IOException {
        //read from terminal
        String line = null;
        while((line=inTerminal.readLine())!=null){
            out.println(line);
            if(line.equals("#886")) break;
        }

        out.close();
        client.close();
    }
}
