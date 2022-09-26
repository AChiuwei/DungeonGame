package byow.TileEngine;

import java.awt.Color;

/**
 * Contains constant tile objects, to avoid having to remake the same tiles in different parts of
 * the code.
 *
 * You are free to (and encouraged to) create and add your own tiles to this file. This file will
 * be turned in with the rest of your code.
 *
 * Ex:
 *      world[x][y] = Tileset.FLOOR;
 *
 * The style checker may crash when you try to style check this file due to use of unicode
 * characters. This is OK.
 */

public class Tileset {
    //⟟⦲§
    public static final TETile AVATAR = new TETile('ඞ', Color.white, Color.black, "you");
    public static final TETile WALL = new TETile('#', new Color(216, 128, 128), Color.darkGray,
            "wall");
    public static final TETile FLOOR = new TETile('·', new Color(128, 192, 128), Color.black,
            "floor");
    public static final TETile NOTHING = new TETile(' ', Color.black, Color.black, "nothing");
    public static final TETile GRASS = new TETile('"', Color.green, Color.black, "grass");
    public static final TETile Disoriented = new TETile('≈', Color.blue, Color.black, "disoriented");
    public static final TETile FLOWER = new TETile('❀', Color.magenta, Color.pink, "flower");
    public static final TETile LOCKED_DOOR = new TETile('█', Color.orange, Color.black,
            "locked door");
    public static final TETile UNLOCKED_DOOR = new TETile('▢', Color.orange, Color.black,
            "unlocked door");
    public static final TETile SAND = new TETile('▒', Color.yellow, Color.black, "sand");
    public static final TETile MOUNTAIN = new TETile('▲', Color.gray, Color.black, "mountain");
    public static final TETile TREE = new TETile('♠', Color.green, Color.black, "tree");

    public static final TETile Coin = new TETile('●', Color.yellow, Color.black, "Coin");
    public static final TETile Teleport = new TETile('§', new Color(0, 100, 100), Color.black, "Teleport");
    public static final TETile Encounter = new TETile('E', new Color(87, 255, 51), Color.black, "Encounter");
    public static final TETile DeathCoin = new TETile('●', new Color(255, 0, 0), Color.black, "DeathCoin");
    public static final TETile Shield = new TETile('҉', Color.CYAN, Color.black, "Shield");

    public static TETile number(int num) {
        String string = "" + num;
        char c = string.charAt(0);
        return new TETile(c, Color.red, Color.black, "tree");
    }
}


