package calculadora.calculo;
import java.util.Map;
/** Nodo base del árbol de expresiones matemáticas. */
public abstract class Expresion {
    public abstract Expresion derivar(String variable);
    public abstract Expresion integrar(String variable);
    public abstract double evaluar(Map<String, Double> valores);
    public abstract Expresion simplificar();
    public boolean esCero() { return simplificar() instanceof Constante c && Math.abs(c.getValor()) < 1e-12; }
    public boolean esUno() { return simplificar() instanceof Constante c && Math.abs(c.getValor()-1) < 1e-12; }
    public boolean esConstante() { return simplificar() instanceof Constante; }
    @Override public abstract String toString();
}
