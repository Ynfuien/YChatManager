package pl.ynfuien.ychatmanager.storage;

/**
 *
 * @param serialized Nickname in MiniMessage format
 * @param input Input that player used to change nickname
 */
public record Nickname(String serialized, String input) {}