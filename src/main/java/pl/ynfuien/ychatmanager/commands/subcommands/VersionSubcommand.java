package pl.ynfuien.ychatmanager.commands.subcommands;

import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.commands.Subcommand;
import pl.ynfuien.ychatmanager.utils.Lang;

import java.util.HashMap;
import java.util.List;

public class VersionSubcommand implements Subcommand {
    @Override
    public String permission() {
        return "yresizingborders.command.main."+name();
    }

    @Override
    public String name() {
        return "version";
    }

    @Override
    public String description() {
        return Lang.Message.COMMAND_ADMIN_VERSION_DESCRIPTION.get();
    }

    @Override
    public String usage() {
        return null;
    }

    @Override
    public void run(CommandSender sender, Command command, String label, String[] args) {
        PluginMeta info = YChatManager.getInstance().getPluginMeta();

        HashMap<String, Object> placeholders = new HashMap<>() {{
            put("name", info.getName());
            put("version", info.getVersion());
            put("author", info.getAuthors().get(0));
            put("description", info.getDescription());
            put("website", info.getWebsite());
        }};

        Lang.Message.COMMAND_MAIN_VERSION.send(sender, placeholders);
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return null;
    }
}
