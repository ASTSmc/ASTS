package tw.asts.mc.asts.util.adventure;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.AbstractTranslationStore;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.asts.mc.asts.util.text;

import java.text.MessageFormat;
import java.util.Locale;

final public class TextMessageTranslationStore extends AbstractTranslationStore.StringBased<String> {
    private final Translator translator;
    public TextMessageTranslationStore(final Key name) {
        super(name);
        this.translator = new Translator(MiniMessage.miniMessage());
    }

    @Override
    protected @NotNull String parse(@NotNull String string, @NotNull Locale locale) {
        return text.legacy(string);
    }

    @Override
    public @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        return this.translator.translate(component, locale);
    }
    private final class Translator extends MiniMessageTranslator {

        private Translator(final @NotNull MiniMessage miniMessage) {
            super(miniMessage);
        }

        @Override
        protected @Nullable String getMiniMessageString(final @NotNull String key, final @NotNull Locale locale) {
            return TextMessageTranslationStore.this.translationValue(key, locale);
        }

        @Override
        public @NotNull Key name() {
            return TextMessageTranslationStore.this.name();
        }

        @Override
        public @NotNull TriState hasAnyTranslations() {
            return TextMessageTranslationStore.this.hasAnyTranslations();
        }
    }
}
