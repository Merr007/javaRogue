package ru.s21.rogue_game.model.items;

import ru.s21.rogue_game.model.common.Logger;
import ru.s21.rogue_game.model.entities.Player;

public interface Consumable {
    boolean consume(Player player, int level, Logger log);
    String getOnUseDescription();
}
