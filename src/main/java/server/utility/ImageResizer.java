package server.utility;

import static server.constants.ImageSize.BACKGROUND_HEIGHT;
import static server.constants.ImageSize.BACKGROUND_WIDTH;
import static server.constants.ImageSize.THUMBNAIL_DIM;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageResizer {

  public static BufferedImage resizeToBackgroundImage(BufferedImage originalImage) {
    BufferedImage resized = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    BufferedImage padded = Scalr.pad(resized, BACKGROUND_WIDTH, Color.white);
    int x = (padded.getWidth() - BACKGROUND_WIDTH) / 2;
    int y = (padded.getHeight() - BACKGROUND_HEIGHT) / 2;

    return Scalr.crop(padded, x, y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
  }

  public static BufferedImage resizeToAvatarImage(BufferedImage originalImage) {
    BufferedImage resized = Scalr.resize(originalImage, Scalr.Method.QUALITY, Scalr.Mode.AUTOMATIC, THUMBNAIL_DIM, THUMBNAIL_DIM);
    BufferedImage padded = Scalr.pad(resized, THUMBNAIL_DIM, Color.white);
    int xAndY = (padded.getWidth() - THUMBNAIL_DIM) / 2;

    return Scalr.crop(padded, xAndY, xAndY, THUMBNAIL_DIM, THUMBNAIL_DIM);
  }
}
