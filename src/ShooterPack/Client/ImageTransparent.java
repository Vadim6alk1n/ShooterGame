package ShooterPack.Client;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageTransparent {
    //make image transparent by colorkey
    public static Image makeTransparent(Image img, int cr,int cg, int cb)
    {
        int w = (int) img.getWidth();
        int h = (int) img.getHeight();
        //read image as array of pixels
        WritableImage outputImage = new WritableImage(w,h);
        PixelReader reader = img.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                //read pixel
                int argb = reader.getArgb(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (r == cr
                        && g == cg
                        && b == cb) {
                    argb &= 0x00FFFFFF;
                }

                //write pixel
                writer.setArgb(x, y, argb);
            }
        }
        return outputImage;
    }
}
