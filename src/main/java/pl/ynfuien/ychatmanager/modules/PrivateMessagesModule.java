package pl.ynfuien.ychatmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.utils.Lang;
import pl.ynfuien.ychatmanager.utils.Logger;

import java.util.HashMap;

public class PrivateMessagesModule {
    private boolean consoleSocialspy;
    private boolean soundEnabled;
    private Sound sound;
    private SoundCategory soundCategory;
    private float volume;
    private float pitch;

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        consoleSocialspy = config.getBoolean("console-socialspy");
        ConfigurationSection receiveSound = config.getConfigurationSection("receive-sound");
        soundEnabled = receiveSound.getBoolean("enabled");

        if (soundEnabled) {
            String name = receiveSound.getString("name").replace('.', '_').toUpperCase();
            try {
                sound = Sound.valueOf(name);
            } catch (IllegalArgumentException e) {
                soundEnabled = false;
                logError(String.format("Sound name '%s' is incorrect!", name));
            }

            name = receiveSound.getString("category").toUpperCase();
            try {
                soundCategory = SoundCategory.valueOf(name);
            } catch (IllegalArgumentException e) {
                soundEnabled = false;
                logError(String.format("Sound category '%s' is incorrect!", name));
            }

            String value = receiveSound.getString("volume");
            try {
                volume = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                soundEnabled = false;
                logError(String.format("Volume '%s' is incorrect!", value));
            }

            value = receiveSound.getString("pitch");
            try {
                pitch = Float.parseFloat(value);
            } catch (NumberFormatException e) {
                soundEnabled = false;
                logError(String.format("Pitch '%s' is incorrect!", value));
            }

            if (!soundEnabled) logError("Sound won't be played till you correct the config!");
        }

        return true;
    }

    private void logError(String message) {
        Logger.logWarning("[Private-Messages] " + message);
    }

    /**
     * Puts placeholders used in a private messages into a hashmap.
     */
    public static void putPlaceholders(HashMap<String, Object> placeholders, CommandSender receiver, CommandSender sender, String message) {
        placeholders.putAll(ChatFormatter.createPlayerPlaceholders(sender, "sender"));
        placeholders.putAll(ChatFormatter.createPlayerPlaceholders(receiver, "receiver"));

        placeholders.put("message", message);
    }

    public void sendMessage(CommandSender receiver, CommandSender sender, HashMap<String, Object> placeholders) {
        Lang.Message.PRIVATE_MESSAGE_SENT.send(sender, placeholders);
        Lang.Message.PRIVATE_MESSAGE_RECEIVE.send(receiver, placeholders);
        if (receiver instanceof Player) playSound((Player) receiver);

        // Social spy
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("ychatmanager.socialspy")) continue;
            if (p.equals(sender) || p.equals(receiver)) continue;

            Lang.Message.PRIVATE_MESSAGE_SOCIALSPY.send(p, placeholders);
        }
        // SS in console
        if (!consoleSocialspy) return;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        if (sender.equals(console) || receiver.equals(console)) return;
        Lang.Message.PRIVATE_MESSAGE_SOCIALSPY.send(console, placeholders);
    }

    public void playSound(Player p) {
        if (!soundEnabled) return;

        p.playSound(p.getLocation(), sound, soundCategory, volume, pitch);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    public Sound getSound() {
        return sound;
    }
    public SoundCategory getSoundCategory() {
        return soundCategory;
    }
    public float getVolume() {
        return volume;
    }
    public float getPitch() {
        return pitch;
    }
    public boolean isConsoleSocialspy() {
        return consoleSocialspy;
    }
}
