//todo убратьт всю статику !!!

package ru.geekbrains.netty_server;

import java.sql.*;

class DBConnection {
    public static Connection getConnection() {
        return connection;
    }

    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement ps;

    static void connect() {
        System.out.println("DB connection start");
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:/Users/aiiganty/IdeaProjects/ru.geekbrains.cloud_netty_final/server/users.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Connection error");
        }
    }

    static Integer getIdByLoginAndPass(String login, String password) {
        String sql = String.format("SELECT * FROM main " + "WHERE login = '%s' AND password = '%s'", login, password);
        try {
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {
                System.out.println(resultSet.getInt(1) + " " + resultSet.getString(2) + " " +
                        resultSet.getString(3));
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static Integer findLogin(String login) {
        String sql = String.format("SELECT id FROM main " + "WHERE login = '%s'", login);
        try {
            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void registrationByLoginPassAndNick(String login, String password){
        try {
            String query = "INSERT INTO main (login, password) VALUES (?, ?);";
            ps = connection.prepareStatement(query);
            ps.setString(1, login);
            ps.setString(2, password);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
