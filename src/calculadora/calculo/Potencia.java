package calculadora.calculo;
import java.util.Map;
/** Potencia. Admite exponente variable mediante derivación logarítmica. */
public class Potencia extends Expresion {
    private final Expresion base,exponente;
    public Potencia(Expresion base,Expresion exponente){this.base=base;this.exponente=exponente;}
    public Expresion getBase(){return base;}
    public Expresion getExponente(){return exponente;}
    @Override public Expresion derivar(String v){
        if(exponente.simplificar() instanceof Constante c)
            return new Producto(new Producto(new Constante(c.getValor()),new Potencia(base,new Constante(c.getValor()-1))),base.derivar(v)).simplificar();
        // (u^v)'=u^v (v' ln(u)+v u'/u), para u>0.
        return new Producto(this,new Suma(new Producto(exponente.derivar(v),new LogaritmoNatural(base)),new Producto(exponente,new Cociente(base.derivar(v),base)))).simplificar();
    }
    @Override public Expresion integrar(String v){
        Expresion a=base.simplificar(),b=exponente.simplificar();
        if(a instanceof Variable var && var.getNombre().equals(v) && b instanceof Constante c){
            double n=c.getValor();
            if(Math.abs(n+1)<1e-12)return new LogaritmoNatural(a);
            return new Producto(new Constante(1/(n+1)),new Potencia(a,new Constante(n+1))).simplificar();
        }
        throw new UnsupportedOperationException("Integral de potencia no disponible: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return Math.pow(base.evaluar(m),exponente.evaluar(m));}
    @Override public Expresion simplificar(){
        Expresion a=base.simplificar(),b=exponente.simplificar();
        if(b.esCero())return new Constante(1);if(b.esUno())return a;if(a.esUno())return new Constante(1);
        if(a instanceof Constante x && b instanceof Constante y){double n=Math.pow(x.getValor(),y.getValor());if(Double.isFinite(n))return new Constante(n);}
        return new Potencia(a,b);
    }
    @Override public String toString(){return "("+base+"^"+exponente+")";}
}
