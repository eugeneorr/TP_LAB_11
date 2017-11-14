package server;

import client.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static String clientName;
    private static final Logger logger = Logger.getLogger(Client.class.toString());


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        print("Enter port to listen: ");
        int port = Client.enterPort(scanner);
        print("Enter your name: ");
        String username = scanner.nextLine();

        try(ServerSocket serverSocket = new ServerSocket(port)) {
            Socket socket = serverSocket.accept();
            println("Started, waiting for connection.");

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            sendMessage(out, username);
            clientName = in.readUTF();
            println("Accepted. You're connected with " + clientName + ".");

            sendMessage(out, "Enter the beginning of the range: ");
            startRange = Integer.parseInt(in.readUTF()) - 2;
            println(Integer.toString(startRange));
            sendMessage(out, "Great! Enter the end of the range: ");
            endRange = Integer.parseInt(in.readUTF()) + 1;
            println(Integer.toString(endRange));

            if (endRange == startRange) {
                println("Incorrect range!");
                return;
            }

            if (endRange < startRange) {
                int tmp = endRange;
                endRange = startRange;
                startRange = tmp;
            }

            sendMessage(out,"Is the number more then " + getMiddle(startRange, endRange, "yes"));

            Thread listenerThread = listenClient(in, out);
            listenerThread.start();

        } catch (IOException e) {
            println("Error: " + e);
        }

    }

    private static int startRange = 0;
    private static int endRange = 0;


    private static Thread listenClient(DataInputStream in, DataOutputStream out) {
        return new Thread(() -> {
            try {
                String message;
                while (!(message = in.readUTF()).equals("@quit")) {
                    if (message.equals("yes")) {
                        startRange = getMiddle(startRange, endRange, message) - 1;
                    } else if (message.equals("no")) {
                        endRange = getMiddle(startRange, endRange, message) + 1;
                    }
                    if (endRange - startRange == 3) {
                        sendMessage(out, "Found: " + (endRange - 1) + "!");
                    }
                    sendMessage(out,
                            "Is the number more then " +
                                     getMiddle(startRange, endRange, message));
                }
                println("The chat is finished with " + clientName + ".");
            } catch (IOException e) {
                println("Lost connection with " + clientName + ": " + e);
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

    private static int getMiddle(int start, int end, String message) {
        int rangeLength =  end - start;
        if (rangeLength == 1) {
            return message.equals("no") ? start : end;
        }
        return end - (rangeLength / 2);
    }

    private static void println(String string) {
        System.out.println(string);
    }
    private static void print(String string) {
        System.out.print(string);
    }

}
