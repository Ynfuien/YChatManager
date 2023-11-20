package pl.ynfuien.ychatmanager.storage;

import org.jetbrains.annotations.NotNull;

/**
 *
 * @param serialized Nickname in MiniMessage format
 * @param input Input that player used to change nickname
 */
public record Nickname(@NotNull String serialized, @NotNull String input) {}