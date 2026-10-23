package calculadora.avanzado;

import java.util.*;

/** Motor simbólico autocontenido. No necesita las clases .class del proyecto anterior. */
public final class MotorAvanzado {
    private MotorAvanzado() {}
    public static final class Funcion {
        private final Nodo raiz;
        private Funcion(Nodo n) { raiz = simpl(n); }
        public double evaluar(String variable, double valor) {
            return evaluar(Map.of(variable, valor));
        }
        public double evaluar(Map<String, Double> valores) { return eval(raiz, valores); }
        public Funcion derivar(String variable) { return new Funcion(d(raiz, variable)); }
        public Funcion derivar(String variable, int orden) {
            if (orden < 1 || orden > 12) throw new IllegalArgumentException("Orden permitido: 1 a 12");
            Funcion r = this;
            for (int i=0; i<orden; i++) r = r.derivar(variable);
            return r;
        }
        public Funcion integrar(String variable) { return new Funcion(integral(raiz, variable)); }
        public String latex() { return tex(raiz, 0); }
        public String latex(boolean fraccion){return fraccion?latex():texDecimal(raiz,0);}
        public String formato(boolean fraccion){return fraccion?toString():strDecimal(raiz,0);}
        @Override public String toString() { return str(raiz, 0); }
    }
    private static final class Nodo {
        final String op, id;
        final double val;
        final Nodo[] a;
        Nodo(String op, double val, String id, Nodo... a) {
            this.op=op; this.val=val; this.id=id; this.a=a;
        }
    }
    private static Nodo c(double x) { return new Nodo("num",x,null); }
    private static Nodo v(String x) { return new Nodo("var",0,x); }
    private static Nodo b(String op, Nodo x, Nodo y) { return simpl(new Nodo(op,0,null,x,y)); }
    private static Nodo u(String op, Nodo x) { return simpl(new Nodo(op,0,null,x)); }
    private static Nodo f(String op, Nodo x) { return u(op,x); }
    private static boolean num(Nodo a) { return a.op.equals("num"); }
    private static boolean zero(Nodo a) { return num(a) && Math.abs(a.val)<1e-12; }
    private static boolean one(Nodo a) { return num(a) && Math.abs(a.val-1)<1e-12; }
    private static boolean eq(Nodo a,Nodo b) { return str(a,0).equals(str(b,0)); }
    public static Funcion parsear(String texto) {
        if (texto == null || texto.isBlank()) throw new IllegalArgumentException("Escribe una función.");
        Analizador p = new Analizador(texto.replace('π','p').replace("√", "sqrt"));
        Nodo n = p.expr(); p.espacios();
        if (p.pos < p.s.length()) throw new IllegalArgumentException("Símbolo no reconocido en posición " + (p.pos+1));
        return new Funcion(n);
    }
    private static final class Analizador {
        String s; int pos;
        Analizador(String s) {this.s=s;}
        void espacios() { while(pos<s.length() && Character.isWhitespace(s.charAt(pos)))pos++; }
        boolean toma(char c) { espacios();if(pos<s.length()&&s.charAt(pos)==c){pos++;return true;}return false; }
        char mira(){ espacios();return pos<s.length()?s.charAt(pos):'\0'; }
        Nodo expr() { Nodo n=term();while(true){if(toma('+'))n=b("+",n,term());else if(toma('-'))n=b("-",n,term());else return n;} }
        Nodo term() {
            Nodo n=unary();
            while(true) {
                if(toma('*'))n=b("*",n,unary());
                else if(toma('/'))n=b("/",n,unary());
                else if (mira()=='(' || Character.isLetter(mira()) || Character.isDigit(mira()) || mira()=='.') n=b("*",n,unary());
                else return n;
            }
        }
        Nodo unary(){if(toma('+'))return unary();if(toma('-'))return u("neg",unary());return power();}
        Nodo power() { Nodo n=primary();if(toma('^'))n=b("^",n,unary());return n; }
        Nodo primary() {
            if(toma('(')){Nodo n=expr();if(!toma(')'))throw new IllegalArgumentException("Falta cerrar un paréntesis ')'");return n;}
            char ch=mira();
            if(Character.isDigit(ch)||ch=='.') {
                int st=pos;while(pos<s.length()&&(Character.isDigit(s.charAt(pos))||s.charAt(pos)=='.'))pos++;
                try{return c(Double.parseDouble(s.substring(st,pos)));}catch(NumberFormatException e){throw new IllegalArgumentException("Número inválido");}
            }
            if(Character.isLetter(ch)) {
                int st=pos;while(pos<s.length()&&Character.isLetterOrDigit(s.charAt(pos)))pos++;
                String nombre=s.substring(st,pos).toLowerCase(Locale.ROOT);
                if(nombre.equals("pi")||nombre.equals("p"))return c(Math.PI);
                if(nombre.equals("e"))return c(Math.E);
                if(nombre.equals("x")||nombre.equals("y"))return v(nombre);
                if(!toma('('))throw new IllegalArgumentException("Usa "+nombre+"(x), por ejemplo sin(x)");
                Nodo arg=expr();
                if(nombre.equals("log") && toma(',')) {
                    Nodo base=expr();if(!toma(')'))throw new IllegalArgumentException("Falta ')' en log");
                    return b("/",f("ln",arg),f("ln",base));
                }
                if(!toma(')'))throw new IllegalArgumentException("Falta cerrar ')' en " + nombre);
                if(nombre.equals("log"))return b("/",f("ln",arg),c(Math.log(10)));
                if(nombre.equals("log10"))return b("/",f("ln",arg),c(Math.log(10)));
                if(!Set.of("sin","cos","tan","ln","exp","sqrt","abs","asin","acos","atan","sinh","cosh").contains(nombre))
                    throw new IllegalArgumentException("Función desconocida: " + nombre);
                return f(nombre,arg);
            }
            throw new IllegalArgumentException("Se esperaba número, variable o función en posición " +(pos+1));
        }
    }
    private static Nodo simpl(Nodo n) {
        if(n.a.length==0)return n;
        Nodo x=n.a[0], y=n.a.length>1?n.a[1]:null;
        if(n.op.equals("neg")) {if(num(x))return c(-x.val);if(x.op.equals("neg"))return x.a[0];return n;}
        if(y!=null) {
            if(num(x)&&num(y)) {
                double z=switch(n.op){case "+"->x.val+y.val;case "-"->x.val-y.val;case "*"->x.val*y.val;case "/"->x.val/y.val;case "^"->Math.pow(x.val,y.val);default->Double.NaN;};
                if(Double.isFinite(z))return c(z);
            }
            switch(n.op){
                case "+": if(zero(x))return y;if(zero(y))return x;break;
                case "-": if(zero(y))return x;if(zero(x))return u("neg",y);if(eq(x,y))return c(0);break;
                case "*": if(zero(x)||zero(y))return c(0);if(one(x))return y;if(one(y))return x;
                    if(num(y)&&!num(x))return b("*",y,x);
                    if(x.op.equals("*")&&num(x.a[0])&&num(y))return b("*",c(x.a[0].val*y.val),x.a[1]);break;
                case "/": if(zero(x))return c(0);if(one(y))return x;if(eq(x,y))return c(1);break;
                case "^": if(zero(y))return c(1);if(one(y))return x;if(one(x))return c(1);break;
            }
        } else if(num(x)){
            double z=switch(n.op){case "sin"->Math.sin(x.val);case "cos"->Math.cos(x.val);case "tan"->Math.tan(x.val);
            case "ln"->Math.log(x.val);case "exp"->Math.exp(x.val);case "sqrt"->Math.sqrt(x.val);
            case "abs"->Math.abs(x.val);case "asin"->Math.asin(x.val);case "acos"->Math.acos(x.val);
            case "atan"->Math.atan(x.val);case "sinh"->Math.sinh(x.val);case "cosh"->Math.cosh(x.val);default->Double.NaN;};
            if(Double.isFinite(z))return c(z);
        }
        return n;
    }
    private static double eval(Nodo n,Map<String,Double> m) {
        double x=n.a.length>0?eval(n.a[0],m):0,y=n.a.length>1?eval(n.a[1],m):0;
        return switch(n.op){
            case "num"->n.val;case "var"->m.getOrDefault(n.id,0.0);
            case "+"->x+y;case "-"->x-y;case "*"->x*y;case "/"->x/y;case "^"->Math.pow(x,y);
            case "neg"->-x;case "sin"->Math.sin(x);case "cos"->Math.cos(x);case "tan"->Math.tan(x);
            case "ln"->Math.log(x);case "exp"->Math.exp(x);case "sqrt"->Math.sqrt(x);case "abs"->Math.abs(x);
            case "asin"->Math.asin(x);case "acos"->Math.acos(x);case "atan"->Math.atan(x);
            case "sinh"->Math.sinh(x);case "cosh"->Math.cosh(x);
            default->Double.NaN;
        };
    }
    private static Nodo d(Nodo n,String variable) {
        Nodo x=n.a.length>0?n.a[0]:null,y=n.a.length>1?n.a[1]:null;
        if(n.op.equals("num"))return c(0);
        if(n.op.equals("var"))return c(n.id.equals(variable)?1:0);
        Nodo dx=d(x,variable);
        return switch(n.op) {
            case "+"->b("+",dx,d(y,variable));
            case "-"->b("-",dx,d(y,variable));
            case "neg"->u("neg",dx);
            case "*"->b("+",b("*",dx,y),b("*",x,d(y,variable)));
            case "/"->b("/",b("-",b("*",dx,y),b("*",x,d(y,variable))),b("^",y,c(2)));
            case "^" -> {
                if(num(y))yield b("*",b("*",y,b("^",x,c(y.val-1))),dx);
                yield b("*",n,b("+",b("*",d(y,variable),f("ln",x)),b("*",y,b("/",dx,x))));
            }
            case "sin"->b("*",f("cos",x),dx);
            case "cos"->b("*",u("neg",f("sin",x)),dx);
            case "tan"->b("*",b("/",c(1),b("^",f("cos",x),c(2))),dx);
            case "ln"->b("/",dx,x);
            case "exp"->b("*",f("exp",x),dx);
            case "sqrt"->b("/",dx,b("*",c(2),f("sqrt",x)));
            case "abs"->b("*",b("/",x,f("abs",x)),dx);
            case "asin"->b("/",dx,f("sqrt",b("-",c(1),b("^",x,c(2)))));
            case "acos"->u("neg",b("/",dx,f("sqrt",b("-",c(1),b("^",x,c(2))))));
            case "atan"->b("/",dx,b("+",c(1),b("^",x,c(2))));
            case "sinh"->b("*",f("cosh",x),dx);
            case "cosh"->b("*",f("sinh",x),dx);
            default->throw new IllegalArgumentException("No se puede derivar " + n.op);
        };
    }
    private static boolean constante(Nodo n,String var){return !contiene(n,var);}
    private static boolean contiene(Nodo n,String var){ if(n.op.equals("var")&&n.id.equals(var))return true;for(Nodo a:n.a)if(contiene(a,var))return true;return false;}
    private static double valorConstante(Nodo n){return eval(n,Map.of());}
    /** Devuelve factor a en u=a*variable+b para argumentos lineales. */
    private static Double pendienteLineal(Nodo n,String var){
        try {Nodo de=d(n,var);if(constante(de,var)){double a=valorConstante(de);if(Double.isFinite(a)&&Math.abs(a)>1e-12)return a;}}
        catch(Exception ignored){}return null;
    }
    private static Nodo integral(Nodo n,String var){
        Nodo x=n.a.length>0?n.a[0]:null,y=n.a.length>1?n.a[1]:null;
        if(constante(n,var))return b("*",n,v(var));
        switch(n.op){
            case "var": return b("/",b("^",n,c(2)),c(2));
            case "+": return b("+",integral(x,var),integral(y,var));
            case "-": return b("-",integral(x,var),integral(y,var));
            case "neg": return u("neg",integral(x,var));
            case "*": {
                if(constante(x,var))return b("*",x,integral(y,var));
                if(constante(y,var))return b("*",y,integral(x,var));
                // Por partes: x*sin(x), x*cos(x), x*exp(x), u lineal con f(u) cuando posible.
                Nodo lin=x, otra=y;
                if(!esVariableLineal(lin,var)){lin=y;otra=x;}
                if(esVariableLineal(lin,var) && Set.of("sin","cos","exp").contains(otra.op)
                    && otra.a.length==1 && esArgumentoDirecto(otra.a[0],var)) {
                    Nodo prim=integral(otra,var);
                    Nodo prim2=integral(prim,var);
                    return b("-",b("*",lin,prim),b("*",d(lin,var),prim2));
                }
                break;
            }
            case "/": {
                if(constante(x,var)) {
                    Double a=pendienteLineal(y,var);
                    if(a!=null)return b("*",b("/",x,c(a)),f("ln",f("abs",y)));
                    if(y.op.equals("^")&&num(y.a[1]))return b("*",x,integral(b("^",y.a[0],c(-y.a[1].val)),var));
                }
                break;
            }
            case "^": {
                if(num(y)){
                    Double a=pendienteLineal(x,var);
                    if(a!=null){double e=y.val;
                        if(Math.abs(e+1)<1e-12)return b("/",f("ln",f("abs",x)),c(a));
                        return b("/",b("^",x,c(e+1)),c(a*(e+1)));
                    }
                }
                if(constante(x,var)&&!constante(y,var) && x.op.equals("num") && x.val>0 && x.val!=1){
                    Double a=pendienteLineal(y,var);
                    if(a!=null)return b("/",n,b("*",c(a),f("ln",x)));
                }
                break;
            }
            case "ln": {
                // Integral de ln(x): integración por partes.
                if(esArgumentoDirecto(x,var))return b("-",b("*",x,f("ln",x)),x);
                Double a=pendienteLineal(x,var);
                if(a!=null)return b("/",b("-",b("*",x,f("ln",x)),x),c(a));
                break;
            }
            case "tan":{
                Double a=pendienteLineal(x,var);
                if(a!=null)return b("/",u("neg",f("ln",f("abs",f("cos",x)))),c(a));
                break;
            }
            case "sqrt":{
                Double a=pendienteLineal(x,var);
                if(a!=null)return b("/",b("*",c(2),b("^",x,c(1.5))),c(3*a));
                break;
            }
            case "abs":{
                Double a=pendienteLineal(x,var);
                if(a!=null)return b("/",b("*",x,f("abs",x)),c(2*a));
                break;
            }
            case "asin": case "acos": case "atan":{
                Double a=pendienteLineal(x,var);
                if(a!=null){
                    Nodo prim=switch(n.op){
                        case "asin"->b("+",b("*",x,n),f("sqrt",b("-",c(1),b("^",x,c(2)))));
                        case "acos"->b("-",b("*",x,n),f("sqrt",b("-",c(1),b("^",x,c(2)))));
                        default->b("-",b("*",x,n),b("/",f("ln",b("+",c(1),b("^",x,c(2)))),c(2)));
                    };
                    return b("/",prim,c(a));
                }
                break;
            }
            case "sin": case "cos": case "exp": case "sinh": case "cosh":{
                Double a=pendienteLineal(x,var);
                if(a!=null){
                    Nodo p=switch(n.op){case "sin"->u("neg",f("cos",x));case "cos"->f("sin",x);
                        case "exp"->f("exp",x);case "sinh"->f("cosh",x);default->f("sinh",x);};
                    return b("/",p,c(a));
                }
                break;
            }
        }
        throw new UnsupportedOperationException("No hay una primitiva simbólica implementada para " +str(n,0)+". Si usas límites, se puede aproximar numéricamente.");
    }
    private static boolean esArgumentoDirecto(Nodo n,String var){return n.op.equals("var")&&n.id.equals(var);}
    private static boolean esVariableLineal(Nodo n,String var){return contiene(n,var)&&pendienteLineal(n,var)!=null;}
    public static double integrarDefinida(Funcion f,String var,double a,double b){
        if(!Double.isFinite(a)||!Double.isFinite(b))throw new IllegalArgumentException("Límites finitos requeridos");
        if(a==b)return 0;
        // Una primitiva permite evaluar el Teorema Fundamental, cuando es aplicable.
        // Para evitar resultados engañosos, verificamos finitud del integrando en el intervalo.
        int div=1000;double l=Math.min(a,b),r=Math.max(a,b);
        for(int i=0;i<=div;i++){
            double t=l+(r-l)*i/div, val=f.evaluar(var,t);
            if(!Double.isFinite(val) || Math.abs(val)>1e12)
                throw new ArithmeticException("Posible singularidad o discontinuidad en el intervalo. No se calcula la integral impropia.");
        }
        try{
            Funcion prim=f.integrar(var);
            double exacta=prim.evaluar(var,b)-prim.evaluar(var,a);
            if(Double.isFinite(exacta))return exacta;
        }catch(Exception ignored){}
        double suma=f.evaluar(var,a)+f.evaluar(var,b);
        int n=4000;
        double h=(b-a)/n;
        for(int i=1;i<n;i++)suma+=(i%2==0?2:4)*f.evaluar(var,a+i*h);
        return suma*h/3;
    }
    public static double limite(Funcion f,String var,double punto) {
        double[] lado=new double[2];
        for(int s=0;s<2;s++){
            double signo=s==0?-1:1;
            double previo=Double.NaN,actual=Double.NaN;
            for(int k=2;k<=8;k++){
                actual=f.evaluar(var,punto+signo*Math.pow(10,-k));
                if(!Double.isFinite(actual))throw new ArithmeticException("Límite no finito o no identificable");
                if(k==8&&Double.isFinite(previo)&&Math.abs(actual-previo)>1e-3*Math.max(1,Math.abs(actual)))
                    throw new ArithmeticException("El límite no converge numéricamente");
                previo=actual;
            }
            lado[s]=actual;
        }
        if(Math.abs(lado[0]-lado[1])>1e-3*Math.max(1,Math.abs(lado[0])))throw new ArithmeticException("Los límites laterales son distintos");
        return (lado[0]+lado[1])/2;
    }
    public static String taylor(Funcion f,String var,double centro,int grado){
        if(grado<0||grado>8)throw new IllegalArgumentException("Grado entre 0 y 8");
        StringBuilder sb=new StringBuilder("P"+grado+"("+var+") = ");
        Funcion iter=f; double fact=1;
        for(int i=0;i<=grado;i++){
            if(i>0)fact*=i;
            double coef=iter.evaluar(var,centro)/fact;
            if(!Double.isFinite(coef))throw new ArithmeticException("La función no tiene derivadas finitas en el centro");
            if(i>0)sb.append(coef>=0?" + ":" - ");
            else if(coef<0)sb.append("-");
            sb.append(formato(Math.abs(coef)));
            if(i>0)sb.append("·(").append(var).append(centro<0?" + ":" - ").append(formato(Math.abs(centro))).append(")").append(i>1?"^"+i:"");
            if(i<grado)iter=iter.derivar(var);
        }
        return sb.toString();
    }
    public static String pasosDerivada(Funcion f,String variable,int orden) {
        StringBuilder sb=new StringBuilder("Función: "+f+"\nVariable: "+variable+"\n");
        LinkedHashSet<String> reglas=new LinkedHashSet<>();
        recopilarReglas(f.raiz,reglas);
        int indice=1;
        for(String regla:reglas)sb.append(indice++).append(". ").append(regla).append("\n");
        Funcion r=f;
        for(int i=1;i<=orden;i++){
            r=r.derivar(variable);
            sb.append(indice++).append(". Derivar y simplificar (orden ").append(i).append(")\n")
                .append("   d^").append(i).append("f/d").append(variable).append("^").append(i).append(" = ").append(r).append("\n");
        }
        return sb.toString();
    }
    public static String pasosIntegral(Funcion f,String variable,Double a,Double b) {
        StringBuilder sb=new StringBuilder("Integrando: ").append(f).append("\n");
        LinkedHashSet<String> reglas=new LinkedHashSet<>();
        recopilarReglasIntegral(f.raiz,reglas);
        for(String regla:reglas)sb.append("• ").append(regla).append("\n");
        try {
            Funcion ant=f.integrar(variable);
            sb.append("1. Identificar la regla (potencia, linealidad, sustitución lineal o por partes).\n")
                .append("2. Primitiva: F(").append(variable).append(") = ").append(ant).append("\n");
            if(a!=null&&b!=null)sb.append("3. Teorema fundamental: F(").append(formato(b)).append(") - F(")
                .append(formato(a)).append(") = ").append(formato(integrarDefinida(f,variable,a,b))).append("\n");
            else sb.append("3. Añadir constante de integración: + C\n");
        }catch(Exception ex){
            if(a!=null&&b!=null)sb.append("1. Primitiva no disponible.\n2. Aproximación numérica de Simpson: ")
                .append(formato(integrarDefinida(f,variable,a,b))).append("\n");
            else sb.append("No existe una regla simbólica implementada para esta expresión.\n").append(ex.getMessage());
        }
        return sb.toString();
    }
    private static void recopilarReglas(Nodo n,LinkedHashSet<String> reglas) {
        if(reglas.size()>8)return;
        switch(n.op){
            case "+","-":reglas.add("Linealidad: derivar cada término por separado.");break;
            case "*":reglas.add("Producto: (uv)' = u'v + uv'.");break;
            case "/":reglas.add("Cociente: (u/v)' = (u'v-uv')/v².");break;
            case "^":
                if(num(n.a[1]))reglas.add("Potencia: d(u^n)/dx = n·u^(n-1)·u'.");
                else reglas.add("Potencia variable: derivación logarítmica de u(x)^v(x).");
                break;
            case "sin","cos","tan","ln","exp","sqrt","abs","asin","acos","atan","sinh","cosh":
                reglas.add("Cadena: derivar "+n.op+"(u) y multiplicar por u'.");break;
        }
        for(Nodo a:n.a)recopilarReglas(a,reglas);
    }
    private static void recopilarReglasIntegral(Nodo n,LinkedHashSet<String> reglas) {
        if(reglas.size()>6)return;
        switch(n.op){
            case "+","-":reglas.add("Linealidad: integrar término por término.");break;
            case "*":
                if(!num(n.a[0])&&!num(n.a[1]))reglas.add("Si aparece x·sen(x) o x·exp(x), usar integración por partes: ∫u dv = uv - ∫v du.");
                break;
            case "sin","cos","exp","sqrt","ln","tan":
                reglas.add("Reconocer una primitiva conocida y, si procede, sustituir u = ax + b.");break;
            case "^":reglas.add("Potencia: ∫u^n du = u^(n+1)/(n+1), para n distinto de -1.");break;
        }
        for(Nodo a:n.a)recopilarReglasIntegral(a,reglas);
    }

