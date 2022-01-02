package controller.tools;

import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class Text extends Label {

    private boolean bold;

    private boolean italic;
    
    private FontWeight fontWeight;
    
    private FontPosture fontPosture;
    
    private int tipo;
    
    private int modulo;
    
    private int campo;
    
    private String variable;

    public Text(String text, double x, double y) {
        setText(text);
        setCursor(Cursor.MOVE);
        setTranslateX(x);
        setTranslateY(y);
        setTextFill(Color.BLACK);
        bold = true;
        italic = false;
        
    }
    
    public void setFontText(String fontFamily,FontWeight fontWeight,FontPosture fontPosture,double size){
        setFont(Font.font(fontFamily, fontWeight, fontPosture, size));
        this.fontWeight=fontWeight;
        this.fontPosture=fontPosture;
    }
            
    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public FontWeight getFontWeight() {
        return fontWeight;
    }

    public FontPosture getFontPosture() {
        return fontPosture;
    }

    public void setFontWeight(FontWeight fontWight) {
        this.fontWeight = fontWight;
    }

    public void setFontPosture(FontPosture fontPosture) {
        this.fontPosture = fontPosture;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int getModulo() {
        return modulo;
    }

    public void setModulo(int modulo) {
        this.modulo = modulo;
    }

    public int getCampo() {
        return campo;
    }

    public void setCampo(int campo) {
        this.campo = campo;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }
    
}
