package ru.s21.rogue_game.model.map;

import ru.s21.rogue_game.model.common.Position;
import ru.s21.rogue_game.model.items.Item;

/**
 * An object that represents an {@code Item} instance on the map reflected by {@code Position}
 */
public record OnMapItem(Item item, Position position) {
}
