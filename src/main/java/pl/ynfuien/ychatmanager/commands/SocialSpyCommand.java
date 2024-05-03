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
import pl.ynfuien.ychatmanager.storage.Storage;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SocialSpyCommand implements CommandExecutor, TabCompleter {
    private final YChatManager instance;
    private static final String PERMISSION_OTHERS = "ychatmanager.command.socialspy.others";

    public SocialSpyCommand(YChatManager instance) {
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        HashMap<String, Object> placeholders = new HashMap<>() {{ put("command", label); }};

        // Self
        if (args.length < 2 || !sender.hasPermission(PERMISSION_OTHERS)) {
            if (!(sender instanceof Player p)) {
                Lang.Message.COMMAND_SOCIALSPY_USAGE_OTHERS.send(sender, placeholders);
                return true;
            }

            Boolean newState = getToggleState(p, args.length == 0 ? "" : args[0]);
            if (newState == null) {
                Lang.Message.COMMAND_SOCIALSPY_USAGE.send(sender, placeholders);
                if (sender.hasPermission(PERMISSION_OTHERS)) Lang.Message.COMMAND_SOCIALSPY_USAGE_OTHERS.send(sender, placeholders);
                return true;
            }

            Storage.setSocialSpy(p, newState);
            if (newState) {
                Lang.Message.COMMAND_SOCIALSPY_SUCCESS_ENABLE.send(sender, placeholders);
                return true;
            }

            Lang.Message.COMMAND_SOCIALSPY_SUCCESS_DISABLE.send(sender, placeholders);
            return true;
        }


        // Other player
        Player p = Bukkit.getPlayer(args[1]);
        placeholders.put("player", args[1]);
        if (p == null || (sender instanceof Player player && !player.canSee(p))) {
            Lang.Message.COMMAND_SOCIALSPY_FAIL_PLAYER_ISNT_ONLINE.send(sender, placeholders);
            return true;
        }
        placeholders.put("player", p.getName());

        Boolean newState = getToggleState(p, args[0]);
        if (newState == null) {
            Lang.Message.COMMAND_SOCIALSPY_USAGE_OTHERS.send(sender, placeholders);
            return true;
        }


        Storage.setSocialSpy(p, newState);
        if (newState) {
            Lang.Message.COMMAND_SOCIALSPY_SUCCESS_OTHER_ENABLE.send(sender, placeholders);
            return true;
        }

        Lang.Message.COMMAND_SOCIALSPY_SUCCESS_OTHER_DISABLE.send(sender, placeholders);
        return true;
    }

    private Boolean getToggleState(Player p, String argument) {
        if (argument.equalsIgnoreCase("enable")) return true;
        if (argument.equalsIgnoreCase("disable")) return false;

        if (argument.isEmpty()) {
            boolean currentState = Storage.getSocialSpy(p);
            return !currentState;
        }

        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length > 2) return completions;

        String arg1 = args[0].toLowerCase();
        if (args.length == 1) {
            for (String completion : new String[] {"enable", "disable"}) {
                if (completion.startsWith(arg1)) completions.add((completion));
            }

            return completions;
        }

        if (!sender.hasPermission(PERMISSION_OTHERS)) return completions;

        String arg2 = args[1].toLowerCase();
        Player player = sender instanceof Player p ? p : null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (player != null && !player.canSee(p)) continue;

            String name = p.getName();
            if (name.toLowerCase().startsWith(arg2)) completions.add(name);
        }

        return completions;
    }
}
