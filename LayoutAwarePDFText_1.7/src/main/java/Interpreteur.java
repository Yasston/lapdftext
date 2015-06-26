
import edu.isi.bmkeg.pdf.classification.ruleBased.RuleBasedChunkClassifier;
import edu.isi.bmkeg.pdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.pdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.pdf.model.ChunkBlock;
import edu.isi.bmkeg.pdf.model.Document;
import edu.isi.bmkeg.pdf.model.PageBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.pdf.model.RTree.RTPageBlock;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.parser.RuleBasedParser;
import java.util.ArrayList;
import java.util.List;
import org.jpedal.exception.PdfException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Samih
 */
public class Interpreteur {

    private Document pdf;

    public Interpreteur(String pdf) throws PdfException, AccessException, EncryptionException {
        RuleBasedParser rbp = new RuleBasedParser(new RTModelFactory());
        this.pdf = rbp.parse(pdf);
    }

    public void classify() {
        RuleBasedChunkClassifier classifier = new RuleBasedChunkClassifier("src/main/resources/rules/rules.drl", new RTModelFactory());
        for (int i = 0; i < pdf.getPageList().size(); i++) {
            classifier.classify(pdf.getPageList().get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE));
        }
    }

    public void affichage() {
        pdf.affichage();
    }

    public void verifSuite() { //Fonction vérifiant s'il existe des pages dont le premier bloc est la suite d'un bloc antérieur
        ChunkBlock chunk1, chunk2;
        for (int i = 1; i < pdf.getPageList().size(); i++) {
            chunk1 = pdf.getPageList().get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(0);
            chunk2 = pdf.getPageList().get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(pdf.getPageList().get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() - 1);
            //System.out.println(chunk1.getMostPopularWordFont() + " " + chunk1.getMostPopularWordStyle() + " " + chunk1.getMostPopularWordHeight() + " " + chunk1.getX1() + " " + chunk1.getX2() + " " + chunk1.getY1() + " " + chunk1.getY2() + " " + chunk1.getchunkText().replaceAll("<[^<>]*>", ""));
            //System.out.println(chunk2.getMostPopularWordFont() + " " + chunk2.getMostPopularWordStyle() + " " + chunk2.getMostPopularWordHeight() + " " + chunk2.getX1() + " " + chunk2.getX2() + " " + chunk2.getY1() + " " + chunk2.getY2() + " " + chunk2.getchunkText().replaceAll("<[^<>]*>", ""));
            if (chunk1.getX1() == chunk2.getX1() && chunk1.getX2() == chunk2.getX2()) {
                if (chunk1.getMostPopularWordFont().equals(chunk2.getMostPopularWordFont())) {
                    if (chunk1.getMostPopularWordHeight() == chunk2.getMostPopularWordHeight()) {
                        System.out.println("continuité sur la page " + (i + 1));
                        ((RTPageBlock) pdf.getPageList().get(i)).setSuite(true);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws PdfException, AccessException, EncryptionException {
        Interpreteur i = new Interpreteur("test.pdf");
        //i.affichage();
        i.classify();
    }
}