    public static String formato(double x){
        if(!Double.isFinite(x))return "No definido";
        if(Math.abs(x)<1e-10)x=0;
        return String.format(Locale.US,"%.8f",x).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
    public static String fraccion(double x){
        if(!Double.isFinite(x))return "No definido";
        if(Math.abs(x)<1e-10)return "0";
        for(int d=1;d<=10000;d++){
            long n=Math.round(x*d);
            if(Math.abs(x-(double)n/d)<1e-9)return d==1?Long.toString(n):n+"/"+d;
        }
        return formato(x);
    }
    private static int prec(Nodo n){return switch(n.op){case "+","-"->1;case "*","/"->2;case "neg"->3;case "^"->4;default->5;};}
    private static String str(Nodo n,int parent){
        String s;Nodo x=n.a.length>0?n.a[0]:null,y=n.a.length>1?n.a[1]:null;
        switch(n.op){
            case "num":s=formato(n.val);break;case "var":s=n.id;break;
            case "neg":s="-"+str(x,prec(n));break;
            case "+":s=str(x,1)+" + "+str(y,1);break;case "-":s=str(x,1)+" - "+str(y,2);break;
            case "*":s=str(x,2)+"*"+str(y,2);break;case "/":s=str(x,2)+"/"+str(y,3);break;
            case "^":s=str(x,4)+"^"+str(y,4);break;
            default:s=n.op+"("+str(x,0)+")";
        }
        return prec(n)<parent?"("+s+")":s;
    }
    private static String tex(Nodo n,int parent){
        String s;Nodo x=n.a.length>0?n.a[0]:null,y=n.a.length>1?n.a[1]:null;
        switch(n.op){
            case "num":String q=fraccion(n.val);
                if(q.contains("/")){String[] ps=q.split("/");s="\\frac{"+ps[0]+"}{"+ps[1]+"}";}
                else s=q;
                break;case "var":s=n.id;break;
            case "neg":s="-"+tex(x,3);break;
            case "+":s=tex(x,1)+" + "+tex(y,1);break;
            case "-":s=tex(x,1)+" - "+tex(y,2);break;
            case "*":s=tex(x,2)+" \\cdot "+tex(y,2);break;
            case "/":s="\\frac{"+tex(x,0)+"}{"+tex(y,0)+"}";break;
            case "^":s="{"+tex(x,4)+"}^{"+tex(y,0)+"}";break;
            case "sqrt":s="\\sqrt{"+tex(x,0)+"}";break;
            case "abs":s="\\left|"+tex(x,0)+"\\right|";break;
            default:s="\\operatorname{"+n.op+"}\\left("+tex(x,0)+"\\right)";
        }
        return prec(n)<parent?"\\left("+s+"\\right)":s;
    }
    private static String strDecimal(Nodo n,int parent){
        if(n.op.equals("/")&&num(n.a[1])&&!zero(n.a[1])) {
            String s=formato(1/n.a[1].val)+"*("+strDecimal(n.a[0],0)+")";
            return parent>2?"("+s+")":s;
        }
        if(n.a.length==0)return str(n,parent);
        Nodo x=n.a[0],y=n.a.length>1?n.a[1]:null;
        String s=switch(n.op){
            case "+"->strDecimal(x,1)+" + "+strDecimal(y,1);
            case "-"->strDecimal(x,1)+" - "+strDecimal(y,2);
            case "*"->strDecimal(x,2)+"*"+strDecimal(y,2);
            case "/"->strDecimal(x,2)+"/"+strDecimal(y,3);
            case "^"->strDecimal(x,4)+"^"+strDecimal(y,4);
            case "neg"->"-"+strDecimal(x,3);
            default->n.op+"("+strDecimal(x,0)+")";
        };
        return prec(n)<parent?"("+s+")":s;
    }
    private static String texDecimal(Nodo n,int parent){
        if(n.op.equals("/")&&num(n.a[1])&&!zero(n.a[1]))
            return formato(1/n.a[1].val)+"\\cdot "+texDecimal(n.a[0],2);
        if(n.op.equals("num"))return formato(n.val);
        if(n.a.length==0)return tex(n,parent);
        Nodo x=n.a[0],y=n.a.length>1?n.a[1]:null;
        String s=switch(n.op){
            case "+"->texDecimal(x,1)+" + "+texDecimal(y,1);
            case "-"->texDecimal(x,1)+" - "+texDecimal(y,2);
            case "*"->texDecimal(x,2)+" \\cdot "+texDecimal(y,2);
            case "/"->"\\frac{"+texDecimal(x,0)+"}{"+texDecimal(y,0)+"}";
            case "^"->"{"+texDecimal(x,4)+"}^{"+texDecimal(y,0)+"}";
            case "neg"->"-"+texDecimal(x,3);
            case "sqrt"->"\\sqrt{"+texDecimal(x,0)+"}";
            default->"\\operatorname{"+n.op+"}\\left("+texDecimal(x,0)+"\\right)";
        };
        return prec(n)<parent?"\\left("+s+"\\right)":s;
    }

}
