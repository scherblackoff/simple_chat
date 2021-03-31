package server.handlers;

import java.sql.*;

public class SQLHandler {

    private static Connection connection;

    private static PreparedStatement psGetNickname;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNickname;
    private static PreparedStatement psAddMessage;
    private static PreparedStatement psGetMessageForNick;

    public static boolean connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:users.db");
            prepareAllStatements();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void prepareAllStatements() throws SQLException {
        psGetNickname = connection.prepareStatement("SELECT nickname FROM users WHERE login = ?  AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);");
        psChangeNickname = connection.prepareStatement("UPDATE users SET nickname = ? WHERE nickname = ?;");

        psAddMessage = connection.prepareStatement("INSERT INTO messages (sender, receiver, text, date) VALUES (" +
                "(SELECT id FROM users WHERE nickname = ?), " +
                "(SELECT id FROM users WHERE nickname = ?), " +
                "?, " +
                "? " +
                ");");

        psGetMessageForNick = connection.prepareStatement("SELECT " +
                "(SELECT nickname FROM users WHERE users.id = sender), " +
                "(SELECT nickname FROM users WHERE users.id = receiver), " +
                "text, " +
                "date " +
                "FROM messages " +
                "WHERE " +
                "sender = (SELECT id FROM users WHERE nickname = ?) " +
                "OR " +
                "receiver = (SELECT id FROM users WHERE nickname = ?) " +
                "OR " +
                "receiver IS NULL; ");
    }

    public static String getNicknameByLoginAndPassword(String login, String password) {
        String nick = null;
        try {
            psGetNickname.setString(1, login);
            psGetNickname.setString(2, password);
            ResultSet resultSet = psGetNickname.executeQuery();
            if (resultSet.next()) {
                nick = resultSet.getString(1);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nick;
    }

    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, login);
            psRegistration.setString(2, password);
            psRegistration.setString(3, nickname);
            psRegistration.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changeNick(String oldNick, String newNick){
        try {
            psChangeNickname.setString(1, newNick);
            psChangeNickname.setString(2, oldNick);
            psChangeNickname.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addMessage(String sender, String receiver, String text, String date) {
        try {
            psAddMessage.setString(1, sender);
            psAddMessage.setString(2, receiver);
            psAddMessage.setString(3, text);
            psAddMessage.setString(4, date);
            psAddMessage.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getMessageForNick(String nickname) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            psGetMessageForNick.setString(1, nickname);
            psGetMessageForNick.setString(2, nickname);
            ResultSet resultSet = psGetMessageForNick.executeQuery();
            while (resultSet.next()) {
                String sender = resultSet.getString(1);
                String receiver = resultSet.getString(2);
                String text = resultSet.getString(3);
                String date = resultSet.getString(4);
                if (receiver == null) {
                    stringBuilder.append(String.format("%s : %s\n", sender, text));
                } else {
                    stringBuilder.append(String.format("%s (to %s) : %s\n", sender, receiver, text));
                }
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private static void disconnect() {
        try {
            psChangeNickname.close();
            psGetMessageForNick.close();
            psAddMessage.close();
            psRegistration.close();
            psGetNickname.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
