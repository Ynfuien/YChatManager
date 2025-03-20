package pl.ynfuien.ychatmanager.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;

import java.util.HashMap;

public class PlayerDeathListener implements Listener {
    private final YChatManager instance;

    public PlayerDeathListener(YChatManager instance) {
        this.instance = instance;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        ConfigurationSection config = instance.getConfig().getConfigurationSection("death-message.players");
        if (!config.getBoolean("change")) return;

        // Empty death message
        String format = config.getString("format");
        if (format.isEmpty()) {
            event.deathMessage(null);
            return;
        }

        // Custom message
        Player player = event.getPlayer();
        Component originalMessage = event.deathMessage();
        HashMap<String, Object> placeholders = ChatFormatter.createPlayerPlaceholders(player);
        placeholders.put("original-message", ColorFormatter.SERIALIZER.serialize(originalMessage));
        placeholders.put("original-message-key", "death.attack.genericKill");
        if (originalMessage instanceof TranslatableComponent translatable) {
            placeholders.put("original-message-key", translatable.key());
        }

        format = ChatFormatter.parseTemplatePlaceholders(format, placeholders);
        format = ColorFormatter.parsePAPI(player, format);

        event.deathMessage(ColorFormatter.SERIALIZER.deserialize(format));
    }
}
