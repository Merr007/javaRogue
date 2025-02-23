package ru.s21.rogue_game.model;

import ru.s21.rogue_game.data.InfoProcessor;
import ru.s21.rogue_game.data.StatisticsItem;
import ru.s21.rogue_game.model.common.Logger;
import ru.s21.rogue_game.model.common.Position;
import ru.s21.rogue_game.model.entities.Character;
import ru.s21.rogue_game.model.entities.Enemy;
import ru.s21.rogue_game.model.entities.Player;
import ru.s21.rogue_game.model.items.*;
import ru.s21.rogue_game.model.map.Corridor;
import ru.s21.rogue_game.model.map.LevelMap;
import ru.s21.rogue_game.model.map.OnMapItem;
import ru.s21.rogue_game.model.map.Room;
import ru.s21.rogue_game.model.map.generator.EntitiesGenerator;
import java.util.List;

public class RogueGame {
    private int goldAmount;
    private int currentLevel;
    private final Logger logger;
    private final Player player;
    private LevelMap levelMap;
    private StatisticsItem statisticsItem;

    public RogueGame() {
        currentLevel = 1;
        generateLevel();
        this.player = new Player(
                20,
                4,
                2,
                new Satchel(),
                EntitiesGenerator.generatePlayerPosition(levelMap)
        );
        logger = new Logger();
        goldAmount = 0;
        Character.setLogger(logger);
        statisticsItem = new StatisticsItem();
        statisticsItem.setLevel(currentLevel);
    }

    private void generateLevel() {
        levelMap = new LevelMap();
        levelMap.generateMap();
        Character.setLevel(currentLevel);
        generateEnemies();
        generateItems();
        if (currentLevel != 1 && player != null) {
            this.player.setPosition(EntitiesGenerator.generatePlayerPosition(levelMap));
        }
    }

    private void generateEnemies() {
        levelMap.setEnemies(EntitiesGenerator.generateEnemies(levelMap));
    }

    private void generateItems() {
        levelMap.setItems(EntitiesGenerator.generateItems(levelMap));
    }

    public void moveEnemy() {
        Room currentRoom = getCurrentRoom(player.getPosition());
        List<Character> characters = levelMap.getEnemies().get(currentRoom);
        if (characters != null && !characters.isEmpty()) {
            characters.removeIf(enemy -> {
                if (enemy.isAlive()) {
                    if (isEnemySeePlayer(enemy)) {
                        if (!((Enemy) enemy).moveToPlayer(player, currentRoom, characters)) {
                            enemy.attack(player);
                            statisticsItem.setAttacksReceived(player.getMissedStrokes());
                        }
                    } else {
                        enemy.move(characters, currentRoom);
                    }
                    return !enemy.isAlive();
                } else {
                    goldAmount += GoldItem.getFromEnemy(enemy.getStrength());
                    statisticsItem.setGold(goldAmount);
                    statisticsItem.setEnemyKilled(statisticsItem.getEnemyKilled() + 1);
                    return true;
                }
            });
        }
    }


    public Room getPlayerRoom() {
        return getCurrentRoom(player.getPosition());
    }

    private boolean isEnemySeePlayer(Character enemy) {
        int agr = enemy.getAggression();

        Position enemyPosition = enemy.getPosition();
        Position playerPosition = player.getPosition();

        int absX = Math.abs(playerPosition.getX() - enemyPosition.getX());
        int absY = Math.abs(playerPosition.getY() - enemyPosition.getY());

        int distance;
        if (absX == 0 || absY == 0) {
            distance = (absX == 0) ? absY : absX;
        } else {
            distance = (int) Math.sqrt(absX * absX + absY * absY);
        }

        return agr >= distance;
    }


    public void movePlayer(int xMove, int yMove) {
        if (!player.isParalyzed()) {
            Position p = player.getPosition();
            Position newPosition = new Position(
                    p.getX() + xMove,
                    p.getY() + yMove
            );

            if (checkPosition(newPosition)) {
                // если выполняется условие, значит клетка находится в пределах комнаты или коридоров

                var currentRoom = getCurrentRoom(newPosition);
                if (currentRoom != null) {
                    // если находимся в комнате, нужно проверить атаку на врагов и переход на следующий уровень
                    var transitionPoint = currentRoom.getTransitionPoint();
                    if (transitionPoint != null && transitionPoint.equals(newPosition)) {
                        currentLevel++;
                        statisticsItem.setLevel(currentLevel);
                        InfoProcessor.writeSaveGame(this);
                        generateLevel();
                        // переход на следующий уровень, так как коснулись двери для перехода
                    } else {
                        List<Character> characters = levelMap.getEnemies().get(currentRoom);

                        boolean isAttackEnemy = false;
                        for (Character character : characters) {
                            if (character.getPosition().equals(newPosition)) {
                                // атакуем врага
                                player.attack(character);
                                statisticsItem.setAttacksDealt(player.getStruckStrokes());
                                isAttackEnemy = true;
                            }
                        }
                        if (!isAttackEnemy) {
                            player.makeMove(newPosition);
                            statisticsItem.setTilesPassed(statisticsItem.getTilesPassed() + 1);
                        }
                    }
                } else {
                    player.makeMove(newPosition);
                }
            }
        } else {
            player.setParalyzed(false);
        }
        player.updateStats();
    }

