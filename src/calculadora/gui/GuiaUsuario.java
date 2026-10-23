package calculadora.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/**
 * Centro de ayuda integrado en la calculadora. No necesita dependencias externas
 * y adopta la paleta del tema activo en la ventana principal.
 */
public final class GuiaUsuario extends JDialog {
    private static final String[] SECCIONES = {
            "Comenzar", "Escribir funciones", "Derivadas", "Integrales",
            "Gráfica", "Herramientas", "Atajos y consejos"
    };

    private static final String[][] EJEMPLOS = {
            {"x^2 + 3*x + 2", "sin(x)"},
            {"2*x + 3", "sin(2*x)", "sqrt(x)", "x^(1/3)", "log(8,2)"},
            {"x^3 - 4*x", "x^x", "sin(x)"},
            {"x^2", "sin(2*x)", "x*exp(x)"},
            {"x^2 + 3*x + 2", "cos(x)", "1/x"},
            {"ln(x)", "exp(3*x)"},
            {"x^2", "sin(x)"}
    };

    private final InterfazMathPro.Tema tema;
    private final Consumer<String> insertarEjemplo;
    private final JList<String> lista;
    private final JLabel tituloPagina = new JLabel();
    private final JLabel contador = new JLabel();
    private final JEditorPane contenido = new JEditorPane();
    private final JPanel barraEjemplos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final JScrollPane scroll;
    private final JButton anterior;
    private final JButton siguiente;

