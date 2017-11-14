package client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {

    private static String serverName;
    private static final Logger logger = Logger.getLogger(Client.class.toString());

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        print("Enter port to connect: ");
        int port = enterPort(scanner);
        print("Enter your name: ");
        String username = scanner.nextLine();


        try (Socket socket = new Socket(InetAddress.getLocalHost(), port);) {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            sendMessage(out, username);
            serverName = in.readUTF();
            println("You're connected with " + serverName + ".");

            Thread listenerThread = listenServer(in);
            listenerThread.start();

            String message;
            while (true) {
                message = scanner.nextLine();
                sendMessage(out, message);
                if (message.equals("@quit")) {
                    println("The chat is finished.");
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            println("Failed to create socket: " + e);
        }
    }

    private static Thread listenServer(DataInputStream in) {
        return new Thread(() -> {
            try {
                String message;
                while (!(message = in.readUTF()).equals("@quit")) {
                    println(serverName + ": " + message);
                }
                println("The chat is finished with " + serverName + ".");
            } catch (IOException e) {
                println("Lost connection with " + serverName + ": " + e);
            }
        });
    }

    private static void sendMessage(DataOutputStream out, String message) {
        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            println("Failed to send message: " + e);
        }
    }

    public static int enterPort(Scanner scanner) {
        boolean isSuccessEnteredPort = false;
        int port = 0;
        while(!isSuccessEnteredPort) {
            try {
                port = Integer.parseInt(scanner.nextLine());
                isSuccessEnteredPort = true;
            } catch (RuntimeException e) {
                print("Incorrect value. Try again: ");
            }
        }
        return port;
    }

    private static void println(String string) {
        System.out.println(string);
    }
    private static void print(String string) {
        System.out.print(string);
    }

}
