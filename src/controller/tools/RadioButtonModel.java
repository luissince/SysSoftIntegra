
package controller.tools;

import javafx.scene.control.RadioButton;


public class RadioButtonModel extends RadioButton{

    
    private double valor;
    
    public RadioButtonModel() {
    }
    
    public RadioButtonModel(String text) {
        super(text);
    }        

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    
    
    
}
