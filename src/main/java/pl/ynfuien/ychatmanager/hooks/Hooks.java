package pl.ynfuien.ychatmanager.hooks;

import org.bukkit.Bukkit;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.hooks.luckperms.LuckPermsHook;
import pl.ynfuien.ychatmanager.hooks.placeholderapi.PlaceholderAPIHook;
import pl.ynfuien.ychatmanager.hooks.vault.VaultHook;
import pl.ynfuien.ychatmanager.utils.Logger;

public class Hooks {
    private static PlaceholderAPIHook papiHook = null;

    public static void load(YChatManager instance) {
        // Register PlaceholderAPI hook
        if (isPluginEnabled(Plugin.PAPI)) {
            papiHook = new PlaceholderAPIHook(instance);
            if (!papiHook.register()) {
                papiHook = null;
                Logger.logError("[Hooks] Something went wrong while registering PlaceholderAPI hook!");
            }
            else {
                Logger.log("[Hooks] Successfully registered hook for PlaceholderAPI!");
            }
        }

        // Register Vault hook
        if (isPluginEnabled(Plugin.VAULT)) {
            new VaultHook();
            Logger.log("[Hooks] Successfully registered hook for Vault!");
        }

        // Register LuckPerms hook
        if (isPluginEnabled(Plugin.LUCKPERMS)) {
            new LuckPermsHook(instance);
            Logger.log("[Hooks] Successfully registered hook for LuckPerms!");
        }
    }

    public static boolean isPluginEnabled(Plugin plugin) {
        return Bukkit.getPluginManager().isPluginEnabled(plugin.getName());
    }

    public enum Plugin {
        PAPI("PlaceholderAPI"),
        VAULT("Vault"),
        LUCKPERMS("LuckPerms");

        private final String name;
        Plugin(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
