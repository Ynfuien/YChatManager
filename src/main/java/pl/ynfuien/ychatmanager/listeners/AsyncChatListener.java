package pl.ynfuien.ychatmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
        String format = chatModule.getFormattingFormat();

        Component formattedMessage = ChatFormatter.format(format, e.getPlayer(), e.message());
        if (formattedMessage == null) {
            e.setCancelled(true);
            return;
        }

        e.renderer((p, displayName, originalMessage, viewer) -> formattedMessage);
    }
}
