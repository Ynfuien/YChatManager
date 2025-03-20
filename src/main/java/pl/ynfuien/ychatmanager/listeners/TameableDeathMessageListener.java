package pl.ynfuien.ychatmanager.listeners;

import io.papermc.paper.event.entity.TameableDeathMessageEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ydevlib.messages.colors.ColorFormatter;
import pl.ynfuien.ydevlib.utils.CommonPlaceholders;

import java.util.HashMap;
import java.util.UUID;

public class TameableDeathMessageListener implements Listener {
    private final YChatManager instance;

    public TameableDeathMessageListener(YChatManager instance) {
        this.instance = instance;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onTameableDeathMessage(TameableDeathMessageEvent event) {
        ConfigurationSection config = instance.getConfig().getConfigurationSection("death-message.tamed-pets");
        if (!config.getBoolean("change")) return;

        // Check and get the owner
        Entity entity = event.getEntity();
        UUID ownerUuid = ((Tameable) entity).getOwnerUniqueId();
        if (ownerUuid == null) return;

        Player owner = Bukkit.getPlayer(ownerUuid);
        if (owner == null) return;

        // Empty death message
        String format = config.getString("format");
        if (format.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        // Custom message
        Component originalMessage = event.deathMessage();
        HashMap<String, Object> placeholders = ChatFormatter.createPlayerPlaceholders(owner, "owner");
        CommonPlaceholders.setEntity(placeholders, entity, "pet");
        placeholders.put("original-message", ColorFormatter.SERIALIZER.serialize(originalMessage));
        placeholders.put("original-message-key", "death.attack.genericKill");
        if (originalMessage instanceof TranslatableComponent translatable) {
            placeholders.put("original-message-key", translatable.key());
        }

        format = ChatFormatter.parseTemplatePlaceholders(format, placeholders);
        format = ColorFormatter.parsePAPI(owner, format);

        event.deathMessage(ColorFormatter.SERIALIZER.deserialize(format));
    }
}
