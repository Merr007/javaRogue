package ru.s21.rogue_game.model.entities;

import ru.s21.rogue_game.model.map.Room;

import java.util.List;

public interface Enemy {
    boolean moveToPlayer(Player player, Room room, List<Character> characters);

}
