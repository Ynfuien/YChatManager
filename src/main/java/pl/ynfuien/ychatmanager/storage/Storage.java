package pl.ynfuien.ychatmanager.storage;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ychatmanager.YChatManager;

import java.util.HashMap;
import java.util.UUID;

public class Storage {
    private static YChatManager instance;
    private static Database database;
    private static HashMap<UUID, Nickname> nicknames = new HashMap<>();

    public static void setup(YChatManager instance) {
        Storage.instance = instance;
        Storage.database = instance.getDatabase();
    }

    @NotNull
    public static Nickname getNick(@NotNull UUID uuid) {
        if (nicknames.containsKey(uuid)) return nicknames.get(uuid);

        Nickname nick = database.getNick(uuid);
        Player p = Bukkit.getPlayer(uuid);
        if (nick == null) {
            if (p != null) nick = new Nickname(p.getName(), p.getName());
            else {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (op.hasPlayedBefore()) nick = new Nickname(op.getName(), op.getName());
            }
        }
        if (p != null) nicknames.put(uuid, nick);

        return nick;
    }

    public static void setNick(UUID uuid, Nickname nick) {
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) nicknames.put(uuid, nick);

        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            database.setNick(uuid, nick);
        });
    }

    public static void removeNickFromCache(UUID uuid) {
        nicknames.remove(uuid);
    }

    public static HashMap<UUID, Nickname> getNicknames() {
        return nicknames;
    }

    public static Database getDatabase() {
        return database;
    }
}
