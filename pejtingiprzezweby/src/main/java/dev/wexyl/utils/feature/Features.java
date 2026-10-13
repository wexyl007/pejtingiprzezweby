package dev.wexyl.utils.feature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lista wszystkich funkcji. Nowa funkcja = jedna linijka tutaj + jej klasa. */
public final class Features {

    private static final List<Feature> ALL = new ArrayList<>();

    public static final Feature COBWEB_PAINTINGS = register(new Feature(
            "cobweb_paintings",
            "Cobweb Paintings",
            "Stawiaj obrazy (paintingi) na ścianie przez pajęczyny.",
            false));

    public static final TextSetting FAKE_NAME = new TextSetting(
            "name_protect_fake_name",
            "Fałszywy nick",
            "Player",
            32);

    public static final Feature NAME_PROTECT = register(new Feature(
            "name_protect",
            "NameProtect",
            "Zmienia Twój nick widoczny u Ciebie (czat, lista graczy itd.). Inni dalej widzą prawdziwy.",
            false,
            List.of(FAKE_NAME)));

    private static Feature register(Feature feature) {
        ALL.add(feature);
        return feature;
    }

    public static List<Feature> all() {
        return Collections.unmodifiableList(ALL);
    }

    private Features() {
    }
}
