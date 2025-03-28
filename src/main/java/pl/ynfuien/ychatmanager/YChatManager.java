package pl.ynfuien.ychatmanager;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ychatmanager.commands.*;
import pl.ynfuien.ychatmanager.hooks.Hooks;
import pl.ynfuien.ychatmanager.listeners.*;
import pl.ynfuien.ychatmanager.modules.Modules;
import pl.ynfuien.ychatmanager.storage.Database;
import pl.ynfuien.ychatmanager.storage.MysqlDatabase;
import pl.ynfuien.ychatmanager.storage.SqliteDatabase;
import pl.ynfuien.ychatmanager.storage.Storage;
import pl.ynfuien.ydevlib.config.ConfigHandler;
import pl.ynfuien.ydevlib.config.ConfigObject;
import pl.ynfuien.ydevlib.messages.YLogger;

import java.util.HashMap;
import java.util.List;

public final class YChatManager extends JavaPlugin {
    private static YChatManager instance;
    private final ConfigHandler configHandler = new ConfigHandler(this);
    private ConfigObject config;
    private final Modules modules = new Modules(this);
    private Database database = null;

    @Override
    public void onEnable() {
        instance = this;
        YLogger.setup("<dark_aqua>[<aqua>Y<white>ChatManager<dark_aqua>] <white>", getComponentLogger());

        loadConfigs();
        loadLang();
        config = configHandler.getConfigObject(ConfigName.CONFIG);


        ConfigurationSection dbConfig = config.getConfig().getConfigurationSection("database");
        database = getDatabase(dbConfig);
        if (database != null && database.setup(dbConfig)) database.createNicknamesTable();
        Storage.setup(this);

        modules.load(config.getConfig());

        // Load hooks
        Hooks.load(this);

        setupCommands();
        registerListeners();

        // BStats
        new Metrics(this, 22171);

        YLogger.info("Plugin successfully <green>enabled<white>!");
    }

    @Override
    public void onDisable() {
        if (database != null) database.close();

        YLogger.info("Plugin successfully <red>disabled<white>!");
    }

    private void setupCommands() {
        HashMap<String, CommandExecutor> commands = new HashMap<>();
        commands.put("ychatmanager", new MainCommand());
        commands.put("clearchat", new ClearChatCommand(this));
        commands.put("message", new MessageCommand(this));
        commands.put("reply", new ReplyCommand(this));
        commands.put("nick", new NickCommand(this));
        commands.put("socialspy", new SocialSpyCommand(this));

        for (String name : commands.keySet()) {
            CommandExecutor cmd = commands.get(name);

            getCommand(name).setExecutor(cmd);
            getCommand(name).setTabCompleter((TabCompleter) cmd);
        }
    }

    private void registerListeners() {
        Listener[] listeners = new Listener[] {
            new AsyncChatListener(this),
            new AsyncPlayerChatListener(this),
            new PlayerCommandPreprocessListener(this),
            new PlayerJoinListener(this),
            new PlayerQuitListener(this),
            new PlayerDeathListener(this),
            new TameableDeathMessageListener(this),
        };

        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    private Database getDatabase(ConfigurationSection config) {
        String type = config.getString("type");
        if (type.equalsIgnoreCase("sqlite")) return new SqliteDatabase();
        else if (type.equalsIgnoreCase("mysql")) return new MysqlDatabase();

        YLogger.error("Database type is incorrect! Available database types: sqlite, mysql");
        return null;
    }

    private void loadLang() {
        // Get lang config
        FileConfiguration config = configHandler.getConfig(ConfigName.LANG);

        // Reload lang
        Lang.loadLang(config);
    }

    private void loadConfigs() {
        configHandler.load(ConfigName.CONFIG, true, false, List.of("chat.anti-flood.patterns", "chat.anti-swear.pattern.replacements"));
        configHandler.load(ConfigName.LANG, true, true);
        configHandler.load(ConfigName.SWEAR_WORDS, false, false);
        configHandler.load(ConfigName.SWEAR_WORD_EXCEPTIONS, false, false);
    }

    public boolean reloadPlugin() {
        try {
            // Reload all configs
            configHandler.reloadAll();

            modules.load(config.getConfig());

            // Reload lang
            instance.loadLang();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static YChatManager getInstance() {
        return instance;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        return config.getConfig();
    }

    public Modules getModules() {
        return modules;
    }

    public Database getDatabase() {
        return database;
    }
}
