package pl.ynfuien.ychatmanager.chat;

import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Parses single color tags.
// Strong inspiration from https://github.com/EternalCodeTeam/ChatFormatter
public class SingleColorTagResolver implements TagResolver, SerializableResolver.Single {
    private static final String TAG_NAME = "color";
    private static final Set<String> ALIASES = Set.of("colour", "c");

    private static final StyleClaim<TextColor> STYLE = StyleClaim.claim(TAG_NAME, Style::color, (color, emitter) -> {
        if (color instanceof NamedTextColor) {
            emitter.tag(NamedTextColor.NAMES.key((NamedTextColor) color));
        }
    });

    private static final Map<String, TextColor> COLOR_ALIASES = new HashMap<>();

    static {
        COLOR_ALIASES.put("dark_grey", NamedTextColor.DARK_GRAY);
        COLOR_ALIASES.put("grey", NamedTextColor.GRAY);
    }

    private static boolean isColorOrAbbreviation(final String name) {
        return name.equals(TAG_NAME) || ALIASES.contains(name);
    }

    private final NamedTextColor allowedColor;
    public SingleColorTagResolver(NamedTextColor color) {
        this.allowedColor = color;
    }


    @Override
    public @Nullable Tag resolve(final @NotNull String name, final @NotNull ArgumentQueue args, final @NotNull Context ctx) throws ParsingException {
        if (!this.has(name)) return null;

        String colorName = name;
        if (isColorOrAbbreviation(name)) {
            colorName = args.popOr("Expected to find a color parameter: <name>|#RRGGBB").lowerValue();
        }

        final TextColor color = resolveColor(colorName, ctx);
        if (!allowedColor.equals(color)) return null;

        return Tag.styling(color);
    }

    static @NotNull TextColor resolveColor(final @NotNull String colorName, final @NotNull Context ctx) throws ParsingException {
        if (COLOR_ALIASES.containsKey(colorName)) return COLOR_ALIASES.get(colorName);

        TextColor color = NamedTextColor.NAMES.value(colorName);
        if (color != null) return color;

        throw ctx.newException(String.format("Unable to parse a color from '%s'. Please use named colours or hex (#RRGGBB) colors.", colorName));
    }

    @Override
    public boolean has(final @NotNull String name) {
        return isColorOrAbbreviation(name)
                || NamedTextColor.NAMES.value(name) != null
                || COLOR_ALIASES.containsKey(name);
    }

    @Override
    public @Nullable StyleClaim<?> claimStyle() {
        return STYLE;
    }


    public static SingleColorTagResolver of(NamedTextColor color) {
        return new SingleColorTagResolver(color);
    }
}