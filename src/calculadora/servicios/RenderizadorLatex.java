package calculadora.servicios;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

/** Renderizado matemático con JLaTeXMath (Maven). Fallback HTML sin dependencias. */
public final class RenderizadorLatex {
    private RenderizadorLatex() {}
    public static JLabel crear(String formula,Color color) {
        JLabel label = new JLabel();
        label.setForeground(color);
        label.setFont(new Font("Cambria Math",Font.PLAIN,17));
        label.setVerticalAlignment(SwingConstants.CENTER);
        try {
            Class<?> tex=Class.forName("org.scilab.forge.jlatexmath.TeXFormula");
            Object obj=tex.getConstructor(String.class).newInstance(formula);
            Class<?> constants=Class.forName("org.scilab.forge.jlatexmath.TeXConstants");
            int style=constants.getField("STYLE_DISPLAY").getInt(null);
            Method method=tex.getMethod("createTeXIcon",int.class,float.class);
            Icon icon=(Icon)method.invoke(obj,style,17f);
            label.setIcon(icon);
        }catch(Exception | LinkageError ex){
            label.setText("<html>"+aHtml(formula)+"</html>");
        }
        return label;
    }
    private static String aHtml(String formula){
        StringBuilder out=new StringBuilder();
        for(int i=0;i<formula.length();) {
            if(formula.startsWith("\\frac",i)) {
                i+=5;int[] p={i};String a=grupo(formula,p),b=grupo(formula,p);i=p[0];
                out.append("<span style='font-size:85%'>[").append(aHtml(a)).append(" / ").append(aHtml(b)).append("]</span>");
            }else if(formula.startsWith("\\sqrt",i)) {
                i+=5;int[] p={i};String a=grupo(formula,p);i=p[0];out.append("√(").append(aHtml(a)).append(")");
            }else if(formula.startsWith("\\int",i)) {i+=4;out.append("∫");}
            else if(formula.startsWith("\\cdot",i)){i+=5;out.append(" · ");}
            else if(formula.startsWith("\\approx",i)){i+=7;out.append(" ≈ ");}
            else if(formula.startsWith("\\left",i)){i+=5;}
            else if(formula.startsWith("\\right",i)){i+=6;}
            else if(formula.startsWith("\\operatorname",i)){i+=13;int[] p={i};String x=grupo(formula,p);i=p[0];out.append(aHtml(x));}
            else if(formula.startsWith("\\,",i)){i+=2;out.append(" ");}
            else if(formula.charAt(i)=='^'||formula.charAt(i)=='_') {
                char ch=formula.charAt(i++);int[] p={i};String a=grupo(formula,p);i=p[0];
                out.append(ch=='^'?"<sup>":"<sub>").append(aHtml(a)).append(ch=='^'?"</sup>":"</sub>");
            }else if(formula.charAt(i)=='{'){i++;}
            else if(formula.charAt(i)=='}'){i++;}
            else if(formula.charAt(i)=='&'){out.append("&amp;");i++;}
            else if(formula.charAt(i)=='<'){out.append("&lt;");i++;}
            else if(formula.charAt(i)=='>'){out.append("&gt;");i++;}
            else {out.append(formula.charAt(i++));}
        }
        return out.toString();
    }
    private static String grupo(String s,int[] p){
        if(p[0]>=s.length())return "";
        if(s.charAt(p[0])!='{')return String.valueOf(s.charAt(p[0]++));
        int ini=++p[0],nivel=1;
        while(p[0]<s.length()&&nivel>0){char c=s.charAt(p[0]++);if(c=='{')nivel++;if(c=='}')nivel--;}
        return s.substring(ini,Math.max(ini,p[0]-1));
    }
}
