//package pl.ynfuien.ychatmanager.hooks.placeholderapi.placeholders;//package pl.ynfuien.ychatmanager.hooks.placeholderapi.placeholders;
////
////import org.bukkit.OfflinePlayer;
////import pl.ynfuien.yresizingborders.hooks.placeholderapi.Placeholder;
////import pl.ynfuien.yresizingborders.profiles.BorderProfile;
////import pl.ynfuien.yresizingborders.profiles.BorderProfiles;
////import pl.ynfuien.yresizingborders.utils.CronTask;
////import pl.ynfuien.yresizingborders.utils.DoubleFormatter;
////
////import java.text.SimpleDateFormat;
////import java.util.Date;
////
////public class ProfilePlaceholders implements Placeholder {
////    private final BorderProfiles borderProfiles;
////    private final DoubleFormatter df = new DoubleFormatter();
////
////    public ProfilePlaceholders(BorderProfiles borderProfiles) {
////        this.borderProfiles = borderProfiles;
////    }
////
////    @Override
////    public String name() {
////        return "profile";
////    }
////
////    @Override
////    public String getPlaceholder(String id, OfflinePlayer p) {
////        BorderProfile prof = null;
////        // Loop through all profiles to get that with provided name
////        for (BorderProfile profile : borderProfiles.getProfiles()) {
////            String name = profile.getName();
////
////            // If id starts with provided name
////            if (id.startsWith(name + "_")) {
////                prof = profile;
////                break;
////            }
////        }
////
////        // Return if profile with provided name doesn't exist
////        if (prof == null) {
////            return "profile doesn't exist";
////        }
////
////        // Set id to provided properties after profile name
////        id = id.substring(prof.getName().length() + 1).toLowerCase();
////
////        // Placeholder: %yrb_profile_<name>_enabled%
////        // Returns: whether profile is enabled
////        if (id.equals("enabled")) {
////            return prof.isEnabled() ? "yes" : "no";
////        }
////
////        // Placeholder: %yrb_profile_<name>_worlds%
////        // Returns: profile worlds
////        if (id.equals("worlds")) {
////            return String.join(", ", prof.getWorlds());
////        }
////
////        // Placeholder: %yrb_profile_<name>_border.min-size%
////        // Returns: border min-size
////        if (id.equals("border.min-size")) {
////            return df.format(prof.getMinSize());
////        }
////
////        // Placeholder: %yrb_profile_<name>_border.max-size%
////        // Returns: border max-size
////        if (id.equals("border.max-size")) {
////            return df.format(prof.getMaxSize());
////        }
////
////        // Placeholder: %yrb_profile_<name>_resize.by%
////        // Returns: resize by
////        if (id.equals("resize.by")) {
////            return df.format(prof.getResizeBy());
////        }
////
////        // Placeholders:
////        // - %yrb_profile_<name>_resize.time_seconds%
////        // - %yrb_profile_<name>_resize.time_minutes%
////        // - %yrb_profile_<name>_resize.time_hours%
////        if (id.startsWith("resize.time_")) {
////            // Set id to property provided after "resize.time_"
////            id = id.substring(12);
////
////            double resizeTime = prof.getResizeTime();
////
////            // Placeholder: %yrb_profile_<name>_resize.time_seconds%
////            // Returns: resize time in seconds
////            if (id.equals("seconds")) {
////                return df.format(resizeTime);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.time_minutes%
////            // Returns: resize time in minutes
////            if (id.equals("minutes")) {
////                return df.format(resizeTime / 60);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.time_hours%
////            // Returns: resize time in hours
////            if (id.equals("hours")) {
////                return df.format(resizeTime / 60 / 60);
////            }
////
////            return null;
////        }
////
////        // Placeholders:
////        // - %yrb_profile_<name>_resize.interval_seconds%
////        // - %yrb_profile_<name>_resize.interval_minutes%
////        // - %yrb_profile_<name>_resize.interval_hours%
////        // - %yrb_profile_<name>_resize.interval_days%
////        if (id.startsWith("resize.interval_")) {
////            // Set id to property provided after "resize.interval_"
////            id = id.substring(16);
////
////            double resizeInterval = prof.getResizeIntervalMinutes();
////
////            // Placeholder: %yrb_profile_<name>_resize.interval_seconds%
////            // Returns: resize interval in seconds
////            if (id.equals("seconds")) {
////                return df.format(resizeInterval * 60);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.interval_minutes%
////            // Returns: resize interval in minutes
////            if (id.equals("minutes")) {
////                return df.format(resizeInterval);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.interval_hours%
////            // Returns: resize interval in hours
////            if (id.equals("hours")) {
////                return df.format(resizeInterval / 60);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.interval_days%
////            // Returns: resize interval in days
////            if (id.equals("days")) {
////                return df.format(resizeInterval / 60 / 24);
////            }
////
////            return null;
////        }
////
////        // Placeholders:
////        // - %yrb_profile_<name>_resize.last-resize_timestamp%
////        // - %yrb_profile_<name>_resize.last-resize_time%
////        // - %yrb_profile_<name>_resize.last-resize_date%
////        // - %yrb_profile_<name>_resize.last-resize_time-date%
////        if (id.startsWith("resize.last-resize_")) {
////            // Set id to property provided after "resize.last-resize_"
////            id = id.substring(19);
////
////            Date lastResize = prof.getLastResize();
////
////            // Placeholder: %yrb_profile_<name>_resize.last-resize_timestamp%
////            // Returns: last-resize timestamp
////            if (id.equals("timestamp")) {
////                return String.valueOf(lastResize.getTime());
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.last-resize_time%
////            // Returns: last-resize time
////            if (id.equals("time")) {
////                return new SimpleDateFormat("HH:mm:ss").format(lastResize);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.last-resize_date%
////            // Returns: last-resize date
////            if (id.equals("date")) {
////                return new SimpleDateFormat("dd.MM.yyyy").format(lastResize);
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.last-resize_time-date%
////            // Returns: last-resize time-date
////            if (id.equals("time-date")) {
////                return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(lastResize);
////            }
////
////            return null;
////        }
////
////        // Placeholders:
////        // - %yrb_profile_<name>_resize.crontask_expression%
////        // - %yrb_profile_<name>_resize.crontask_description%
////        if (id.startsWith("resize.crontask_")) {
////            // Set id to property provided after "resize.crontask_"
////            id = id.substring(16);
////
////            if (!prof.isUsingCrontask()) {
////                return "no crontask";
////            }
////
////            CronTask crontask = prof.getResizeCrontask();
////
////            // Placeholder: %yrb_profile_<name>_resize.crontask_expression%
////            // Returns: resize crontask expression
////            if (id.equals("expression")) {
////                return crontask.getExpression();
////            }
////
////            // Placeholder: %yrb_profile_<name>_resize.crontask_description%
////            // Returns: resize crontask description
////            if (id.equals("description")) {
////                return crontask.getDescription();
////            }
////
////            return null;
////        }
////
////        // Placeholder: %yrb_profile_<name>_resize.message%
////        // Returns: resize message
////        if (id.equals("resize.message")) {
////            return prof.getResizeMessage();
////        }
////
////        return null;
////    }
////}
