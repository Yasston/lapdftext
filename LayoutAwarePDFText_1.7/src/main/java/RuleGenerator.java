
import edu.isi.bmkeg.pdf.model.ChunkBlock;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Samih
 */
public class RuleGenerator {

    private File rulefile;

    public RuleGenerator(String filepath) {
        rulefile = new File(filepath);
    }

    //Ajout de chaîne de caractères dans le fichier de règles
    public void ajouter(String arg) {
        if (rulefile.canWrite()) {
            try {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(rulefile.getAbsolutePath(), true)));
                out.println(arg);
                out.close();
            } catch (IOException e) {
            }
        } else {
            System.out.println("Error : can't write in file\n");
        }
    }
    
    //Ajout d'une règle dans le fichier de règles
    public void regle(Rule reg) {
        ajouter("rule \""+reg.getName()+"\"\nno-loop\nactivation-group \"blockClassification\"\nsalience "+reg.getPriorite()+"\n");
        ajouter("\twhen\n");
        for (int i = 0; i<reg.conditions.size();i++) {
            ajouter(reg.conditions.get(i));
        }
        ajouter("\tthen\n");
        ajouter("chunk.setType("+reg.getTypefin()+")\n");
        ajouter("end\n");
    }
    
    //Programme de test, à ignorer
    public static void main(String[] args) {
        RuleGenerator gen = new RuleGenerator("src/main/resources/rules/rules.drl");
        Rule r = new Rule("toto",5,ChunkBlock.TYPE_TITLE);
        gen.regle(r);
    }
}
