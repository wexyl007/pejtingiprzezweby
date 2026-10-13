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
