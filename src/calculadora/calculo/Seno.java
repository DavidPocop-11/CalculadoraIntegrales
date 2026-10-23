package calculadora.calculo;
import java.util.Map;
/** Función seno (radianes). */
public class Seno extends Expresion {
    private final Expresion argumento;
    public Seno(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Producto(new Coseno(argumento),argumento.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){
        if(argumento.simplificar() instanceof Variable a && a.getNombre().equals(v))return new Negativo(new Coseno(argumento));
        throw new UnsupportedOperationException("La integración compuesta de sin requiere sustitución: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.sin(argumento.evaluar(m));}
    @Override public Expresion simplificar(){Expresion a=argumento.simplificar();return a instanceof Constante c?new Constante(Math.sin(c.getValor())):new Seno(a);}
    @Override public String toString(){return "sin("+argumento+")";}
}
