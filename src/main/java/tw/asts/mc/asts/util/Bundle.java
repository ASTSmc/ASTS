package tw.asts.mc.asts.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.AbstractTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import tw.asts.mc.asts.util.adventure.TextMessageTranslationStore;

import java.util.Locale;
import java.util.ResourceBundle;

final public class Bundle {
    public Bundle() {
        runText();
    }
    private void runText() {
        final TextMessageTranslationStore store = new TextMessageTranslationStore(Key.key("asts", "text"));
        translateStores(
                store,
                "bundle.Text",
                Locale.TRADITIONAL_CHINESE,
                Locale.US
        );
        GlobalTranslator.translator().addSource(store);
    }
    private static void translateStores(AbstractTranslationStore.StringBased store, @SuppressWarnings("SameParameterValue") String baseName, Locale... locales) {
        for (Locale locale : locales) {
            translateStore(store, baseName, locale);
        }
    }
    private static void translateStore(AbstractTranslationStore.StringBased store, String baseName, Locale locale) {
        final ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, UTF8ResourceBundleControl.utf8ResourceBundleControl());
        store.registerAll(locale, bundle, true);
    }
}
