package calculadora.calculo;
import java.util.Map;
/** Una variable, como x o y. */
public class Variable extends Expresion {
    private final String nombre;
    public Variable(String nombre){if(nombre==null||nombre.isBlank())throw new IllegalArgumentException("Variable vacía");this.nombre=nombre;}
    public String getNombre(){return nombre;}
    @Override public Expresion derivar(String variable){return new Constante(nombre.equals(variable)?1:0);}
    @Override public Expresion integrar(String variable){
        if(nombre.equals(variable)) return new Producto(new Constante(.5),new Potencia(this,new Constante(2))).simplificar();
        return new Producto(this,new Variable(variable));
    }
    @Override public double evaluar(Map<String,Double> valores){
        Double v=valores.get(nombre);
        if(v==null) throw new IllegalArgumentException("Falta asignar valor a la variable: "+nombre);
        return v;
    }
    @Override public Expresion simplificar(){return this;}
    @Override public String toString(){return nombre;}
}
