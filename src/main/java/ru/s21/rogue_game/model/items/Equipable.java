package ru.s21.rogue_game.model.items;

import ru.s21.rogue_game.model.common.Logger;
import ru.s21.rogue_game.model.entities.Player;

public interface Equipable {
    void equip(Player player, Logger log);
    void unequip(Player player, Logger log);
    String getEquipName();
}