    private boolean checkPosition(Position newPosition) {
        for (Corridor corridor : levelMap.getCorridors()) {
            if (corridor.getCorridorCoordinatesList().contains(newPosition)) {
                corridor.setExplored(true);
                return true;
            }
        }

        Room currentRoom = getCurrentRoom(player.getPosition()); // Остается null, если игрок в коридоре

        if (currentRoom != null) {
            // если игрок находится в какой-то из комнат,
            // проверяем не находится ли желаемая позиция границей комнаты
            return !currentRoom.getBorderCoordinatesList().contains(newPosition);
        } else {
            return getCurrentRoom(newPosition) != null;
        }
    }


    private Room getCurrentRoom(Position p) {

        for (var entry : levelMap.getRoomMap().entrySet()) {
            var room = entry.getValue();

            var roomBorderTopLeftPosition = room.getBorderTopLeftPosition();
            var roomBorderBottomRightPosition = room.getBorderBottomRightPosition();

            if (roomBorderTopLeftPosition.getX() < p.getX() &&
                roomBorderBottomRightPosition.getX() > p.getX() &&
                roomBorderTopLeftPosition.getY() < p.getY() &&
                roomBorderBottomRightPosition.getY() > p.getY()
            ) {
                room.setExplored(true);
                return room; // Нашли комнату — сразу возвращаем её
            }
        }

        return null; // Если не нашли, возвращаем null
    }

    public LevelMap getLevelMap() {
        return levelMap;
    }

    public int getGoldAmount() {
        return goldAmount;
    }

    public void setGoldAmount(int goldAmount) {
        this.goldAmount = goldAmount;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public Logger getLogger() {
        return logger;
    }

    public Player getPlayer() {
        return player;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public StatisticsItem getStatisticsItem() {
        return statisticsItem;
    }

    public void setStatisticsItem(StatisticsItem statisticsItem) {
        this.statisticsItem = statisticsItem;
    }

    public void checkItemOnMap() {
        Room currentRoom = getCurrentRoom(player.getPosition());
        List<OnMapItem> onMapItems = levelMap.getItems().get(currentRoom);
        if (onMapItems != null && !onMapItems.isEmpty()) {
            OnMapItem itemToRemove = null;
            for (OnMapItem onMapItem : onMapItems) {
                if (onMapItem.position().equals(player.getPosition())) {
                    Item item = onMapItem.item();
                    logger.add(item.getOnAcquireDescription());
                    if (item instanceof GoldItem goldItem) {
                        goldAmount = goldAmount + goldItem.getAmount();
                        statisticsItem.setGold(goldAmount);
                        itemToRemove = onMapItem;
                        continue;
                    }
                    if (player.getSatchel().addItem(onMapItem.item())) {
                        itemToRemove = onMapItem;
                    }
                }
            }
            if (itemToRemove != null) {
                onMapItems.remove(itemToRemove);
            }
        }

    }

    public void useWeapon(int choice) {
        var weaponItems = player.getSatchel()
                .getItems()
                .stream()
                .filter(item -> item instanceof WeaponItem)
                .map(item -> (WeaponItem) item)
                .toList();
        if (weaponItems.size() >= choice) {
            weaponItems.get(choice - 1).equip(player, logger);
        }
    }

    public void useFood(int choice) {
        var foodItems = player.getSatchel()
                .getItems()
                .stream()
                .filter(item -> item instanceof FoodItem)
                .map(item -> (FoodItem) item)
                .toList();
        if (foodItems.size() >= choice) {
            FoodItem foodItem = foodItems.get(choice - 1);
            if (foodItem.consume(player, currentLevel, logger)) {
                logger.add(foodItem.getOnUseDescription());
                player.getSatchel().removeItem(foodItem);
                statisticsItem.setFoodConsumed(statisticsItem.getFoodConsumed() + 1);
            }
        }
    }

    public void usePotion(int choice) {
        var potionItems = player.getSatchel()
                .getItems()
                .stream()
                .filter(item -> item instanceof PotionItem)
                .map(item -> (PotionItem) item)
                .toList();
        if (potionItems.size() >= choice) {
            PotionItem potionItem = potionItems.get(choice - 1);
            if (potionItem.consume(player, currentLevel, logger)) {
                logger.add(potionItem.getOnUseDescription());
                player.getSatchel().removeItem(potionItem);
                statisticsItem.setPotionsConsumed(statisticsItem.getPotionsConsumed() + 1);
            }
        }
    }

    public void useScroll(int choice) {
        var scrollItems = player.getSatchel()
                .getItems()
                .stream()
                .filter(item -> item instanceof ScrollItem)
                .map(item -> (ScrollItem) item)
                .toList();
        if (scrollItems.size() >= choice) {
            ScrollItem scrollItem = scrollItems.get(choice - 1);
            if (scrollItem.consume(player, currentLevel, logger)) {
                logger.add(scrollItem.getOnUseDescription());
                player.getSatchel().removeItem(scrollItem);
                statisticsItem.setScrollsConsumed(statisticsItem.getScrollsConsumed() + 1);
            }
        }
    }

}
