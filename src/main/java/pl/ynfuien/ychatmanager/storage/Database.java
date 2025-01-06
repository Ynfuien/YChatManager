package pl.ynfuien.ychatmanager.storage;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public abstract class Database {
    protected HikariDataSource dbSource;
    protected String dbName;
    protected String nicknamesTableName = "ycm_nicknames";


    public abstract boolean setup(ConfigurationSection config);

    public void close() {
        if (dbSource != null) dbSource.close();
    }

    public boolean nickExists(UUID uuid) {
        String query = String.format("SELECT serialized FROM `%s` WHERE uuid=? LIMIT 1", nicknamesTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();

            return resultSet.next();
        } catch (SQLException e) {
            YLogger.warn(String.format("Couldn't retrieve data from table '%s'.", nicknamesTableName));
            e.printStackTrace();
            return false;
        }
    }

    public Nickname getNick(UUID uuid) {
        String query = String.format("SELECT serialized, input FROM `%s` WHERE uuid=? LIMIT 1", nicknamesTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) return new Nickname(resultSet.getString("serialized"), resultSet.getString("input"));
            return null;
        } catch (SQLException e) {
            YLogger.warn(String.format("Couldn't retrieve data from table '%s'.", nicknamesTableName));
            e.printStackTrace();
            return null;
        }
    }

    public boolean setNick(UUID uuid, Nickname nick) {
        String query = String.format("UPDATE `%s` SET serialized=?, input=? WHERE uuid=?", nicknamesTableName);

        if (!nickExists(uuid)) {
            query = String.format("INSERT INTO `%s`(serialized, input, uuid) VALUES(?, ?, ?)", nicknamesTableName);
        }

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nick.serialized());
            stmt.setString(2, nick.input());
            stmt.setString(3, uuid.toString());
            stmt.execute();

        } catch (SQLException e) {
            YLogger.warn(String.format("Couldn't save data to table '%s'.", nicknamesTableName));
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public abstract boolean createNicknamesTable();

    public boolean isSetup() {
        return dbSource != null;
    }
}
