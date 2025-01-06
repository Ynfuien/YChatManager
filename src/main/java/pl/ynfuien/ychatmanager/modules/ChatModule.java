package pl.ynfuien.ychatmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ChatModule {
    private final YChatManager instance;
    public final static String PLAYER_PLACEHOLDER = String.format("<p-%s>", UUID.randomUUID());


    private boolean formattingEnabled;
    private ChatType formattingType;
    private String formattingFormat;
    private boolean playerFormats;
    private Pattern allowedPattern;
    private int messageCooldown;

    public ChatModule(YChatManager instance) {
        this.instance = instance;
    }

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        formattingEnabled = config.getBoolean("formatting.enabled");

        String type = config.getString("formatting.type");
        if (type.equalsIgnoreCase("player")) formattingType = ChatType.PLAYER;
        else if (type.equalsIgnoreCase("server")) formattingType = ChatType.SERVER;
        else {
            YLogger.error(String.format("[Config] [Chat] Provided incorrect value '%s' for the field 'type'. For now will be used type 'player'.", type));
            formattingType = ChatType.PLAYER;
        }

        formattingFormat = config.getString("formatting.format");
        playerFormats = config.getBoolean("formatting.player-formats");

        String pattern = config.getString("allowed-pattern");
        allowedPattern = pattern.length() > 0 ? Pattern.compile(pattern) : null;

        messageCooldown = config.getInt("message-cooldown");

        return true;
    }

    /**
     * Method used in anti-modules for checking for players usernames.
     * It replaces usernames with a placeholder, applies anti-module, and switches placeholders back to usernames.
     * @param formatFunction Anti-module method to use
     * @return Message with applied anti-module method, and usernames not touched.
     */
    public static String checkUsernames(String message, Function<String, String> formatFunction) {
        List<String> usedUsernames = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            String username = p.getName();
            if (!message.contains(username)) continue;
            if (!Pattern.compile(String.format("\\b%s\\b", username)).matcher(message).find()) continue;

            usedUsernames.add(username);
            message = message.replaceFirst(username, PLAYER_PLACEHOLDER);
        }

        message = formatFunction.apply(message);

        for (String username : usedUsernames) {
            message = message.replaceFirst(PLAYER_PLACEHOLDER, username);
        }

        return message;
    }

    private final List<UUID> cooldowns = new ArrayList<>();

    /**
     * Checks message cooldown for a provided player.
     * @return True if player can send a message now
     */
    public boolean checkCooldown(Player p) {
        if (messageCooldown <= 0) return true;
        if (p.hasPermission("ychatmanager.bypass.chat_cooldown")) return true;

        UUID uuid = p.getUniqueId();
        if (cooldowns.contains(uuid)) return false;

        cooldowns.add(uuid);
        Bukkit.getAsyncScheduler().runDelayed(instance, (task) -> {
            synchronized (cooldowns) {
                cooldowns.remove(uuid);
            }
        }, (long) messageCooldown * 50, TimeUnit.MILLISECONDS);

        return true;
    }

    /**
     * Checks whether provided message matches an allowed-pattern.
     * @return True if message matches the pattern
     */
    public boolean checkPattern(Player p, String message) {
        if (p.hasPermission("ychatmanager.bypass.chat_pattern")) return true;
        if (allowedPattern == null) return true;

        return message.matches(allowedPattern.pattern());
    }

    // Getters
    public ChatType getFormattingType() {
        return formattingType;
    }

    public boolean isFormattingEnabled() {
        return formattingEnabled;
    }

    public String getFormattingFormat() {
        return formattingFormat;
    }
    public boolean isPlayerFormats() {
        return playerFormats;
    }

    public Pattern getAllowedPattern() {
        return allowedPattern;
    }

    public int getMessageCooldown() {
        return messageCooldown;
    }

    public enum ChatType {
        PLAYER,
        SERVER;
    }
}
