package pl.ynfuien.ychatmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.YChatManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ChatModule {
    private final YChatManager instance;

    private boolean formattingEnabled;
    private String formattingFormat;
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
        formattingFormat = config.getString("formatting.format");

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
        String placeholder = String.format("<p-%s>", UUID.randomUUID());

        for (Player p : Bukkit.getOnlinePlayers()) {
            String username = p.getName();
            if (!message.contains(username)) continue;

            usedUsernames.add(username);
            message = message.replaceFirst(username, placeholder);
        }

        message = formatFunction.apply(message);

        for (String username : usedUsernames) {
            message = message.replaceFirst(placeholder, username);
        }

        return message;
    }

    private List<UUID> cooldowns = new ArrayList<>();

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
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            cooldowns.remove(uuid);
        }, messageCooldown);

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
    public boolean isFormattingEnabled() {
        return formattingEnabled;
    }

    public String getFormattingFormat() {
        return formattingFormat;
    }

    public Pattern getAllowedPattern() {
        return allowedPattern;
    }

    public int getMessageCooldown() {
        return messageCooldown;
    }
}
