package controller.tools;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class LoadFont {

    private final GraphicsEnvironment ge[];

    public LoadFont() {
        ge = new GraphicsEnvironment[8];
        for (int i = 0; i < ge.length; i++) {
            ge[i] = GraphicsEnvironment.getLocalGraphicsEnvironment();
        }
    }

    public void loadFont() {
        try {
            ge[0].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_CONSOLAS)));
            ge[1].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_ROBOTO_BOLD)));
            ge[2].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_ROBOTO_REGULAR)));
            ge[4].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_RANCHES_REGULAR)));
            ge[5].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_IBM_PLEX_REGULAR)));
            ge[6].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                    rut(FontTTF.FONT_IBM_PLEX_BOLD)));
        } catch (FontFormatException | IOException e) {
            Tools.println("Error en cargar fuentes: "+e.getLocalizedMessage());
        }
    }

    private InputStream rut(String s) {
        return this.getClass().getResourceAsStream(s);
    }

}
