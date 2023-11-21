package pl.ynfuien.ychatmanager.chat;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

// Parses hex colors.
// Strong inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public class HexColorTagResolver implements TagResolver, SerializableResolver.Single {
    private static final char HEX = '#';
    private static final String TAG_NAME = "color";
    private static final Set<String> ALIASES = Set.of("colour", "c");

    private static final StyleClaim<TextColor> STYLE = StyleClaim.claim(TAG_NAME, Style::color, (color, emitter) -> {
        emitter.tag(color.asHexString());
    });

    private static boolean isColorOrAbbreviation(final String name) {
        return name.equals(TAG_NAME) || ALIASES.contains(name);
    }

    private final static HexColorTagResolver INSTANCE;
    static {
        INSTANCE = new HexColorTagResolver();
    }

    @Override
    public @Nullable Tag resolve(final @NotNull String name, final @NotNull ArgumentQueue args, final @NotNull Context ctx) throws ParsingException {
        if (!this.has(name)) return null;

        String colorName = name;
        if (isColorOrAbbreviation(name)) {
            colorName = args.popOr("Expected to find a color parameter: #RRGGBB").lowerValue();
        }

        final TextColor color = resolveColor(colorName, ctx);
        return Tag.styling(color);
    }

    static @NotNull TextColor resolveColor(final @NotNull String colorName, final @NotNull Context ctx) throws ParsingException {
        if (colorName.charAt(0) == HEX) return TextColor.fromHexString(colorName);

        throw ctx.newException(String.format("Unable to parse a color from '%s'. Please use hex (#RRGGBB) colors.", colorName));
    }

    @Override
    public boolean has(final @NotNull String name) {
        return isColorOrAbbreviation(name)
                || TextColor.fromHexString(name) != null;
    }

    @Override
    public @Nullable StyleClaim<?> claimStyle() {
        return STYLE;
    }

    public static HexColorTagResolver get() {
        return INSTANCE;
    }
}