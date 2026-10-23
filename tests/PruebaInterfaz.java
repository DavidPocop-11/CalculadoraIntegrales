import calculadora.gui.InterfazMathPro;
import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.io.File;
public class PruebaInterfaz {
 public static void main(String[]args)throws Exception {
  SwingUtilities.invokeAndWait(()->{try{InterfazMathPro f=new InterfazMathPro();f.setVisible(true);
      Timer timer=new Timer(1700,e->{try{Robot r=new Robot();ImageIO.write(r.createScreenCapture(new Rectangle(0,0,1366,900)),"png",new File("/mnt/data/CalculadoraIntegrales/prueba_interfaz.png"));}catch(Exception ex){ex.printStackTrace();}f.dispose();System.exit(0);});timer.setRepeats(false);timer.start();
  }catch(Exception e){e.printStackTrace();System.exit(1);}});
 }
}
