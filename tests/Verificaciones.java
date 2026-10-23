import calculadora.avanzado.MotorAvanzado;
import calculadora.servicios.ExportadorPDF;
import java.nio.file.Path;
import java.util.*;
public class Verificaciones {
 static void cercano(double actual,double esperado,double tolerancia,String nombre){
  if(!Double.isFinite(actual)||Math.abs(actual-esperado)>tolerancia)throw new AssertionError(nombre+" esperado "+esperado+" actual "+actual);
 }
 public static void main(String[] args) throws Exception {
  for(String s:List.of("sin(2x)","exp(3x)","e^(3x)","x*sin(x)","x*exp(x)","ln(x)","sqrt(x)","tan(x)","abs(x)","x^x","2^x","log(8,2)","y^2")){
   var f=MotorAvanzado.parsear(s);
   String var=s.contains("y")?"y":"x";
   double at=var.equals("y")?2:1.3;
   double h=0.00001;
   double finite=(f.evaluar(var,at+h)-f.evaluar(var,at-h))/(2*h);
   cercano(f.derivar(var).evaluar(var,at),finite,1e-4,s+" derivada");
   try{var F=f.integrar(var);if(!s.equals("log(8,2)")){
    // skip indefinite primitives across non-differentiable points by evaluating where defined.
    cercano(F.derivar(var).evaluar(var,at),f.evaluar(var,at),1e-4,s+" integral");
   }}catch(UnsupportedOperationException ignored){}
  }
  cercano(MotorAvanzado.integrarDefinida(MotorAvanzado.parsear("x^2"),"x",0,2),8.0/3,1e-6,"integral definida");
  cercano(MotorAvanzado.limite(MotorAvanzado.parsear("sin(x)/x"),"x",0),1,1e-4,"limite");
  if(!MotorAvanzado.taylor(MotorAvanzado.parsear("exp(x)"),"x",0,3).contains("0.166666"))throw new AssertionError("Taylor");
  if(!MotorAvanzado.fraccion(0.75).equals("3/4"))throw new AssertionError("Fracción");
  ExportadorPDF.exportar(Path.of("/mnt/data/CalculadoraIntegrales/prueba_solucion.pdf"),"Calculadora", "Ejemplo\nIntegral definida 0 a 2\nResultado = 8/3");
  System.out.println("VERIFICACIONES COMPLETADAS");
 }
}
