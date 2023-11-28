package pl.ynfuien.ychatmanager.listeners;

import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;

public class PlayerJoinListener implements Listener {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;

    public PlayerJoinListener(YChatManager instance) {
        this.instance = instance;
        this.displayNameModule = instance.getModules().getDisplaynameModule();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Format join message
        ConfigurationSection config = instance.getConfig().getConfigurationSection("join-message");
        if (!config.getBoolean("change")) return;

        String format = config.getString("format");
        if (format.length() == 0) {
            event.joinMessage(null);
            return;
        }

        Player p = event.getPlayer();
        displayNameModule.updateDisplayname(p);
        format = ChatFormatter.parseTemplatePlaceholders(format, ChatFormatter.createPlayerPlaceholders(p));
        format = ChatFormatter.parsePAPI(p, format);
        format = format.replace('§', '&');

        event.joinMessage(ChatFormatter.SERIALIZER.deserialize(format, StandardTags.defaults()));
    }
}
