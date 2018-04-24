package de.gutenko.roguelike.data;

/**
 * Centralized input management.
 */
public class Input {

    private static boolean tapEvent = false;
    public static float touchX, touchY;

    public static boolean wasTapEvent() {
        boolean b = tapEvent;
        tapEvent = false;
        return b;
    }

    public static void touchEventDown(float x, float y, int index) {
        touchX = x;
        touchY = y;
        tapEvent = true;
    }
    public static void touchEventUp(float x, float y, int index) {
        //touchX = x;
        //touchY = y;
    }
    public static void touchEventMove(float x, float y, int index) {
        //touchX = x;
        //touchY = y;
    }
}
