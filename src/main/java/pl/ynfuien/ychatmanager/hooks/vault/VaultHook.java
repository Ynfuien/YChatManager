package pl.ynfuien.ychatmanager.hooks.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;

public class VaultHook {
    private static boolean enabled;

    private static Economy economy;
    private static Chat chat;
    private static Permission permission;


    public VaultHook() {
        ServicesManager manager = Bukkit.getServer().getServicesManager();

        economy = manager.getRegistration(Economy.class).getProvider();
        chat = manager.getRegistration(Chat.class).getProvider();
        permission = manager.getRegistration(Permission.class).getProvider();

        enabled = true;
    }


    public static boolean isEnabled() {
        return enabled;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static Chat getChat() {
        return chat;
    }

    public static Permission getPermission() {
        return permission;
    }
}
