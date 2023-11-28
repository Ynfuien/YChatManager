package pl.ynfuien.ychatmanager.modules;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.chat.ChatFormatter;
import pl.ynfuien.ychatmanager.config.ConfigHandler;
import pl.ynfuien.ychatmanager.config.ConfigName;
import pl.ynfuien.ychatmanager.config.ConfigObject;
import pl.ynfuien.ychatmanager.utils.Lang;
import pl.ynfuien.ychatmanager.utils.Logger;
import pl.ynfuien.ychatmanager.utils.Messenger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiSwearModule {
    private final YChatManager instance;

    private boolean enabled;
    private String replacementChar;
    private String patternStart;
    private String patternEnd;
    private String patternSeparator;
    private HashMap<String, String> patternReplacements;
    private boolean checkExceptions;
    private boolean checkUsernames;
    private boolean consoleLog;
    private boolean punishmentEnabled;
    private int punishmentWarnings;
    private int punishmentWarningExpire;
    private String punishmentCommand;
    private List<Integer> punishmentExcludedHours;

    private final HashMap<String, Pattern> swearWords = new LinkedHashMap<>();
    private final HashMap<String, Pattern> swearWordsWithReplacement = new LinkedHashMap<>();
    private List<String> swearWordExceptions = new ArrayList<>();

    private ConfigObject swearConfig;
    private ConfigObject exceptionsConfig;

    public AntiSwearModule(YChatManager instance) {
        this.instance = instance;
    }


    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        enabled = config.getBoolean("enabled");
        replacementChar = config.getString("replacement-char");

        ConfigurationSection pattern = config.getConfigurationSection("pattern");
        patternStart = pattern.getString("start");
        patternEnd = pattern.getString("end");
        patternSeparator = pattern.getString("separator");

        patternReplacements = new HashMap<>();
        ConfigurationSection replacements = pattern.getConfigurationSection("replacements");
        for (String key : replacements.getKeys(false)) {
            patternReplacements.put(key, replacements.getString(key));
        }

        checkExceptions = config.getBoolean("check-exceptions");
        checkUsernames = config.getBoolean("check-usernames");
        consoleLog = config.getBoolean("console-log");

        ConfigurationSection punishment = config.getConfigurationSection("punishment");
        punishmentEnabled = punishment.getBoolean("enabled");
        punishmentWarnings = punishment.getInt("warnings");
        punishmentWarningExpire = punishment.getInt("warning-expire");
        punishmentCommand = punishment.getString("command");
        punishmentExcludedHours = punishment.getIntegerList("excluded-hours");

        swearWords.clear();
        swearWordsWithReplacement.clear();
        swearWordExceptions.clear();

        ConfigHandler configHandler = instance.getConfigHandler();
        swearConfig = configHandler.get(ConfigName.SWEAR_WORDS);
        exceptionsConfig = configHandler.get(ConfigName.SWEAR_WORD_EXCEPTIONS);
        List<String> words = swearConfig.getConfig().getStringList("list");
        words.sort(Comparator.comparingInt(String::length).reversed());
        swearWordExceptions = exceptionsConfig.getConfig().getStringList("list");

        for (String word : words) {
            // Normal
            swearWords.put(word, Pattern.compile(formPattern(word), Pattern.CASE_INSENSITIVE));

            // With replacement chars
            swearWordsWithReplacement.put(word, Pattern.compile(formPattern(useReplacements(word)), Pattern.CASE_INSENSITIVE));
        }

        return true;
    }

    private String formPattern(String word) {
        return patternStart + String.join(patternSeparator, word.split("")) + patternEnd;
    }

    private final HashMap<UUID, Integer> swearWarnings = new HashMap<>();
    /**
     * Applies anti swear to a provided message. Warns or punishes player if needed. Sends alert to online admins and to console.
     * @param player Player that sent a message
     * @return Censored message
     */
    public String apply(Player player, String message) {
//        if (checkUsernames) return ChatModule.checkUsernames(message, this::censureSwears);
//        return censureSwears(message);

        String censored = checkUsernames ? ChatModule.checkUsernames(message, this::censureSwears) : censureSwears(message);

        // If player swore
        if (censored.equals(message)) return message;

        UUID uuid = player.getUniqueId();

        // Send alert to admins
        Lang.Message alertMessage = Lang.Message.CHAT_ANTI_SWEAR_ALERT;
        HashMap<String, Object> phs = ChatFormatter.createPlayerPlaceholders(player);
        phs.put("message", message);

        for (Player lp : Bukkit.getOnlinePlayers()) {
            if (!lp.hasPermission("ychatmanager.alert.anti_swear")) continue;
            alertMessage.send(lp, phs);
        }
        if (consoleLog) alertMessage.send(Bukkit.getConsoleSender(), phs);

        // Check excluded hours
        if (punishmentExcludedHours.contains(LocalDateTime.now().getHour())) return censored;

        // Check for warning count for that player
        if (!swearWarnings.containsKey(uuid)) swearWarnings.put(uuid, 0);
        int warnings = swearWarnings.get(uuid) + 1;
        swearWarnings.put(uuid, warnings);

        // Set scheduler to remove warning after provided time
        Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
            Integer warningCount = swearWarnings.get(uuid);
            if (warningCount == null) return;
            if (warningCount <= 1) {
                swearWarnings.remove(uuid);
                return;
            }

            swearWarnings.put(uuid, warningCount - 1);
        }, (long) punishmentWarningExpire * 60 * 20);


        // Punish player if warning count is over limit
        int maxWarnings = punishmentWarnings;
        if (maxWarnings != -1 && warnings > maxWarnings) {
            String command = Messenger.parsePluginPlaceholders(punishmentCommand, new HashMap<>() {{
                put("player", player.getName());
                put("warnings", warnings);
            }});
            Bukkit.getScheduler().runTask(instance, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            });
            return null;
        }

        if (maxWarnings != 0) {
            // Using scheduler to sent warning after player's message
            Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
                Lang.Message.CHAT_ANTI_SWEAR_WARNING.send(player);
            }, 1);
        }

        return censored;
    }

    private String censureSwears(String message) {
        HashMap<String, List<String>> logMatches = new LinkedHashMap<>();

        // Patterns with char replacements
        for (String word : swearWordsWithReplacement.keySet()) {
            Pattern pattern = swearWordsWithReplacement.get(word);

            Matcher matcher = pattern.matcher(useReplacements(message));
            while (matcher.find()) {
                String match = matcher.group(0);

                List<String> matches;
                if (!logMatches.containsKey(word)) logMatches.put(word, new ArrayList<>());
                matches = logMatches.get(word);
                if (!matches.contains(match)) matches.add(match);

                if (checkExceptions && swearWordExceptions.contains(match.toLowerCase())) continue;

                message = message.substring(0, matcher.start()) + replacementChar.repeat(word.length()) + message.substring(matcher.end());

                if (match.length() != word.length()) matcher = pattern.matcher(useReplacements(message));
            }
        }

        // Normal patterns
        for (String word : swearWords.keySet()) {
            Pattern pattern = swearWords.get(word);

            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                String match = matcher.group(0);

                List<String> matches;
                if (!logMatches.containsKey(word)) logMatches.put(word, new ArrayList<>());
                matches = logMatches.get(word);
                if (!matches.contains(match)) matches.add(match);

                if (checkExceptions && swearWordExceptions.contains(match.toLowerCase())) continue;

                message = matcher.replaceAll(replacementChar.repeat(word.length()));
            }
        }

        // Console log
        if (consoleLog && logMatches.size() > 0) {
            Logger.log("<gray>Swears in the message:");
            for (String word : logMatches.keySet()) {
                List<String> matches = logMatches.get(word);

                if (matches.size() > 1) {
                    Logger.log(String.format("<gray>Word <dark_red>'%s'<gray>:", word));
                    for (String match : matches) {
                        // Using placeholder, so potential color formats, entered by a player, won't be used.
                        String log = "<white>- <yellow>{!match}'";
                        if (swearWordExceptions.contains(match.toLowerCase())) log += " <gold>[exception]";
                        Logger.log(log, new HashMap<>() {{put("match", match);}});
                    }
                    continue;
                }

                String match = matches.get(0);
                String log = String.format("<white>- <gray>word <dark_red>'%s' <gray>- match <yellow>'{!match}'", word);
                if (swearWordExceptions.contains(match.toLowerCase())) log += " <gold>[exception]";
                Logger.log(log, new HashMap<>() {{put("match", match);}});
            }
        }

        return message;
    }

    private String useReplacements(String message) {
        for (String key : patternReplacements.keySet()) {
            message = message.replace(key, patternReplacements.get(key));
        }

        return message;
    }


    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public String getReplacementChar() {
        return replacementChar;
    }
    public String getPatternStart() {
        return patternStart;
    }
    public String getPatternEnd() {
        return patternEnd;
    }
    public String getPatternSeparator() {
        return patternSeparator;
    }
    public HashMap<String, String> getPatternReplacements() {
        return patternReplacements;
    }


    public boolean isPunishmentEnabled() {
        return punishmentEnabled;
    }

    public int getPunishmentWarnings() {
        return punishmentWarnings;
    }
    public int getPunishmentWarningExpire() {
        return punishmentWarningExpire;
    }
    public String getPunishmentCommand() {
        return punishmentCommand;
    }
    public List<Integer> getPunishmentExcludedHours() {
        return punishmentExcludedHours;
    }

    public HashMap<String, Pattern> getSwearWords() {
        return swearWords;
    }
    public HashMap<String, Pattern> getSwearWordsWithReplacement() {
        return swearWordsWithReplacement;
    }
    public List<String> getSwearWordExceptions() {
        return swearWordExceptions;
    }
    public HashMap<UUID, Integer> getSwearWarnings() {
        return swearWarnings;
    }

    public boolean isCheckExceptions() {
        return checkExceptions;
    }
    public boolean isCheckUsernames() {
        return checkUsernames;
    }

    // Setters

    /**
     * Adds specified word to the swear list.
     * Returns false if word was already in the list.
     * @return Whether word was added
     */
    public boolean addSwear(String word) {
        List<String> list = swearConfig.getConfig().getStringList("list");
        if (list.contains(word)) return false;

        list.add(word);
        swearConfig.getConfig().set("list", list);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            swearConfig.save();
        });

        swearWords.put(word, Pattern.compile(formPattern(word), Pattern.CASE_INSENSITIVE));
        swearWordsWithReplacement.put(word, Pattern.compile(formPattern(useReplacements(word)), Pattern.CASE_INSENSITIVE));

        return true;
    }

    /**
     * Removes specified word from the swear list.
     * Returns false if word wasn't on the list.
     * @return Whether word was removed
     */
    public boolean removeSwear(String word) {
        List<String> list = swearConfig.getConfig().getStringList("list");
        if (!list.contains(word)) return false;

        list.remove(word);
        swearConfig.getConfig().set("list", list);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            swearConfig.save();
        });

        swearWords.remove(word);
        swearWordsWithReplacement.remove(word);

        return true;
    }

    /**
     * Adds specified exception to the exception list.
     * Returns false if exception was already in the list.
     * @return Whether exception was added
     */
    public boolean addException(String exception) {
        List<String> list = exceptionsConfig.getConfig().getStringList("list");
        if (list.contains(exception)) return false;

        list.add(exception);
        exceptionsConfig.getConfig().set("list", list);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            exceptionsConfig.save();
        });

        swearWordExceptions.add(exception);

        return true;
    }

    /**
     * Removes specified exception from the exception list.
     * Returns false if exception wasn't on the list.
     * @return Whether exception was removed
     */
    public boolean removeException(String exception) {
        List<String> list = exceptionsConfig.getConfig().getStringList("list");
        if (!list.contains(exception)) return false;

        list.remove(exception);
        exceptionsConfig.getConfig().set("list", list);
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            exceptionsConfig.save();
        });

        swearWordExceptions.remove(exception);

        return true;
    }
}
