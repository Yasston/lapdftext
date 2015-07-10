
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Samih
 */
public class Rule {
    //Une règle possède un nom, une priorité, un ensemble de conditions et un type final si les conditions sont remplies.
    private String name;
    private int priorite;
    private String typefin;
    ArrayList<String> conditions;
    
    public Rule(String name, int priorite, String typefin) {
        this.name = name;
        this.priorite = priorite;
        this.typefin = typefin;
        conditions.clear();
    }

    //Ajout de condition
    public void addCond(String cond) {
        conditions.add(cond);
    }
    
    public String getName() {
        return name;
    }

    public int getPriorite() {
        return priorite;
    }

    public String getTypefin() {
        return typefin;
    }
}
