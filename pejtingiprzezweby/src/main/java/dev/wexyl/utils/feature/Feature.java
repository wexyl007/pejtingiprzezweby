package dev.wexyl.utils.feature;

/** Opis jednej funkcji widocznej w menu. */
public record Feature(String id, String name, String description, boolean defaultEnabled) {
}
