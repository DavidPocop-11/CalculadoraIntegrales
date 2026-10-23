package calculadora.calculo;
import java.util.Map;
/** Exponencial con base e. */
public class Exponencial extends Expresion {
    private final Expresion argumento;
    public Exponencial(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Producto(new Exponencial(argumento),argumento.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){
        Expresion a=argumento.simplificar();
        if(a instanceof Variable x && x.getNombre().equals(v))return this;
        if(a instanceof Producto p && p.getIzquierda().simplificar() instanceof Constante c && p.getDerecha().simplificar() instanceof Variable x && x.getNombre().equals(v) && c.getValor()!=0)
            return new Producto(new Constante(1/c.getValor()),this).simplificar();
        throw new UnsupportedOperationException("Integral de exponencial compuesta no disponible: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.exp(argumento.evaluar(m));}
    @Override public Expresion simplificar(){Expresion a=argumento.simplificar();return a instanceof Constante c?new Constante(Math.exp(c.getValor())):new Exponencial(a);}
    @Override public String toString(){return "exp("+argumento+")";}
}
