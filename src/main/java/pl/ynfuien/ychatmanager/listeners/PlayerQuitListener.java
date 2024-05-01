package pl.ynfuien.ychatmanager.listeners;

import org.bukkit.Bukkit;
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove nickname from cache
        Player p = event.getPlayer();
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            if (p.isOnline()) return;
            Storage.removeNickFromCache(p.getUniqueId());
        }, 10 * 20);

        // Remove command cooldowns from cache
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            if (p.isOnline()) return;
            commandCooldownsModule.removePlayerFromCache(p.getUniqueId());
        }, 60 * 20);


        //// Format quit message
        ConfigurationSection config = instance.getConfig().getConfigurationSection("quit-message");
        if (!config.getBoolean("change")) return;

        String format = config.getString("format");
        if (format.isEmpty()) {
            event.quitMessage(null);
            return;
        }

        displayNameModule.updateDisplayname(p);
        format = ChatFormatter.parseTemplatePlaceholders(format, ChatFormatter.createPlayerPlaceholders(p));
        format = ColorFormatter.parsePAPI(p, format);

        event.quitMessage(ColorFormatter.SERIALIZER.deserialize(format));
    }
}
