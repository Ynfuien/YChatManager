package pl.ynfuien.ychatmanager.listeners;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.chat.ColorFormatter;
import pl.ynfuien.ychatmanager.modules.CommandCooldownsModule;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.storage.Storage;

public class PlayerQuitListener implements Listener {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;
    private final CommandCooldownsModule commandCooldownsModule;

    public PlayerQuitListener(YChatManager instance) {
        this.instance = instance;
        Modules modules = instance.getModules();
        this.displayNameModule = modules.getDisplaynameModule();
        this.commandCooldownsModule = modules.getCommandCooldownsModule();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove nickname from cache
        Player p = event.getPlayer();
        Storage.removeNickFromCache(p.getUniqueId());

        // Remove command cooldowns from cache
        commandCooldownsModule.removePlayerFromCache(p.getUniqueId());


        //// Format quit message
        if (event.quitMessage() == null) return;

        ConfigurationSection config = instance.getConfig().getConfigurationSection("quit-message");
        if (!config.getBoolean("change")) return;

        String format = config.getString("format");
        if (format.isEmpty()) {
            event.quitMessage(null);
            return;
        }

        displayNameModule.updateDisplayName(p);
        format = ChatFormatter.parseTemplatePlaceholders(format, ChatFormatter.createPlayerPlaceholders(p));
        format = ColorFormatter.parsePAPI(p, format);

        event.quitMessage(ColorFormatter.SERIALIZER.deserialize(format));
    }
}
