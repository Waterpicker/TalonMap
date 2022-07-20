package me.talonos.talonmap.lib;

import me.talonos.talonmap.Talonmap;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageUtil {
    public static BufferedImage loadImage(String fileName) {
        try 
        {
            return ImageIO.read(new ByteArrayInputStream(Base64.getMimeDecoder().decode(fileName)));
        }
        catch (IOException e) {
        	System.err.println("Warning! Exception Getting: "+fileName); 
        }
        return new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
    }
}
