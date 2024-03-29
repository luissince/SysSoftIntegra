package controller.tools;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class LoadFont {

        private final GraphicsEnvironment ge[];

        public LoadFont() {
                ge = new GraphicsEnvironment[11];
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

                        ge[7].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                                        rut(FontTTF.FONT_IBM_QWITCHER_GRYPEN_BOLD)));

                        ge[8].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                                        rut(FontTTF.FONT_IBM_QWITCHER_GRYPEN_REGULAR)));

                        ge[9].registerFont(Font.createFont(Font.TRUETYPE_FONT,
                                        rut(FontTTF.FONT_IBM_DACING_SCRIPT)));
                } catch (FontFormatException | IOException e) {
                }
        }

        private InputStream rut(String s) {
                return this.getClass().getResourceAsStream(s);
        }

}
