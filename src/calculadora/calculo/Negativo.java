package calculadora.calculo;
import java.util.Map;
/** Negación de una expresión. */
public class Negativo extends Expresion {
    private final Expresion argumento;
    public Negativo(Expresion argumento){this.argumento=argumento;}
    public Expresion getArgumento(){return argumento;}
    @Override public Expresion derivar(String v){return new Negativo(argumento.derivar(v)).simplificar();}
    @Override public Expresion integrar(String v){return new Negativo(argumento.integrar(v)).simplificar();}
    @Override public double evaluar(Map<String,Double> m){return -argumento.evaluar(m);}
    @Override public Expresion simplificar(){
        Expresion a=argumento.simplificar();
        if(a instanceof Constante c)return new Constante(-c.getValor());
        if(a instanceof Negativo n)return n.getArgumento().simplificar();
        return new Negativo(a);
    }
    @Override public String toString(){return "-("+argumento+")";}
}
