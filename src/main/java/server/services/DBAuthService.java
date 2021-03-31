package server.services;

import server.handlers.SQLHandler;

public class DBAuthService implements AuthService{
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return SQLHandler.getNicknameByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nick) {
        return SQLHandler.registration(login, password, nick);
    }

    @Override
    public boolean changeNick(String oldNickname, String newNickname) {
        return changeNick(oldNickname, newNickname);
    }
}
