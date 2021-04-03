package server.handlers;

import server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nick;
    private String login;


    public ClientHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            server.getExecutorService().execute(()-> {
                try {
                    while (true) {
                        String str = in.readUTF();
                        System.out.println(str);
                        if (str.equals("/end")){
                            sendMsg("/end");
                        }
                        if (str.startsWith("/reg ")){
                            String[] token = str.split("\\s");
                            if (token.length < 4){
                                continue;
                            }
                            boolean succeed = server.
                                    getAuthService().
                                    registration(token[1], token[2], token[3]);
                            if (succeed){
                                sendMsg("Регистрация прошла успешно");
                            }else{
                                sendMsg("Регистрация не удалась");
                            }
                        }
                        if (str.startsWith("/auth")) {
                            String[] token = str.split("\\s");
                            if (token.length < 2) {
                                continue;
                            }
                            String newNick = server.getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            login = token[1];
                            System.out.println(newNick);
                            if (newNick != null) {
                                if (!server.isLoginAuthorized(login)){
                                    sendMsg("/authOK " + newNick);
                                    nick = newNick;

                                    server.subscribe(this);
                                    System.out.println("клиент " + nick + " подключился");

                                    sendMsg(SQLHandler.getMessageForNick(nick));
                                    break;
                                }else{
                                    sendMsg("С данным логином аунтификация уже была пройдена");
                                }
                            }else {
                                sendMsg("Неверный логин или пароль");
                            }
                        }
                    }
                    while (true) {
                        String str = in.readUTF();
                        System.out.println(str);

                        if (str.startsWith("/")){
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                server.unsubscribe(this);
                                System.out.println("Клиент отключился");
                                break;
                            }
                            if (str.startsWith("/w")){
                                System.out.println(str);
                                String[] msg = str.split("\\s", 3);
                                if (msg.length < 3){
                                    continue;
                                }
                                server.personalMessage(this, msg[2], msg[1]);
                            }
                            if (str.startsWith("/chnick ")) {
                                String[] token = str.split("\\s", 2);
                                if (token.length < 2) {
                                    continue;
                                }
                                if (token[1].contains(" ")) {
                                    sendMsg("Ник не может содержать пробелов");
                                    continue;
                                }
                                if (server.getAuthService().changeNick(this.nick, token[1])) {
                                    sendMsg("/yournickis " + token[1]);
                                    sendMsg("Ваш ник изменен на " + token[1]);
                                    this.nick = token[1];
                                    server.broadcastClientList();
                                } else {
                                    sendMsg("Не удалось изменить ник. Ник " + token[1] + " уже существует");
                                }
                            }

                        }else {
                            server.broadcastMsg(this, str);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        server.unsubscribe(this);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }


    public String getNick() {
        return nick;
    }
}
