package server;

import server.handlers.ClientHandler;
import server.handlers.SQLHandler;
import server.services.AuthService;
import server.services.DBAuthService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

public class Server {

    private List<ClientHandler> clients;

    private AuthService authService;

    private SimpleDateFormat simpleDateFormat;

    public Server() {

        clients = new Vector<>();
        simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");

        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключиться к БД");
        }
        authService = new DBAuthService();
        ServerSocket server = null;
        Socket socket;

        final int PORT = 8080;

        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запущен!");

            while (true) {
                socket = server.accept();
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNick(), msg);
        SQLHandler.addMessage(sender.getNick(), "null", msg, simpleDateFormat.format(new Date()));
        for (ClientHandler c:clients) {
            c.sendMsg(message);
        }
    }

    public void personalMessage(ClientHandler sender, String msg, String receiver){

        String message = String.format("%s (to %s) : %s", sender.getNick(), receiver, msg);

        for (ClientHandler c : clients) {
            if(c.getNick().equals(receiver)){
                c.sendMsg(message);
                SQLHandler.addMessage(sender.getNick(),receiver, msg,simpleDateFormat.format(new Date()));
                if (!sender.getNick().equals(receiver)) {
                    sender.sendMsg(message);
                }
                return;
            }
        }
        sender.sendMsg(String.format("Client %s not found", receiver));
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public boolean isLoginAuthorized(String login){
        for (ClientHandler c:clients) {
            if (c.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList(){
        StringBuilder stringBuilder = new StringBuilder("/clientList ");
        for (ClientHandler c:clients) {
            stringBuilder.append(c.getNick()).append(" ");
        }
        String msg = stringBuilder.toString();
        for (ClientHandler c: clients){
            c.sendMsg(msg);
        }
    }

}
