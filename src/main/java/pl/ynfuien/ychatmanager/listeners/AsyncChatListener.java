package pl.ynfuien.ychatmanager.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
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

        e.renderer((p, displayName, originalMessage, viewer) -> {
            // If provided message is the same as the message in e.message(), then change e.message() to formatted message.
            // It's here so ChatFormatter.format() won't be used {online-players-count} times
            if (originalMessage.equals(e.message())) e.message(ChatFormatter.format(format, p, originalMessage));
            return e.message();
        });
    }
}
