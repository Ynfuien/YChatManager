package pl.ynfuien.ychatmanager.hooks.vault;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;

import java.util.Objects;

public class VaultHook {
    private static boolean enabled;

    private static Economy economy = null;
    private static Chat chat = null;
    private static Permission permission = null;


    public VaultHook() {
        ServicesManager manager = Bukkit.getServer().getServicesManager();

        RegisteredServiceProvider<Economy> economyRsp = manager.getRegistration(Economy.class);
        if (economyRsp != null) economy = economyRsp.getProvider();

        RegisteredServiceProvider<Chat> chatRsp = manager.getRegistration(Chat.class);
        if (chatRsp != null) chat = chatRsp.getProvider();

        RegisteredServiceProvider<Permission> permissionRsp = manager.getRegistration(Permission.class);
        if (permissionRsp != null) permission = permissionRsp.getProvider();

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
