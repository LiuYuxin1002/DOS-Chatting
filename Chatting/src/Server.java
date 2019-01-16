import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Server {
    public static Map<String, User> userMap = new HashMap<>();//we needn't to sort them

    /**
     * What do we want?
     * A Server for as more client as better.
     * Besides, we need to achieve flowing functions:
     *      1.get one's id and password in the server properties file,
     *      2.store one's chatting info and add these to user form with permit,
     *      3.input "list" then user can get id all of the users,
     *      4.then they can input "#userid#" to change user to chat.
     * @param args
     */
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(25525);
        while(true){
            //get a user
            Socket userClient = serverSocket.accept();//get its id
            System.out.println(userClient.getPort()+" is connected.");
            //get userId
            PrintWriter out = new PrintWriter(userClient.getOutputStream(), true);
            BufferedReader nameBuffer = new BufferedReader(
                    new InputStreamReader(userClient.getInputStream(), StandardCharsets.UTF_8));
            out.println("INPUT YOUR ID---请输入昵称：");
            String name = nameBuffer.readLine();
            //add user to userMap
            User user = new User(userClient, name);
            userMap.put(name, user);

            //informed users he is online
            userMap.forEach((k,v)->{
                try {
                    PrintStream outk = new PrintStream(v.client.getOutputStream(), true);
                    outk.println("***"+name+" IS CONNECTED!***");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {

                }
            });


            //Thread begin
            Thread clientThread = new Thread(user);
            clientThread.start();
        }
    }
}
//question: choosing user function should be taken by whom??

class User implements Runnable{//********remember to change these field to private!!!!!********************
    //local data
    public Socket client;
    public String ip;
    public String myName;
    //friend's data
    public String friendIp;
    public Socket friendSocket;
    public User friend;
    //stream
    PrintWriter out1, out2;
    BufferedReader in1;


    public User(Socket client, String myName){
        this.myName = myName;
        this.client=client;
        this.ip = client.getInetAddress().getHostAddress();
        //so my client and friend's are "client" and "friendSocket" ,respectively.
        friendSocket = this.client;
    }
    @Override
    public void run() {
        try {
            chatting();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * function list:
     * 1.client1 send msg to his friend client2
     * 2.get client
     *
     */
    private void chatting() throws IOException {
        //ask him question or give him a echo
        out1 = new PrintWriter(client.getOutputStream(), true);
        //transport msg
        out2 = new PrintWriter(friendSocket.getOutputStream(), true);
        //get ORDER or MSG
        in1 = new BufferedReader(
                new InputStreamReader(client.getInputStream(),"utf-8"));
        changeFriend();

        String line = null;
        while((line = in1.readLine())!=null){
            if(line.equals("#886")){
                deleteUser();
                break;
            }
            else if(line.equals("#change")){
                changeFriend();
            }
            else if(line.equals("#list")){
                listFirend(myName);
            }
            System.out.println("got a msg!!");//debug
            out2.println(myName +" "+ LocalDate.now().toString()+" : " + line);
        }
    }

    private void deleteUser() throws IOException {
        //tell you friend you are offline
        out2.println(LocalDate.now().toString()+" USER "+ myName+" IS OFFLINE");
        client.close();
        Server.userMap.remove(myName);
    }

    private void changeFriend() throws IOException {
        //ask him which user he want to chat
        out1.println("WHO TO CHAT?");
        listFirend(myName);
        //read from him
        String name = in1.readLine();
        while(!selectFriend(name)){
            if(name.equals("#886")) deleteUser();
            out1.println("CAN NOT FIND YOUR FRIEND OR HE(SHE) IS OFFLINE, PLEASE RETEST!");
            name = in1.readLine();
        }
        out1.println(name+" IS CONNECTED SUCCESS!!!");
    }

    private void listFirend(String name) {
        //only yourself
        if(Server.userMap.size()==1){
            out1.println(" THERE IS ONLY YOURSELF ONLINE! ");
            return;
        }
        //give him a list of user on line:::question: how to traversal HashMap?
        out1.println("USER LIST:\n =============");
        Server.userMap.forEach((k, v)->{
            if(!k.equals(name))
                out1.println(" -> "+k);
        });
    }

    private boolean selectFriend(String fName) throws IOException {
        //get friend
        if((this.friend = Server.userMap.get(fName))==null)
            return false;

        //set friendIp
        friendSocket=friend.client;
        out2 = new PrintWriter(friendSocket.getOutputStream(), true);
        this.friendIp = friend.client.getInetAddress().getHostAddress();
        return true;
    }
}
