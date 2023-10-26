package pl.ynfuien.ychatmanager.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.modules.ChatModule;
import pl.ynfuien.ychatmanager.modules.DisplaynameModule;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.utils.Lang;

public class AsyncPlayerChatListener implements Listener {
    private final YChatManager instance;
    private final Modules modules;
    private final DisplaynameModule displaynameModule;
    private final ChatModule chatModule;


    public AsyncPlayerChatListener(YChatManager instance) {
        this.instance = instance;
        this.modules = instance.getModules();
        this.displaynameModule = modules.getDisplaynameModule();
        this.chatModule = modules.getChatModule();
    }

    // Doing anti flood, caps, swear, cooldown etc.
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        displaynameModule.updateDisplayname(p);

        // Check cooldown
        if (!chatModule.checkCooldown(p)) {
            Lang.Message.CHAT_COOLDOWN.send(p);
            e.setCancelled(true);
            return;
        }

        String message = e.getMessage();
        // Check allowed pattern
        if (!chatModule.checkPattern(p, message)) {
            Lang.Message.CHAT_INCORRECT_MESSAGE.send(p);
            e.setCancelled(true);
            return;
        }

        // Anti-flood, caps, swear
        if (!p.hasPermission("ychatmanager.bypass.anti_flood")) message = modules.getAntiFloodModule().apply(message);
        if (!p.hasPermission("ychatmanager.bypass.anti_caps")) message = modules.getAntiCapsModule().apply(message);
        if (!p.hasPermission("ychatmanager.bypass.anti_swear")) message = modules.getAntiSwearModule().apply(p, message);

        if (message == null) {
            e.setCancelled(true);
            return;
        }

        if (message.isBlank()) {
            e.setCancelled(true);
            return;
        }

        e.setMessage(message);
    }
}
