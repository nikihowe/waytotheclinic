package uk.ac.cam.cl.waytotheclinic;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RoomType {

    /**
     * blue - toilets
     * green - accessible toilets
     * yellow - stairs
     * red - lift
     * grey - node
     * pink - food
     * black - hallway
     * white - wall
     */
    public static String getColour(int colour) {
        if (colour == 0xFF808080) {
            return "grey";
        } else if ((colour & 0xFFFFFF00) == 0xFFFF0000) {
            return "red";
        } else if (colour == 0xFF002AFF || colour == 0xFF0000FF) {
            return "blue";
        } else if ((colour & 0xFFFFFF00) == 0xFFFFFF00) { // any shade of yellow
            return "yellow";
        } else if (colour == 0xFFCC00FF) {
            return "pink";
        } else if (colour == 0xFF295F29) {
            return "darkgreen";
        } else if ((colour & 0xFFFFFF00) == 0xFF00FF00) {
            return "green";
        } else if (colour == 0xFF000000) {
            return "black";
        } else if (colour == 0xFFFFFFFF) {
            return "white";
        } else if (colour == 0xFFFF9000) {
            return "orange";
        } else if (colour == 0xFF00FFFF) {
            return "lightblue";
        } else if (colour == 0xFF7F007F) {
            return "purple";
        } else {
            System.err.println("strange colour: " + String.format("0x%08X", colour));
            return null;
        }
    }

    // Colours corresponding to rooms
    public static String getVertexType(int colourRGB) {
        String colour = getColour(colourRGB);

        if (colour.equals("grey")) {
            return "Node";
        } else if (colour.equals("red")) {
            return "Lift";
        } else if (colour.equals("blue")) {
            return "Toilet";
        } else if (colour.equals("yellow")) {
            return "Stairs";
        } else if (colour.equals("pink")) {
            return "Food";
        } else if (colour.equals("darkgreen")) {
            return "Accessible Toilet";
        } else if (colour.equals("green")) {
            return "Hall";
        } else if (colour.equals("black")) {
            return "Hall";
        } else if (colour.equals("white")) {
            return "Wall";
        } else if (colour.equals("orange")) {
            return "Entrance";
        } else if (colour.equals("purple")) {
            return "Cash Machine";
        } else {
            System.err.println("strange colour: " + String.format("0x%08X", colour));
            return null;
        }
    }

    public static boolean isGrey(int colour) {
        return (colour & 0x00FFFFFF) == 0x00808080;
    }

    public static boolean notBW(int colour) {
        return !isWhite(colour) && !isBlack(colour);
    }

    public static boolean isWhite(int colour) {
        return (colour & 0x00FFFFFF) == 0x00FFFFFF;
    }

    public static boolean isBlack(int colour) {
        return (colour & 0x00FFFFFF) == 0;
    }

    public static boolean isYellow(int colour) {
        return (colour & 0xFFFFFF00) == 0xFFFFFF00;
    }

    public static boolean isRed(int colour) {
        return (colour & 0xFFFFFF00) == 0xFFFF0000;
    }

    public static boolean isGreen(int colour) {
        return (colour & 0xFFFFFF00) == 0xFF00FF00;
    }

    /** For testing */
    public static void main(String[] args) throws IOException {
        BufferedImage myImage = javax.imageio.ImageIO.read(new File("Levels/eight.png"));
        for (int i = 0; i < myImage.getWidth(); i++) {
            for (int j = 0; j < myImage.getHeight(); j++) {
                System.out.println(String.format("0x%08X", myImage.getRGB(i, j)));
            }
        }
    }
}

