package pl.ynfuien.ychatmanager.hooks.luckperms;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.ynfuien.ychatmanager.YChatManager;
import pl.ynfuien.ychatmanager.modules.DisplayNameModule;
import pl.ynfuien.ychatmanager.utils.Logger;

import java.util.Collection;

// Hooks into user and group change event, and updates players display names,
// to keep prefixes and suffixed up to date.
public class LuckPermsHook {
    private final YChatManager instance;
    private final DisplayNameModule displayNameModule;
    private ScheduledTask userTask = null;
    private ScheduledTask groupTask = null;

    public LuckPermsHook(YChatManager instance) {
        this.instance = instance;
        this.displayNameModule = instance.getModules().getDisplaynameModule();

        LuckPerms lp = LuckPermsProvider.get();
        EventBus eventBus = lp.getEventBus();

        eventBus.subscribe(instance, UserDataRecalculateEvent.class, e -> {
            if (!displayNameModule.isEnabled()) return;

            // Using scheduler to ignore multiple events
            // (UserDataRecalculate firing a few times at the same time)
            if (userTask != null) userTask.cancel();
            userTask = Bukkit.getGlobalRegionScheduler().runDelayed(instance, (task) -> {
                Player p = Bukkit.getPlayer(e.getUser().getUniqueId());
                if (p == null || !p.isOnline()) return;

                displayNameModule.updateDisplayName(p);
            }, 1);
        });


        eventBus.subscribe(instance, GroupDataRecalculateEvent.class, e -> {
            if (!displayNameModule.isEnabled()) return;

            // Using scheduler to ignore multiple events
            // (GroupDataRecalculateEvent firing a few times at the same time)
            // And because in the moment of an event, player's prefix/suffix
            // isn't yet changed.
            if (groupTask != null) groupTask.cancel();
            groupTask = Bukkit.getGlobalRegionScheduler().runDelayed(instance, (task) -> {
                Group group = e.getGroup();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    User user = lp.getPlayerAdapter(Player.class).getUser(p);
                    Collection<Group> groups = user.getInheritedGroups(user.getQueryOptions());

                    if (!groups.contains(group)) continue;
                    displayNameModule.updateDisplayName(p);
                }
            }, 1);
        });
    }
}
