package pl.ynfuien.ychatmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.modules.ChatModule;

public class AsyncChatListener implements Listener {
    private final YChatManager instance;
    private final ChatModule chatModule;

    public AsyncChatListener(YChatManager instance) {
        this.instance = instance;
        this.chatModule = instance.getModules().getChatModule();
    }

    // Chat formatting
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncChatEvent e) {
        if (!chatModule.isFormattingEnabled()) return;

        // Parse the message
        String format = chatModule.getFormattingFormat();
        Component formattedMessage = ChatFormatter.format(format, e.getPlayer(), e.message(), chatModule.isPlayerFormats());
        // Cancel if it's null. Probably player sent just a color format, without any actual message
        if (formattedMessage == null) {
            e.setCancelled(true);
            return;
        }

        //// ChatType.SERVER
        // Cancel and resend messages as the server
        if (chatModule.getFormattingType().equals(ChatModule.ChatType.SERVER)) {
            e.setCancelled(true);

            Bukkit.broadcast(formattedMessage);
            return;
        }

        //// ChatType.PLAYER
        e.renderer((p, displayName, originalMessage, viewer) -> formattedMessage);
    }
}
