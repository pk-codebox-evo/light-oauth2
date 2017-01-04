package com.networknt.oauth.cache;

import com.hazelcast.core.MapStore;
import com.networknt.oauth.cache.model.Client;
import com.networknt.oauth.cache.model.User;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by stevehu on 2016-12-27.
 */
public class ClientMapStore implements MapStore<String, Client> {
    private static final Logger logger = LoggerFactory.getLogger(ClientMapStore.class);
    private static final DataSource ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
    private static final String insert = "INSERT INTO clients (client_id, client_secret, client_type, client_name, client_desc, scope, redirect_url, owner_id, create_dt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String delete = "DELETE FROM clients WHERE client_id = ?";
    private static final String select = "SELECT * FROM clients WHERE client_id = ?";
    private static final String update = "UPDATE clients SET client_type=?, client_name=?, client_desc=?, scope=?, redirect_url=?, owner_id=?, update_dt=? WHERE client_id=?";
    private static final String loadall = "SELECT client_id FROM clients";

    @Override
    public synchronized void delete(String key) {
        if(logger.isDebugEnabled()) logger.debug("Delete:" + key);
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(delete)) {
            stmt.setString(1, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
    }
    @Override
    public synchronized void store(String key, Client client) {
        if(logger.isDebugEnabled()) logger.debug("Store:"  + key);
        if(load(key) == null) {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(insert)) {
                stmt.setString(1, client.getClientId());
                stmt.setString(2, client.getClientSecret());
                stmt.setString(3, client.getClientType().toString());
                stmt.setString(4, client.getClientName());
                stmt.setString(5, client.getClientDesc());
                stmt.setString(6, client.getScope());
                stmt.setString(7, client.getRedirectUrl());
                stmt.setString(8, client.getOwnerId());
                stmt.setDate(9, new Date(System.currentTimeMillis()));
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        } else {
            try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(update)) {
                stmt.setString(1, client.getClientType().toString());
                stmt.setString(2, client.getClientName());
                stmt.setString(3, client.getClientDesc());
                stmt.setString(4, client.getScope());
                stmt.setString(5, client.getRedirectUrl());
                stmt.setString(6, client.getOwnerId());
                stmt.setDate(7, new Date(System.currentTimeMillis()));
                stmt.setString(8, client.getClientId());
                stmt.executeUpdate();
            } catch (SQLException e) {
                logger.error("Exception:", e);
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public synchronized void storeAll(Map<String, Client> map) {
        for (Map.Entry<String, Client> entry : map.entrySet())
            store(entry.getKey(), entry.getValue());
    }
    @Override
    public synchronized void deleteAll(Collection<String> keys) {
        keys.forEach(this::delete);
    }
    @Override
    public synchronized Client load(String key) {
        if(logger.isDebugEnabled()) logger.debug("Load:"  + key);
        Client client = null;
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(select)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client();
                    client.setClientId(key);
                    client.setClientSecret(rs.getString("client_secret"));
                    client.setClientType(Client.ClientTypeEnum.fromValue(rs.getString("client_type")));
                    client.setClientName(rs.getString("client_name"));
                    client.setClientDesc(rs.getString("client_desc"));
                    client.setScope(rs.getString("scope"));
                    client.setRedirectUrl(rs.getString("redirect_url"));
                    client.setOwnerId(rs.getString("owner_id"));
                    client.setCreateDt(rs.getDate("create_dt"));
                    client.setUpdateDt(rs.getDate("update_dt"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return client;
    }
    @Override
    public synchronized Map<String, Client> loadAll(Collection<String> keys) {
        Map<String, Client> result = new HashMap<>();
        for (String key : keys) result.put(key, load(key));
        return result;
    }
    @Override
    public Iterable<String> loadAllKeys() {
        if(logger.isDebugEnabled()) logger.debug("loadAllKeys is called");
        List<String> keys = new ArrayList<>();
        try (Connection connection = ds.getConnection(); PreparedStatement stmt = connection.prepareStatement(loadall)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keys.add(rs.getString("client_id"));
                }
            }
        } catch (SQLException e) {
            logger.error("Exception:", e);
            throw new RuntimeException(e);
        }
        return keys;
    }

}