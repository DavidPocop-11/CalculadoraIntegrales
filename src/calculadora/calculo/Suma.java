package calculadora.calculo;
import java.util.Map;
/** Suma de expresiones. */
public class Suma extends Expresion {
    private final Expresion izquierda,derecha;
    public Suma(Expresion izquierda,Expresion derecha){this.izquierda=izquierda;this.derecha=derecha;}
    public Expresion getIzquierda(){return izquierda;}
    public Expresion getDerecha(){return derecha;}
    @Override public Expresion derivar(String v){return new Suma(izquierda.derivar(v),derecha.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){return new Suma(izquierda.integrar(v),derecha.integrar(v)).simplificar();}
    @Override public double evaluar(Map<String,Double> m){return izquierda.evaluar(m)+derecha.evaluar(m);}
    @Override public Expresion simplificar(){
        Expresion a=izquierda.simplificar(),b=derecha.simplificar();
        if(a.esCero())return b;if(b.esCero())return a;
        if(a instanceof Constante x && b instanceof Constante y)return new Constante(x.getValor()+y.getValor());
        return new Suma(a,b);
    }
    @Override public String toString(){return "("+izquierda+" + "+derecha+")";}
}
