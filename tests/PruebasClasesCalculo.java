import calculadora.calculo.*;
import java.util.Map;
/** Comprobación de las clases .java editables del paquete calculo. */
public final class PruebasClasesCalculo {
    private static void cerca(double real, double esperado, String etiqueta) {
        if (!Double.isFinite(real) || Math.abs(real-esperado)>1e-7)
            throw new AssertionError(etiqueta+": esperado="+esperado+", recibido="+real);
    }
    public static void main(String[] args) throws Exception {
        Parser parser=new Parser();
        Expresion f=parser.parsear("x^2 + 3*x + 2");
        cerca(f.evaluar(Map.of("x",2.0)),12,"evaluación polinomial");
        cerca(f.derivar("x").evaluar(Map.of("x",2.0)),7,"derivada polinomial");
        cerca(f.integrar("x").evaluar(Map.of("x",2.0)), (8.0/3)+6+4, "integral polinomial");
        Expresion seno=parser.parsear("sin(x)");
        cerca(seno.derivar("x").evaluar(Map.of("x",0.0)),1,"derivada del seno");
        cerca(seno.integrar("x").evaluar(Map.of("x",0.0)),-1,"integral del seno");
        Expresion g=parser.parsear("sin(2x)");
        cerca(g.derivar("x").evaluar(Map.of("x",0.0)),2,"regla de cadena");
        Expresion potencia=parser.parsear("x^x");
        cerca(potencia.derivar("x").evaluar(Map.of("x",2.0)),4*(Math.log(2)+1),"potencia variable");
        Expresion otra=parser.parsear("y^2");
        cerca(otra.derivar("y").evaluar(Map.of("y",3.0)),6,"variable y");
        if (!Formateador.generarPasoAPasoIntegral(f,"x",0.0,1.0).contains("Resultado"))
            throw new AssertionError("Formateador no generó el cálculo definido");
        System.out.println("17 CLASES JAVA DEL PAQUETE CALCULO: PRUEBAS CORRECTAS");
    }
}
