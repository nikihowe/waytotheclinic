import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RoomType {

    /**
    blue - toilets
    green - accessible toilets
    yellow - stairs
    red - lift
    grey - node
    pink - food
    black - hallway
    white - wall
     */
    public static String getColour(int colour) {
        if (colour == 0xFF808080) {
            return "grey";
        } else if ((colour & 0xFFFFFF00) == 0xFFFF0000) {
            return "red";
        } else if (colour == 0xFF002AFF) {
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
        } else {
            System.err.println("strange colour: " + String.format("0x%08X", colour));
            return "no colour found";
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

    /** For testing only */
    public static void main(String[] args) throws IOException {
        BufferedImage myImage = javax.imageio.ImageIO.read(new File("Levels/eight.png"));
        for (int i = 0; i < myImage.getWidth(); i++) {
            for (int j = 0; j < myImage.getHeight(); j++) {
                System.out.println(String.format("0x%08X", myImage.getRGB(i, j)));
            }
        }
    }
}

