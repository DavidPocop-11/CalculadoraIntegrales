package calculadora.calculo;
import java.util.Map;
/** Logaritmo natural, definido para argumentos positivos. */
public class LogaritmoNatural extends Expresion {
    private final Expresion argumento;
    public LogaritmoNatural(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Cociente(argumento.derivar(v),argumento).simplificar();}
    @Override public Expresion integrar(String v){
        if(argumento.simplificar() instanceof Variable a && a.getNombre().equals(v))
            return new Resta(new Producto(argumento,this),argumento).simplificar();
        throw new UnsupportedOperationException("Integral logarítmica no disponible para: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.log(argumento.evaluar(m));}
    @Override public Expresion simplificar(){Expresion a=argumento.simplificar();return a instanceof Constante c && c.getValor()>0?new Constante(Math.log(c.getValor())):new LogaritmoNatural(a);}
    @Override public String toString(){return "ln("+argumento+")";}
}
