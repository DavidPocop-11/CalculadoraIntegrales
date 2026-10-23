# Calculadora de derivadas e integrales — versión ampliada

Proyecto Java Swing con los cuatro temas originales: Neón, Oscuro, Claro y Esmeralda.

## 1. Estructura para IntelliJ IDEA (TODAS LAS CLASES EN .java)

```
CalculadoraIntegrales/
├── pom.xml
├── README.md
├── src/
│   └── calculadora/
│       ├── calculo/                  # 17 ARCHIVOS FUENTE EDITABLES .java
│       │   ├── Cociente.java
│       │   ├── Constante.java
│       │   ├── Coseno.java
│       │   ├── Exponencial.java
│       │   ├── Expresion.java
│       │   ├── Formateador.java
│       │   ├── LogaritmoNatural.java
│       │   ├── Negativo.java
│       │   ├── Parser.java
│       │   ├── Potencia.java
│       │   ├── Producto.java
│       │   ├── Resta.java
│       │   ├── Seno.java
│       │   ├── Simplificador.java
│       │   ├── Suma.java
│       │   ├── Tangente.java
│       │   └── Variable.java
│       ├── avanzado/
│       │   └── MotorAvanzado.java
│       ├── servicios/
│       │   ├── HistorialCalculos.java
│       │   ├── ExportadorPDF.java
│       │   └── RenderizadorLatex.java
│       └── gui/
│           ├── InterfazMathPro.java
│           └── Main.java
└── tests/
    ├── PruebasClasesCalculo.java
    └── ...
```

**Importante sobre la conversión:** los archivos `.class` son bytecode compilado,
no contienen el código fuente original íntegro. Por eso se **reconstruyeron
implementaciones Java editables** con los mismos nombres de clase, paquetes,
constructores y métodos públicos detectados en los archivos originales.
No son copias byte a byte ni una descompilación exacta de tus antiguos `.java`.
Las operaciones básicas se implementaron y probaron. La interfaz ampliada usa
principalmente `MotorAvanzado.java` y se conserva sin cambios en esta entrega.

El ZIP no incluye archivos `.class`; conserva tu copia de respaldo de los
`.class` antiguos fuera del nuevo proyecto si la necesitas.

## 2. Cómo reemplazar los antiguos .class en IntelliJ IDEA

1. Haz una copia de tu proyecto actual.
2. Abre el paquete `src/calculadora/calculo` de tu proyecto en IntelliJ.
3. **Elimina o mueve FUERA DE `src` los 17 archivos `.class` antiguos.** No los
   dejes mezclados con los `.java` de igual nombre dentro del código fuente.
4. Copia los 17 archivos `.java` de este proyecto a `src/calculadora/calculo`.
   **No crees una carpeta `calculo` adicional dentro de `calculo`.**
5. Conserva `src/calculadora/gui` y `src/calculadora/avanzado` y `servicios`.
   La versión completa del ZIP ya incluye esos archivos.
6. En IntelliJ, elige JDK 17 o superior y verifica que `src` sea Sources Root.
7. Ejecuta `calculadora.gui.Main`. Opcional: carga `pom.xml` para obtener
   JLaTeXMath, usado para presentar fórmulas con tipografía TeX.

**No es necesario utilizar Java 26:** los `.class` anteriores eran versión 70
(Java 26). Estas clases nuevas se compilan como fuentes con Java 17 o superior.

## 3. Si abres todo el ZIP como proyecto nuevo

1. Descomprime el ZIP, por ejemplo en `C:\Proyectos\CalculadoraIntegrales`.
2. IntelliJ IDEA → `File > Open` → selecciona `CalculadoraIntegrales`.
3. Selecciona JDK 17 o 21 en `File > Project Structure > Project SDK`.
4. Asegura que `src` esté marcado como `Sources Root`.
5. Ejecuta el `main()` de `src/calculadora/gui/Main.java`.

### Compilar sin Maven desde la carpeta del proyecto (Linux/macOS)

```bash
find src -name '*.java' > fuentes.txt
javac --release 17 -encoding UTF-8 -d out @fuentes.txt
java -cp out calculadora.gui.Main
```

### Comprobación de las clases `calculo`

```bash
javac --release 17 -encoding UTF-8 -cp out -d out tests/PruebasClasesCalculo.java
java -cp out PruebasClasesCalculo
```

## 4. Qué funciones hay

