package calculadora.servicios;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Historial persistente local, sin cuentas ni conexión a Internet. */
public final class HistorialCalculos {
    private static final int MAX = 40;
    private final Path archivo = Paths.get(System.getProperty("user.home"), ".calculadora-integrales", "historial.txt");
    private final List<String> funciones = new ArrayList<>();

    public HistorialCalculos() { cargar(); }
    private void cargar() {
        if (!Files.isRegularFile(archivo)) return;
        try {
            for(String linea : Files.readAllLines(archivo)) {
                if (!linea.isBlank() && funciones.size() < MAX) funciones.add(linea);
            }
        } catch(IOException ignored) { }
    }
    public synchronized void agregar(String funcion) {
        if(funcion==null || funcion.isBlank())return;
        funciones.remove(funcion);
        funciones.add(0,funcion);
        while(funciones.size()>MAX)funciones.remove(funciones.size()-1);
        guardar();
    }
    public synchronized List<String> obtener() {return List.copyOf(funciones);}
    public synchronized void limpiar() {funciones.clear();guardar();}
    private void guardar() {
        try {
            Files.createDirectories(archivo.getParent());
            Files.write(archivo,funciones);
        } catch(IOException ex) {System.err.println("No se guardó historial: "+ex.getMessage());}
    }
}
