package dev.wexyl.utils.feature;

/** Pole tekstowe w menu, przypisane do funkcji (np. fałszywy nick). */
public record TextSetting(String key, String label, String defaultValue, int maxLength) {
}
