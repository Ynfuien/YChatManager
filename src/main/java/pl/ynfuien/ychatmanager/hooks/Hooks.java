package pl.ynfuien.ychatmanager.hooks;

import org.bukkit.Bukkit;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.hooks.placeholderapi.PlaceholderAPIHook;
import pl.ynfuien.ychatmanager.hooks.vault.VaultHook;
import pl.ynfuien.ychatmanager.utils.Logger;

public class Hooks {
    private static PlaceholderAPIHook papiHook = null;

    public static void load(YChatManager instance) {
        // Register PlaceholderAPI hook
        if (isPapiEnabled()) {
            papiHook = new PlaceholderAPIHook(instance);
            if (!papiHook.register()) {
                papiHook = null;
                Logger.logError("[Hooks] Something went wrong while registering PlaceholderAPI hook!");
            }
            else {
                Logger.log("[Hooks] Successfully registered hook for PlaceholderAPI!");
            }
        }

        // Register vault hook
        if (isVaultEnabled()) {
            new VaultHook();
            Logger.log("[Hooks] Successfully registered hook for Vault!");
        }
    }

    public static boolean isPapiEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    public static boolean isPapiHookEnabled() {
        return papiHook != null;
    }


    public static boolean isVaultEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }
}
