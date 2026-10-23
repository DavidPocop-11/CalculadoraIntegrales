package calculadora.calculo;
import java.util.Map;
/** Producto con regla de Leibniz para derivadas. */
public class Producto extends Expresion {
    private final Expresion izquierda,derecha;
    public Producto(Expresion izquierda,Expresion derecha){this.izquierda=izquierda;this.derecha=derecha;}
    public Expresion getIzquierda(){return izquierda;}
    public Expresion getDerecha(){return derecha;}
    @Override public Expresion derivar(String v){return new Suma(new Producto(izquierda.derivar(v),derecha),new Producto(izquierda,derecha.derivar(v))).simplificar();}
    @Override public Expresion integrar(String v){
        Expresion a=izquierda.simplificar(),b=derecha.simplificar();
        if(a instanceof Constante) return new Producto(a,b.integrar(v)).simplificar();
        if(b instanceof Constante) return new Producto(b,a.integrar(v)).simplificar();
        throw new UnsupportedOperationException("Este producto requiere integración por partes: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return izquierda.evaluar(m)*derecha.evaluar(m);}
    @Override public Expresion simplificar(){
        Expresion a=izquierda.simplificar(),b=derecha.simplificar();
        if(a.esCero()||b.esCero())return new Constante(0);
        if(a.esUno())return b;if(b.esUno())return a;
        if(a instanceof Constante x && b instanceof Constante y)return new Constante(x.getValor()*y.getValor());
        if(b instanceof Constante && !(a instanceof Constante))return new Producto(b,a);
        return new Producto(a,b);
    }
    @Override public String toString(){return "("+izquierda+" * "+derecha+")";}
}
