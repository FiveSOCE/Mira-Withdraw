package com.mira.withdraw.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    private Text() {}

    public static Component c(String value) {
        return LEGACY.deserialize(value == null ? "" : value).decoration(TextDecoration.ITALIC, false);
    }
}
