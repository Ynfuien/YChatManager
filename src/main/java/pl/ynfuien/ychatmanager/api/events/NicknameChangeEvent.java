package pl.ynfuien.ychatmanager.api.events;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pl.ynfuien.ychatmanager.storage.Nickname;

public class NicknameChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final OfflinePlayer player;
    private Nickname nickname;
    private boolean cancelled;


    public NicknameChangeEvent(@NotNull OfflinePlayer player, @NotNull Nickname nickname) {
        super(false);

        this.player = player;
        this.nickname = nickname;
    }

    @NotNull
    public OfflinePlayer getPlayer() {
        return player;
    }
    @NotNull
    public Nickname getNickname() {
        return nickname;
    }

    public void setNickname(@NotNull Nickname nickname) throws IllegalArgumentException {
        Preconditions.checkArgument(nickname != null, "Nickname cannot be null");
        Preconditions.checkArgument(nickname.input() != null, "Nickname.input() cannot be empty");
        Preconditions.checkArgument(nickname.serialized() != null, "Nickname.serialized() cannot be empty");

        this.nickname = nickname;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
