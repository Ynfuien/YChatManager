package pl.ynfuien.ychatmanager.api;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.storage.Nickname;
import pl.ynfuien.ychatmanager.storage.Storage;

import java.util.UUID;

public class YChatManagerAPI {

    /**
     * Gets the modules instance of the plugin.
     * It's created at plugin start, and maintained
     * even after /ycm reload. So you only need to get
     * it once.
     * <p></p>
     * <p>It also applies to each module.
     * Get it once, and it will be the same,
     * till plugin's disabled.</p>
     * @return Modules instance
     */
    public static Modules getModules() {
        return YChatManager.getInstance().getModules();
    }

    /**
     * Gets nickname record of the provided player.
     * Returns null if provided player was never on the server.
     * @param player Player
     * @return Player's nickname record
     * @throws IllegalArgumentException In case of null arguments
     */
    @Nullable
    public static Nickname getNickname(@NotNull OfflinePlayer player) throws IllegalArgumentException {
        Preconditions.checkArgument(player != null, "Player cannot be null!");
        if (!player.hasPlayedBefore() && !player.isOnline()) return null;

        return getNickname(player.getUniqueId());
    }
    /**
     * Gets nickname record by player's uuid.
     * Returns null if player was never on the server.
     * @param uuid Player's uuid
     * @return Player's nickname record
     * @throws IllegalArgumentException In case of null arguments
     */
    @Nullable
    public static Nickname getNickname(@NotNull UUID uuid) throws IllegalArgumentException {
        Preconditions.checkArgument(uuid != null, "Uuid cannot be null!");
        return Storage.getNick(uuid);
    }

    /**
     * Changes player's nickname.
     * <p></p>
     * <p>Setting player's nickname, keep in mind that:</p>
     * <p>- serialized field has to be a MiniMessage serialized string</p>
     * <p>- input field will be used in /nick tab completion,
     * so it should be a string, that actually represents a formatted nickname</p>
     * <p></p>
     * <p><b>Every time this method is used, it saves new nickname in the database.</b></p>
     * @param player Nickname's new owner
     * @param nickname New nickname to be set
     * @throws IllegalArgumentException In case of null arguments
     */
    public static void setNickname(@NotNull OfflinePlayer player, @NotNull Nickname nickname) throws IllegalArgumentException {
        Preconditions.checkArgument(player != null, "Player cannot be null!");

        setNickname(player.getUniqueId(), nickname);
    }

    /**
     * Changes player's nickname by his uuid.
     * <p></p>
     * <p>Setting player's nickname, keep in mind that:</p>
     * <p>- serialized field has to be a MiniMessage serialized string</p>
     * <p>- input field will be used in /nick tab completion,
     * so it should be a string, that actually represents a formatted nickname</p>
     * <p></p>
     * <p><b>Every time this method is used, it saves new nickname in the database.</b></p>
     * @param uuid Player's uuid
     * @param nickname New nickname to be set
     * @throws IllegalArgumentException In case of null arguments
     */
    public static void setNickname(@NotNull UUID uuid, @NotNull Nickname nickname) throws IllegalArgumentException {
        Preconditions.checkArgument(uuid != null, "Uuid cannot be null!");
        Preconditions.checkArgument(nickname != null, "Nickname cannot be null!");
        Preconditions.checkArgument(nickname.input() != null, "Nickname.input() cannot be null!");
        Preconditions.checkArgument(nickname.serialized() != null, "Nickname.serialized() cannot be null!");

        Storage.setNick(uuid, nickname);
    }

    /**
     * Gets social spy state of a player.
     * @param player Player
     * @return True or false
     * @throws IllegalArgumentException In case of null argument
     */
    public static boolean getSocialSpyState(@NotNull Player player) throws IllegalArgumentException {
        Preconditions.checkArgument(player != null, "Player cannot be null!");

        return Storage.getSocialSpy(player);
    }

    /**
     * Changes social spy state of a player.
     * @param player Player
     * @param state New state
     * @throws IllegalArgumentException In case of null arguments
     */
    public static void setSocialSpyState(@NotNull Player player, boolean state) throws IllegalArgumentException {
        Preconditions.checkArgument(player != null, "Player cannot be null!");

        Storage.setSocialSpy(player, state);
    }

    /**
     * Updates player's displayname.
     */
    public static void updateDisplayname(Player player) {
        getModules().getDisplaynameModule().updateDisplayname(player);
    }
}
