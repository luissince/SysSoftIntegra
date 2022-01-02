
package controller.tools;

import java.awt.Font;
import javafx.scene.image.ImageView;

public class CodBar extends ImageView {
    
    private String texto;
    
    private Font font;
    
    private int tipo;
    
    private int modulo;
    
    private int campo;
    
    private String variable;
    
    public CodBar(String value,double x,double y,Font f){
        texto=value;
        setTranslateX(x);
        setTranslateY(y);
        font=f;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
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
