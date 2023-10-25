package pl.ynfuien.ychatmanager.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.modules.CommandCooldownsModule;
import pl.ynfuien.ychatmanager.utils.Lang;

public class PlayerCommandPreprocessListener implements Listener {
    private final YChatManager instance;
    private final CommandCooldownsModule commandCooldowns;

    public PlayerCommandPreprocessListener(YChatManager instance) {
        this.instance = instance;
        this.commandCooldowns = instance.getModules().getCommandCooldownsModule();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player p = event.getPlayer();
        if (p.hasPermission("ychatmanager.bypass.command_cooldown")) return;

        String command = event.getMessage();
        if (command.startsWith("/")) command = command.substring(1);

        if (commandCooldowns.checkCooldown(p, command)) return;

        event.setCancelled(true);
        Lang.Message.COMMAND_COOLDOWN.send(p);
    }
}
