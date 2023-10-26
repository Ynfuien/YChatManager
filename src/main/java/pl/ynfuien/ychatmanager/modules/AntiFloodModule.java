package pl.ynfuien.ychatmanager.modules;

import org.bukkit.configuration.ConfigurationSection;
import pl.ynfuien.ychatmanager.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class AntiFloodModule {
    private boolean enabled;
    private boolean checkUsernames;
    private boolean consoleLog;

    private final List<FloodPattern> floodPatterns = new ArrayList<>();

    /**
     * Method used to load module from a configuration file.
     * <p><b>Do not use it.</b></p>
     * @return Whether loading was successful
     */
    public boolean load(ConfigurationSection config) {
        enabled = config.getBoolean("enabled");
        checkUsernames = config.getBoolean("check-usernames");
        consoleLog = config.getBoolean("console-log");


        floodPatterns.clear();
        ConfigurationSection patternSection = config.getConfigurationSection("patterns");
        if (patternSection == null) {
            logError("There is no 'patterns' section in config!");
            return false;
        }
        for (String name : patternSection.getKeys(false)) {
            String regex = patternSection.getString(name+".pattern");
            if (regex == null) {
                logError(String.format("Pattern for check '%s' doesn't exist!", name));
                return false;
            }

            String replacement = patternSection.getString(name+".replacement");
            if (replacement == null) {
                logError(String.format("Replacement for check '%s' doesn't exist!", name));
                return false;
            }

//            if (!patternSection.isInt(name+".shorted")) {
//                logError(String.format("'shorted' for check '%s' is incorrect or doesn't exist!", name));
//                return false;
//            }

//            int shorted = patternSection.getInt(name+".shorted");
//            if (shorted < 0) {
//                shorted = 0;
//                logError(String.format("'shorted' for check '%s' is below 0!", name));
//            }

            Pattern pattern;
            try {
                pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                logError(String.format("Pattern for check '%s' is incorrect:", name));
                e.printStackTrace();
                return false;
            }
            floodPatterns.add(new FloodPattern(name, pattern, replacement));
        }

        return true;
    }

    private void logError(String message) {
        Logger.logWarning("[Anti-Flood] " + message);
    }

    /**
     * Applies anti flood to a provided message
     * @return Message with shortened words if needed
     */
    public String apply(String message) {
        if (checkUsernames) return ChatModule.checkUsernames(message, this::shortenFlood);

        return shortenFlood(message);
    }

    private String shortenFlood(String message) {

        HashMap<FloodPattern, List<String>> logMatches = new LinkedHashMap<>();
        for (FloodPattern pattern : floodPatterns) {
            Pattern regex = pattern.pattern;
            Matcher matcher = regex.matcher(message);

            while (matcher.find()) {
                String match = matcher.group();
                if (consoleLog && !logMatches.containsKey(pattern)) logMatches.put(pattern, new ArrayList<>());

                String replacement = match.replaceFirst(regex.pattern(), pattern.replacement);
                if (consoleLog) logMatches.get(pattern).add(String.format("'%s' - '%s'", match, replacement));

                message = message.replace(match, replacement);
            }
        }

        // Console log
        if (logMatches.size() > 0) {
            Logger.log("<white>Flood in the message:");
            for (FloodPattern pattern : logMatches.keySet()) {
                Logger.log(String.format("<gray>Pattern '%s':", pattern.name));
                for (String match : logMatches.get(pattern)) {
                    // Using placeholder, so potential color formats, entered by a player, won't be used.
                    Logger.log("<gray>- <dark_gray>{!match}", new HashMap<>() {{put("match", match);}});
                }
            }
        }

        return message;
    }

    // Getters
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isCheckUsernames() {
        return checkUsernames;
    }

    public boolean isConsoleLog() {
        return consoleLog;
    }

//    private record FloodPattern(String name, Pattern pattern, int shorted) {}
    private record FloodPattern(String name, Pattern pattern, String replacement) {}
}
