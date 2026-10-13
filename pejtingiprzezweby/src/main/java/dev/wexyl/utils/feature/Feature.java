package dev.wexyl.utils.feature;

import java.util.List;

/** Opis jednej funkcji widocznej w menu. */
public record Feature(String id, String name, String description, boolean defaultEnabled,
                      List<TextSetting> textSettings) {

    public Feature(String id, String name, String description, boolean defaultEnabled) {
        this(id, name, description, defaultEnabled, List.of());
    }
}
