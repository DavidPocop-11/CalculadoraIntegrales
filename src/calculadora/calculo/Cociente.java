package calculadora.calculo;
import java.util.Map;
/** Cociente y regla del cociente. */
public class Cociente extends Expresion {
    private final Expresion numerador,denominador;
    public Cociente(Expresion numerador,Expresion denominador){this.numerador=numerador;this.denominador=denominador;}
    public Expresion getNumerador(){return numerador;}
    public Expresion getDenominador(){return denominador;}
    @Override public Expresion derivar(String v){return new Cociente(new Resta(new Producto(numerador.derivar(v),denominador),new Producto(numerador,denominador.derivar(v))),new Potencia(denominador,new Constante(2))).simplificar();}
    @Override public Expresion integrar(String v){
        Expresion a=numerador.simplificar(),b=denominador.simplificar();
        if(a.esUno() && b instanceof Variable var && var.getNombre().equals(v))return new LogaritmoNatural(b);
        if(b instanceof Constante c && c.getValor()!=0)return new Producto(new Constante(1/c.getValor()),a.integrar(v)).simplificar();
        throw new UnsupportedOperationException("Integración no disponible para el cociente: "+this);
    }
    @Override public double evaluar(Map<String,Double> m){return numerador.evaluar(m)/denominador.evaluar(m);}
    @Override public Expresion simplificar(){
        Expresion a=numerador.simplificar(),b=denominador.simplificar();
        if(b.esCero())throw new ArithmeticException("División entre cero");
        if(a.esCero())return new Constante(0);if(b.esUno())return a;
        if(a instanceof Constante x && b instanceof Constante y)return new Constante(x.getValor()/y.getValor());
        if(a.toString().equals(b.toString()))return new Constante(1);
        return new Cociente(a,b);
    }
    @Override public String toString(){return "("+numerador+" / "+denominador+")";}
}
