package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.Tileset;
import byow.TileEngine.TETile;
import java.util.Random;
import java.util.ArrayList;
import edu.princeton.cs.algs4.StdDraw;
import java.io.File;

public class Engine {
    public static final File CWD = new File(System.getProperty("user.dir"));
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    public static final ArrayList<Room> ALLROOMS = new ArrayList<>();
    private int avX;
    private int avY;
    private int countTimes = 0;

    private int teleport1X;
    private int teleport1Y;
    private int teleport2X;
    private int teleport2Y;
    private int cCollect;
    private int tCoins;
    private int disoriented = 0;
    private boolean shield = false;



    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        ter.initialize(WIDTH, HEIGHT + 10);
        ter.createMenuScreen(WIDTH, HEIGHT + 10);
        while (true) {
            String seed = "";
            boolean startGame;
            boolean seedStart = false;
            boolean saveQuit;
            String inputs = "";
            boolean load = false;
            while (true) {
                while (!StdDraw.hasNextKeyTyped()) {
                    StdDraw.pause(100);
                }
                char c = solicitNCharsInput().toLowerCase().charAt(0);
                if (c == 'q') {
                    ter.exitScreen(WIDTH, HEIGHT + 10);
                } else if (c == 'n') {
                    seedStart = startSeed(seed);
                } else if (c == 'l') {
                    seedStart = startSeedLoad(seed);
                    load = true;
                } else if (c == 's') {
                    if (load) {
                        if (seed.equals("")) {
                            seed = loadSave();
                        }
                        File loadFile = Utils.join(CWD, seed + ".txt");
                        if (loadFile.exists()) {
                            inputs = Utils.readContentsAsString(loadFile);
                            startGame = true;
                            break;
                        } else {
                            ter.createMenuScreen(WIDTH, HEIGHT + 10);
                        }
                    }
                    startGame = true;
                    inputs += Character.toLowerCase(c);
                    break;
                } else if (seedStart && Character.isDigit(c)) {
                    seed += c;
                    ter.seedEnterScreen(seed, WIDTH, HEIGHT + 10);
                }
                inputs += c;
            }
            if (startGame) {
                ter.rules();
                TETile[][] world = renderWorld(load, seed, inputs);
                saveQuit = false;
                boolean wholeScreen = false;
                while (tCoins > 0) {
                    while (!StdDraw.hasNextKeyTyped()) {
                        noNextKey(world);
                    }
                    char c = solicitNCharsInput().toLowerCase().charAt(0);
                    if (c != ':' && c != 'q') {
                        inputs += Character.toLowerCase(c);
                    }
                    if (c == ':') {
                        saveQuit = true;
                    } else if (c == 'q' && saveQuit) {
                        saveQuit(seed, inputs);
                        break;
                    } else if (c == 'm') {
                        wholeScreen = !wholeScreen;
                    }
                    inputs += moveCharacter(world, c, seed, load, "");
                    if (wholeScreen) {
                        ter.renderFrame(world, "", cCollect, tCoins);
                    } else {
                        ter.renderFrameClose(world, "", avX, avY, cCollect, tCoins);
                    }
                }
                if (tCoins <= 0) {
                    ter.exitScreen(WIDTH, HEIGHT + 10);
                }
            }
        }
    }

    public boolean startSeed(String seed) {
        ter.seedEnterScreen(seed, WIDTH, HEIGHT + 10);
        return true;
    }

    public boolean startSeedLoad(String seed) {
        ter.seedEnterScreenLoad(seed, WIDTH, HEIGHT + 10);
        return true;
    }

    public void noNextKey(TETile[][] world) {
        StdDraw.pause(100);
        if (StdDraw.isMousePressed()) {
            mousePressed(world);
        }
    }

    public TETile[][] renderWorld(boolean load, String seed, String inputs) {
        TETile[][] world;
        if (!load) {
            ALLROOMS.clear();
            world = new TETile[WIDTH][HEIGHT];
            long seed1 = Long.parseLong(seed);
            generateWorld(world, seed1);
            ter.renderFrame(world, "", cCollect, tCoins);
        } else {
            world = runGame(inputs, load);
            ter.renderFrame(world, "", cCollect, tCoins);
        }
        return world;
    }

    public String loadSave() {
        File recent = Utils.join(CWD, "recent.txt");
        String recentFile = Utils.readContentsAsString(recent);
        File loadFile = Utils.join(CWD, recentFile);
        String inputs1 = Utils.readContentsAsString(loadFile);
        String seedWord = "";
        for (int num = 0; num < inputs1.length(); num++) {
            if (Character.isDigit(inputs1.charAt(num))) {
                seedWord += inputs1.charAt(num);
            }
        }
        return seedWord;
    }

    public void mousePressed(TETile[][] world) {
        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();
        boolean inRange = true;
        if (mouseX < 0 || mouseX >= WIDTH) {
            inRange = false;
        }
        if (mouseY < 0 || mouseY >= HEIGHT) {
            inRange = false;
        }
        if (inRange) {
            String name = tileName(world, mouseX, mouseY);
            ter.renderFrameClose(world, name, avX, avY, cCollect, tCoins);
        }
    }

    public void saveQuit(String seed, String inputs) {
        File file = Utils.join(CWD, seed + ".txt");
        Utils.writeContents(file, inputs);
        File recent = Utils.join(CWD, "recent.txt");
        recent.delete();
        Utils.writeContents(recent, seed + ".txt");
        ter.createMenuScreen(WIDTH, HEIGHT + 10);
    }

    public String tileName(TETile[][] world, int mouseX, int mouseY) {
        if (world[mouseX][mouseY] == Tileset.WALL) {
            return "Wall";
        } else if (world[mouseX][mouseY] == Tileset.FLOOR) {
            return "Floor";
        } else if (world[mouseX][mouseY] == Tileset.AVATAR) {
            return "Avatar";
        } else if (world[mouseX][mouseY] == Tileset.NOTHING) {
            return "Nothing";
        } else if (world[mouseX][mouseY] == Tileset.Coin) {
            return "Coin";
        } else if (world[mouseX][mouseY] == Tileset.Teleport) {
            return "Teleporter";
        } else if (world[mouseX][mouseY] == Tileset.Encounter) {
            return "Encounter";
        } else if (world[mouseX][mouseY] == Tileset.DeathCoin) {
            return "Death Coin";
        } else if (world[mouseX][mouseY] == Tileset.Disoriented) {
            return "Alcohol";
        }
        return "";
    }

    public String solicitNCharsInput() {
        String result = "";
        if (StdDraw.hasNextKeyTyped()) {
            String character = Character.toString(StdDraw.nextKeyTyped());
            result += character;
        }
        return result;
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        ALLROOMS.clear();
        String seed = "";
        input = input.toLowerCase();
        boolean startGame = false;
        boolean seedStart = false;
        boolean saveQuit = false;
        String inputs1 = "";
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        boolean load = false;
        String inputs = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!startGame) {
                if (c == 'n') {
                    seedStart = true;
                    inputs += c;
                } else if (c == 's') {
                    seedStart = false;
                    startGame = true;
                    long seed1 = Long.parseLong(seed);
                    generateWorld(world, seed1);
                    inputs += c;
                } else if (Character.isDigit(input.charAt(i)) && seedStart) {
                    seed += input.charAt(i);
                    inputs += c;
                } else if (c == 'l') {
                    load = true;
                    startGame = true;
                }
            } else {
                if (load) {
                    File recent = Utils.join(CWD, "recent.txt");
                    String recentFile = Utils.readContentsAsString(recent);
                    File loadFile = Utils.join(CWD, recentFile);
                    inputs1 = Utils.readContentsAsString(loadFile);
                    world = runGameNoKeyboard(inputs1, load);
                    load = false;
                    inputs = inputs1;
                }
                if (c == ':') {
                    saveQuit = true;
                } else if (c == 'q' && saveQuit) {
                    if (seed.equals("")) {
                        String seedWord = "";
                        for (int num = 0; num < inputs1.length(); num++) {
                            if (Character.isDigit(inputs1.charAt(num))) {
                                seedWord += inputs1.charAt(num);
                            }
                        }
                        seed = seedWord;
                    }
                    File file = Utils.join(CWD, seed + ".txt");
                    Utils.writeContents(file, inputs);
                    File recent = Utils.join(CWD, "recent.txt");
                    recent.delete();
                    Utils.writeContents(recent, seed + ".txt");
                }
                moveCharacterNoKeyboard(world, c, seed);
                if (c != ':' && c != 'q') {
                    inputs += c;
                }
            }
        }
        return world;
    }

    public TETile[][] runGame(String input, boolean load) {
        ALLROOMS.clear();
        String input1 = "";
        boolean startGame = false;
        boolean seedStart = false;
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!startGame) {
                if (c == 'n') {
                    seedStart = true;
                } else if (c == 's') {
                    seedStart = false;
                    startGame = true;
                    long seed = Long.parseLong(input1);
                    generateWorld(world, seed);
                } else if (Character.isDigit(input.charAt(i)) && seedStart) {
                    input1 += input.charAt(i);
                }
            } else if (!Character.isUpperCase(c)) {
                moveCharacter(world, c, input1, load, input);
            }
        }
        return world;
    }

    public TETile[][] runGameNoKeyboard(String input, boolean load) {
        ALLROOMS.clear();
        String input1 = "";
        boolean startGame = false;
        boolean seedStart = false;
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!startGame) {
                if (c == 'n') {
                    seedStart = true;
                } else if (c == 's') {
                    seedStart = false;
                    startGame = true;
                    long seed = Long.parseLong(input1);
                    generateWorld(world, seed);
                } else if (Character.isDigit(input.charAt(i)) && seedStart) {
                    input1 += input.charAt(i);
                }
            } else if (!Character.isUpperCase(c)) {
                moveCharacterNoKeyboard(world, c, input1);
            }
        }
        return world;
    }

    public void initializeworld(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    public void debugHelper(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            world[x][0] = Tileset.number(x);
        }
        for (int y = 0; y < HEIGHT; y += 1) {
            world[0][y] = Tileset.number(y);
        }
    }

    public void generateWorld(TETile[][] world, long seed) {
        cCollect = 0;
        tCoins = 0;
        ALLROOMS.clear();
        initializeworld(world);
        //debugHelper(world);
        generateRooms(world, seed);
        generateHalls(world, ALLROOMS);
        checkSLRoom(world);
        checkSLRoom(world);
        connectHalves(world);
        addWallsFlowerToFloor(world);
        addCharacter(world, seed);
        addCoins(world, 2, seed);
        createTeleport(world, seed);
        addEncounter(world, seed);
        addDeathCoin(world, seed);
        addDrunk(world, seed);
        addShield(world, seed);
    }

    public void generateRooms(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int numberOfRooms = 100;
        for (int i = 0; i < numberOfRooms; i++) {
            boolean collides = false;
            Room room = new Room(random, seed, WIDTH, HEIGHT);
            int x = room.getX();
            int y = room.getY();
            int roomWidth = room.getRoomWidth();
            int roomHeight = room.getRoomHeight();
            for (int xTile = x; xTile < x + roomWidth; xTile++) {
                for (int yTile = y; yTile < y + roomHeight; yTile++) {
                    if (!world[xTile][yTile].equals(Tileset.NOTHING)) {
                        collides = true;
                        break;
                    }
                }
                if (!collides) {
                    break;
                }
            }
            for (int extraY = y - 1; extraY <= y + roomHeight + 1; extraY++) {
                boolean bool = false;
                if (!world[x - 1][extraY].equals(Tileset.NOTHING)) {
                    collides = true;
                    bool = true;
                }
                if (!world[x + 1 + roomWidth][extraY].equals(Tileset.NOTHING)) {
                    collides = true;
                    bool = true;
                }
                if (bool) {
                    break;
                }
            }
            for (int extraX = x - 1; extraX <= x + roomWidth + 1; extraX++) {
                boolean bool = false;
                if (!world[extraX][y - 1].equals(Tileset.NOTHING)) {
                    collides = true;
                    bool = true;
                }
                if (!world[extraX][y + 1 + roomHeight].equals(Tileset.NOTHING)) {
                    collides = true;
                    bool = true;
                }
                if (bool) {
                    break;
                }
            }
            if (!collides) {
                buildRoom(world, room);
                setRandomPoints(room, seed);
                ALLROOMS.add(room);
            }
        }
    }

    public void buildRoom(TETile[][] world, Room room) {
        int x = room.getX();
        int y = room.getY();
        int width = room.getRoomWidth();
        int height = room.getRoomHeight();
        for (int xTile = x; xTile <= x + width; xTile++) {
            for (int yTile = y; yTile <= y + height; yTile++) {
                if (xTile != x && xTile != x + width && yTile != y && yTile != y + height) {
                    world[xTile][yTile] = Tileset.FLOOR;
                } else {
                    world[xTile][yTile] = Tileset.WALL;
                }
            }
        }
    }

    public void setRandomPoints(Room room, long seed) {
        int x = room.getX();
        int y = room.getY();
        int width = room.getRoomWidth();
        int height = room.getRoomHeight();
        Random random = new Random(seed);
        int randomXNorth = RandomUtils.uniform(random, x + 1, x + width);
        room.setSideX(randomXNorth, 0);
        room.setSideY(y + height, 0);
        int randomXSouth = RandomUtils.uniform(random, x + 1, x + width);
        room.setSideX(randomXSouth, 1);
        room.setSideY(y, 1);
        int randomYEast = RandomUtils.uniform(random, y + 1, y + height);
        room.setSideX(x + width, 2);
        room.setSideY(randomYEast, 2);
        int randomYWest = RandomUtils.uniform(random, y + 1, y + height);
        room.setSideX(x, 3);
        room.setSideY(randomYWest, 3);
    }
    public void generateHalls(TETile[][] world, ArrayList<Room> theseRooms) {
        for (Room room : theseRooms) {
            Room closestRoom = room;
            int closestSide = 0;
            int closestThisSide = 0;
            double closestDiagonal = Integer.MAX_VALUE;
            int[] xCoor = room.getPointX();
            int[] yCoor = room.getPointY();
            for (int i = 0; i < 4; i++) {
                int xValue = xCoor[i];
                int yValue = yCoor[i];
                for (Room otherRoom: theseRooms) {
                    int[] otherXCoor = otherRoom.getPointX();
                    int[] otherYCoor = otherRoom.getPointY();
                    if (!room.equals(otherRoom)) {
                        for (int elem = 0; elem < 4; elem++) {
                            int tXDist = otherXCoor[elem] - xValue;
                            int tYDist = otherYCoor[elem] - yValue;
                            double tDiag;
                            tDiag = Math.sqrt((tXDist * tXDist) + (tYDist * tYDist));
                            if (closestDiagonal >= tDiag) {
                                closestDiagonal = tDiag;
                                closestRoom = otherRoom;
                                closestSide = elem;
                                closestThisSide = i;
                            }
                        }
                    }
                }
            }
            connectRooms(world, room, closestRoom, closestSide, closestThisSide);
        }
    }

    public void connectRooms(TETile[][] world, Room room1, Room room2, int side, int startingSide) {
        int firstX = room1.getPointX()[startingSide];
        int firstY = room1.getPointY()[startingSide];
        int secondX = room2.getPointX()[side];
        int secondY = room2.getPointY()[side];
        world[firstX][firstY] = Tileset.FLOWER;
        world[secondX][secondY] = Tileset.FLOWER;
        if (startingSide == 0) {
            firstY++;
        }
        if (startingSide == 1) {
            firstY--;
        }
        if (startingSide == 2) {
            firstX++;
        }
        if (startingSide == 3) {
            firstX--;
        }
        if (side == 0) {
            secondY++;
        }
        if (side == 1) {
            secondY--;
        }
        if (side == 2) {
            secondX++;
        }
        if (side == 3) {
            secondX--;
        }
        world[firstX][firstY] = Tileset.FLOWER;
        world[secondX][secondY] = Tileset.FLOWER;
        if (startingSide == 0 || startingSide == 1) {
            travelHorFirst(world, firstX, firstY, secondX, secondY, startingSide);
        } else {
            travelVertFirst(world, firstX, firstY, secondX, secondY, startingSide);
        }
        countTimes = 0;
    }

    public void travelHorFirst(TETile[][] world, int x1, int y1, int x2, int y2, int side) {
        if (countTimes > 5) {
            return;
        }
        countTimes++;
        boolean notReached = false;
        boolean goDown;
        if (y1 == y2) {
            goDown = HEIGHT - y1 > HEIGHT / 2;
        } else {
            goDown = y1 > y2;
        }
        while (x1 != x2) {
            if (x2 > x1) {
                if (checkIfAllowed(world, x1 + 1, y1)) {
                    world[x1 + 1][y1] = Tileset.FLOWER;
                    x1++;
                    notReached = false;
                } else {
                    notReached = true;
                }
            }
            if (x2 < x1) {
                if (checkIfAllowed(world, x1 - 1, y1)) {
                    world[x1 - 1][y1] = Tileset.FLOWER;
                    x1--;
                    notReached = false;
                } else {
                    notReached = true;
                }
            }
            while (true) {
                if (notReached) {
                    if (goDown) {
                        if (checkIfAllowed(world, x1, y1 - 1)) {
                            world[x1][y1 - 1] = Tileset.SAND;
                            y1--;
                            break;
                        } else if (checkIfAllowed(world, x1, y1 + 1)) {
                            world[x1][y1 + 1] = Tileset.SAND;
                            y1++;
                            break;
                        } else {
                            if (x1 > x2) {
                                x1--;
                            } else {
                                x1++;
                            }
                            notReached = false;
                        }
                    } else {
                        if (checkIfAllowed(world, x1, y1 + 1)) {
                            world[x1][y1 + 1] = Tileset.SAND;
                            y1++;
                            break;
                        } else if (checkIfAllowed(world, x1, y1 - 1)) {
                            world[x1][y1 - 1] = Tileset.SAND;
                            y1--;
                            break;
                        } else {
                            if (x1 > x2) {
                                x1--;
                            } else {
                                x1++;
                            }
                            notReached = false;
                        }
                    }

                }
                break;
            }
        }
        sandToFlower(world);
        if (y1 != y2) {
            travelVertFirst(world, x1, y1, x2, y2, side);
        }
    }

    public void travelVertFirst(TETile[][] world, int x1, int y1, int x2, int y2, int side) {
        if (countTimes > 5) {
            return;
        }
        countTimes++;
        boolean notReached = false;
        boolean goLeft;
        if (x1 == x2) {
            goLeft = WIDTH - x1 > WIDTH / 2;
        } else {
            goLeft = x1 > x2;
        }
        while (y1 != y2) {
            if (y2 > y1) {
                if (checkIfAllowed(world, x1, y1 + 1)) {
                    world[x1][y1 + 1] = Tileset.FLOWER;
                    y1++;
                    notReached = false;
                } else {
                    notReached = true;
                }
            }
            if (y2 < y1) {
                if (checkIfAllowed(world, x1, y1 - 1)) {
                    world[x1][y1 - 1] = Tileset.FLOWER;
                    y1--;
                    notReached = false;
                } else {
                    notReached = true;
                }
            }
            while (true) {
                if (notReached) {
                    if (goLeft) {
                        if (checkIfAllowed(world, x1 - 1, y1)) {
                            world[x1 - 1][y1] = Tileset.SAND;
                            x1--;
                            break;
                        } else if (checkIfAllowed(world, x1 + 1, y1)) {
                            world[x1 + 1][y1] = Tileset.SAND;
                            x1++;
                            break;
                        } else {
                            if (y1 > y2) {
                                y1--;
                            } else {
                                y1++;
                            }
                            notReached = false;
                        }
                    } else {
                        if (checkIfAllowed(world, x1 + 1, y1)) {
                            world[x1 + 1][y1] = Tileset.SAND;
                            x1++;
                            break;
                        } else if (checkIfAllowed(world, x1 - 1, y1)) {
                            world[x1 - 1][y1] = Tileset.SAND;
                            x1--;
                            break;
                        } else {
                            if (y1 > y2) {
                                y1--;
                            } else {
                                y1++;
                            }
                            notReached = false;
                        }
                    }
                }
                break;
            }
        }
        sandToFlower(world);
        if (x1 != x2) {
            travelHorFirst(world, x1, y1, x2, y2, side);
        }
    }

    public void sandToFlower(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y].equals(Tileset.SAND)) {
                    world[x][y] = Tileset.FLOWER;
                }
            }
        }
    }

    public void checkSLRoom(TETile[][] world) {
        ArrayList<Room> slRoom = new ArrayList<Room>();
        for (Room room : ALLROOMS) {
            int count = 0;
            int x = room.getX();
            int y = room.getY();
            int width = room.getRoomWidth();
            int height = room.getRoomHeight();
            for (int xTile = x; xTile <= x + width; xTile++) {
                for (int yTile = y; yTile <= y + height; yTile++) {
                    if (!(xTile != x && xTile != x + width && yTile != y && yTile != y + height)) {
                        if (world[xTile][yTile] != Tileset.WALL) {
                            count++;
                        }
                    }
                }
            }
            if (count <= 1) {
                slRoom.add(room);
            }
        }
        if (slRoom.size() > 1) {
            generateHalls(world, slRoom);
        }
    }

    public boolean checkIfAllowed(TETile[][] world, int x, int y) {
        if (!world[x][y].equals(Tileset.WALL)) {
            if (!world[x][y].equals(Tileset.FLOOR)) {
                if (!world[x][y].equals(Tileset.SAND)) {
                    if (x >= 1 && x < WIDTH - 1) {
                        return y >= 1 && y < HEIGHT - 1;
                    }
                }
            }
        }
        return false;
    }

    public void connectHalves(TETile[][] world) {
        ArrayList<Integer> xValues = checkIfConnected(world);
        if (!xValues.isEmpty()) {
            int centerX = xValues.get(xValues.size() / 2);
            int centerY = HEIGHT / 2;
            Room leftClosest = ALLROOMS.get(0);
            Room rightClosest = ALLROOMS.get(0);
            double leftClosestDiagonal = Integer.MAX_VALUE;
            double rightClosestDiagonal = Integer.MAX_VALUE;
            int startingSide = 0;
            int side = 0;
            for (Room room : ALLROOMS) {
                int[] x = room.getPointX();
                int[] y = room.getPointY();
                for (int i = 0; i < 4; i++) {
                    int tXDist = x[i] - centerX;
                    int tYDist = y[i] - centerY;
                    double tDiag;
                    tDiag = Math.sqrt((tXDist * tXDist) + (tYDist * tYDist));
                    if (x[i] < centerX) {
                        if (tDiag <= leftClosestDiagonal) {
                            startingSide = i;
                            leftClosest = room;
                            leftClosestDiagonal = tDiag;
                        }
                    } else {
                        if (tDiag <= rightClosestDiagonal) {
                            side = i;
                            rightClosest = room;
                            rightClosestDiagonal = tDiag;
                        }
                    }
                }
            }
            connectRooms(world, leftClosest, rightClosest, side, startingSide);
        }
    }

    public ArrayList<Integer> checkIfConnected(TETile[][] world) {
        int leftSideDungeon = WIDTH / 2;
        boolean foundLeft = false;
        int rightSideDungeon = WIDTH / 2;
        boolean foundRight = false;
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.WALL || world[x][y] == Tileset.FLOWER) {
                    leftSideDungeon = x;
                    foundLeft = true;
                    break;
                }
            }
            if (foundLeft) {
                break;
            }
        }
        for (int x = WIDTH - 1; x >= 0; x--) {
            for (int y = HEIGHT - 1; y >= 0; y--) {
                if (world[x][y] == Tileset.WALL || world[x][y] == Tileset.FLOWER) {
                    rightSideDungeon = x;
                    foundRight = true;
                    break;
                }
            }
            if (foundRight) {
                break;
            }
        }
        ArrayList<Integer> result = new ArrayList<>();
        for (int x = leftSideDungeon; x <= rightSideDungeon; x++) {
            boolean connected = false;
            for (int y = 0; y < HEIGHT; y++) {
                if (world[x][y] == Tileset.WALL || world[x][y] == Tileset.FLOWER) {
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                result.add(x);
            }
        }
        return result;
    }

    public void addWallsFlowerToFloor(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (world[x][y] == Tileset.FLOWER) {
                    world[x][y] = Tileset.FLOOR;
                    if (world[x + 1][y + 1] == Tileset.NOTHING) {
                        world[x + 1][y + 1] = Tileset.WALL;
                    }
                    if (world[x + 1][y] == Tileset.NOTHING) {
                        world[x + 1][y] = Tileset.WALL;
                    }
                    if (world[x + 1][y - 1] == Tileset.NOTHING) {
                        world[x + 1][y - 1] = Tileset.WALL;
                    }
                    if (world[x][y + 1] == Tileset.NOTHING) {
                        world[x][y + 1] = Tileset.WALL;
                    }
                    if (world[x][y - 1] == Tileset.NOTHING) {
                        world[x][y - 1] = Tileset.WALL;
                    }
                    if (world[x - 1][y + 1] == Tileset.NOTHING) {
                        world[x - 1][y + 1] = Tileset.WALL;
                    }
                    if (world[x - 1][y] == Tileset.NOTHING) {
                        world[x - 1][y] = Tileset.WALL;
                    }
                    if (world[x - 1][y - 1] == Tileset.NOTHING) {
                        world[x - 1][y - 1] = Tileset.WALL;
                    }
                }
            }
        }
    }

    public void addCharacter(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int randomRoom = RandomUtils.uniform(random, 0, ALLROOMS.size());
        Room room = ALLROOMS.get(randomRoom);
        int centerX = room.getRoomWidth() / 2 + room.getX();
        int centerY = room.getRoomHeight() / 2 + room.getY();
        world[centerX][centerY] = Tileset.AVATAR;
        avX = centerX;
        avY = centerY;
    }

    public void addCoins(TETile[][] world, int numCoins, long seed) {
        Random random = new Random(seed);
        for (Room room : ALLROOMS) {
            for (int num = numCoins; num > 0; num--) {
                int startX = room.getX();
                int startY = room.getY();
                int x = RandomUtils.uniform(random, startX + 1, startX + room.getRoomWidth());
                int y = RandomUtils.uniform(random, startY + 1, startY + room.getRoomHeight());
                if (world[x][y] != Tileset.Coin) {
                    tCoins++;
                }
                world[x][y] = Tileset.Coin;
            }
        }
    }

    public void createTeleport(TETile[][] world, long seed) {
        Random random = new Random(seed);
        double leftFarthestDiagonal = Integer.MIN_VALUE;
        double rightFarthestDiagonal = Integer.MIN_VALUE;
        int rightX = 0;
        int leftX = 0;
        int rightY = 0;
        int leftY = 0;
        for (Room room : ALLROOMS) {
            int startX = room.getX();
            int startY = room.getY();
            int x = RandomUtils.uniform(random, startX + 2, startX + room.getRoomWidth() - 1);
            int y = RandomUtils.uniform(random, startY + 2, startY + room.getRoomHeight() - 1);
            int tXDist = x - avX;
            int tYDist = y - avY;
            double tDiag;
            tDiag = Math.sqrt((tXDist * tXDist) + (tYDist * tYDist));
            if (x <= avX) {
                if (tDiag >= leftFarthestDiagonal) {
                    leftX = x;
                    leftY = y;
                    leftFarthestDiagonal = tDiag;
                }
            } else {
                if (tDiag >= rightFarthestDiagonal) {
                    rightX = x;
                    rightY = y;
                    rightFarthestDiagonal = tDiag;
                }
            }
        }
        world[leftX][leftY] = Tileset.Teleport;
        world[rightX][rightY] = Tileset.Teleport;
        teleport1X = leftX;
        teleport1Y = leftY;
        teleport2X = rightX;
        teleport2Y = rightY;
    }

    public void addEncounter(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int amount = 4;
        while (amount > 0) {
            int randomRoom = RandomUtils.uniform(random, 0, ALLROOMS.size());
            int roomX = ALLROOMS.get(randomRoom).getX();
            int roomY = ALLROOMS.get(randomRoom).getY();
            int roomWidth = ALLROOMS.get(randomRoom).getRoomWidth();
            int roomHeight = ALLROOMS.get(randomRoom).getRoomHeight();
            int x = RandomUtils.uniform(random, roomX + 1, roomX + roomWidth - 1);
            int y = RandomUtils.uniform(random, roomY + 1, roomY + roomHeight - 1);
            if (world[x][y] != Tileset.Coin && world[x][y] != Tileset.Teleport) {
                world[x][y] = Tileset.Encounter;
                amount--;
            }
        }
    }

    public String moveCharacter(TETile[][] world, char c, String s, boolean load, String string) {
        String inputs = "";
        if (disoriented > 0) {
            if (c == 'w') {
                c = 's';
            } else if (c == 's') {
                c = 'w';
            } else if (c == 'a') {
                c = 'd';
            } else if (c == 'd') {
                c = 'a';
            }
        }
        if (c == 'w') {
            inputs = moveCharH(world, s, 0, 1, load, string);
        } else if (c == 's') {
            inputs = moveCharH(world, s, 0, -1, load, string);
        } else if (c == 'd') {
            inputs = moveCharH(world, s, 1, 0, load, string);
        } else if (c == 'a') {
            inputs = moveCharH(world, s, -1, 0, load, string);
        }
        disoriented--;
        return inputs;
    }

    public String moveCharH(TETile[][] world, String s, int x, int y, boolean load, String str) {
        String inputs = "";
        if (world[avX + x][avY + y] != Tileset.WALL) {
            if (world[avX + x][avY + y] == Tileset.DeathCoin) {
                if (shield) {
                    shield = false;
                } else {
                    ter.loseScreen(cCollect);
                }
            } else if (world[avX + x][avY + y] == Tileset.Coin) {
                cCollect++;
                tCoins--;
            } else if (world[avX + x][avY + y] == Tileset.Encounter) {
                if (load) {
                    inputs = encounterLoad(avX, avY, s, tCoins, str);
                } else {
                    inputs = encounter(avX, avY, s, tCoins);
                }
            } else if (world[avX + x][avY + y] == Tileset.Disoriented) {
                disoriented = 5;
            } else if (world[avX + x][avY + y] == Tileset.Shield) {
                shield = true;
            }
            if (world[avX + x][avY + y] == Tileset.Teleport) {
                world[avX][avY] = Tileset.FLOOR;
                teleport(world, avX + x, avY + y);
            } else {
                world[avX][avY] = Tileset.FLOOR;
                world[avX + x][avY + y] = Tileset.AVATAR;
                avY += y;
                avX += x;
            }
        }
        return inputs;
    }

    public void moveCharacterNoKeyboard(TETile[][] world, char c, String seed) {
        if (c == 'w') {
            if (world[avX][avY + 1] != Tileset.WALL) {
                if (world[avX][avY + 1] == Tileset.Coin) {
                    cCollect++;
                    tCoins--;
                }
                if (world[avX][avY + 1] == Tileset.Teleport) {
                    world[avX][avY] = Tileset.FLOOR;
                    teleport(world, avX, avY + 1);
                } else if (world[avX][avY + 1] == Tileset.Encounter) {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX][avY + 1] = Tileset.AVATAR;
                    avY++;
                } else {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX][avY + 1] = Tileset.AVATAR;
                    avY++;
                }
            }
        } else if (c == 's') {
            if (world[avX][avY - 1] != Tileset.WALL) {
                if (world[avX][avY - 1] == Tileset.Coin) {
                    cCollect++;
                    tCoins--;
                }
                if (world[avX][avY - 1] == Tileset.Teleport) {
                    world[avX][avY] = Tileset.FLOOR;
                    teleport(world, avX, avY - 1);
                } else if (world[avX][avY - 1] == Tileset.Encounter) {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX][avY - 1] = Tileset.AVATAR;
                    avY--;
                } else {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX][avY - 1] = Tileset.AVATAR;
                    avY--;
                }
            }
        } else if (c == 'd') {
            if (world[avX + 1][avY] != Tileset.WALL) {
                if (world[avX + 1][avY] == Tileset.Coin) {
                    cCollect++;
                    tCoins--;
                }
                if (world[avX + 1][avY] == Tileset.Teleport) {
                    world[avX][avY] = Tileset.FLOOR;
                    teleport(world, avX + 1, avY);
                } else if (world[avX + 1][avY] == Tileset.Encounter) {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX + 1][avY] = Tileset.AVATAR;
                    avX++;
                } else {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX + 1][avY] = Tileset.AVATAR;
                    avX++;
                }
            }
        } else if (c == 'a') {
            if (world[avX - 1][avY] != Tileset.WALL) {
                if (world[avX - 1][avY] == Tileset.Coin) {
                    cCollect++;
                    tCoins--;
                }
                if (world[avX - 1][avY] == Tileset.Teleport) {
                    world[avX][avY] = Tileset.FLOOR;
                    teleport(world, avX - 1, avY);
                } else if (world[avX - 1][avY] == Tileset.Encounter) {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX - 1][avY] = Tileset.AVATAR;
                    avX--;
                } else {
                    world[avX][avY] = Tileset.FLOOR;
                    world[avX - 1][avY] = Tileset.AVATAR;
                    avX--;
                }
            }
        }
    }

    public void teleport(TETile[][] world, int x, int y) {
        if (x == teleport1X) {
            avX = teleport2X - 1;
            avY = teleport2Y;
        } else {
            avX = teleport1X + 1;
            avY = teleport1Y;
        }
        world[avX][avY] = Tileset.AVATAR;
    }

    public TETile[][] encounterWorld(String seed) {
        TETile[][] eWorld = new TETile[80][30];
        long seed1 = Long.parseLong(seed);
        Random random = new Random(seed1);
        for (int x = 0; x < 80; x += 1) {
            for (int y = 0; y < 30; y += 1) {
                eWorld[x][y] = Tileset.NOTHING;
            }
        }
        for (int x = 29; x < 60; x += 1) {
            for (int y = 4; y < 15; y += 1) {
                if (RandomUtils.uniform(random, 0, 10) >= 6) {
                    eWorld[x][y] = Tileset.Coin;
                } else {
                    eWorld[x][y] = Tileset.FLOOR;
                }
            }
        }
        for (int x = 29; x < 60; x += 1) {
            for (int y = 4; y < 15; y += 1) {
                if (RandomUtils.uniform(random, 0, 10) >= 9) {
                    eWorld[x][y] = Tileset.DeathCoin;
                }
            }
        }
        for (int x = 28; x < 61; x++) {
            eWorld[x][3] = Tileset.WALL;
            eWorld[x][15] = Tileset.WALL;
        }
        for (int y = 4; y < 15; y++) {
            eWorld[28][y] = Tileset.WALL;
            eWorld[60][y] = Tileset.WALL;
        }
        eWorld[avX][avY] = Tileset.AVATAR;
        return eWorld;
    }

    public String encounter(int oX, int oY, String seed, int total) {
        avX = 44;
        avY = 10;
        TETile[][] eWorld = encounterWorld(seed);
        ter.renderEncounter(eWorld);
        long startTime = System.currentTimeMillis();
        long eTime = System.currentTimeMillis() - startTime;
        int collected = 0;
        String inputs = "";
        while (eTime <= 10000) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = solicitNCharsInput().toLowerCase().charAt(0);
                if (c == 'w') {
                    if (eWorld[avX][avY + 1] != Tileset.WALL) {
                        if (eWorld[avX][avY + 1] == Tileset.DeathCoin) {
                            ter.loseScreen(cCollect + collected);
                        } else if (eWorld[avX][avY + 1] == Tileset.Coin) {
                            collected++;
                        }
                        eWorld[avX][avY] = Tileset.FLOOR;
                        eWorld[avX][avY + 1] = Tileset.AVATAR;
                        avY++;
                    }
                } else if (c == 's') {
                    if (eWorld[avX][avY - 1] != Tileset.WALL) {
                        if (eWorld[avX][avY - 1] == Tileset.DeathCoin) {
                            ter.loseScreen(cCollect + collected);
                        } else if (eWorld[avX][avY - 1] == Tileset.Coin) {
                            collected++;
                        }
                        eWorld[avX][avY] = Tileset.FLOOR;
                        eWorld[avX][avY - 1] = Tileset.AVATAR;
                        avY--;
                    }
                } else if (c == 'd') {
                    if (eWorld[avX + 1][avY] != Tileset.WALL) {
                        if (eWorld[avX + 1][avY] == Tileset.DeathCoin) {
                            ter.loseScreen(cCollect + collected);
                        } else if (eWorld[avX + 1][avY] == Tileset.Coin) {
                            collected++;
                        }
                        eWorld[avX][avY] = Tileset.FLOOR;
                        eWorld[avX + 1][avY] = Tileset.AVATAR;
                        avX++;
                    }
                } else if (c == 'a') {
                    if (eWorld[avX - 1][avY] != Tileset.WALL) {
                        if (eWorld[avX - 1][avY] == Tileset.DeathCoin) {
                            ter.loseScreen(cCollect + collected);
                        } else if (eWorld[avX - 1][avY] == Tileset.Coin) {
                            collected++;
                        }
                        eWorld[avX][avY] = Tileset.FLOOR;
                        eWorld[avX - 1][avY] = Tileset.AVATAR;
                        avX--;
                    }
                }
                inputs += Character.toUpperCase(c);
            }
            eTime = System.currentTimeMillis() - startTime;
            long seconds = (eTime / 1000) % 60;
            ter.renderEncounterWorld(eWorld, collected, (int) seconds);
        }
        ter.renderEncounterEnd(collected);
        cCollect += collected;
        avX = oX;
        avY = oY;
        tCoins = total;
        return inputs;
    }

    public String encounterLoad(int oX, int oY, String seed, int total, String string) {
        TETile[][] eWorld = encounterWorld(seed);
        avX = 39;
        avY = 14;
        String move = "";
        for (int i = 0; i < string.length(); i++) {
            if (Character.isUpperCase(string.charAt(i))) {
                move += string.charAt(i);
            }
        }
        int collected = 0;
        int i = 0;
        while (i < move.length()) {
            char c = move.toLowerCase().charAt(i);
            if (c == 'w') {
                if (eWorld[avX][avY + 1] != Tileset.WALL) {
                    if (eWorld[avX][avY + 1] == Tileset.Coin) {
                        collected++;
                    }
                    eWorld[avX][avY] = Tileset.FLOOR;
                    eWorld[avX][avY + 1] = Tileset.AVATAR;
                    avY++;
                }
            } else if (c == 's') {
                if (eWorld[avX][avY - 1] != Tileset.WALL) {
                    if (eWorld[avX][avY - 1] == Tileset.Coin) {
                        collected++;
                    }
                    eWorld[avX][avY] = Tileset.FLOOR;
                    eWorld[avX][avY - 1] = Tileset.AVATAR;
                    avY--;
                }
            } else if (c == 'd') {
                if (eWorld[avX + 1][avY] != Tileset.WALL) {
                    if (eWorld[avX + 1][avY] == Tileset.Coin) {
                        collected++;
                    }
                    eWorld[avX][avY] = Tileset.FLOOR;
                    eWorld[avX + 1][avY] = Tileset.AVATAR;
                    avX++;
                }
            } else if (c == 'a') {
                if (eWorld[avX - 1][avY] != Tileset.WALL) {
                    if (eWorld[avX - 1][avY] == Tileset.Coin) {
                        collected++;
                    }
                    eWorld[avX][avY] = Tileset.FLOOR;
                    eWorld[avX - 1][avY] = Tileset.AVATAR;
                    avX--;
                }
            }
            i++;
        }
        cCollect += collected;
        avX = oX;
        avY = oY;
        tCoins = total;
        return move.toUpperCase();
    }

    public void addDeathCoin(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int amount = 10;
        while (amount > 0) {
            int randomRoom = RandomUtils.uniform(random, 0, ALLROOMS.size());
            int roomX = ALLROOMS.get(randomRoom).getX();
            int roomY = ALLROOMS.get(randomRoom).getY();
            int roomWidth = ALLROOMS.get(randomRoom).getRoomWidth();
            int roomHeight = ALLROOMS.get(randomRoom).getRoomHeight();
            int x = RandomUtils.uniform(random, roomX + 1, roomX + roomWidth - 1);
            int y = RandomUtils.uniform(random, roomY + 1, roomY + roomHeight - 1);
            if (world[x][y] != Tileset.Coin && world[x][y] != Tileset.Teleport) {
                if (world[x][y] != Tileset.Encounter) {
                    world[x][y] = Tileset.DeathCoin;
                    amount--;
                }
            }
        }
    }

    public void addDrunk(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int amount = 10;
        while (amount > 0) {
            int randomRoom = RandomUtils.uniform(random, 0, ALLROOMS.size());
            int roomX = ALLROOMS.get(randomRoom).getX();
            int roomY = ALLROOMS.get(randomRoom).getY();
            int roomWidth = ALLROOMS.get(randomRoom).getRoomWidth();
            int roomHeight = ALLROOMS.get(randomRoom).getRoomHeight();
            int x = RandomUtils.uniform(random, roomX + 1, roomX + roomWidth - 1);
            int y = RandomUtils.uniform(random, roomY + 1, roomY + roomHeight - 1);
            if (world[x][y] != Tileset.Coin && world[x][y] != Tileset.Teleport) {
                if (world[x][y] != Tileset.Encounter && world[x][y] != Tileset.DeathCoin) {
                    world[x][y] = Tileset.Disoriented;
                    amount--;
                }
            }
        }
    }

    public void addShield(TETile[][] world, long seed) {
        Random random = new Random(seed);
        int amount = 2;
        while (amount > 0) {
            int randomRoom = RandomUtils.uniform(random, 0, ALLROOMS.size());
            int roomX = ALLROOMS.get(randomRoom).getX();
            int roomY = ALLROOMS.get(randomRoom).getY();
            int roomWidth = ALLROOMS.get(randomRoom).getRoomWidth();
            int roomHeight = ALLROOMS.get(randomRoom).getRoomHeight();
            int x = RandomUtils.uniform(random, roomX + 1, roomX + roomWidth - 1);
            int y = RandomUtils.uniform(random, roomY + 1, roomY + roomHeight - 1);
            if (world[x][y] != Tileset.Coin && world[x][y] != Tileset.Teleport) {
                if (world[x][y] != Tileset.Encounter && world[x][y] != Tileset.DeathCoin) {
                    world[x][y] = Tileset.Shield;
                    amount--;
                }
            }
        }
    }
}
