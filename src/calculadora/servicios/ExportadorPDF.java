package calculadora.servicios;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** PDF de texto multipágina, sin dependencias adicionales. */
public final class ExportadorPDF {
    private ExportadorPDF() {}
    public static void exportar(Path destino,String titulo,String cuerpo) throws IOException {
        List<String> lineas=new ArrayList<>();
        lineas.add(titulo);lineas.add("");
        for(String l:cuerpo.split("\\R",-1)) {
            String clean=normalizar(l);
            if(clean.length()==0) {lineas.add("");continue;}
            while(clean.length()>88){int cut=clean.lastIndexOf(' ',88);if(cut<20)cut=88;
                lineas.add(clean.substring(0,cut));clean=clean.substring(cut).stripLeading();}
            lineas.add(clean);
        }
        List<String> streams=new ArrayList<>();
        for(int i=0;i<lineas.size();i+=58) {
            StringBuilder content=new StringBuilder("BT /F1 11 Tf 44 790 Td 14 TL\n");
            for(int j=i;j<Math.min(i+58,lineas.size());j++){
                String linea=lineas.get(j);
                content.append('(').append(escapar(linea)).append(") Tj T*\n");
            }
            streams.add(content.append("ET\n").toString());
        }
        if(streams.isEmpty())streams.add("BT ET\n");
        int pages=streams.size();int fontId=3+2*pages;
        List<byte[]> objs=new ArrayList<>();objs.add(null);
        StringBuilder refs=new StringBuilder();
        for(int i=0;i<pages;i++)refs.append(3+2*i).append(" 0 R ");
        objs.add(bytes("<< /Type /Catalog /Pages 2 0 R >>"));
        objs.add(bytes("<< /Type /Pages /Kids ["+refs+"] /Count "+pages+" >>"));
        for(int i=0;i<pages;i++){
            int pageId=3+i*2,contentId=pageId+1;
            objs.add(bytes("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 "+fontId+" 0 R >> >> /Contents "+contentId+" 0 R >>"));
            byte[] src=streams.get(i).getBytes(StandardCharsets.ISO_8859_1);
            ByteArrayOutputStream st=new ByteArrayOutputStream();
            st.write(bytes("<< /Length "+src.length+" >>\nstream\n"));st.write(src);st.write(bytes("endstream"));
            objs.add(st.toByteArray());
        }
        objs.add(bytes("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        out.write(bytes("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n"));
        List<Integer> offsets=new ArrayList<>();offsets.add(0);
        for(int i=1;i<objs.size();i++){
            offsets.add(out.size());out.write(bytes(i+" 0 obj\n"));out.write(objs.get(i));out.write(bytes("\nendobj\n"));
        }
        int xref=out.size();out.write(bytes("xref\n0 "+objs.size()+"\n0000000000 65535 f \n"));
        for(int i=1;i<offsets.size();i++)out.write(bytes(String.format(Locale.US,"%010d 00000 n \n",offsets.get(i))));
        out.write(bytes("trailer\n<< /Root 1 0 R /Size "+objs.size()+" >>\nstartxref\n"+xref+"\n%%EOF\n"));
        Files.write(destino,out.toByteArray());
    }
    private static byte[] bytes(String s){return s.getBytes(StandardCharsets.ISO_8859_1);}
    private static String escapar(String t){return t.replace("\\","\\\\").replace("(","\\(").replace(")","\\)");}
    private static String normalizar(String s){
        return s.replace("∫","Integral ").replace("√","sqrt").replace("π","pi")
            .replace("∞","infinito").replace("²","^2").replace("³","^3").replace("·","*")
            .replace("→","->").replace("−","-").replace("≈","~").replace("ℝ","R")
            .replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]","?");
    }
}