    public GuiaUsuario(JFrame propietario, InterfazMathPro.Tema tema, Consumer<String> insertarEjemplo) {
        super(propietario, "Guía de uso — Calculadora", ModalityType.APPLICATION_MODAL);
        this.tema = tema;
        this.insertarEjemplo = insertarEjemplo;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(680, 490));
        setSize(920, 680);
        setLocationRelativeTo(propietario);
        setIconImage(propietario.getIconImage());

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(tema.fondo);
        raiz.setBorder(new EmptyBorder(16, 17, 14, 17));
        setContentPane(raiz);

        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);
        cabecera.setBorder(new EmptyBorder(2, 7, 15, 4));
        JPanel marcas = new JPanel();
        marcas.setOpaque(false);
        marcas.setLayout(new BoxLayout(marcas, BoxLayout.Y_AXIS));
        JLabel encabezado = new JLabel("?  Centro de ayuda");
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 24));
        encabezado.setForeground(tema.texto);
        JLabel subtitulo = new JLabel("Aprende a usar tu calculadora paso a paso");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitulo.setForeground(tema.textoSec);
        marcas.add(encabezado);
        marcas.add(Box.createVerticalStrut(3));
        marcas.add(subtitulo);
        cabecera.add(marcas, BorderLayout.WEST);
        JLabel atajo = new JLabel("F1 para abrir la guía");
        atajo.setForeground(tema.cian);
        atajo.setFont(new Font("SansSerif", Font.BOLD, 12));
        cabecera.add(atajo, BorderLayout.EAST);
        raiz.add(cabecera, BorderLayout.NORTH);

        lista = new JList<>(SECCIONES);
        lista.setFont(new Font("SansSerif", Font.BOLD, 13));
        lista.setFixedCellHeight(49);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setBackground(tema.panel);
        lista.setForeground(tema.textoSec);
        lista.setSelectionBackground(tema.ac1);
        lista.setSelectionForeground(Color.WHITE);
        lista.setFocusable(true);
        lista.setCellRenderer((list, item, i, selected, focused) -> {
            JLabel l = new JLabel((i + 1) + ".   " + item);
            l.setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 13));
            l.setOpaque(true);
            l.setBackground(selected ? tema.ac1 : tema.panel);
            l.setForeground(selected ? Color.WHITE : tema.textoSec);
            l.setBorder(new EmptyBorder(0, 12, 0, 4));
            return l;
        });
        JScrollPane menu = new JScrollPane(lista);
        menu.setBorder(BorderFactory.createLineBorder(tema.borde));
        menu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        menu.setPreferredSize(new Dimension(194, 100));
        menu.getViewport().setBackground(tema.panel);

        JPanel principal = new JPanel(new BorderLayout(0, 10));
        principal.setBackground(tema.panel);
        principal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tema.borde), new EmptyBorder(17, 18, 14, 18)));
        tituloPagina.setForeground(tema.texto);
        tituloPagina.setFont(new Font("SansSerif", Font.BOLD, 21));
        principal.add(tituloPagina, BorderLayout.NORTH);

        contenido.setContentType("text/html");
        contenido.setEditable(false);
        contenido.setOpaque(true);
        contenido.setBackground(tema.panel);
        contenido.setForeground(tema.texto);
        contenido.setBorder(new EmptyBorder(2, 2, 2, 10));
        contenido.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        contenido.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED
                    && e.getDescription().startsWith("ejemplo:")) {
                probarEjemplo(e.getDescription().substring("ejemplo:".length()));
            }
        });
        scroll = new JScrollPane(contenido);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(tema.panel);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(17);
        principal.add(scroll, BorderLayout.CENTER);

        JPanel zonaEjemplos = new JPanel(new BorderLayout(0, 5));
        zonaEjemplos.setOpaque(false);
        zonaEjemplos.setBorder(new EmptyBorder(8, 0, 0, 0));
        JLabel instruccion = new JLabel("Prueba una función con un clic:");
        instruccion.setForeground(tema.textoSec);
        instruccion.setFont(new Font("SansSerif", Font.BOLD, 12));
        barraEjemplos.setOpaque(false);
        zonaEjemplos.add(instruccion, BorderLayout.NORTH);
        zonaEjemplos.add(barraEjemplos, BorderLayout.CENTER);
        principal.add(zonaEjemplos, BorderLayout.SOUTH);

        JPanel columnas = new JPanel(new BorderLayout(12, 0));
        columnas.setOpaque(false);
        columnas.add(menu, BorderLayout.WEST);
        columnas.add(principal, BorderLayout.CENTER);
        raiz.add(columnas, BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.setBorder(new EmptyBorder(13, 3, 0, 3));
        contador.setForeground(tema.textoSec);
        contador.setFont(new Font("SansSerif", Font.PLAIN, 12));
        pie.add(contador, BorderLayout.WEST);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        anterior = boton("← Anterior", false);
        siguiente = boton("Siguiente →", true);
        JButton cerrar = boton("Cerrar", false);
        anterior.addActionListener(e -> irA(lista.getSelectedIndex() - 1));
        siguiente.addActionListener(e -> irA(lista.getSelectedIndex() + 1));
        cerrar.addActionListener(e -> dispose());
        acciones.add(anterior);
        acciones.add(siguiente);
        acciones.add(cerrar);
        pie.add(acciones, BorderLayout.EAST);
        raiz.add(pie, BorderLayout.SOUTH);

        lista.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarPagina(lista.getSelectedIndex());
        });
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        irA(0);
    }

    private JButton boton(String texto, boolean destacado) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBackground(destacado ? tema.ac1 : tema.panel2);
        boton.setForeground(destacado ? Color.WHITE : tema.texto);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(destacado ? tema.ac1 : tema.borde),
                new EmptyBorder(9, 13, 9, 13)));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setOpaque(true);
        return boton;
    }

    private void irA(int indice) {
        if (indice >= 0 && indice < SECCIONES.length) lista.setSelectedIndex(indice);
    }

    private void actualizarPagina(int indice) {
        if (indice < 0) return;
        tituloPagina.setText(SECCIONES[indice]);
        contador.setText("Sección " + (indice + 1) + " de " + SECCIONES.length);
        anterior.setEnabled(indice > 0);
        siguiente.setEnabled(indice < SECCIONES.length - 1);
        contenido.setText(crearHtml(indice));
        contenido.setCaretPosition(0);
        SwingUtilities.invokeLater(() -> scroll.getViewport().setViewPosition(new Point(0, 0)));
        barraEjemplos.removeAll();
        for (String ejemplo : EJEMPLOS[indice]) {
            JButton opcion = boton(ejemplo, false);
            opcion.setToolTipText("Cargar y calcular " + ejemplo);
            opcion.addActionListener(e -> probarEjemplo(ejemplo));
            barraEjemplos.add(opcion);
        }
        barraEjemplos.revalidate();
        barraEjemplos.repaint();
    }

    private void probarEjemplo(String ejemplo) {
        dispose();
        insertarEjemplo.accept(ejemplo);
    }

    private String crearHtml(int pagina) {
        String css = "<style>body{font-family:SansSerif;font-size:13pt;color:" + rgb(tema.texto)
                + ";background:" + rgb(tema.panel) + ";margin:6px 10px 14px 3px;}"
                + "h3{font-size:16pt;color:" + rgb(tema.cian) + ";margin:13px 0 5px 0;}"
                + "p{margin:6px 0 10px 0;line-height:1.35;}li{margin-bottom:8px;}"
                + "pre{background:" + rgb(tema.panel2) + ";color:" + rgb(tema.texto)
                + ";padding:9px;font-family:Monospaced;font-size:12pt;}"
                + "code{font-family:Monospaced;color:" + rgb(tema.cian) + ";}"
                + "b{color:" + rgb(tema.texto) + ";}"
                + "small{color:" + rgb(tema.textoSec) + ";}"
                + "</style>";
        return "<html><head>" + css + "</head><body>" + pagina(pagina) + "</body></html>";
    }

    private static String rgb(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static String pagina(int p) {
        return switch (p) {
            case 0 -> """
                    <h3>¡Bienvenido a la Calculadora!</h3>
                    <p>Resuelve derivadas e integrales, observa los pasos de la solución y explora la gráfica de cada función.</p>
                    <ol>
                      <li>Escribe una función en el campo <b>f(x) =</b>, o usa el teclado matemático.</li>
                      <li>Elige <b>Derivadas</b> o <b>Integrales</b>.</li>
                      <li>Selecciona la variable <b>x</b> o <b>y</b> si hace falta.</li>
                      <li>Presiona <b>Calcular</b> o utiliza <b>Ctrl + Enter</b>.</li>
                      <li>Consulta el resultado, los pasos y la gráfica interactiva.</li>
                    </ol>
                    <h3>Un primer ejercicio</h3>
                    <pre>x^2 + 3*x + 2</pre>
                    <p>Prueba esta función con el botón inferior. La calculadora mostrará su derivada, su integral y su gráfica.</p>
                    <p><small>Nota: cuando la antiderivada no está disponible en forma simbólica, la calculadora lo indica. No todos los problemas tienen solución cerrada.</small></p>
                    """;
            case 1 -> """
                    <h3>¿Cómo escribir correctamente una función?</h3>
                    <p>Utiliza números, letras, operadores y paréntesis. Puedes escribirlos con el teclado físico o con los botones de la pantalla.</p>
                    <ul>
                      <li><b>Potencias:</b> <code>x^2</code>, <code>x^3</code>, <code>2^x</code>, <code>x^x</code>.</li>
                      <li><b>Multiplicación:</b> <code>3*x</code> (recomendado). También se aceptan casos como <code>2x</code>.</li>
                      <li><b>División:</b> <code>1/x</code> o <code>(x+1)/(x-1)</code>.</li>
                      <li><b>Trigonometría:</b> <code>sin(x)</code>, <code>cos(x)</code>, <code>tan(x)</code>.</li>
                      <li><b>Otras funciones:</b> <code>ln(x)</code>, <code>exp(x)</code>, <code>sqrt(x)</code>, <code>abs(x)</code>.</li>
                      <li><b>Raíz n-ésima:</b> pulsa <b>ⁿ√x</b>, elige el índice (por ejemplo 4) y selecciona o introduce el radicando. También puedes escribir <code>x^(1/4)</code>. El botón <b>∛x</b> coloca directamente la raíz cúbica.</li>
                      <li><b>Otras disponibles:</b> <code>asin(x)</code>, <code>acos(x)</code>, <code>atan(x)</code>, <code>sinh(x)</code>, <code>cosh(x)</code>.</li>
                      <li><b>Logaritmos:</b> <code>log(x)</code> es base 10; <code>log(8,2)</code> es base 2.</li>
                      <li><b>Constantes:</b> <code>pi</code> y <code>e</code>.</li>
                    </ul>
                    <h3>Consejos</h3>
                    <p>Coloca los argumentos entre paréntesis, por ejemplo <code>sin(2*x)</code>. Las funciones trigonométricas usan <b>radianes</b>. Pulsa <b>Ctrl + Espacio</b> en el campo para ver sugerencias.</p>
                    """;
            case 2 -> """
                    <h3>Calcular derivadas</h3>
                    <ol>
                      <li>Escribe la expresión. Ejemplo: <code>x^3 - 4*x</code>.</li>
                      <li>Selecciona <b>Derivadas</b> y elige la variable en <b>Respecto a</b>.</li>
                      <li>Ajusta <b>Orden</b>: 1 para primera derivada, 2 para segunda, hasta 8.</li>
                      <li>Haz clic en <b>Calcular</b> y revisa el resultado y los pasos.</li>
                    </ol>
                    <h3>Recta tangente</h3>
                    <p>Escribe un número en <b>Tangente en</b> (por ejemplo 1) y presiona <b>Aplicar</b> o <b>Calcular</b>. Si la función es derivable en ese punto, verás la recta tangente en la gráfica y su ecuación en los pasos.</p>
                    <h3>Ejemplo con variable y</h3>
                    <p>Elige <b>y</b> en <b>Respecto a</b> y escribe <code>y^2 + 4*y</code>. No necesitas cambiar de proyecto.</p>
                    <p><small>La recta tangente siempre se calcula con la primera derivada, aunque el orden seleccionado sea mayor.</small></p>
                    """;
            case 3 -> """
                    <h3>Integrales indefinidas</h3>
                    <p>Selecciona <b>Integrales</b> y escoge <b>Indefinida</b>. Escribe la función y pulsa <b>Calcular</b>.</p>
                    <pre>∫ x^2 dx = x^3/3 + C</pre>
                    <p><b>+ C</b> es la constante de integración.</p>
                    <h3>Integrales definidas</h3>
                    <ol>
                      <li>Selecciona <b>Definida</b> en el desplegable.</li>
                      <li>Escribe los límites <b>a</b> (inferior) y <b>b</b> (superior).</li>
                      <li>Pulsa <b>Calcular</b>. Verás el valor y el área <b>con signo</b> sombreada en la gráfica.</li>
                    </ol>
                    <p>Por ejemplo, para <code>x^2</code>, usa <b>a = 0</b> y <b>b = 2</b>.</p>
                    <h3>Métodos disponibles</h3>
                    <p>El motor reconoce algunos casos de sustitución lineal, como <code>sin(2*x)</code>, e integración por partes, como <code>x*exp(x)</code>.</p>
                    <p><small>En integrales definidas sin primitiva simbólica puede usarse una aproximación numérica. Las integrales impropias o discontinuas requieren especial cuidado.</small></p>
                    """;
            case 4 -> """
                    <h3>Explora la gráfica</h3>
                    <ul>
                      <li><b>Zoom:</b> gira la rueda del ratón o usa <b>+</b> y <b>-</b>.</li>
                      <li><b>Mover:</b> mantén pulsado el ratón y arrastra el gráfico.</li>
                      <li><b>Ajustar:</b> devuelve la vista a una escala automática.</li>
                      <li><b>Datos importantes:</b> consulta dominio, rango, extremos y cortes con los ejes.</li>
                      <li><b>Tangente:</b> se muestra cuando existe una pendiente definida en el punto elegido.</li>
                      <li><b>Integral definida:</b> el área con signo se sombrea entre a y b.</li>
                    </ul>
                    <h3>Comparar varias funciones</h3>
                    <p>En el campo para funciones adicionales de la tarjeta de gráfica, escríbelas separadas por punto y coma (<code>;</code>) y pulsa <b>Comparar</b>.</p>
                    <pre>x^2; sin(x); cos(x)</pre>
                    <p>Puedes comparar hasta cuatro funciones adicionales, cada una con un color diferente.</p>
                    <p><small>El dominio, el rango, las raíces y los extremos se estiman numéricamente en el intervalo [-10, 10].</small></p>
                    """;
            case 5 -> """
                    <h3>Herramientas adicionales</h3>
                    <ul>
                      <li><b>Aprender:</b> cambia a ON para revelar los pasos de la solución de uno en uno con <b>Siguiente paso</b>.</li>
                      <li><b>Fracción / Decimal:</b> cambia la presentación de resultados; en algunas expresiones simbólicas se conservan fracciones.</li>
                      <li><b>Historial:</b> abre cálculos anteriores y carga una función con un clic. Se guarda localmente.</li>
                      <li><b>Límites:</b> calcula aproximaciones numéricas cerca de un punto.</li>
                      <li><b>Taylor:</b> solicita el centro y el grado (de 0 a 8).</li>
                      <li><b>PDF:</b> guarda resultados y pasos en un informe de texto.</li>
                      <li><b>PNG:</b> guarda una imagen de la gráfica.</li>
                      <li><b>Temas:</b> usa los círculos superiores para cambiar entre Neón, Oscuro, Claro y Esmeralda.</li>
                    </ul>
                    <h3>Vista previa matemática</h3>
                    <p>La fórmula se previsualiza mientras escribes. Si Maven descargó JLaTeXMath, las expresiones podrán mostrarse con formato matemático más elaborado.</p>
                    """;
            default -> """
                    <h3>Atajos de teclado</h3>
                    <ul>
                      <li><b>F1:</b> abrir esta guía.</li>
                      <li><b>Ctrl + Enter:</b> calcular.</li>
                      <li><b>Ctrl + L:</b> limpiar el campo de función.</li>
                      <li><b>Ctrl + Z:</b> deshacer mientras editas la función.</li>
                      <li><b>Ctrl + Espacio:</b> abrir el menú de autocompletado.</li>
                      <li><b>Esc:</b> cerrar la guía.</li>
                    </ul>
                    <h3>Si aparece un error</h3>
                    <ul>
                      <li>Comprueba los paréntesis: <code>sin(x)</code> tiene apertura y cierre.</li>
                      <li>Prueba la multiplicación explícita: <code>2*x</code> en vez de <code>2 x</code>.</li>
                      <li>Usa <code>sin</code>, no <code>sen</code>, y <code>sqrt(x)</code> para la raíz cuadrada.</li>
                      <li>Para derivar respecto a <code>y</code>, selecciona <b>y</b> antes de calcular.</li>
                      <li>Para integrales definidas, comprueba que los límites sean números.</li>
                    </ul>
                    <p><small>La calculadora es una herramienta educativa: no puede resolver simbólicamente todas las funciones. En casos complejos, valida el resultado con métodos matemáticos adecuados.</small></p>
                    """;
        };
    }
}
