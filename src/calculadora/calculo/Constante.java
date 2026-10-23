package calculadora.calculo;
import java.util.Map;
/** Número real constante. */
public class Constante extends Expresion {
    private final double valor;
    public Constante(double valor) { this.valor=valor; }
    public double getValor(){return valor;}
    @Override public Expresion derivar(String variable){return new Constante(0);}
    @Override public Expresion integrar(String variable){return new Producto(this,new Variable(variable)).simplificar();}
    @Override public double evaluar(Map<String,Double> valores){return valor;}
    @Override public Expresion simplificar(){return this;}
    @Override public String toString(){ if(valor==(long)valor && Math.abs(valor)<1e15) return Long.toString((long)valor); return Double.toString(valor); }
}
