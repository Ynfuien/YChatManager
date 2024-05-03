package pl.ynfuien.ychatmanager.storage;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ychatmanager.YChatManager;

import java.util.HashMap;
import java.util.UUID;

public class Storage {
    private static YChatManager instance;
    private static Database database;
    private static HashMap<UUID, Nickname> nicknames = new HashMap<>();
    private static NamespacedKey socialSpyKey;

    public static void setup(YChatManager instance) {
        Storage.instance = instance;
        Storage.database = instance.getDatabase();

        socialSpyKey = new NamespacedKey(instance, "socialspy");
    }

    // Nicknames
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

    // Social spy
    public static void setSocialSpy(Player player, boolean enabled) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(socialSpyKey, PersistentDataType.BOOLEAN, enabled);
    }

    public static boolean getSocialSpy(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(socialSpyKey)) return false;

        return pdc.get(socialSpyKey, PersistentDataType.BOOLEAN);
    }


    // Database
    public static Database getDatabase() {
        return database;
    }
}
