package pl.ynfuien.ychatmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.modules.ChatModule;

public class AsyncChatListener implements Listener {
    private final YChatManager instance;
    private final ChatModule chatModule;
    private final ChatFormatter chatFormatter;

    public AsyncChatListener(YChatManager instance) {
        this.instance = instance;
        this.chatModule = instance.getModules().getChatModule();
        this.chatFormatter = new ChatFormatter(chatModule);
    }

    // Chat formatting
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncChatEvent e) {
        if (!chatModule.isFormattingEnabled()) return;

        // Parse the message
        Component formattedMessage = chatFormatter.format(e.getPlayer(), e.message());
        // Cancel if it's null. Probably player sent just a color format, without any actual message
        if (formattedMessage == null) {
            e.setCancelled(true);
            return;
        }

        //// ChatType.SERVER
        // Cancel and resend messages as the server
        if (chatModule.getFormattingType().equals(ChatModule.ChatType.SERVER)) {
            e.setCancelled(true);

            // Why that instead of Bukkit.broadcast()?
            // Because Folia is broken.
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("bukkit.broadcast.user")) continue;
                p.sendMessage(formattedMessage);
            }
            return;
        }

        //// ChatType.PLAYER
        e.renderer((p, displayName, originalMessage, viewer) -> formattedMessage);
    }
}
