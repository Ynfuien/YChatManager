package pl.ynfuien.ychatmanager.listeners;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;

public class PlayerJoinListener implements Listener {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;

    public PlayerJoinListener(YChatManager instance) {
        this.instance = instance;
        this.displayNameModule = instance.getModules().getDisplaynameModule();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        displayNameModule.updateDisplayName(p);

        // Format join message
        if (event.joinMessage() == null) return;

        ConfigurationSection config = instance.getConfig().getConfigurationSection("join-message");
        if (!config.getBoolean("change")) return;

        String format = config.getString("format");
        if (format.isEmpty()) {
            event.joinMessage(null);
            return;
        }

        format = ChatFormatter.parseTemplatePlaceholders(format, ChatFormatter.createPlayerPlaceholders(p));
        format = ColorFormatter.parsePAPI(p, format);

        event.joinMessage(ColorFormatter.SERIALIZER.deserialize(format));
    }
}
