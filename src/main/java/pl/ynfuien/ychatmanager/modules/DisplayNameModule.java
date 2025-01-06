package pl.ynfuien.ychatmanager.modules;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;

import java.util.HashMap;

public class DisplayNameModule {
    private boolean enabled;
    private String format;

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        enabled = config.getBoolean("enabled");
        format = config.getString("format");

        return true;
    }

    /**
     * Updates player's display name according to the configuration.
     */
    public void updateDisplayName(Player p) {
        if (!enabled) return;

        HashMap<String, Object> phs = ChatFormatter.createPlayerPlaceholders(p, null, false);

        String format = ChatFormatter.parseTemplatePlaceholders(this.format, phs);
        format = ColorFormatter.parsePAPI(p, format);

        Component displayName = ColorFormatter.SERIALIZER.deserialize(format);
        p.displayName(displayName);
        p.playerListName(displayName);
    }

    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public String getFormat() {
        return format;
    }
}
