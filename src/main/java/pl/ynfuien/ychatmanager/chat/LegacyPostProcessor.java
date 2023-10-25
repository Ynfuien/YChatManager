package pl.ynfuien.ychatmanager.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

// I have no idea how exactly and why this works. But it parses legacy format to correct component colors/styles.
public final class LegacyPostProcessor implements UnaryOperator<Component> {
    private static final Replacer REPLACER = new Replacer();

    @Override
    public Component apply(Component component) {
        return component.replaceText(REPLACER);
    }

    private static final class Replacer implements Consumer<TextReplacementConfig.Builder> {

        private static final Replacement REPLACEMENT = new Replacement();
        private static final Pattern PATTERN_ANY = Pattern.compile(".*");

        @Override
        public void accept(TextReplacementConfig.Builder builder) {
            builder
                    .match(PATTERN_ANY)
                    .replacement(REPLACEMENT);
        }

    }

    private static final class Replacement implements BiFunction<MatchResult, TextComponent.Builder, ComponentLike> {

        @Override
        public ComponentLike apply(MatchResult matchResult, TextComponent.Builder builder) {
//            LegacyComponentSerializer serializer = LegacyComponentSerializer.builder()
//                    .character('&')
//                    .hexCharacter('#')
//                    .hexColors()
//                    .useUnusualXRepeatedCharacterHexFormat()
//                    .build();
//            return serializer.deserialize(matchResult.group());
            return LegacyComponentSerializer.legacyAmpersand().deserialize(matchResult.group());
        }

    }

}
