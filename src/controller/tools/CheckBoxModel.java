
package controller.tools;

import javafx.scene.control.CheckBox;


public class CheckBoxModel extends CheckBox{
    
    private double valor;
    
    public CheckBoxModel(){
        super();
    }
    public CheckBoxModel(String text){
        super(text);
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }
    
    
    
}
