package calculadora.gui;

import calculadora.avanzado.MotorAvanzado;
import calculadora.avanzado.MotorAvanzado.Funcion;
import calculadora.servicios.HistorialCalculos;
import calculadora.servicios.ExportadorPDF;
import calculadora.servicios.RenderizadorLatex;
import javax.imageio.ImageIO;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.io.File;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InterfazMathPro extends JFrame {

    // =====================================================================
    //  TEMAS
    // =====================================================================
    public enum Tema {
        NEON("Neón",
                new Color(7, 10, 26), new Color(13, 19, 43), new Color(20, 28, 62),
                new Color(240, 243, 255), new Color(140, 155, 200), new Color(38, 52, 100),
                new Color(130, 60, 240), new Color(220, 60, 220), new Color(0, 210, 255)),
        OSCURO("Oscuro",
                new Color(18, 18, 24), new Color(28, 28, 38), new Color(38, 38, 52),
                new Color(230, 230, 235), new Color(150, 150, 170), new Color(55, 55, 78),
                new Color(70, 90, 210), new Color(110, 130, 250), new Color(0, 180, 216)),
        CLARO("Claro",
                new Color(242, 245, 252), new Color(255, 255, 255), new Color(235, 240, 250),
                new Color(25, 30, 50), new Color(100, 110, 135), new Color(210, 220, 240),
                new Color(90, 80, 230), new Color(160, 70, 230), new Color(0, 150, 220)),
        ESMERALDA("Esmeralda",
                new Color(6, 20, 20), new Color(12, 35, 35), new Color(18, 48, 48),
                new Color(230, 255, 245), new Color(120, 180, 160), new Color(25, 70, 65),
                new Color(10, 180, 140), new Color(0, 220, 200), new Color(80, 235, 170));

        final String nombre;
        final Color fondo, panel, panel2, texto, textoSec, borde, ac1, ac2, cian;

        Tema(String nombre, Color fondo, Color panel, Color panel2, Color texto, Color textoSec,
             Color borde, Color ac1, Color ac2, Color cian) {
            this.nombre = nombre;
            this.fondo = fondo;
            this.panel = panel;
            this.panel2 = panel2;
            this.texto = texto;
            this.textoSec = textoSec;
            this.borde = borde;
            this.ac1 = ac1;
            this.ac2 = ac2;
            this.cian = cian;
        }
    }

    private static final Color NARANJA = new Color(255, 170, 60);
    private static final Color AMARILLO = new Color(255, 205, 70);
    private static final Color VERDE = new Color(150, 230, 90);
    private static final Color ROJO = new Color(255, 100, 120);

    // Rango de muestreo para calcular los "datos importantes"
    private static final double X0 = -10, X1 = 10, PASO = 0.01;

    // =====================================================================
    //  ESTADO
    // =====================================================================
    private Tema temaActual = Tema.NEON;
    private boolean modoDerivada = true;
    private String textoFuncion = "x^2 + 3*x + 2";

    private Funcion exprActual, derActual, intgActual;
    private static String variableActiva = "x";
    private String variableElegida = "x";
    private int ordenDerivada = 1;
    private boolean integralDefinida = false, mostrarFraccion = true, modoAprender = false;
    private double limiteA = 0, limiteB = 2, puntoTangente = 1;
    private final HistorialCalculos historial = new HistorialCalculos();
    private final UndoManager undo = new UndoManager();
    private final java.util.List<String> pasosDer = new ArrayList<>(), pasosInt = new ArrayList<>();
    private int pasoVisibleDer=0, pasoVisibleInt=0;
    private DefaultListModel<String> modeloHistorial;
    private JList<String> listaHistorial;
    private JComboBox<String> cmbVariable, cmbTipoIntegral, cmbFormato;
    private JSpinner spOrden;
    private JTextField txtLimiteA, txtLimiteB, txtPunto, txtAdicionales;
    private JLabel vistaPrevia;
    private JPanel vistaLatexDer, vistaLatexInt, vistaPasoMatDer, vistaPasoMatInt;
    private JButton siguienteDer, siguienteInt;
    private JPanel panelHistorial;
    private String comparacionesGuardadas="";
    private javax.swing.Timer temporizadorVista, temporizadorBrillo;
    private static int brilloFase=0;


    // Componentes
    private JTextField txtFuncion;
    private JLabel lblError;
    private JTextArea txtDerivadaRes, txtIntegralRes, txtPasosDerivada, txtPasosIntegral;
    private Tarjeta cardDerivada, cardIntegral;
    private Boton btnModoDer, btnModoInt;
    private PanelGrafico panelGrafico;
    private JLabel lblDominio, lblRango, lblExtremos, lblRaices, lblCorteY;

    // =====================================================================
    //  CONSTRUCTOR
    // =====================================================================
    public InterfazMathPro() {
        setTitle("Calculadora — Derivadas e Integrales");
        setSize(1280, 800);
        setMinimumSize(new Dimension(870, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setIconImage(crearIcono());
        construirUI();
        configurarAtajos();
        ejecutarCalculo();
        temporizadorBrillo = new javax.swing.Timer(140, e -> {
            brilloFase = (brilloFase + 1) % 360;
            if (temaActual == Tema.NEON && getContentPane() != null) getContentPane().repaint();
        });
        temporizadorBrillo.start();
    }

    private void construirUI() {
        Tema t = temaActual;

        Fondo raiz = new Fondo(t);
        raiz.setLayout(new BorderLayout(0, 0));
        raiz.setBorder(new EmptyBorder(14, 18, 10, 18));

        raiz.add(crearHeader(t), BorderLayout.NORTH);
        raiz.add(crearCuerpo(t), BorderLayout.CENTER);
        raiz.add(crearFooter(t), BorderLayout.SOUTH);

        setContentPane(raiz);
        revalidate();
        repaint();
    }

    private void cambiarTema(Tema nuevo) {
        BufferedImage imagenAnterior=null;
        if(getContentPane().getWidth()>0&&getContentPane().getHeight()>0){
            imagenAnterior=new BufferedImage(getContentPane().getWidth(),getContentPane().getHeight(),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=imagenAnterior.createGraphics();getContentPane().printAll(g);g.dispose();
        }
        if (txtFuncion != null) textoFuncion = txtFuncion.getText();
        if (txtAdicionales != null) comparacionesGuardadas=txtAdicionales.getText();
        temaActual = nuevo;
        construirUI();
        ejecutarCalculo();
        if(imagenAnterior!=null)transicionTema(imagenAnterior);
    }
    private void transicionTema(BufferedImage anterior){
        final float[] alpha={1f};
        JComponent capa=new JComponent(){
            @Override protected void paintComponent(Graphics gr){Graphics2D g=(Graphics2D)gr.create();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha[0]));g.drawImage(anterior,0,0,null);g.dispose();}
        };
        setGlassPane(capa);capa.setVisible(true);
        final int[] tick={0};javax.swing.Timer tm=new javax.swing.Timer(22,null);
        tm.addActionListener(e->{tick[0]++;try{
            alpha[0]=Math.max(0f,1f-tick[0]/12f);
        }catch(Exception ignored){}capa.repaint();if(tick[0]>=12){tm.stop();capa.setVisible(false);}});
        tm.start();
    }

    // =====================================================================
    //  HEADER / FOOTER
    // =====================================================================
    private JPanel crearHeader(Tema t) {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 12, 4));

        // Logo
        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        logo.setOpaque(false);

        JLabel inf = new JLabel("∞");
        inf.setFont(new Font("SansSerif", Font.BOLD, 46));
        inf.setForeground(t.cian);

        JPanel textos = new JPanel();
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        textos.setOpaque(false);

        JLabel titulo = new JLabel("Calculadora");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        titulo.setForeground(t.texto);

        JLabel sub = new JLabel("Calculadora de Derivadas e Integrales");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(mezclar(t.cian, t.texto, 0.2));

        JLabel sub2 = new JLabel("Convierte problemas en soluciones ✦");
        sub2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub2.setForeground(t.textoSec);

        textos.add(titulo);
        textos.add(sub);
        textos.add(sub2);
        logo.add(inf);
        logo.add(textos);

        // Eslogan
        JLabel slogan = new JLabel("Más que cálculos, ideas que se grafican ✦", SwingConstants.CENTER);
        slogan.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 17));
        slogan.setForeground(mezclar(t.ac2, t.texto, 0.25));

        // Selector de tema (círculos de color)
        JPanel temas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        temas.setOpaque(false);
        JLabel lt = new JLabel("Tema");
        lt.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lt.setForeground(t.textoSec);
        // El botón de ayuda permanece visible en todos los temas.
        Boton botonGuia = new Boton("?  Guía de uso", 14, t.panel2, t.borde, t.cian, t.ac1, t.ac2);
        botonGuia.setFont(new Font("SansSerif", Font.BOLD, 12));
        botonGuia.setPreferredSize(new Dimension(112, 32));
        botonGuia.setToolTipText("Aprende a usar la calculadora (F1)");
        botonGuia.addActionListener(e -> mostrarGuia());
        temas.add(botonGuia);
        temas.add(lt);
        for (Tema tm : Tema.values()) temas.add(new Muestra(tm));

        JPanel temasWrap = new JPanel(new GridBagLayout());
        temasWrap.setOpaque(false);
        temasWrap.add(temas);

        header.add(logo, BorderLayout.WEST);
        header.add(slogan, BorderLayout.CENTER);
        header.add(temasWrap, BorderLayout.EAST);
        return header;
    }

    private JPanel crearFooter(Tema t) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 4, 0, 4));

        JLabel logo = new JLabel("∞  Calculadora");
        logo.setFont(new Font("SansSerif", Font.BOLD, 13));
        logo.setForeground(t.textoSec);

        JLabel centro = new JLabel("Calcula  •  Aprende  •  Visualiza  •  Domina", SwingConstants.CENTER);
        centro.setFont(new Font("SansSerif", Font.PLAIN, 11));
        centro.setForeground(t.textoSec);

        JLabel der = new JLabel("Todo resultado, también es un paso hacia tu meta ♥");
        der.setFont(new Font("Serif", Font.ITALIC, 12));
        der.setForeground(mezclar(t.ac2, t.texto, 0.2));

        footer.add(logo, BorderLayout.WEST);
        footer.add(centro, BorderLayout.CENTER);
        footer.add(der, BorderLayout.EAST);
        return footer;
    }

    // =====================================================================
    //  CUERPO
    // =====================================================================
    private JPanel crearCuerpo(Tema t) {
        JPanel contenedor = new JPanel(new BorderLayout(10, 0));
        contenedor.setOpaque(false);
        JPanel cuerpo = new JPanel(new GridBagLayout());
        cuerpo.setOpaque(false);
        JPanel izquierda = crearColumnaIzquierda(t);
        JPanel derecha = crearColumnaDerecha(t);
        panelHistorial = crearHistorial(t);
        JScrollPane sc = new JScrollPane(cuerpo,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sc.setOpaque(false);sc.getViewport().setOpaque(false);sc.setBorder(null);
        contenedor.add(panelHistorial, BorderLayout.WEST);
        contenedor.add(sc, BorderLayout.CENTER);
        Runnable ajustar = () -> {
            int width = Math.max(getWidth(), contenedor.getWidth());
            boolean vertical = width < 1120;
            boolean mostrarHistorial = width >= 1450;
            panelHistorial.setVisible(mostrarHistorial);
            cuerpo.removeAll();
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill=GridBagConstraints.BOTH; gc.anchor=GridBagConstraints.NORTHWEST;
            gc.gridx=0;gc.gridy=0;gc.weightx=0.56;gc.weighty=1;
            gc.insets=new Insets(0,0,vertical?12:0,vertical?0:7);
            cuerpo.add(izquierda,gc);
            gc.gridx=vertical?0:1;gc.gridy=vertical?1:0;
            gc.weightx=vertical?1:0.44;gc.weighty=0;
            gc.anchor=GridBagConstraints.NORTH;
            gc.fill=GridBagConstraints.HORIZONTAL;
            gc.insets=new Insets(0,vertical?0:7,0,0);
            cuerpo.add(derecha,gc);
            if(vertical){
                izquierda.setPreferredSize(new Dimension(620,930));
                derecha.setPreferredSize(new Dimension(620,610));
            }else{
                izquierda.setPreferredSize(new Dimension(560,920));
                derecha.setPreferredSize(new Dimension(440,620));
            }
            cuerpo.revalidate();cuerpo.repaint();
        };
        ajustar.run();
        contenedor.addComponentListener(new java.awt.event.ComponentAdapter(){
            private boolean previoVertical=getWidth()<1120, previoHist=getWidth()>=1450;
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                boolean vertical=getWidth()<1120,hist=getWidth()>=1450;
                if(vertical!=previoVertical||hist!=previoHist){
                    previoVertical=vertical;previoHist=hist;ajustar.run();
                }
            }
        });
        setModo(modoDerivada);
        return contenedor;
    }

    private JPanel crearHistorial(Tema t) {
        Tarjeta tarjeta=new Tarjeta(new BorderLayout(4,8),t.panel,t.borde,20);
        tarjeta.setPreferredSize(new Dimension(185,200));
        tarjeta.setBorder(new EmptyBorder(12,10,12,10));
        JLabel titulo=new JLabel("◷  Historial");
        titulo.setFont(new Font("SansSerif",Font.BOLD,15));titulo.setForeground(t.texto);
        modeloHistorial=new DefaultListModel<>();
        for(String f:historial.obtener())modeloHistorial.addElement(f);
        listaHistorial=new JList<>(modeloHistorial);
        listaHistorial.setBackground(t.panel);listaHistorial.setForeground(t.texto);
        listaHistorial.setSelectionBackground(t.ac1);listaHistorial.setSelectionForeground(Color.WHITE);
        listaHistorial.setFont(new Font("Monospaced",Font.PLAIN,12));
        listaHistorial.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&listaHistorial.getSelectedValue()!=null){
            txtFuncion.setText(listaHistorial.getSelectedValue());ejecutarCalculo();
        }});
        JScrollPane scroll=new JScrollPane(listaHistorial);scroll.setBorder(null);
        Boton abrir=botonControl("Historial",t,110);
        abrir.addActionListener(e->mostrarDialogoHistorial());
        tarjeta.add(titulo,BorderLayout.NORTH);tarjeta.add(scroll,BorderLayout.CENTER);tarjeta.add(abrir,BorderLayout.SOUTH);
        return tarjeta;
    }

    // ---------------------------------------------------------------- IZQUIERDA
    private JPanel crearColumnaIzquierda(Tema t) {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(560, 100));

        JPanel arriba = new JPanel(new BorderLayout(0, 12));
        arriba.setOpaque(false);
        arriba.add(crearSelectorModo(t), BorderLayout.NORTH);
        arriba.add(crearTarjetaEntrada(t), BorderLayout.CENTER);

        col.add(arriba, BorderLayout.NORTH);
        col.add(crearResultados(t), BorderLayout.CENTER);
        return col;
    }

    private JPanel crearSelectorModo(Tema t) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fila.setOpaque(false);

        btnModoDer = new Boton("d/dx    Derivadas", 18, t.panel, t.borde, t.textoSec, t.ac1, t.ac2);
        btnModoInt = new Boton("∫    Integrales", 18, t.panel, t.borde, t.textoSec, t.ac1, t.ac2);
        for (Boton b : new Boton[]{btnModoDer, btnModoInt}) {
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            b.setPreferredSize(new Dimension(190, 44));
        }
        btnModoDer.addActionListener(e -> setModo(true));
        btnModoInt.addActionListener(e -> setModo(false));

        fila.add(btnModoDer);
        fila.add(btnModoInt);
        Boton btnHist=botonControl("◷ Historial",t,106);
        btnHist.setPreferredSize(new Dimension(106,44));
        btnHist.addActionListener(e->mostrarDialogoHistorial());
        fila.add(btnHist);
        return fila;
    }

    private Tarjeta crearTarjetaEntrada(Tema t) {
        Tarjeta card = new Tarjeta(new GridBagLayout(), t.panel, t.borde, 20);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblFun = new JLabel("Función");
        lblFun.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblFun.setForeground(t.texto);

        // Campo de texto con forma redondeada
        Tarjeta campo = new Tarjeta(new BorderLayout(10, 0), t.panel2, t.borde, 16);
        campo.setBorder(new EmptyBorder(6, 14, 6, 8));
        campo.setPreferredSize(new Dimension(100, 54));

        JLabel fx = new JLabel("f(x) =");
        fx.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 20));
        fx.setForeground(mezclar(t.ac2, t.texto, 0.3));

        txtFuncion = new JTextField(textoFuncion);
        txtFuncion.setFont(new Font("Monospaced", Font.BOLD, 18));
        txtFuncion.setForeground(t.texto);
        txtFuncion.setCaretColor(t.cian);
        txtFuncion.setOpaque(false);
        txtFuncion.setBorder(null);
        txtFuncion.addActionListener(e -> ejecutarCalculo());
        txtFuncion.getDocument().addUndoableEditListener(undo);
        txtFuncion.getInputMap().put(KeyStroke.getKeyStroke("control SPACE"),"autocompletar");
        txtFuncion.getActionMap().put("autocompletar",new AbstractAction(){
            public void actionPerformed(java.awt.event.ActionEvent e){mostrarAutocompletado();}
        });
        txtFuncion.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e){programarVista();}
            public void removeUpdate(DocumentEvent e){programarVista();}
            public void changedUpdate(DocumentEvent e){programarVista();}
        });

        Boton btnLimpiar = new Boton("×", 10, t.panel2, null, t.textoSec, t.ac1, t.ac2);
        btnLimpiar.setFont(new Font("SansSerif", Font.BOLD, 18));
        btnLimpiar.setPreferredSize(new Dimension(32, 32));
        btnLimpiar.addActionListener(e -> {
            txtFuncion.setText("");
            txtFuncion.requestFocusInWindow();
        });

        JPanel limpiarWrap = new JPanel(new GridBagLayout());
        limpiarWrap.setOpaque(false);
        limpiarWrap.add(btnLimpiar);

        campo.add(fx, BorderLayout.WEST);
        campo.add(txtFuncion, BorderLayout.CENTER);
        campo.add(limpiarWrap, BorderLayout.EAST);

        // Ejemplos rápidos
        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        JLabel lblEj = new JLabel("Ejemplos rápidos:");
        lblEj.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblEj.setForeground(t.textoSec);
        chips.add(lblEj);

        String[] ejemplos = {"x²", "sin(x)", "cos(x)", "ln(x)", "eˣ", "1/x"};
        String[] codigos = {"x^2", "sin(x)", "cos(x)", "ln(x)", "exp(x)", "1/x"};
        for (int i = 0; i < ejemplos.length; i++) {
            final String codigo = codigos[i];
            Boton chip = new Boton(ejemplos[i], 12, t.panel2, t.borde, t.cian, t.ac1, t.ac2);
            chip.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 14));
            chip.setPreferredSize(new Dimension(i == 0 || i == 4 || i == 5 ? 46 : 62, 30));
            chip.addActionListener(e -> {
                txtFuncion.setText(codigo);
                ejecutarCalculo();
            });
            chips.add(chip);
        }

        JPanel extras = crearControlesAvanzados(t);
        JPanel teclado = crearTeclado(t);
        vistaPrevia = new JLabel("Vista previa matemática");
        vistaPrevia.setForeground(t.textoSec);
        vistaPrevia.setFont(new Font("Cambria Math",Font.PLAIN,17));
        vistaPrevia.setPreferredSize(new Dimension(100,32));

        lblError = new JLabel(" ");
        lblError.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblError.setForeground(ROJO);

        Boton btnCalcular = new Boton("▶    Calcular", 16, t.ac1, null, Color.WHITE, t.ac1, t.ac2);
        btnCalcular.setSeleccionado(true);
        btnCalcular.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCalcular.setPreferredSize(new Dimension(100, 50));
        btnCalcular.addActionListener(e -> ejecutarCalculo());

        fila(card, lblFun, 0, 0, new Insets(0, 0, 8, 0));
        fila(card, campo, 1, 0, new Insets(0, 0, 10, 0));
        fila(card, chips, 2, 0, new Insets(0, 0, 4, 0));
        fila(card, extras, 3, 0, new Insets(0,0,8,0));
        fila(card, teclado, 4, 0, new Insets(0,0,8,0));
        fila(card, vistaPrevia, 5, 0, new Insets(0,0,3,0));
        fila(card, lblError, 6, 0, new Insets(2, 2, 6, 0));
        fila(card, btnCalcular, 7, 0, new Insets(0, 0, 0, 0));
        return card;
    }

    private JPanel crearControlesAvanzados(Tema t) {
        JPanel panel=new JPanel(new GridLayout(3,1,3,4));panel.setOpaque(false);
        JPanel r1=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));r1.setOpaque(false);
        cmbVariable=new JComboBox<>(new String[]{"x","y"});cmbVariable.setSelectedItem(variableElegida);
        cmbVariable.addActionListener(e->{variableElegida=(String)cmbVariable.getSelectedItem();ejecutarCalculo();});
        spOrden=new JSpinner(new SpinnerNumberModel(ordenDerivada,1,8,1));
        spOrden.addChangeListener(e->{ordenDerivada=(Integer)spOrden.getValue();ejecutarCalculo();});
        cmbTipoIntegral=new JComboBox<>(new String[]{"Indefinida","Definida"});
        cmbTipoIntegral.setSelectedIndex(integralDefinida?1:0);
        cmbTipoIntegral.addActionListener(e->{integralDefinida=cmbTipoIntegral.getSelectedIndex()==1;ejecutarCalculo();});
        cmbFormato=new JComboBox<>(new String[]{"Fracción","Decimal"});
        cmbFormato.setSelectedIndex(mostrarFraccion?0:1);
        cmbFormato.addActionListener(e->{mostrarFraccion=cmbFormato.getSelectedIndex()==0;ejecutarCalculo();});
        r1.add(etiquetaMini("Respecto a:",t));r1.add(cmbVariable);
        r1.add(etiquetaMini("Orden:",t));r1.add(spOrden);
        r1.add(cmbTipoIntegral);r1.add(cmbFormato);

        JPanel r2=new JPanel(new FlowLayout(FlowLayout.LEFT,6,0));r2.setOpaque(false);
        txtLimiteA=miniCampo(formatoMini(limiteA),48);
        txtLimiteB=miniCampo(formatoMini(limiteB),48);
        txtPunto=miniCampo(formatoMini(puntoTangente),48);
        r2.add(etiquetaMini("a =",t));r2.add(txtLimiteA);
        r2.add(etiquetaMini("b =",t));r2.add(txtLimiteB);
        r2.add(etiquetaMini("Tangente en:",t));r2.add(txtPunto);
        Boton btnPunto=botonControl("Aplicar",t,62);
        btnPunto.addActionListener(e->ejecutarCalculo());r2.add(btnPunto);
        JPanel r3=new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));r3.setOpaque(false);
        JButton aprender=botonControl("Aprender: OFF",t,116);
        aprender.addActionListener(e->{modoAprender=!modoAprender;aprender.setText("Aprender: "+(modoAprender?"ON":"OFF"));actualizarPasos();});
        JButton limites=botonControl("Límites",t,72);limites.addActionListener(e->calcularLimite());
        JButton taylor=botonControl("Taylor",t,68);taylor.addActionListener(e->calcularTaylor());
        JButton pdf=botonControl("PDF",t,50);pdf.addActionListener(e->exportarPDF());
        JButton png=botonControl("PNG",t,50);png.addActionListener(e->exportarPNG());
        r3.add(aprender);r3.add(limites);r3.add(taylor);r3.add(pdf);r3.add(png);
        panel.add(r1);panel.add(r2);panel.add(r3);
        return panel;
    }
    private String formatoMini(double a) {return MotorAvanzado.formato(a);}
    private JLabel etiquetaMini(String txt,Tema t){JLabel lbl=new JLabel(txt);lbl.setForeground(t.textoSec);lbl.setFont(new Font("SansSerif",Font.PLAIN,11));return lbl;}
    private JTextField miniCampo(String val,int width){
        JTextField campo=new JTextField(val);campo.setColumns(4);campo.setPreferredSize(new Dimension(width,27));
        campo.setHorizontalAlignment(SwingConstants.CENTER);campo.setFont(new Font("SansSerif",Font.PLAIN,12));
        return campo;
    }
    /**
     * Teclado matemático. Incluye el botón de raíz n-ésima y un acceso
     * directo a la raíz cúbica sin eliminar ninguno de los botones existentes.
     */
    private JPanel crearTeclado(Tema t) {
        String[][] simbolos = {
                {"x²", "√", "ⁿ√x", "π", "e", "sin", "cos", "tan", "ln", "log", "exp"},
                {"x", "y", "(", ")", "^", "/", "*", "+", "-", "abs", "∛x"}
        };
        JPanel pan = new JPanel(new GridLayout(2, 11, 3, 4));
        pan.setOpaque(false);
        for (String[] filaBotones : simbolos) {
            for (String texto : filaBotones) {
                Boton boton = new Boton(texto, 10, t.panel2, t.borde, t.cian, t.ac1, t.ac2);
                boton.setFont(new Font("SansSerif", Font.BOLD, 11));
                boton.setPreferredSize(new Dimension(36, 28));

                if (texto.equals("ⁿ√x")) {
                    boton.setToolTipText("Elige el índice n (2 a 99) de la raíz");
                    boton.addActionListener(e -> pedirIndiceRaiz());
                } else if (texto.equals("∛x")) {
                    boton.setToolTipText("Insertar raíz cúbica (índice 3)");
                    boton.addActionListener(e -> insertarRaizConIndice(3));
                } else {
                    boton.addActionListener(e -> {
                        String valor = switch (texto) {
                            case "x²" -> variableElegida + "^2";
                            case "√" -> "sqrt()";
                            case "π" -> "pi";
                            case "sin", "cos", "tan", "ln", "log", "exp", "abs" -> texto + "()";
                            default -> texto;
                        };
                        int inicio = txtFuncion.getSelectionStart();
                        int fin = txtFuncion.getSelectionEnd();
                        String anterior = txtFuncion.getText();
                        txtFuncion.setText(anterior.substring(0, inicio) + valor + anterior.substring(fin));
                        txtFuncion.requestFocusInWindow();
                        txtFuncion.setCaretPosition(inicio + valor.length() - (valor.endsWith("()") ? 1 : 0));
                    });
                }
                pan.add(boton);
            }
        }
        return pan;
    }

    /** Pregunta al usuario el índice sin exigir que aprenda una nueva sintaxis. */
    private void pedirIndiceRaiz() {
        JSpinner indice = new JSpinner(new SpinnerNumberModel(3, 2, 99, 1));
        indice.setFont(new Font("SansSerif", Font.BOLD, 14));
        JPanel contenido = new JPanel(new BorderLayout(0, 9));
        contenido.add(new JLabel("Elige el índice de la raíz (n):"), BorderLayout.NORTH);
        contenido.add(indice, BorderLayout.CENTER);
        contenido.add(new JLabel("Ejemplo: n = 4 significa raíz cuarta."), BorderLayout.SOUTH);
        int opcion = JOptionPane.showConfirmDialog(
                this, contenido, "Calcular raíz de índice n",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                indice.commitEdit();
                int n = ((Number) indice.getValue()).intValue();
                if (n < 2 || n > 99) {
                    JOptionPane.showMessageDialog(this, "El índice debe estar entre 2 y 99.",
                            "Índice inválido", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                insertarRaizConIndice(n);
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(this, "Escribe un índice entero válido.",
                        "Índice inválido", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /**
     * Inserta (radicando)^(1/n), formato que entiende MotorAvanzado.
     * Si hay texto seleccionado, se usa como radicando. En caso contrario,
     * se inserta x (o y) y se selecciona para poder sustituirla al teclear.
     */
    private void insertarRaizConIndice(int n) {
        if (txtFuncion == null) return;
        int inicio = txtFuncion.getSelectionStart();
        int fin = txtFuncion.getSelectionEnd();
        String anterior = txtFuncion.getText();
        boolean haySeleccion = fin > inicio;
        String radicando = haySeleccion ? anterior.substring(inicio, fin) : variableElegida;
        String expresionRaiz = "(" + radicando + ")^(1/" + n + ")";
        txtFuncion.setText(anterior.substring(0, inicio) + expresionRaiz + anterior.substring(fin));
        txtFuncion.requestFocusInWindow();
        if (haySeleccion) {
            txtFuncion.setCaretPosition(inicio + expresionRaiz.length());
        } else {
            txtFuncion.select(inicio + 1, inicio + 1 + radicando.length());
        }
    }

    private void mostrarAutocompletado(){
        JPopupMenu menu=new JPopupMenu();
        for(String fn:new String[]{"sin(x)","cos(x)","tan(x)","ln(x)","log(x)","sqrt(x)",
                "exp(x)","abs(x)","asin(x)","acos(x)","atan(x)","sinh(x)","cosh(x)"}){
            JMenuItem item=new JMenuItem(fn);
            item.addActionListener(e->{int inicio=txtFuncion.getCaretPosition();
                String actual=txtFuncion.getText();
                txtFuncion.setText(actual.substring(0,inicio)+fn+actual.substring(inicio));
                txtFuncion.requestFocusInWindow();
                txtFuncion.setCaretPosition(inicio+fn.indexOf('(')+1);
            });menu.add(item);
        }
        menu.show(txtFuncion,10,txtFuncion.getHeight());
    }

    private void programarVista(){
        if(temporizadorVista!=null)temporizadorVista.stop();
        temporizadorVista=new javax.swing.Timer(220,e->{
            if(vistaPrevia==null||txtFuncion==null)return;
            try{
                Funcion f=MotorAvanzado.parsear(txtFuncion.getText());
                JLabel pre=RenderizadorLatex.crear("f("+variableElegida+")="+f.latex(),temaActual.texto);
                vistaPrevia.setText(pre.getText());vistaPrevia.setIcon(pre.getIcon());
                vistaPrevia.setToolTipText(f.toString());
            }catch(Exception ex){vistaPrevia.setIcon(null);vistaPrevia.setText("Vista previa: "+ayudaError(ex.getMessage()));}
        });temporizadorVista.setRepeats(false);temporizadorVista.start();
    }
    private String ayudaError(String error) {
        if(error==null)return "Revisa la expresión";
        if(error.contains("Función desconocida: sen")||error.contains("Función desconocida: sni"))
            return "¿Quisiste decir sin(x)? Pulsa Ctrl+Espacio para autocompletar.";
        if(error.contains("Falta cerrar"))return "Falta un paréntesis ')'. "+error;
        return error;
    }

    private JPanel crearResultados(Tema t) {
        JPanel grid = new JPanel(new GridLayout(1, 2, 12, 0));
        grid.setOpaque(false);

        txtDerivadaRes = areaResultado(t);
        txtPasosDerivada = areaPasos(t);
        txtIntegralRes = areaResultado(t);
        txtPasosIntegral = areaPasos(t);

        cardDerivada = crearCardResultado(t, "Derivada", "d/dx", txtDerivadaRes, txtPasosDerivada);
        cardIntegral = crearCardResultado(t, "Integral", "∫", txtIntegralRes, txtPasosIntegral);

        grid.add(cardDerivada);
        grid.add(cardIntegral);
        return grid;
    }

    private Tarjeta crearCardResultado(Tema t, String titulo, String simbolo, JTextArea res, JTextArea pasos) {
        Tarjeta card = new Tarjeta(new BorderLayout(0, 10), t.panel, t.borde, 20);
        card.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel cabecera = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cabecera.setOpaque(false);
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(t.texto);
        cabecera.add(new Insignia(simbolo, t));
        cabecera.add(lbl);
        JPanel superior=new JPanel(new BorderLayout(0,4));superior.setOpaque(false);
        superior.add(cabecera,BorderLayout.NORTH);
        JPanel formulas=new JPanel(new BorderLayout());formulas.setOpaque(false);
        formulas.setPreferredSize(new Dimension(100,35));
        if("Derivada".equals(titulo))vistaLatexDer=formulas;else vistaLatexInt=formulas;
        superior.add(formulas,BorderLayout.SOUTH);
        card.add(superior,BorderLayout.NORTH);

        JPanel contenido = new JPanel(new GridBagLayout());
        contenido.setOpaque(false);

        // Caja del resultado + botón copiar
        Tarjeta cajaRes = new Tarjeta(new BorderLayout(6, 0), t.panel2, t.borde, 14);
        cajaRes.setBorder(new EmptyBorder(6, 12, 6, 8));
        JScrollPane spRes = crearScroll(res, t);
        spRes.setPreferredSize(new Dimension(100, 76));

        Boton copiar = new Boton("Copiar", 10, t.panel, t.borde, t.cian, t.ac1, t.ac2);
        copiar.setFont(new Font("SansSerif", Font.BOLD, 11));
        copiar.setPreferredSize(new Dimension(62, 26));
        copiar.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(res.getText()), null);
            copiar.setText("¡Listo!");
            Timer tm = new Timer(1200, ev -> copiar.setText("Copiar"));
            tm.setRepeats(false);
            tm.start();
        });
        JPanel copiarWrap = new JPanel(new BorderLayout());
        copiarWrap.setOpaque(false);
        copiarWrap.add(copiar, BorderLayout.NORTH);

        cajaRes.add(spRes, BorderLayout.CENTER);
        cajaRes.add(copiarWrap, BorderLayout.EAST);

        // Pasos
        JLabel lblPasos = new JLabel("⏱  Pasos de la solución");
        lblPasos.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblPasos.setForeground(t.textoSec);

        Tarjeta cajaPasos = new Tarjeta(new BorderLayout(), t.panel2, t.borde, 14);
        cajaPasos.setBorder(new EmptyBorder(6, 10, 6, 6));
        cajaPasos.add(crearScroll(pasos, t), BorderLayout.CENTER);
        JPanel pasoMath=new JPanel(new BorderLayout());pasoMath.setOpaque(false);
        pasoMath.setPreferredSize(new Dimension(100,32));
        if(pasos==txtPasosDerivada)vistaPasoMatDer=pasoMath;else vistaPasoMatInt=pasoMath;
        cajaPasos.add(pasoMath,BorderLayout.NORTH);
        JButton siguiente=botonControl("Siguiente paso ▶",t,128);
        siguiente.addActionListener(e->{
            if(pasos==txtPasosDerivada)pasoVisibleDer=Math.min(pasosDer.size(),pasoVisibleDer+1);
            else pasoVisibleInt=Math.min(pasosInt.size(),pasoVisibleInt+1);
            actualizarPasos();
        });
        if(pasos==txtPasosDerivada)siguienteDer=siguiente;else siguienteInt=siguiente;
        cajaPasos.add(siguiente,BorderLayout.SOUTH);

        fila(contenido, cajaRes, 0, 0, new Insets(0, 0, 0, 0));
        fila(contenido, lblPasos, 1, 0, new Insets(12, 2, 6, 0));
        fila(contenido, cajaPasos, 2, 1, new Insets(0, 0, 0, 0));

        card.add(contenido, BorderLayout.CENTER);
        return card;
    }

    private JTextArea areaResultado(Tema t) {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setFont(new Font("Monospaced", Font.BOLD, 15));
        a.setForeground(t.texto);
        a.setBorder(new EmptyBorder(4, 4, 4, 4));
        return a;
    }

    private JTextArea areaPasos(Tema t) {
        JTextArea a = new JTextArea();
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setOpaque(false);
        a.setFont(new Font("SansSerif", Font.PLAIN, 12));
        a.setForeground(mezclar(t.textoSec, t.texto, 0.4));
        a.setBorder(new EmptyBorder(4, 4, 4, 4));
        return a;
    }

    private JScrollPane crearScroll(JTextArea area, Tema t) {
        JScrollPane sp = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(null);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        JScrollBar sb = sp.getVerticalScrollBar();
        sb.setPreferredSize(new Dimension(8, 0));
        sb.setOpaque(false);
        final Color thumb = mezclar(t.borde, t.textoSec, 0.5);
        sb.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = thumb;
                trackColor = t.panel2;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return botonVacio();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return botonVacio();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumb);
                g2.fill(new RoundRectangle2D.Double(r.x + 1, r.y, r.width - 2, r.height, 6, 6));
                g2.dispose();
            }
        });
        return sp;
    }

    private static JButton botonVacio() {
        JButton b = new JButton();
        Dimension d = new Dimension(0, 0);
        b.setPreferredSize(d);
        b.setMinimumSize(d);
        b.setMaximumSize(d);
        return b;
    }

    // ---------------------------------------------------------------- DERECHA
    private JPanel crearColumnaDerecha(Tema t) {
        JPanel col = new JPanel(new BorderLayout(0, 12));
        col.setOpaque(false);
        col.setPreferredSize(new Dimension(440, 100));
        col.add(crearTarjetaGrafica(t), BorderLayout.CENTER);
        col.add(crearTarjetaDatos(t), BorderLayout.SOUTH);
        return col;
    }

    private Tarjeta crearTarjetaGrafica(Tema t) {
        Tarjeta card = new Tarjeta(new BorderLayout(0, 10), t.panel, t.borde, 20);
        card.setBorder(new EmptyBorder(14, 14, 12, 14));

        JLabel titulo = new JLabel("📈  Gráfica de la función");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 15));
        titulo.setForeground(t.texto);

        panelGrafico = new PanelGrafico(t);
        panelGrafico.setPreferredSize(new Dimension(380, 300));

        JPanel controles = new JPanel(new GridLayout(2,1,0,4));
        controles.setOpaque(false);
        JPanel controlesArriba=new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));controlesArriba.setOpaque(false);
        JPanel controlesAbajo=new JPanel(new FlowLayout(FlowLayout.RIGHT,4,0));controlesAbajo.setOpaque(false);
        controles.add(controlesArriba);controles.add(controlesAbajo);
        JLabel ayuda = new JLabel("Rueda: zoom  •  Arrastrar: mover");
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ayuda.setForeground(t.textoSec);
        controlesArriba.add(ayuda);
        txtAdicionales=new JTextField(comparacionesGuardadas,12);
        txtAdicionales.setToolTipText("Funciones separadas por punto y coma. Ejemplo: x^2;sin(x)");
        txtAdicionales.setFont(new Font("SansSerif",Font.PLAIN,11));
        controlesArriba.add(txtAdicionales);
        Boton otras=botonControl("Comparar",t,75);
        otras.addActionListener(e->{panelGrafico.setAdicionales(leerComparaciones());panelGrafico.repaint();});
        controlesArriba.add(otras);

        Boton mas = botonControl("+", t, 34);
        Boton menos = botonControl("-", t, 34);
        Boton ajustar = botonControl("Ajustar", t, 68);
        mas.addActionListener(e -> panelGrafico.acercar());
        menos.addActionListener(e -> panelGrafico.alejar());
        ajustar.addActionListener(e -> panelGrafico.ajustar());
        controlesAbajo.add(mas);
        controlesAbajo.add(menos);
        controlesAbajo.add(ajustar);

        card.add(titulo, BorderLayout.NORTH);
        card.add(panelGrafico, BorderLayout.CENTER);
        card.add(controles, BorderLayout.SOUTH);
        return card;
    }

    private Boton botonControl(String texto, Tema t, int ancho) {
        Boton b = new Boton(texto, 12, t.panel2, t.borde, t.texto, t.ac1, t.ac2);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(ancho, 30));
        return b;
    }

    private Tarjeta crearTarjetaDatos(Tema t) {
        Tarjeta card = new Tarjeta(new GridBagLayout(), t.panel, t.borde, 20);
        card.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel titulo = new JLabel("💡  Datos importantes");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 14));
        titulo.setForeground(t.texto);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 3;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 8, 0);
        card.add(titulo, gc);

        lblDominio = new JLabel("—");
        lblRango = new JLabel("—");
        lblExtremos = new JLabel("—");
        lblRaices = new JLabel("—");
        lblCorteY = new JLabel("—");

        filaDato(card, 1, t.cian, "Dominio:", lblDominio, t);
        filaDato(card, 2, t.ac2, "Rango:", lblRango, t);
        filaDato(card, 3, mezclar(t.cian, Color.WHITE, 0.3), "Extremos:", lblExtremos, t);
        filaDato(card, 4, VERDE, "Cortes con el eje x:", lblRaices, t);
        filaDato(card, 5, NARANJA, "Corte con el eje y:", lblCorteY, t);
        return card;
    }

    private void filaDato(JPanel card, int y, Color punto, String nombre, JLabel valor, Tema t) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = y;
        gc.anchor = GridBagConstraints.WEST;

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setForeground(punto);
        gc.gridx = 0;
        gc.insets = new Insets(3, 0, 3, 8);
        card.add(dot, gc);

        JLabel n = new JLabel(nombre);
        n.setFont(new Font("SansSerif", Font.PLAIN, 13));
        n.setForeground(t.textoSec);
        gc.gridx = 1;
        gc.insets = new Insets(3, 0, 3, 10);
        card.add(n, gc);

        valor.setFont(new Font("SansSerif", Font.BOLD, 13));
        valor.setForeground(t.texto);
        gc.gridx = 2;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(3, 0, 3, 0);
        card.add(valor, gc);
    }

    private static void fila(JPanel p, Component c, int y, double pesoY, Insets ins) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = y;
        gc.weightx = 1;
        gc.weighty = pesoY;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.insets = ins;
        p.add(c, gc);
    }

    // =====================================================================
    //  LÓGICA
    // =====================================================================
    private void setModo(boolean derivada) {
        modoDerivada = derivada;
        Tema t = temaActual;
        btnModoDer.setSeleccionado(derivada);
        btnModoInt.setSeleccionado(!derivada);
        cardDerivada.setBorde(derivada ? t.ac1 : t.borde);
        cardIntegral.setBorde(!derivada ? t.cian : t.borde);
        actualizarGrafica(false);
    }

    private void actualizarGrafica(boolean reajustar) {
        if (modoDerivada) {
            panelGrafico.setExpresiones(exprActual, derActual, ordenDerivada==1?"f'("+variableElegida+")":"f^("+ordenDerivada+")", reajustar);
        } else {
            panelGrafico.setExpresiones(exprActual, intgActual, "F("+variableElegida+")", reajustar);
        }
    }

    private void mostrarError(String msg) {
        String linea = msg == null ? "Error desconocido" : msg.split("\n")[0];
        lblError.setText("⚠  " + linea);
    }

    private void ejecutarCalculo() {
        if(txtFuncion==null)return;
        String entrada=txtFuncion.getText().trim();
        textoFuncion=entrada;
        if(entrada.isEmpty()) {mostrarError("Escribe una función, por ejemplo x^2 + 3*x + 2");return;}
        try {
            variableElegida=(String)cmbVariable.getSelectedItem();
            variableActiva=variableElegida;
            ordenDerivada=(Integer)spOrden.getValue();
            integralDefinida=cmbTipoIntegral.getSelectedIndex()==1;
            mostrarFraccion=cmbFormato.getSelectedIndex()==0;
            limiteA=Double.parseDouble(txtLimiteA.getText().replace(',','.'));
            limiteB=Double.parseDouble(txtLimiteB.getText().replace(',','.'));
            puntoTangente=Double.parseDouble(txtPunto.getText().replace(',','.'));
            Funcion expr=MotorAvanzado.parsear(entrada);
            Funcion der=expr.derivar(variableElegida,ordenDerivada);
            String pd=MotorAvanzado.pasosDerivada(expr,variableElegida,ordenDerivada);
            lblError.setText(" ");
            exprActual=expr;derActual=der;
            String etiqueta=ordenDerivada==1?"f'("+variableElegida+")": "f^("+ordenDerivada+")("+variableElegida+")";
            txtDerivadaRes.setText(etiqueta+" = "+der.formato(mostrarFraccion));
            String latex= "\\frac{d^{"+ordenDerivada+"}}{d"+variableElegida+"^{"+ordenDerivada+"}} f="+der.latex(mostrarFraccion);
            mostrarFormula(vistaLatexDer,latex);
            // Tangente usa la primera derivada incluso si se selecciona un orden superior.
            double x=puntoTangente, y=expr.evaluar(variableElegida,x);
            double pendiente=expr.derivar(variableElegida).evaluar(variableElegida,x);
            if(Double.isFinite(y)&&Double.isFinite(pendiente)){
                pd+="\nRecta tangente en "+variableElegida+"="+MotorAvanzado.formato(x)
                        +": y = "+MotorAvanzado.formato(pendiente)+"*("+variableElegida+" - "+MotorAvanzado.formato(x)
                        +") + "+MotorAvanzado.formato(y)+"\n";
                panelGrafico.setTangente(x,y,pendiente);
            }else panelGrafico.setTangente(Double.NaN,Double.NaN,Double.NaN);
            cargarPasos(pasosDer,pd);
            // Integral definida, indefinida y opción numérica de respaldo.
            try {
                Funcion intg=expr.integrar(variableElegida);
                intgActual=intg;
                if(integralDefinida) {
                    double r=MotorAvanzado.integrarDefinida(expr,variableElegida,limiteA,limiteB);
                    txtIntegralRes.setText("∫["+limiteA+", "+limiteB+"] f("+variableElegida+") d"+variableElegida
                            +" = "+(mostrarFraccion?MotorAvanzado.fraccion(r):MotorAvanzado.formato(r)));
                    mostrarFormula(vistaLatexInt,"\\int_{"+limiteA+"}^{"+limiteB+"}"+expr.latex()+"\\,d"+variableElegida+"="+MotorAvanzado.fraccion(r));
                }else{
                    txtIntegralRes.setText("∫ f("+variableElegida+") d"+variableElegida+" = "+intg.formato(mostrarFraccion)+" + C");
                    mostrarFormula(vistaLatexInt,"\\int "+expr.latex()+"\\,d"+variableElegida+"="+intg.latex(mostrarFraccion)+"+C");
                }
                cargarPasos(pasosInt,MotorAvanzado.pasosIntegral(expr,variableElegida,
                        integralDefinida?limiteA:null,integralDefinida?limiteB:null));
            }catch(Exception ex){
                intgActual=null;
                if(integralDefinida){
                    double r=MotorAvanzado.integrarDefinida(expr,variableElegida,limiteA,limiteB);
                    txtIntegralRes.setText("Aproximación numérica = "+(mostrarFraccion?MotorAvanzado.fraccion(r):MotorAvanzado.formato(r)));
                    mostrarFormula(vistaLatexInt,"\\int_{"+limiteA+"}^{"+limiteB+"}"+expr.latex()+"\\,d"+variableElegida+"\\approx "+MotorAvanzado.formato(r));
                    cargarPasos(pasosInt,MotorAvanzado.pasosIntegral(expr,variableElegida,limiteA,limiteB));
                }else{
                    txtIntegralRes.setText("Primitiva simbólica no disponible");
                    mostrarFormula(vistaLatexInt,"\\int "+expr.latex()+"\\,d"+variableElegida);
                    cargarPasos(pasosInt,ex.getMessage()==null?"Sin regla simbólica":ex.getMessage());
                }
            }
            txtDerivadaRes.setCaretPosition(0);
            txtIntegralRes.setCaretPosition(0);
            Datos datos=calcularDatos(expr);
            mostrarDatos(datos);
            panelGrafico.setMarcadores(datos.raicesPts,datos.extremosPts,datos.yInt);
            panelGrafico.setSombreado(integralDefinida,limiteA,limiteB);
            actualizarGrafica(true);
            panelGrafico.setAdicionales(leerComparaciones());
            panelGrafico.iniciarAnimacion();
            historial.agregar(entrada);
            if(modeloHistorial!=null){modeloHistorial.clear();for(String item:historial.obtener())modeloHistorial.addElement(item);}
            programarVista();
        }catch(Exception ex){mostrarError("Revisa la función o sus parámetros: "+ayudaError(ex.getMessage()));}
    }
    private java.util.List<Funcion> leerComparaciones(){
        java.util.List<Funcion> lista=new ArrayList<>();
        if(txtAdicionales==null)return lista;
        for(String s:txtAdicionales.getText().split(";")){
            if(s.isBlank())continue;
            try{lista.add(MotorAvanzado.parsear(s.trim()));}catch(Exception ignored){}
            if(lista.size()>=4)break;
        }
        return lista;
    }
    private void mostrarFormula(JPanel destino,String latex){
        if(destino==null)return;
        destino.removeAll();destino.add(RenderizadorLatex.crear(latex,temaActual.texto),BorderLayout.CENTER);
        destino.revalidate();destino.repaint();
    }
    private void cargarPasos(java.util.List<String> destino,String texto){
        destino.clear();for(String linea:texto.split("\n"))if(!linea.isBlank())destino.add(linea);
        pasoVisibleDer=pasosDer.size()>0?1:0;pasoVisibleInt=pasosInt.size()>0?1:0;
        actualizarPasos();
    }
    private void actualizarPasos(){
        if(txtPasosDerivada==null||txtPasosIntegral==null)return;
        txtPasosDerivada.setText(modoAprender?String.join("\n",pasosDer.subList(0,Math.min(pasosVisible(pasoVisibleDer),pasosDer.size()))):String.join("\n",pasosDer));
        txtPasosIntegral.setText(modoAprender?String.join("\n",pasosInt.subList(0,Math.min(pasosVisible(pasoVisibleInt),pasosInt.size()))):String.join("\n",pasosInt));
        if(siguienteDer!=null)siguienteDer.setVisible(modoAprender);
        if(siguienteInt!=null)siguienteInt.setVisible(modoAprender);
        if(exprActual!=null){
            if(vistaPasoMatDer!=null&&derActual!=null)
                mostrarFormula(vistaPasoMatDer,"\\frac{d}{d"+variableElegida+"}\\left("+exprActual.latex()+"\\right)="+derActual.latex(mostrarFraccion));
            if(vistaPasoMatInt!=null)
                mostrarFormula(vistaPasoMatInt,"\\int "+exprActual.latex()+"\\,d"+variableElegida);
        }
    }
    private int pasosVisible(int n){return Math.max(1,n);}
    private void mostrarDialogoHistorial(){
        JList<String> vista=new JList<>(historial.obtener().toArray(new String[0]));
        vista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel panel=new JPanel(new BorderLayout(4,8));
        panel.add(new JLabel("Selecciona una función para volver a calcularla:"),BorderLayout.NORTH);
        panel.add(new JScrollPane(vista),BorderLayout.CENTER);
        Object[] opciones={"Cargar", "Borrar historial", "Cancelar"};
        int eleccion=JOptionPane.showOptionDialog(this,panel,"Historial",JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,null,opciones,opciones[0]);
        if(eleccion==0&&vista.getSelectedValue()!=null){txtFuncion.setText(vista.getSelectedValue());ejecutarCalculo();}
        if(eleccion==1){historial.limpiar();if(modeloHistorial!=null)modeloHistorial.clear();}
    }
    private void calcularLimite(){
        if(exprActual==null)return;
        String t=JOptionPane.showInputDialog(this,"Límite cuando "+variableElegida+" tiende a:","0");
        if(t==null)return;
        try {double punto=Double.parseDouble(t.replace(',','.'));
            double valor=MotorAvanzado.limite(exprActual,variableElegida,punto);
            JOptionPane.showMessageDialog(this,"Límite aproximado: "+MotorAvanzado.formato(valor));
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Límite",JOptionPane.WARNING_MESSAGE);}
    }
    private void calcularTaylor(){
        if(exprActual==null)return;
        String centro=JOptionPane.showInputDialog(this,"Centro de Taylor:","0");if(centro==null)return;
        String grado=JOptionPane.showInputDialog(this,"Grado (0 a 8):","4");if(grado==null)return;
        try{JOptionPane.showMessageDialog(this,MotorAvanzado.taylor(exprActual,variableElegida,
                Double.parseDouble(centro.replace(',','.')),Integer.parseInt(grado)),"Serie de Taylor",JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Taylor",JOptionPane.WARNING_MESSAGE);}
    }
    private void exportarPDF(){
        JFileChooser archivo=new JFileChooser();archivo.setSelectedFile(new File("resultado_calculadora.pdf"));
        if(archivo.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try{File f=archivo.getSelectedFile();if(!f.getName().toLowerCase(Locale.ROOT).endsWith(".pdf"))f=new File(f.getPath()+".pdf");
            ExportadorPDF.exportar(f.toPath(),"Calculadora - Soluciones",
                    "Funcion: "+textoFuncion+"\n\nDERIVADA\n"+txtDerivadaRes.getText()+"\n"+txtPasosDerivada.getText()
                            +"\n\nINTEGRAL\n"+txtIntegralRes.getText()+"\n"+txtPasosIntegral.getText());
            JOptionPane.showMessageDialog(this,"PDF guardado en: "+f.getAbsolutePath());
        }catch(Exception ex){mostrarError("No se pudo exportar PDF: "+ex.getMessage());}
    }
    private void exportarPNG(){
        JFileChooser archivo=new JFileChooser();archivo.setSelectedFile(new File("grafica_calculadora.png"));
        if(archivo.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        try{File f=archivo.getSelectedFile();if(!f.getName().toLowerCase(Locale.ROOT).endsWith(".png"))f=new File(f.getPath()+".png");
            BufferedImage imagen=new BufferedImage(Math.max(panelGrafico.getWidth(),600),Math.max(panelGrafico.getHeight(),400),BufferedImage.TYPE_INT_ARGB);
            Graphics2D g=imagen.createGraphics();panelGrafico.paint(g);g.dispose();ImageIO.write(imagen,"png",f);
            JOptionPane.showMessageDialog(this,"Gráfica guardada en: "+f.getAbsolutePath());
        }catch(Exception ex){mostrarError("No se pudo exportar PNG: "+ex.getMessage());}
    }
    private void mostrarGuia() {
        GuiaUsuario guia = new GuiaUsuario(this, temaActual, funcion -> {
            txtFuncion.setText(funcion);
            ejecutarCalculo();
            txtFuncion.requestFocusInWindow();
        });
        guia.setVisible(true);
    }

    private void configurarAtajos(){
        JRootPane root=getRootPane();InputMap keys=root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        keys.put(KeyStroke.getKeyStroke("F1"), "mostrarGuia");
        root.getActionMap().put("mostrarGuia", new AbstractAction(){
            public void actionPerformed(java.awt.event.ActionEvent e){ mostrarGuia(); }
        });
        keys.put(KeyStroke.getKeyStroke("control ENTER"),"calcular");
        keys.put(KeyStroke.getKeyStroke("control L"),"limpiar");
        keys.put(KeyStroke.getKeyStroke("control Z"),"deshacer");
        root.getActionMap().put("calcular",new AbstractAction(){public void actionPerformed(java.awt.event.ActionEvent e){ejecutarCalculo();}});
        root.getActionMap().put("limpiar",new AbstractAction(){public void actionPerformed(java.awt.event.ActionEvent e){txtFuncion.setText("");}});
        root.getActionMap().put("deshacer",new AbstractAction(){public void actionPerformed(java.awt.event.ActionEvent e){
            if(txtFuncion.isFocusOwner()&&undo.canUndo())undo.undo();
        }});
    }
    private Image crearIcono(){
        BufferedImage img=new BufferedImage(64,64,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=img.createGraphics();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,new Color(130,60,240),64,64,new Color(0,210,255)));
        g.fillRoundRect(2,2,60,60,18,18);g.setColor(Color.WHITE);g.setFont(new Font("Serif",Font.BOLD,40));
        g.drawString("∫",25,47);g.dispose();return img;
    }

    // =====================================================================
    //  DATOS IMPORTANTES (cálculo numérico aproximado en [-10, 10])
    // =====================================================================
    private static class Datos {
        String dominio = "ℝ", rango = "—", extremos = "—", raices = "—", corteY = "—";
        List<double[]> raicesPts = new ArrayList<>();
        List<double[]> extremosPts = new ArrayList<>(); // {x, y, tipo(0=min,1=max)}
        double yInt = Double.NaN;
    }

    private void mostrarDatos(Datos d) {
        asignar(lblDominio, d.dominio);
        asignar(lblRango, d.rango);
        asignar(lblExtremos, d.extremos);
        asignar(lblRaices, d.raices);
        asignar(lblCorteY, d.corteY);
    }

    private static void asignar(JLabel l, String s) {
        l.setText(s);
        l.setToolTipText(s);
    }

    private Datos calcularDatos(Funcion f) {
        int n = (int) Math.round((X1 - X0) / PASO) + 1;
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = X0 + i * PASO;
            double y = ev(f, xs[i]);
            ys[i] = (!Double.isFinite(y) || Math.abs(y) > 1e6) ? Double.NaN : y;
        }

        Datos d = new Datos();
        d.dominio = calcularDominio(xs, ys);
        d.rango = calcularRango(ys);
        buscarExtremos(f, xs, ys, d);
        buscarRaices(f, xs, ys, d);

        double y0 = ev(f, 0);
        if (Double.isFinite(y0)) {
            d.yInt = y0;
            d.corteY = "(0, " + fmt(y0) + ")";
        } else {
            d.corteY = "No definido";
        }
        return d;
    }

    private String calcularDominio(double[] xs, double[] ys) {
        int n = xs.length;
        List<double[]> invalidos = new ArrayList<>();
        int i = 0;
        while (i < n) {
            if (Double.isNaN(ys[i])) {
                int j = i;
                while (j + 1 < n && Double.isNaN(ys[j + 1])) j++;
                invalidos.add(new double[]{xs[i], xs[j]});
                i = j + 1;
            } else {
                i++;
            }
        }
        if (invalidos.isEmpty()) return "ℝ";

        boolean puntuales = true;
        for (double[] r : invalidos) if (r[1] - r[0] > 0.05) puntuales = false;

        if (puntuales && invalidos.size() <= 4) {
            StringBuilder sb = new StringBuilder("ℝ \\ {");
            for (int k = 0; k < invalidos.size(); k++) {
                if (k > 0) sb.append(", ");
                sb.append(fmt((invalidos.get(k)[0] + invalidos.get(k)[1]) / 2));
            }
            return sb.append("}").toString();
        }

        List<String> partes = new ArrayList<>();
        double ini = X0;
        boolean iniInf = true;
        for (double[] r : invalidos) {
            if (r[0] > X0 + 1e-9) {
                partes.add(intervalo(ini, r[0], iniInf, false));
            }
            ini = r[1];
            iniInf = false;
        }
        if (ini < X1 - 1e-9) partes.add(intervalo(ini, X1, iniInf, true));
        if (partes.isEmpty()) return "No definido en [-10, 10]";
        return "x ∈ " + String.join(" ∪ ", partes);
    }

    private static String intervalo(double a, double b, boolean aInf, boolean bInf) {
        String izq = aInf ? "(-∞" : "(" + fmt(a);
        String der = bInf ? "∞)" : fmt(b) + ")";
        return izq + ", " + der;
    }

    private String calcularRango(double[] ys) {
        int n = ys.length;
        double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
        int imin = -1, imax = -1, primero = -1, ultimo = -1;
        for (int i = 0; i < n; i++) {
            if (Double.isNaN(ys[i])) continue;
            if (primero < 0) primero = i;
            ultimo = i;
            if (ys[i] < min) {
                min = ys[i];
                imin = i;
            }
            if (ys[i] > max) {
                max = ys[i];
                imax = i;
            }
        }
        if (imin < 0) return "No definido";
        if (max - min < 1e-9) return "y = " + fmt(min);

        boolean minBorde = enBorde(imin, primero, ultimo);
        boolean maxBorde = enBorde(imax, primero, ultimo);
        boolean minInf = min < -50 || (minBorde && sigueCambiando(ys, imin, primero, ultimo));
        boolean maxInf = max > 50 || (maxBorde && sigueCambiando(ys, imax, primero, ultimo));

        String izq = minInf ? "(-∞" : (minBorde ? "(" : "[") + fmt(min);
        String der = maxInf ? "∞)" : fmt(max) + (maxBorde ? ")" : "]");
        return izq + ", " + der;
    }

    private static boolean enBorde(int i, int primero, int ultimo) {
        return (i - primero) < 5 || (ultimo - i) < 5;
    }

    private static boolean sigueCambiando(double[] ys, int i, int primero, int ultimo) {
        int interior = i < (primero + ultimo) / 2 ? Math.min(i + 50, ultimo) : Math.max(i - 50, primero);
        if (Double.isNaN(ys[interior])) return false;
        return Math.abs(ys[i] - ys[interior]) > 0.01;
    }

    private void buscarExtremos(Funcion f, double[] xs, double[] ys, Datos d) {
        int n = xs.length;
        for (int i = 1; i < n - 1; i++) {
            double a = ys[i - 1], b = ys[i], c = ys[i + 1];
            if (Double.isNaN(a) || Double.isNaN(b) || Double.isNaN(c)) continue;
            boolean esMin = b < a && b <= c;
            boolean esMax = b > a && b >= c;
            if (!esMin && !esMax) continue;

            double den = a - 2 * b + c;
            double xv = xs[i];
            if (Math.abs(den) > 1e-12) xv = xs[i] + 0.5 * PASO * (a - c) / den;
            double yv = ev(f, xv);
            if (!Double.isFinite(yv) || Math.abs(yv) > 1e4) continue;

            boolean repetido = false;
            for (double[] p : d.extremosPts) if (Math.abs(p[0] - xv) < 0.05) repetido = true;
            if (repetido) continue;

            d.extremosPts.add(new double[]{xv, yv, esMin ? 0 : 1});
            if (Math.abs(yv) < 1e-6) agregarUnico(d.raicesPts, xv); // raíz tangente
        }

        if (d.extremosPts.isEmpty()) {
            d.extremos = "Ninguno";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < Math.min(2, d.extremosPts.size()); k++) {
            double[] p = d.extremosPts.get(k);
            if (k > 0) sb.append(";  ");
            sb.append(p[2] == 0 ? "mín " : "máx ").append("(").append(fmt(p[0])).append(", ")
                    .append(fmt(p[1])).append(")");
        }
        if (d.extremosPts.size() > 2) sb.append(";  …");
        d.extremos = sb.toString();
    }

    private void buscarRaices(Funcion f, double[] xs, double[] ys, Datos d) {
        int n = xs.length;
        for (int i = 0; i < n - 1 && d.raicesPts.size() < 50; i++) {
            double a = ys[i], b = ys[i + 1];
            if (Double.isNaN(a) || Double.isNaN(b)) continue;
            if (a == 0) {
                agregarUnico(d.raicesPts, xs[i]);
            } else if (a * b < 0) {
                double lo = xs[i], hi = xs[i + 1], flo = a;
                boolean valido = true;
                for (int k = 0; k < 60; k++) {
                    double mid = (lo + hi) / 2;
                    double fm = ev(f, mid);
                    if (!Double.isFinite(fm)) {
                        valido = false;
                        break;
                    }
                    if (flo * fm <= 0) {
                        hi = mid;
                    } else {
                        lo = mid;
                        flo = fm;
                    }
                }
                if (!valido) continue;
                double xr = (lo + hi) / 2;
                double yr = ev(f, xr);
                if (Double.isFinite(yr) && Math.abs(yr) < 1e-3) agregarUnico(d.raicesPts, xr);
            }
        }
        d.raicesPts.sort((p, q) -> Double.compare(p[0], q[0]));

        if (d.raicesPts.isEmpty()) {
            d.raices = "Ninguno en [-10, 10]";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < Math.min(4, d.raicesPts.size()); k++) {
            if (k > 0) sb.append(",  ");
            sb.append("x = ").append(fmt(d.raicesPts.get(k)[0]));
        }
        if (d.raicesPts.size() > 4) sb.append(",  …");
        d.raices = sb.toString();
    }

    private static void agregarUnico(List<double[]> lista, double x) {
        for (double[] p : lista) if (Math.abs(p[0] - x) < 1e-3) return;
        lista.add(new double[]{x, 0});
    }

    // =====================================================================
    //  UTILIDADES
    // =====================================================================
    private static double ev(Funcion e, double x) {
        try {
            Map<String, Double> m = new HashMap<>();
            m.put(variableActiva, x);
            return e.evaluar(m);
        } catch (Exception ex) {
            return Double.NaN;
        }
    }

    private static String fmt(double v) {
        if (Math.abs(v) < 1e-9) return "0";
        String s = String.format(Locale.US, "%.3f", v);
        s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s;
    }

    private static Color mezclar(Color a, Color b, double t) {
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static void suavizar(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // =====================================================================
    //  COMPONENTES PERSONALIZADOS
    // =====================================================================

    /** Fondo con degradado y un brillo suave en la esquina. */
    private static class Fondo extends JPanel {
        private final Tema t;

        Fondo(Tema t) {
            this.t = t;
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, t.fondo, w, h, mezclar(t.fondo, t.ac1, 0.16)));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w * 0.9f, 0), w * 0.55f,
                    new float[]{0f, 1f}, new Color[]{alfa(t.ac1, t==Tema.NEON?(int)(40+16*Math.sin(brilloFase*Math.PI/180)):55), alfa(t.ac1, 0)}));
            g2.fillRect(0, 0, w, h);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(0, h), w * 0.45f,
                    new float[]{0f, 1f}, new Color[]{alfa(t.cian, 30), alfa(t.cian, 0)}));
            g2.fillRect(0, 0, w, h);
            g2.dispose();
        }
    }

    /** Panel redondeado con relleno y borde. */
    private static class Tarjeta extends JPanel {
        private final Color relleno;
        private Color borde;
        private final int arco;

        Tarjeta(LayoutManager lm, Color relleno, Color borde, int arco) {
            super(lm);
            this.relleno = relleno;
            this.borde = borde;
            this.arco = arco;
            setOpaque(false);
        }

        void setBorde(Color c) {
            this.borde = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            Shape s = new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1, getHeight() - 1, arco, arco);
            g2.setColor(relleno);
            g2.fill(s);
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(1.3f));
            g2.draw(s);
            g2.dispose();
        }
    }

    /** Botón redondeado; si está seleccionado se pinta con degradado. */
    private static class Boton extends JButton {
        private final int arco;
        private final Color bg, borde, textoNormal, selA, selB;
        private boolean seleccionado, hover;

        Boton(String texto, int arco, Color bg, Color borde, Color textoNormal, Color selA, Color selB) {
            super(texto);
            this.arco = arco;
            this.bg = bg;
            this.borde = borde;
            this.textoNormal = textoNormal;
            this.selA = selA;
            this.selB = selB;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 10, 4, 10));
            setForeground(textoNormal);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        void setSeleccionado(boolean s) {
            this.seleccionado = s;
            setForeground(s ? Color.WHITE : textoNormal);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            Shape s = new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, arco, arco);
            if (seleccionado) {
                g2.setPaint(new GradientPaint(0, 0, selA, w, 0, selB));
                g2.fill(s);
                if (hover) {
                    g2.setColor(new Color(255, 255, 255, 35));
                    g2.fill(s);
                }
            } else {
                g2.setColor(hover ? mezclar(bg, selA, 0.25) : bg);
                g2.fill(s);
                if (borde != null) {
                    g2.setColor(hover ? selA : borde);
                    g2.draw(s);
                }
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Pequeño icono de texto con fondo degradado (d/dx, ∫). */
    private static class Insignia extends JComponent {
        private final String texto;
        private final Tema t;

        Insignia(String texto, Tema t) {
            this.texto = texto;
            this.t = t;
            setPreferredSize(new Dimension(46, 30));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();
            g2.setPaint(new GradientPaint(0, 0, t.ac1, w, h, t.ac2));
            g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 12, 12));
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(texto, (w - fm.stringWidth(texto)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            g2.dispose();
        }
    }

    /** Círculo de color para elegir el tema. */
    private class Muestra extends JComponent {
        private final Tema tema;
        private boolean hover;

        Muestra(Tema tema) {
            this.tema = tema;
            setPreferredSize(new Dimension(28, 28));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText(tema.nombre);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (tema != temaActual) cambiarTema(tema);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            suavizar(g2);
            int d = hover ? 20 : 18;
            int x = (getWidth() - d) / 2, y = (getHeight() - d) / 2;
            g2.setPaint(new GradientPaint(x, y, tema.ac1, x + d, y + d, tema.ac2));
            g2.fill(new Ellipse2D.Double(x, y, d, d));
            if (tema == temaActual) {
                g2.setColor(temaActual.texto);
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Double(1.5, 1.5, getWidth() - 3, getHeight() - 3));
            }
            g2.dispose();
        }
    }

    // =====================================================================
    //  PANEL DE GRÁFICA
    // =====================================================================
    private static class PanelGrafico extends JPanel {
        private final Tema t;
        private Funcion f, g;
        private String etiquetaG = "f'(x)";
        private double xMin = -5, xMax = 5, yMin = -5, yMax = 5;
        private List<double[]> raices = new ArrayList<>();
        private List<double[]> extremos = new ArrayList<>();
        private double yInt = Double.NaN;
        private Point mouse;
        private boolean sombrear=false;
        private double intervaloA=0,intervaloB=0;
        private double tangX=Double.NaN,tangY=Double.NaN,tangM=Double.NaN;
        private java.util.List<Funcion> adicionales=new ArrayList<>();
        private double progreso=1;
        private javax.swing.Timer animacion;
        void setSombreado(boolean activo,double a,double b){sombrear=activo;intervaloA=Math.min(a,b);intervaloB=Math.max(a,b);repaint();}
        void setTangente(double x,double y,double m){tangX=x;tangY=y;tangM=m;repaint();}
        void setAdicionales(java.util.List<Funcion> lista){adicionales=new ArrayList<>(lista);repaint();}
        void iniciarAnimacion(){
            if(animacion!=null)animacion.stop();progreso=0;
            animacion=new javax.swing.Timer(18,e->{progreso=Math.min(1,progreso+0.035);repaint();if(progreso>=1)animacion.stop();});
            animacion.start();
        }

        PanelGrafico(Tema t) {
            this.t = t;
            setOpaque(false);

            addMouseWheelListener(e -> {
                double factor = e.getWheelRotation() > 0 ? 1.15 : 1 / 1.15;
                zoom(factor, aMundoX(e.getX()), aMundoY(e.getY()));
            });

            MouseAdapter ma = new MouseAdapter() {
                private Point ultimo;

                @Override
                public void mousePressed(MouseEvent e) {
                    ultimo = e.getPoint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (ultimo == null || getWidth() == 0 || getHeight() == 0) return;
                    double dx = (e.getX() - ultimo.x) * (xMax - xMin) / getWidth();
                    double dy = (e.getY() - ultimo.y) * (yMax - yMin) / getHeight();
                    xMin -= dx;
                    xMax -= dx;
                    yMin += dy;
                    yMax += dy;
                    ultimo = e.getPoint();
                    mouse = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseMoved(MouseEvent e) {
                    mouse = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    mouse = null;
                    repaint();
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        void setExpresiones(Funcion f, Funcion g, String etiquetaG, boolean reajustar) {
            this.f = f;
            this.g = g;
            this.etiquetaG = etiquetaG;
            if (reajustar) ajustar();
            else repaint();
        }

        void setMarcadores(List<double[]> raices, List<double[]> extremos, double yInt) {
            this.raices = raices;
            this.extremos = extremos;
            this.yInt = yInt;
        }

        void acercar() {
            zoom(1 / 1.25, (xMin + xMax) / 2, (yMin + yMax) / 2);
        }

        void alejar() {
            zoom(1.25, (xMin + xMax) / 2, (yMin + yMax) / 2);
        }

        private void zoom(double factor, double cx, double cy) {
            double nx = (xMax - xMin) * factor, ny = (yMax - yMin) * factor;
            if (nx < 1e-6 || ny < 1e-6 || nx > 1e6 || ny > 1e6) return;
            xMin = cx + (xMin - cx) * factor;
            xMax = cx + (xMax - cx) * factor;
            yMin = cy + (yMin - cy) * factor;
            yMax = cy + (yMax - cy) * factor;
            repaint();
        }

        /** Autoescala vertical según f(x) en [-5, 5]. */
        void ajustar() {
            xMin = -5;
            xMax = 5;
            if (f == null) {
                yMin = -5;
                yMax = 5;
                repaint();
                return;
            }
            double lo = Double.POSITIVE_INFINITY, hi = Double.NEGATIVE_INFINITY;
            for (int i = 0; i <= 400; i++) {
                double x = xMin + i * (xMax - xMin) / 400;
                double y = ev(f, x);
                if (!Double.isFinite(y) || Math.abs(y) > 1e6) continue;
                lo = Math.min(lo, y);
                hi = Math.max(hi, y);
            }
            if (lo > hi) {
                lo = -5;
                hi = 5;
            }
            double lim = (xMax - xMin) * 0.8;
            lo = Math.max(-lim, Math.min(lim, lo));
            hi = Math.max(-lim, Math.min(lim, hi));
            lo = Math.min(lo, 0);
            hi = Math.max(hi, 0);
            double span = Math.max(hi - lo, 4);
            double centro = (hi + lo) / 2;
            double pad = span * 0.12;
            yMin = centro - span / 2 - pad;
            yMax = centro + span / 2 + pad;
            repaint();
        }

        private double aMundoX(int px) {
            return xMin + px * (xMax - xMin) / Math.max(1, getWidth());
        }

        private double aMundoY(int py) {
            return yMax - py * (yMax - yMin) / Math.max(1, getHeight());
        }

        private double aPx(double x) {
            return (x - xMin) / (xMax - xMin) * getWidth();
        }

        private double aPy(double y) {
            return getHeight() - (y - yMin) / (yMax - yMin) * getHeight();
        }

        private static double pasoBonito(double span) {
            double raw = span / 8;
            double mag = Math.pow(10, Math.floor(Math.log10(raw)));
            double n = raw / mag;
            double paso = n < 1.5 ? 1 : n < 3.5 ? 2 : n < 7.5 ? 5 : 10;
            return paso * mag;
        }

        @Override
        protected void paintComponent(Graphics gr) {
            Graphics2D g2 = (Graphics2D) gr.create();
            suavizar(g2);
            int w = getWidth(), h = getHeight();

            RoundRectangle2D marco = new RoundRectangle2D.Double(0, 0, w, h, 16, 16);
            g2.setColor(mezclar(t.fondo, t.panel, 0.4));
            g2.fill(marco);
            g2.setClip(marco);

            dibujarCuadricula(g2, w, h);

            if (f != null) {
                if(sombrear)dibujarArea(g2,w,h);
                dibujarCurva(g2, f, t.cian, w, h);
                if (g != null) dibujarCurva(g2, g, t.ac2, w, h);
                Color[] gama={NARANJA,VERDE,AMARILLO,new Color(250,110,200)};
                for(int i=0;i<adicionales.size();i++)dibujarCurva(g2,adicionales.get(i),gama[i%gama.length],w,h);
                dibujarTangente(g2,w);
                dibujarMarcadores(g2);
            }
            dibujarLeyenda(g2);
            dibujarLectura(g2, w, h);

            g2.setClip(null);
            g2.setColor(t.borde);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 16, 16));
            g2.dispose();
        }

        private void dibujarCuadricula(Graphics2D g2, int w, int h) {
            double sx = pasoBonito(xMax - xMin), sy = pasoBonito(yMax - yMin);
            Color rejilla = alfa(t.textoSec, 38);
            Color ejeC = alfa(t.textoSec, 170);

            int ejeY = (int) Math.max(0, Math.min(h - 1, aPy(0)));
            int ejeX = (int) Math.max(0, Math.min(w - 1, aPx(0)));

            g2.setStroke(new BasicStroke(1f));
            long kx0 = (long) Math.ceil(xMin / sx), kx1 = (long) Math.floor(xMax / sx);
            long ky0 = (long) Math.ceil(yMin / sy), ky1 = (long) Math.floor(yMax / sy);

            g2.setColor(rejilla);
            for (long k = kx0; k <= kx1 && k - kx0 < 200; k++) {
                int px = (int) aPx(k * sx);
                g2.drawLine(px, 0, px, h);
            }
            for (long k = ky0; k <= ky1 && k - ky0 < 200; k++) {
                int py = (int) aPy(k * sy);
                g2.drawLine(0, py, w, py);
            }

            g2.setColor(ejeC);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(0, ejeY, w, ejeY);
            g2.drawLine(ejeX, 0, ejeX, h);

            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g2.setColor(t.textoSec);
            FontMetrics fm = g2.getFontMetrics();

            int ly = (ejeY + 15 > h - 2) ? ejeY - 6 : ejeY + 15;
            for (long k = kx0; k <= kx1 && k - kx0 < 200; k++) {
                if (k == 0) continue;
                String s = fmt(k * sx);
                g2.drawString(s, (int) aPx(k * sx) - fm.stringWidth(s) / 2, ly);
            }
            for (long k = ky0; k <= ky1 && k - ky0 < 200; k++) {
                if (k == 0) continue;
                String s = fmt(k * sy);
                int lx = ejeX > fm.stringWidth(s) + 12 ? ejeX - 6 - fm.stringWidth(s) : ejeX + 6;
                g2.drawString(s, lx, (int) aPy(k * sy) + 4);
            }
            g2.drawString("0", ejeX > 16 ? ejeX - 12 : ejeX + 6, ly);

            g2.setFont(new Font("Serif", Font.ITALIC | Font.BOLD, 13));
            g2.setColor(t.texto);
            g2.drawString("x", w - 14, Math.max(14, ejeY - 6));
            g2.drawString("y", Math.min(w - 14, ejeX + 7), 14);
        }

        private void dibujarCurva(Graphics2D g2, Funcion expr, Color col, int w, int h) {
            Path2D.Double path = new Path2D.Double();
            boolean nuevo = true;
            double prevPy = 0;
            for (int px = 0; px <= (int)(w*progreso); px++) {
                double x = xMin + px * (xMax - xMin) / w;
                double y = ev(expr, x);
                if (!Double.isFinite(y)) {
                    nuevo = true;
                    continue;
                }
                double py = h - (y - yMin) * h / (yMax - yMin);
                if (Math.abs(py) > 1e5) {
                    nuevo = true;
                    continue;
                }
                if (!nuevo && Math.abs(py - prevPy) > h * 1.2) nuevo = true; // asíntota
                if (nuevo) path.moveTo(px, py);
                else path.lineTo(px, py);
                nuevo = false;
                prevPy = py;
            }

            g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(alfa(col, 30));
            g2.draw(path);
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(alfa(col, 70));
            g2.draw(path);
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(col);
            g2.draw(path);
        }

        private void dibujarArea(Graphics2D g2,int w,int h){
            int inicio=Math.max(0,(int)aPx(intervaloA)),fin=Math.min(w,(int)aPx(intervaloB));
            if(fin<=inicio)return;
            Path2D.Double path=new Path2D.Double();boolean abierto=false;
            double eje=aPy(0);
            for(int px=inicio;px<=fin;px++){
                double valor=ev(f,aMundoX(px));
                if(!Double.isFinite(valor)||Math.abs(valor)>1e8)continue;
                if(!abierto){path.moveTo(px,eje);abierto=true;}
                path.lineTo(px,aPy(valor));
            }
            if(abierto){path.lineTo(fin,eje);path.closePath();g2.setColor(alfa(t.cian,45));g2.fill(path);}
        }
        private void dibujarTangente(Graphics2D g2,int w){
            if(!Double.isFinite(tangX)||!Double.isFinite(tangY)||!Double.isFinite(tangM))return;
            double ya=tangM*(xMin-tangX)+tangY,yb=tangM*(xMax-tangX)+tangY;
            if(!Double.isFinite(ya)||!Double.isFinite(yb)||Math.abs(ya)>1e5||Math.abs(yb)>1e5)return;
            g2.setColor(AMARILLO);g2.setStroke(new BasicStroke(1.5f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL,1f,new float[]{6f,5f},0f));
            g2.drawLine(0,(int)aPy(ya),w,(int)aPy(yb));
            punto(g2,tangX,tangY,AMARILLO);
        }

        private void dibujarMarcadores(Graphics2D g2) {
            for (double[] p : raices) punto(g2, p[0], 0, AMARILLO);
            if (Double.isFinite(yInt)) punto(g2, 0, yInt, NARANJA);
            int n = 0;
            for (double[] p : extremos) {
                if (n++ >= 2) break;
                int px = (int) aPx(p[0]), py = (int) aPy(p[1]);
                punto(g2, p[0], p[1], t.ac2);

                String s = "(" + fmt(p[0]) + ", " + fmt(p[1]) + ")";
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int bw = fm.stringWidth(s) + 14, bh = 20;
                int bx = Math.max(4, Math.min(getWidth() - bw - 4, px - bw / 2));
                int by = p[2] == 0 ? py - bh - 12 : py + 12;
                by = Math.max(4, Math.min(getHeight() - bh - 4, by));
                g2.setColor(alfa(t.panel, 235));
                g2.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 10, 10));
                g2.setColor(t.ac2);
                g2.setStroke(new BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Double(bx, by, bw, bh, 10, 10));
                g2.setColor(t.texto);
                g2.drawString(s, bx + 7, by + 14);
            }
        }

        private void punto(Graphics2D g2, double x, double y, Color c) {
            double px = aPx(x), py = aPy(y);
            if (px < -10 || px > getWidth() + 10 || py < -10 || py > getHeight() + 10) return;
            g2.setColor(alfa(c, 70));
            g2.fill(new Ellipse2D.Double(px - 8, py - 8, 16, 16));
            g2.setColor(c);
            g2.fill(new Ellipse2D.Double(px - 4.5, py - 4.5, 9, 9));
            g2.setColor(t.fondo);
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new Ellipse2D.Double(px - 4.5, py - 4.5, 9, 9));
        }

        private void dibujarLeyenda(Graphics2D g2) {
            if (f == null) return;
            g2.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 13));
            FontMetrics fm = g2.getFontMetrics();
            String a = "f(x)";
            String b = g != null ? etiquetaG : null;
            int ancho = 14 + 12 + fm.stringWidth(a) + 10;
            if (b != null) ancho += 12 + fm.stringWidth(b) + 10;
            int x = 10, y = 10, alto = 24;

            g2.setColor(alfa(t.panel, 225));
            g2.fill(new RoundRectangle2D.Double(x, y, ancho, alto, 12, 12));
            g2.setColor(t.borde);
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(x, y, ancho, alto, 12, 12));

            int cx = x + 12;
            g2.setColor(t.cian);
            g2.fill(new Ellipse2D.Double(cx, y + 8, 8, 8));
            g2.setColor(t.texto);
            g2.drawString(a, cx + 14, y + 17);
            if (b != null) {
                cx += 14 + fm.stringWidth(a) + 12;
                g2.setColor(t.ac2);
                g2.fill(new Ellipse2D.Double(cx, y + 8, 8, 8));
                g2.setColor(t.texto);
                g2.drawString(b, cx + 14, y + 17);
            }
        }

        private void dibujarLectura(Graphics2D g2, int w, int h) {
            if (mouse == null || f == null) return;
            double x = aMundoX(mouse.x);
            double y = ev(f, x);
            String s = "x = " + fmt(x) + "    f(x) = " + (Double.isFinite(y) ? fmt(y) : "no definido");
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int bw = fm.stringWidth(s) + 16, bh = 22;
            int bx = w - bw - 10, by = h - bh - 10;
            g2.setColor(alfa(t.panel, 225));
            g2.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 10, 10));
            g2.setColor(t.textoSec);
            g2.drawString(s, bx + 8, by + 15);

            if (Double.isFinite(y)) {
                g2.setColor(alfa(t.texto, 60));
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                        new float[]{4f, 4f}, 0f));
                g2.drawLine(mouse.x, 0, mouse.x, h);
            }
        }
    }
}