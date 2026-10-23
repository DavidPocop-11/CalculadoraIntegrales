package calculadora.calculo;
import java.util.Map;
/** Función coseno (radianes). */
public class Coseno extends Expresion {
    private final Expresion argumento;
    public Coseno(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Producto(new Negativo(new Seno(argumento)),argumento.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){
        if(argumento.simplificar() instanceof Variable a && a.getNombre().equals(v))return new Seno(argumento);
        throw new UnsupportedOperationException("La integración compuesta de cos requiere sustitución: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.cos(argumento.evaluar(m));}
    @Override public Expresion simplificar(){Expresion a=argumento.simplificar();return a instanceof Constante c?new Constante(Math.cos(c.getValor())):new Coseno(a);}
    @Override public String toString(){return "cos("+argumento+")";}
}
