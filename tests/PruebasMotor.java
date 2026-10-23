import calculadora.avanzado.MotorAvanzado;
import static calculadora.avanzado.MotorAvanzado.*;
public class PruebasMotor {
 public static void main(String[] z) {
  String[] a={"sin(2x)","exp(3x)","e^(3x)","x*sin(x)","x*exp(x)","ln(x)","x^x","2^x","sqrt(x)","asin(x)","cosh(x)","x^2"};
  for(String s:a){
   try{
    var f=parsear(s); var der=f.derivar("x");
    System.out.println(s+" -> d="+der+" ; derivada@2="+der.evaluar("x",2));
    try{var prim=f.integrar("x");System.out.println("         prim="+prim+" @2="+prim.evaluar("x",2));}catch(Exception e){System.out.println("         sin prim: "+e.getMessage());}
   }catch(Exception e){System.out.println("ERROR: "+e.getMessage());}
  }
  System.out.println("definida: "+integrarDefinida(parsear("x^2"),"x",0,2));
  System.out.println("y: "+parsear("y^2").derivar("y"));
  System.out.println("limite: "+limite(parsear("sin(x)/x"),"x",0));
  System.out.println("taylor: "+taylor(parsear("exp(x)"),"x",0,3));
 }
}
