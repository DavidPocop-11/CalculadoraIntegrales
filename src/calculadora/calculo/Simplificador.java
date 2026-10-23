package calculadora.calculo;
/** Utilidad de simplificación compatible con el proyecto anterior. */
public class Simplificador {
    public static Expresion simplificar(Expresion e){if(e==null)throw new IllegalArgumentException("Expresión nula");return e.simplificar();}
}
