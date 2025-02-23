package ru.s21.rogue_game;

import ru.s21.rogue_game.controller.GameController;
import ru.s21.rogue_game.data.InfoProcessor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        GameController gameController = new GameController();
        gameController.run();
    }
}
