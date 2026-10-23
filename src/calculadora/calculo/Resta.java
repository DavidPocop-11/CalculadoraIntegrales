package calculadora.calculo;
import java.util.Map;
/** Resta de expresiones. */
public class Resta extends Expresion {
    private final Expresion izquierda,derecha;
    public Resta(Expresion izquierda,Expresion derecha){this.izquierda=izquierda;this.derecha=derecha;}
    public Expresion getIzquierda(){return izquierda;}
    public Expresion getDerecha(){return derecha;}
    @Override public Expresion derivar(String v){return new Resta(izquierda.derivar(v),derecha.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){return new Resta(izquierda.integrar(v),derecha.integrar(v)).simplificar();}
    @Override public double evaluar(Map<String,Double> m){return izquierda.evaluar(m)-derecha.evaluar(m);}
    @Override public Expresion simplificar(){
        Expresion a=izquierda.simplificar(),b=derecha.simplificar();
        if(b.esCero())return a;if(a.esCero())return new Negativo(b).simplificar();
        if(a instanceof Constante x && b instanceof Constante y)return new Constante(x.getValor()-y.getValor());
        if(a.toString().equals(b.toString())) return new Constante(0);
        return new Resta(a,b);
    }
    @Override public String toString(){return "("+izquierda+" - "+derecha+")";}
}
