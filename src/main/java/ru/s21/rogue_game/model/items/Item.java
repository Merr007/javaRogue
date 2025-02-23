package ru.s21.rogue_game.model.items;

import ru.s21.rogue_game.model.common.GameEntity;

public interface Item {
    GameEntity getEntity();
    String getOnAcquireDescription();
}