- Conserva la interfaz Swing original y los temas Neón, Oscuro, Claro y Esmeralda, con transición y un brillo suave en Neón.
- Cambia el título visible de MathPro a **Calculadora**.
- Derivadas en `x` o `y` y derivadas de orden 1 a 8 (el motor permite 1 a 12).
- Integral indefinida (`+C`) e integral definida con límites `a` y `b`.
- Resultados numéricos en **fracción racional aproximada** o decimal, y visualización de expresiones simbólicas como cocientes; el selector decimal convierte a decimales ciertos coeficientes racionales.
- Sustitución lineal en `sin(ax+b)`, `cos(ax+b)`, `exp(ax+b)`, `e^(ax+b)`, potencias y otras funciones admitidas.
- Integración por partes para expresiones como `x*sin(x)`, `x*cos(x)` y `x*exp(x)`, e integral de `ln(x)`.
- Derivación de potencias de exponente variable `x^x`, `2^x`.
- `sqrt`, `abs`, `asin`, `acos`, `atan`, `sinh`, `cosh`, `ln`, `log` (base 10), `log(x,base)` y funciones trigonométricas normales.
- Recta tangente para un punto elegido de la función, graficada en amarillo.
- Integrales definidas con área aproximada sombreada en la gráfica.
- Taylor de grado 0 a 8 y límites numéricos laterales mediante ventanas.
- Historial local persistente, acceso lateral en pantallas grandes y botón **Historial** siempre accesible.
- Teclado matemático, vista previa de ecuación mientras se teclea y autocompletado con **Ctrl+Espacio**.
- Varias funciones en la gráfica: escribe otras funciones separadas por `;` en la barra de la gráfica, luego haz clic en **Comparar**.
- Modo Aprender para ir revelando pasos con el botón **Siguiente paso**.
- Exportación PNG de la gráfica y PDF del texto de los resultados.
- Atajos **Ctrl+Enter** calcular, **Ctrl+L** limpiar, **Ctrl+Z** deshacer edición.
- Gráfica con zoom, desplazamiento por arrastre, leyenda y dibujo progresivo.
- Diseño adaptable con barras de desplazamiento en pantallas angostas; icono generado por código, sin imágenes externas.

## 5. Escritura de funciones

Ejemplos válidos:

```
x^2 + 3*x + 2
2x + 3
sin(2x)
cos(x)
exp(3x)
e^(3x)
ln(x)
log(100)
log(8,2)
sqrt(x)
abs(x)
asin(x)
atan(x)
sinh(x)
x*sin(x)
x*exp(x)
x^x
2^x
y^2 + 4y + 1
```

Se recomienda `*` como multiplicación explícita, aunque se aceptan algunos productos implícitos como `2x`.
Para `log(x,b)` la base es el segundo argumento. Las trigonométricas usan radianes.

## 6. Límites y advertencias honestas

- Este motor es una **ampliación didáctica**, no un sistema algebraico completo como Mathematica, Maxima o SymPy.
- Algunas integrales indefinidas complejas, como `x^x` o integrales de funciones arbitrarias compuestas, no disponen de primitiva simbólica; la interfaz lo indicará y no inventará un resultado.
- Integrales definidas sin antiderivada soportada usan Simpson numérico sobre intervalos finitos; se detectan singularidades visibles al muestrear, pero esto no sustituye análisis riguroso de integrales impropias.
- El dominio, rango, raíces y extremos están estimados numéricamente usando el intervalo [-10, 10]; pueden no reflejar el conjunto exacto.
- Los límites son estimaciones numéricas por ambos lados, no demostraciones analíticas; algunos límites oscilantes, infinitos o muy difíciles pueden no detectarse.
- La serie de Taylor usa coeficientes numéricos con grado hasta 8 y puede sufrir errores de redondeo.
- El PDF es un informe **textual**, con soporte básico de caracteres latinos; no incorpora la gráfica, que se exporta como PNG aparte.
- `RenderizadorLatex` usa una representación visual HTML de respaldo si no encuentra JLaTeXMath. Para disponer de fracciones apiladas, integrales y exponentes en tipografía TeX real, abre el proyecto Maven e instala la dependencia.
- El historial se guarda en `~/.calculadora-integrales/historial.txt`.

## 7. Pruebas

La aplicación y el motor se compilaron y se probaron con OpenJDK 21. En `tests/Verificaciones.java` encontrarás una prueba de derivadas por comparación con diferencias finitas, integración, Taylor, límite y exportación PDF. También se probó la creación de la ventana en un entorno de pantalla virtual.
