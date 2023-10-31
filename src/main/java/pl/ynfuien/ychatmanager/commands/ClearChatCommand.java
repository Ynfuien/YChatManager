package pl.ynfuien.ychatmanager.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClearChatCommand implements CommandExecutor, TabCompleter {
    private final YChatManager instance;

    public ClearChatCommand(YChatManager instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int clearLinesCount = instance.getConfig().getInt("commands.clearchat.empty-lines");
        Component clearMessage = Component.text(" \n".repeat(clearLinesCount));

        HashMap<String, Object> placeholders = new HashMap<>();
        placeholders.put("clearchat-prefix", Lang.Message.COMMAND_CLEARCHAT_PREFIX.get());
        placeholders.put("message", Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS_OTHER_ADMIN.get());
        placeholders.put("player", sender.getName());
        placeholders.put("displayname", sender instanceof Player ? ChatFormatter.SERIALIZER.serialize(((Player) sender).displayName()) : sender.getName());

        for (Player p : Bukkit.getOnlinePlayers()) {
            // To players
            if (!p.hasPermission(command.getPermission())) {
                p.sendMessage(clearMessage);
                Lang.Message.COMMAND_CLEARCHAT_INFO_PLAYERS.send(p);

                continue;
            }

            // To admins
            if (p.equals(sender)) {
                placeholders.put("message", Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS_SENDER.get());
                Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS.send(p, placeholders);
                placeholders.put("message", Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS_OTHER_ADMIN.get());
                continue;
            }
            Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS.send(p, placeholders);
        }

        if (sender instanceof Player) {
            Lang.Message.COMMAND_CLEARCHAT_INFO_ADMINS.send(Bukkit.getConsoleSender(), placeholders);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
