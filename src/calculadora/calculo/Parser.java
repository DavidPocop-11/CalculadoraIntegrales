package calculadora.calculo;

/**
 * Convierte el texto que escribe el usuario en el árbol Expresion.
 * Acepta operadores +, -, *, /, ^, paréntesis, x, y y funciones básicas.
 * Las funciones más avanzadas las procesa MotorAvanzado (paquete avanzado).
 */
public class Parser {
    private String texto;
    private int posicion;

    public Parser() { }

    public Expresion parsear(String entrada) throws Exception {
        if (entrada == null || entrada.isBlank()) {
            throw new Exception("Introduce una función.");
        }
        texto = entrada.replace('−','-').replace('×','*').replace('÷','/').replace('π','p');
        posicion = 0;
        Expresion resultado = expresion();
        omitirEspacios();
        if (posicion != texto.length()) {
            throw new Exception("Símbolo inesperado en posición " + (posicion + 1) + ": " + texto.charAt(posicion));
        }
        return resultado.simplificar();
    }

    private void omitirEspacios() {
        while (posicion < texto.length() && Character.isWhitespace(texto.charAt(posicion))) posicion++;
    }
    private char actual() {
        omitirEspacios();
        return posicion < texto.length() ? texto.charAt(posicion) : '\0';
    }
    private boolean aceptar(char c) {
        if (actual() == c) { posicion++; return true; }
        return false;
    }
    private Expresion expresion() throws Exception {
        Expresion e = termino();
        while (true) {
            if (aceptar('+')) e = new Suma(e, termino());
            else if (aceptar('-')) e = new Resta(e, termino());
            else break;
        }
        return e;
    }
    private Expresion termino() throws Exception {
        Expresion e = unario();
        while (true) {
            if (aceptar('*')) e = new Producto(e, unario());
            else if (aceptar('/')) e = new Cociente(e, unario());
            else if (actual() == '(' || Character.isLetter(actual()) || Character.isDigit(actual()))
                e = new Producto(e, unario()); // multiplicación implícita (2x, 2(x+1))
            else break;
        }
        return e;
    }
    private Expresion unario() throws Exception {
        if (aceptar('+')) return unario();
        if (aceptar('-')) return new Negativo(unario());
        return potencia();
    }
    private Expresion potencia() throws Exception {
        Expresion e = primario();
        if (aceptar('^')) e = new Potencia(e, unario()); // asociatividad por la derecha
        return e;
    }
    private Expresion primario() throws Exception {
        if (aceptar('(')) {
            Expresion e = expresion();
            if (!aceptar(')')) throw new Exception("Falta cerrar un paréntesis ')' en posición " + (posicion + 1));
            return e;
        }
        char c = actual();
        if (Character.isDigit(c) || c == '.') {
            int inicio = posicion;
            while (posicion < texto.length() && (Character.isDigit(texto.charAt(posicion)) || texto.charAt(posicion) == '.')) posicion++;
            try { return new Constante(Double.parseDouble(texto.substring(inicio, posicion))); }
            catch (NumberFormatException ex) { throw new Exception("Número inválido en posición " + (inicio + 1)); }
        }
        if (Character.isLetter(c)) {
            int inicio = posicion;
            while (posicion < texto.length() && Character.isLetterOrDigit(texto.charAt(posicion))) posicion++;
            String nombre = texto.substring(inicio, posicion).toLowerCase(java.util.Locale.ROOT);
            if (nombre.equals("x") || nombre.equals("y")) return new Variable(nombre);
            if (nombre.equals("pi") || nombre.equals("p")) return new Constante(Math.PI);
            if (nombre.equals("e")) return new Constante(Math.E);
            if (!aceptar('(')) throw new Exception("Para funciones usa " + nombre + "(x)");
            Expresion argumento = expresion();
            if (!aceptar(')')) throw new Exception("Falta cerrar el paréntesis de " + nombre);
            return switch (nombre) {
                case "sin", "sen" -> new Seno(argumento);
                case "cos" -> new Coseno(argumento);
                case "tan", "tg" -> new Tangente(argumento);
                case "exp" -> new Exponencial(argumento);
                case "ln" -> new LogaritmoNatural(argumento);
                case "log" -> new Cociente(new LogaritmoNatural(argumento),new Constante(Math.log(10)));
                default -> throw new Exception("Función no reconocida por el parser clásico: " + nombre + ". Prueba el motor avanzado.");
            };
        }
        throw new Exception("Se esperaba un número, una variable o una función en posición " + (posicion + 1));
    }
}
