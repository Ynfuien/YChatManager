package pl.ynfuien.ychatmanager.modules;

import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.hooks.vault.VaultHook;
import pl.ynfuien.ychatmanager.storage.Storage;

import java.util.HashMap;

public class DisplaynameModule {
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
     * Updates player's displayname according to the configuration.
     */
    public void updateDisplayname(Player p) {
        if (!enabled) return;

        HashMap<String, Object> phs = new HashMap<>() {{
            put("username", p.getName());
            put("nick", Storage.getNick(p.getUniqueId()).serialized());
            put("uuid", p.getUniqueId());
        }};

        if (VaultHook.isEnabled()) {
            Chat chat = VaultHook.getChat();
            phs.put("prefix", chat.getPlayerPrefix(p));
            phs.put("suffix", chat.getPlayerSuffix(p));
            phs.put("group", chat.getPrimaryGroup(p));
        }

        String format = ChatFormatter.parseTemplatePlaceholders(this.format, phs);
        format = ChatFormatter.parsePAPI(p, format);
        format = format.replace('§', '&');

        p.displayName(ChatFormatter.SERIALIZER.deserialize(format, StandardTags.defaults()));
    }

    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public String getFormat() {
        return format;
    }
}
