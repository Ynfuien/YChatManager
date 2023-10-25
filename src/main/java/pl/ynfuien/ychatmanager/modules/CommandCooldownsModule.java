package pl.ynfuien.ychatmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.YChatManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommandCooldownsModule {
    private final YChatManager instance;
    private int anyCommandCooldown;
    private int sameCommandCooldown;

    public CommandCooldownsModule(YChatManager instance) {
        this.instance = instance;
    }

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        anyCommandCooldown = config.getInt("any-command");
        sameCommandCooldown = config.getInt("same-command");


        return true;
    }

    private final List<UUID> anyCommandCooldowns = new ArrayList<>();
    private final HashMap<UUID, List<String>> sameCommandCooldowns = new HashMap<>();

    /**
     * Checks command cooldown for a provided player
     * @return True if player can execute command now
     */
    public boolean checkCooldown(Player p, String command) {
        UUID uuid = p.getUniqueId();

        // Any command cooldown
        if (anyCommandCooldown > 0) {
            if (anyCommandCooldowns.contains(uuid)) return false;
            anyCommandCooldowns.add(uuid);
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                anyCommandCooldowns.remove(uuid);
            }, anyCommandCooldown);
        }

        // The same command cooldown
        if (sameCommandCooldown > 0) {
            if (command.isBlank()) return true;
            if (!sameCommandCooldowns.containsKey(uuid)) sameCommandCooldowns.put(uuid, new ArrayList<>());
            List<String> commands = sameCommandCooldowns.get(uuid);
            if (commands.contains(command)) return false;

            commands.add(command);
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                if (!sameCommandCooldowns.containsKey(uuid)) return;

                sameCommandCooldowns.get(uuid).remove(command);
            }, sameCommandCooldown);
        }

        return true;
    }

    /**
     * Removes player's cooldowns from a cache
     * @param uuid Player's uuid
     */
    public void removePlayerFromCache(UUID uuid) {
        anyCommandCooldowns.remove(uuid);
        sameCommandCooldowns.remove(uuid);
    }
}
