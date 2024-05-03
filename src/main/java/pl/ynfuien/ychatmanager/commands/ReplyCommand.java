package pl.ynfuien.ychatmanager.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.api.event.PrivateMessageSendEvent;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.modules.PrivateMessagesModule;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.HashMap;
import java.util.List;

import static pl.ynfuien.ychatmanager.commands.MessageCommand.lastParticipants;

public class ReplyCommand implements CommandExecutor, TabCompleter {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;
    private final PrivateMessagesModule privateMessagesModule;

    public ReplyCommand(YChatManager instance) {
        this.instance = instance;
        Modules modules = instance.getModules();
        this.displayNameModule = modules.getDisplaynameModule();
        this.privateMessagesModule = modules.getPrivateMessagesModule();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        if (args.length < 1) {
            Lang.Message.COMMAND_REPLY_USAGE.send(sender, placeholders);
            return true;
        }

        CommandSender receiver = lastParticipants.get(sender);
        if (receiver == null) {
            Lang.Message.COMMAND_REPLY_FAIL_NO_RECEIVER.send(sender, placeholders);
            return true;
        }

        // If receiver isn't online or is hidden
        if (receiver instanceof Player) {
            Player p = (Player) receiver;
            if (!p.isOnline() ) {
                Lang.Message.COMMAND_REPLY_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
                return true;
            }

            if (instance.getConfig().getBoolean("command.reply.check-vanished") && sender instanceof Player) {
                if (!sender.hasPermission("ychatmanager.command.message.vanished") && !((Player) sender).canSee(p)) {
                    Lang.Message.COMMAND_REPLY_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
                    return true;
                }
            }
        }


        if (sender instanceof Player) displayNameModule.updateDisplayName((Player) sender);
        if (receiver instanceof Player) displayNameModule.updateDisplayName((Player) receiver);

        lastParticipants.put(receiver, sender);

        String message = String.join(" ", args);

        PrivateMessageSendEvent event = new PrivateMessageSendEvent(sender, receiver, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        PrivateMessagesModule.putPlaceholders(placeholders, receiver, sender, event.getMessage());
        privateMessagesModule.sendMessage(receiver, sender, placeholders);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}
