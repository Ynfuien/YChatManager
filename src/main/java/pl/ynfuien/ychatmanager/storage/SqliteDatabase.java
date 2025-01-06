package pl.ynfuien.ychatmanager.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqliteDatabase extends Database {
    @Override
    public boolean setup(ConfigurationSection config) {
        close();

        dbName = config.getString("path");

        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setJdbcUrl(String.format("jdbc:sqlite:%s/%s", YChatManager.getInstance().getDataFolder().getPath(), config.getString("path")));

        try {
            dbSource = new HikariDataSource(dbConfig);
        } catch (Exception e) {
            YLogger.error("Plugin couldn't connect to a database! Please check connection data, because some plugin's functionality requires the database!");
            return false;
        }

        return true;
    }

    @Override
    public boolean createNicknamesTable() {
        String query = String.format("CREATE TABLE IF NOT EXISTS `%s` (uuid TEXT NOT NULL, serialized TEXT, input TEXT, UNIQUE (uuid))", nicknamesTableName);

        try (Connection conn = dbSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.execute();
        } catch (SQLException e) {
            YLogger.error(String.format("Couldn't create table '%s' in database '%s'", nicknamesTableName, dbName));
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
