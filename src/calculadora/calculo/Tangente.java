package calculadora.calculo;
import java.util.Map;
/** Función tangente (radianes). */
public class Tangente extends Expresion {
    private final Expresion argumento;
    public Tangente(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Cociente(argumento.derivar(v),new Potencia(new Coseno(argumento),new Constante(2))).simplificar();}
    @Override public Expresion integrar(String v){
        if(argumento.simplificar() instanceof Variable a && a.getNombre().equals(v))return new Negativo(new LogaritmoNatural(new Coseno(argumento)));
        throw new UnsupportedOperationException("Integral de tangente compuesta no disponible: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.tan(argumento.evaluar(m));}
    @Override public Expresion simplificar(){Expresion a=argumento.simplificar();return a instanceof Constante c?new Constante(Math.tan(c.getValor())):new Tangente(a);}
    @Override public String toString(){return "tan("+argumento+")";}
}
