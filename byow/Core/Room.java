package byow.Core;
import java.util.Random;

public class Room {
    private int x;
    private int y;
    private int roomHeight;
    private int roomWidth;

    private int[] pointX = new int[4];

    private int[] pointY = new int[4];

    public Room(Random random, long seed, int width, int height) {
        x = (int) RandomUtils.gaussian(random, width / 2, 20);
        y = (int) RandomUtils.gaussian(random, height / 2, 10);
        while (x >= 72 || x < 2) {
            x = (int) RandomUtils.gaussian(random, width / 2, 15);
        }
        while (y >= 22 || y < 2) {
            y = (int) RandomUtils.gaussian(random, height / 2, 8);
        }
        roomHeight = RandomUtils.uniform(random, 4, 7);
        roomWidth = RandomUtils.uniform(random, 4, 7);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRoomWidth() {
        return roomWidth;
    }

    public int getRoomHeight() {
        return roomHeight;
    }

    public void setSideX(int ex, int side) {
        pointX[side] = ex;
    }

    public void setSideY(int why, int side) {
        pointY[side] = why;
    }

    public int[] getPointX() {
        return pointX;
    }

    public int[] getPointY() {
        return pointY;
    }
}
