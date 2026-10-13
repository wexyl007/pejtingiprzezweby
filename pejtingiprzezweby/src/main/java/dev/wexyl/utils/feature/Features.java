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

    public static final TextSetting DRIPSTONE_LENGTH = new TextSetting(
            "auto_dripstone_length",
            "Długość kolumny (1-8)",
            "5",
            1);

    public static final Feature AUTO_DRIPSTONE = register(new Feature(
            "auto_dripstone",
            "AutoDripstone",
            "Klik w zamknięty trapdoor dowolnego rodzaju (dolna połowa) błyskawicznie stawia pod nim kolumnę "
                    + "pointed dripstone, a Twój klik otwiera trapdoor i ją zrzuca. Działa tylko na klikany "
                    + "trapdoor. Potrzebny pointed dripstone w hotbarze. Shift = zwykłe otwieranie.",
            false,
            List.of(DRIPSTONE_LENGTH)));

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
