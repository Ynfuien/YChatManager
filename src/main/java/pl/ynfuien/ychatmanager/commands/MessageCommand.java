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
import pl.ynfuien.ychatmanager.modules.DisplaynameModule;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.modules.PrivateMessagesModule;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MessageCommand implements CommandExecutor, TabCompleter {
    private static YChatManager instance = null;
    private final DisplaynameModule displaynameModule;
    private final PrivateMessagesModule privateMessagesModule;
    public static final HashMap<CommandSender, CommandSender> lastParticipants = new HashMap<>();

    public MessageCommand(YChatManager instance) {
        MessageCommand.instance = instance;
        Modules modules = instance.getModules();
        this.displaynameModule = modules.getDisplaynameModule();
        this.privateMessagesModule = modules.getPrivateMessagesModule();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        if (args.length < 2) {
            Lang.Message.COMMAND_MESSAGE_USAGE.send(sender, placeholders);
            return true;
        }

        String player = args[0];
        placeholders.put("player", player);
        Player p = Bukkit.getPlayer(player);
        CommandSender receiver = p;
        if (p == null) {
            if (!player.equalsIgnoreCase("console")) {
                Lang.Message.COMMAND_MESSAGE_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
                return true;
            }

            receiver = Bukkit.getConsoleSender();
        } else if (sender instanceof Player) {
            if (!sender.hasPermission("ychatmanager.command.message.vanished") && !((Player) sender).canSee(p)) {
                Lang.Message.COMMAND_MESSAGE_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
                return true;
            }
        }

        if (sender instanceof Player) displaynameModule.updateDisplayname((Player) sender);
        if (receiver instanceof Player) displaynameModule.updateDisplayname((Player) receiver);

        lastParticipants.put(sender, receiver);
        // Setting player that sent message, as a last participant, after 15 ticks.
        // It's so that if receiver uses /r command at the same time, that player used /msg,
        // /r message will still go to previous last participant
        CommandSender previousParticipant = lastParticipants.get(receiver);
        CommandSender finalReceiver = receiver;
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            if (previousParticipant != null && !previousParticipant.equals(lastParticipants.get(finalReceiver))) return;

            lastParticipants.put(finalReceiver, sender);
        }, 15);

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        PrivateMessageSendEvent event = new PrivateMessageSendEvent(sender, receiver, message);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return true;

        PrivateMessagesModule.putPlaceholders(placeholders, receiver, sender, event.getMessage());
        privateMessagesModule.sendMessage(receiver, sender, placeholders);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 1) return null;
        List<String> completions = new ArrayList<>();

        Player senderPlayer = sender instanceof Player ? (Player) sender : null;
        boolean checkForVanished = !sender.hasPermission("ychatmanager.command.message.vanished") && senderPlayer != null;

        String arg1 = args[0].toLowerCase();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (checkForVanished && !senderPlayer.canSee(p)) continue;

            String name = p.getName();
            if (name.toLowerCase().startsWith(arg1)) completions.add(name);
        }

        return completions;
    }
}
