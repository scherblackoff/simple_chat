package server.services;

public interface AuthService {

    String getNicknameByLoginAndPassword(String login, String password);

    boolean registration(String login, String password, String nick);

    boolean changeNick(String oldNickname, String newNickname);
}
