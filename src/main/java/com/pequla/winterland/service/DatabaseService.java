package com.pequla.winterland.service;

import com.pequla.winterland.model.RoleModel;

import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseService {

    private final Connection connection;

    public DatabaseService(String host, String db, String user, String password) throws ClassNotFoundException, SQLException {
        String myUrl = String.format("jdbc:mysql://%s/%s", host, db);
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(myUrl, user, password);
    }

    public List<RoleModel> getAllRoles() throws SQLException {
        String query = "SELECT * FROM role";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        List<RoleModel> roles = new ArrayList<>();
        while (rs.next()) {
            roles.add(RoleModel.builder()
                    .id(rs.getInt("role_id"))
                    .discordId(rs.getString("discord_id"))
                    .name(rs.getString("name"))
                    .group(rs.getString("group"))
                    .build());
        }
        return roles;
    }

    public String getConfigByKey(String key) throws SQLException {
        String query = String.format("SELECT * FROM config WHERE name = '%s'", key);
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);
        String result = null;
        while (rs.next()) {
            result = rs.getString("value");
        }
        return result;
    }

    public void close() throws SQLException {
        connection.close();
    }
}
