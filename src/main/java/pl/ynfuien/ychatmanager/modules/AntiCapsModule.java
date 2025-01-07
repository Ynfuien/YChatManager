package pl.ynfuien.ychatmanager.modules;

import org.bukkit.configuration.ConfigurationSection;

public class AntiCapsModule {
    private boolean enabled;
    private int maxChars;
    private boolean checkUsernames;

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        enabled = config.getBoolean("enabled");
        maxChars = config.getInt("max-chars");
        checkUsernames = config.getBoolean("check-usernames");

        return true;
    }

    /**
     * Applies anti capslock to a provided message
     * @return Message with lowered characters if needed
     */
    public String apply(String message) {
        if (!enabled) return message;
        if (maxChars < 0) return message;

        if (checkUsernames) return ChatModule.checkUsernames(message, this::shortenCaps);

        return shortenCaps(message);
    }

    private String shortenCaps(String message) {
        if (maxChars == 0) return message.toLowerCase();

        char[] chars = message.toCharArray();
        int upperCase = 0;

        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            char lowerCase = Character.toLowerCase(ch);
            if (ch == lowerCase) continue;
            upperCase++;

            if (upperCase > maxChars) chars[i] = lowerCase;
        }

        return String.valueOf(chars);
    }

    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxChars() {
        return maxChars;
    }

    public boolean isCheckUsernames() {
        return checkUsernames;
    }
}
