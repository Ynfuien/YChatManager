package pl.ynfuien.ychatmanager.modules;

import org.bukkit.configuration.file.FileConfiguration;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.utils.Logger;

public class Modules {
    private final YChatManager instance;

    private final CommandCooldownsModule commandCooldownsModule;
    private final DisplayNameModule displayNameModule = new DisplayNameModule();
    private final PrivateMessagesModule privateMessagesModule = new PrivateMessagesModule();
    private final ChatModule chatModule;
    private final AntiFloodModule antiFloodModule = new AntiFloodModule();
    private final AntiCapsModule antiCapsModule = new AntiCapsModule();
    private final AntiSwearModule antiSwearModule;


    public Modules(YChatManager instance) {
        this.instance = instance;

        commandCooldownsModule = new CommandCooldownsModule(instance);
        chatModule = new ChatModule(instance);
        antiSwearModule = new AntiSwearModule(instance);
    }

    /**
     * Method used to load modules from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(FileConfiguration config) {
        commandCooldownsModule.load(config.getConfigurationSection("commands.cooldowns"));
        displayNameModule.load(config.getConfigurationSection("displayname"));
        privateMessagesModule.load(config.getConfigurationSection("private-messages"));
        chatModule.load(config.getConfigurationSection("chat"));
        if (!antiFloodModule.load(config.getConfigurationSection("chat.anti-flood"))) {
            Logger.logWarning("Anti-Flood module couldn't be loaded!");
            return false;
        }
        antiCapsModule.load(config.getConfigurationSection("chat.anti-caps"));
        antiSwearModule.load(config.getConfigurationSection("chat.anti-swear"));

        return true;
    }

    // Getters
    public CommandCooldownsModule getCommandCooldownsModule() {
        return commandCooldownsModule;
    }
    public DisplayNameModule getDisplaynameModule() {
        return displayNameModule;
    }
    public PrivateMessagesModule getPrivateMessagesModule() {
        return privateMessagesModule;
    }

    public ChatModule getChatModule() {
        return chatModule;
    }

    public AntiFloodModule getAntiFloodModule() {
        return antiFloodModule;
    }

    public AntiCapsModule getAntiCapsModule() {
        return antiCapsModule;
    }

    public AntiSwearModule getAntiSwearModule() {
        return antiSwearModule;
    }
}
