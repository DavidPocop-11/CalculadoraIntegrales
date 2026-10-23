package calculadora.calculo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Presenta resultados y pasos básicos de cálculo para el motor clásico.
 * Conserva las cinco firmas públicas de Formateador.class.
 */
public class Formateador {
    public Formateador() { }

    public static String generarPasoAPasoDerivada(Expresion expresion, String variable) {
        StringBuilder s = new StringBuilder();
        s.append("FUNCIÓN ORIGINAL\n f(").append(variable).append(") = ").append(expresion).append("\n\n");
        explicarRegla(expresion, variable, s);
        try {
            s.append("\nDERIVADA\n f'(").append(variable).append(") = ")
             .append(expresion.derivar(variable).simplificar());
        } catch (RuntimeException ex) {
            s.append("\nNo se pudo derivar: ").append(ex.getMessage());
        }
        return s.toString();
    }

    public static String formatearPasoAPasoDerivada(Expresion expresion, String variable) {
        return generarPasoAPasoDerivada(expresion, variable);
    }

    public static String generarPasoAPasoIntegral(Expresion expresion, String variable, Double a, Double b) {
        StringBuilder s = new StringBuilder();
        if ((a==null) != (b==null)) {
            return "Para la integral definida necesitas ambos límites: a y b.";
        }
        s.append(a==null ? "INTEGRAL INDEFINIDA\n" : "INTEGRAL DEFINIDA\n");
        s.append("Integrando: ").append(expresion).append("\n");
        s.append("Variable de integración: ").append(variable).append("\n\n");
        if (expresion instanceof Suma || expresion instanceof Resta)
            s.append("Regla: integrar término a término (linealidad).\n");
        else if (expresion instanceof Potencia || expresion instanceof Variable)
            s.append("Regla: potencias.\n");
        else if (expresion instanceof Producto)
            s.append("Regla: extraer factores constantes cuando corresponda.\n");
        else if (expresion instanceof Seno || expresion instanceof Coseno || expresion instanceof Exponencial)
            s.append("Regla: antiderivada de una función elemental.\n");
        try {
            Expresion primitiva = expresion.integrar(variable).simplificar();
            s.append("Primitiva F(").append(variable).append(") = ").append(primitiva).append("\n");
            if (a == null) {
                s.append("Resultado: ").append(primitiva).append(" + C\n");
            } else {
                Map<String,Double> m = new HashMap<>();
                m.put(variable, b);
                double fb = primitiva.evaluar(m);
                m.put(variable, a);
                double fa = primitiva.evaluar(m);
                s.append("Teorema fundamental: F(b) - F(a)\n");
                s.append("F(b) = ").append(numero(fb)).append("\n");
                s.append("F(a) = ").append(numero(fa)).append("\n");
                s.append("Resultado = ").append(numero(fb-fa)).append("\n");
            }
        } catch (RuntimeException ex) {
            s.append("No se obtuvo una primitiva simbólica: ").append(ex.getMessage());
            s.append("\nUtiliza MotorAvanzado para reglas adicionales.");
        }
        return s.toString();
    }

    public static String formatearPasoAPasoIntegral(Expresion expresion, String variable, boolean definida, double a, double b) {
        return generarPasoAPasoIntegral(expresion, variable, definida ? a : null, definida ? b : null);
    }

    public static String formatearPasoAPasoIntegral(Expresion expresion, String variable, boolean definida) {
        return definida ? "Indica los límites a y b antes de calcular una integral definida."
                        : generarPasoAPasoIntegral(expresion, variable, null, null);
    }

    private static void explicarRegla(Expresion e, String v, StringBuilder s) {
        if(e instanceof Constante) s.append("Regla de la constante: d(c)/d").append(v).append(" = 0.\n");
        else if(e instanceof Variable) s.append("Regla de la variable: d(").append(v).append(")/d").append(v).append(" = 1.\n");
        else if(e instanceof Suma) s.append("Regla de la suma: derivar cada término.\n");
        else if(e instanceof Resta) s.append("Regla de la resta: derivar cada término.\n");
        else if(e instanceof Producto) s.append("Regla del producto: (u*v)' = u'*v + u*v'.\n");
        else if(e instanceof Cociente) s.append("Regla del cociente: (u/v)' = (u'*v-u*v')/v².\n");
        else if(e instanceof Potencia) s.append("Regla de potencias (y regla de cadena cuando corresponda).\n");
        else if(e instanceof Seno || e instanceof Coseno || e instanceof Tangente)
            s.append("Regla de derivación trigonométrica y regla de cadena.\n");
        else if(e instanceof Exponencial || e instanceof LogaritmoNatural)
            s.append("Regla de derivación exponencial/logarítmica y regla de cadena.\n");
        else s.append("Aplicar reglas de derivación.\n");
    }

    private static String numero(double n) {return String.format(Locale.US, "%.8g", n);}
}
