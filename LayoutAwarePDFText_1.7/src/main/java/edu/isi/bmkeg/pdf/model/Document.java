package edu.isi.bmkeg.pdf.model;

import edu.isi.bmkeg.pdf.classification.ruleBased.RuleBasedChunkClassifier;
import java.util.ArrayList;
import java.util.List;

import edu.isi.bmkeg.pdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.pdf.model.RTree.RTChunkBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.pdf.model.RTree.RTSpatialRepresentation;
import edu.isi.bmkeg.pdf.model.RTree.RTWordBlock;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Document {

    private ArrayList<PageBlock> pageList;
    private IntegerFrequencyCounter avgHeightFrequencyCounter;
    private int mostPopularWordHeight = -1;
    private boolean jPedalDecodeFailed;
    private boolean words;
    private int x1, x2, y1, y2;

    /**
     * Fonction chargeant une annotation prédéfinie à partir d'un lecteur
     *
     * @param br
     * @throws IOException
     */
    public void automaticAnnotation(BufferedReader br) throws IOException {
        ArrayList<ChunkBlock> aux = returnAllBlocks();
        for (int i = 0; i < aux.size(); i++) {
            String aux1 = br.readLine();
            ((RTChunkBlock) aux.get(i)).setType_annote(aux1.substring(aux1.indexOf("#*#*#") + 5));
        }
    }

    /**
     * Fonction retournant une liste de tous les blocs
     *
     * @return
     */
    public ArrayList<ChunkBlock> returnAllBlocks() {
        ArrayList<ChunkBlock> aux = new ArrayList<ChunkBlock>();
        for (int i = 0; i < pageList.size(); i++) {
            aux.addAll(pageList.get(i).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE));
        }
        return aux;
    }

    /**
     * Calcul de la précision du programme
     *
     * @return
     */
    public float calculPrecis() {
        int bons = 0;
        int total = 0;
        for (PageBlock page : pageList) {
            for (ChunkBlock chunk : page.getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE)) {
                total++;
                if (chunk.getType().equals(((RTChunkBlock) chunk).getType_annote())) {
                    bons++;
                }
            }
        }
        if (total > 0) {
            bons = bons * 100;
            return ((float) bons) / total;
        } else {
            return 0;
        }
    }

    public void setWords(boolean words) {
        this.words = words;
    }

    /**
     * Génération du plan correspondant au fichier PDF
     *
     * @param pan
     */
    public void genArbre(JScrollPane pan) {
        List<ChunkBlock> list = pageList.get(0).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE);
        String arbre = "";
        arbre = parcours((RTChunkBlock) list.get(0), "..........");
        arbre = (((RTChunkBlock) list.get(0)).getchunkText().replaceAll("<[^<>]*>", "")) + arbre;
        JLabel lab = new JLabel("<html>" + arbre + "</html>");
        lab.setLocation(0, 0);
        pan.add(lab);
        pan.setViewportView(lab);
    }

    /**
     * Fonction de parcours associée à la génération de l'arbre
     *
     * @param chunk
     * @param offset
     * @return
     */
    public String parcours(RTChunkBlock chunk, String offset) {
        String arbre = "";
        for (int i = 0; i < chunk.getSons().size(); i++) {
            if (chunk.getSons().get(i).getType().equals("header") || chunk.getSons().get(i).getType().equals("title")) {
                arbre = arbre + "<br>" + offset + (chunk.getSons().get(i).getchunkText().replaceAll("<[^<>]*>", ""));
                arbre = arbre + parcours((RTChunkBlock) chunk.getSons().get(i), offset + "..........");
            } else if (chunk.getSons().get(i).getType().equals("item")) {
                arbre = arbre + "<br>" + offset + "item";
                arbre = arbre + parcours((RTChunkBlock) chunk.getSons().get(i), offset + "..........");
            } else {
                arbre = arbre + "<br>" + offset + "paragraphe";
                arbre = arbre + parcours((RTChunkBlock) chunk.getSons().get(i), offset + "..........");
            }
        }
        return arbre;
    }

    /**
     * Fonction de vérification des blocs en chevauchement
     */
    public void verifSuite() {
//Fonction vérifiant s'il existe des pages dont le premier bloc est la suite d'un bloc antérieur
        ChunkBlock chunk1, chunk2;
        for (int i = 1; i < pageList.size(); i++) {
            if (pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() > 0 && pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() > 0) {
                chunk1 = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(0);
                chunk2 = pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() - 1);
                if (abs(chunk1.getX1() - chunk2.getX1()) < this.getMostPopularWordHeight() && abs(chunk1.getX2() - chunk2.getX2()) < this.getMostPopularWordHeight()) {
                    if (chunk1.getMostPopularWordFont().equals(chunk2.getMostPopularWordFont())) {
                        if (chunk1.getMostPopularWordHeight() == chunk2.getMostPopularWordHeight()) {
                            ((RTChunkBlock) chunk2).setSuiv(true);
                            ((RTChunkBlock) chunk1).setPredec(true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Fonction liée au parcours shift-reduce des blocs pour l'établissement des relations de coordination/subordination : retourne 0 i coordination, 1 si subordination, 2 si shift
     * @param chunk1
     * @param chunk2
     * @param def
     * @return 
     */
    public int checkHierarchie(RTChunkBlock chunk1, RTChunkBlock chunk2, RTChunkBlock def) {
        if ((chunk1.getType().equals("title") || chunk1.getType().equals("header")) && !(chunk2.getType().equals("title") || chunk2.getType().equals("header")) && chunk1.getY2() <= chunk2.getY1()) {
            return 1;
        } else if (chunk2.getX1() > chunk1.getX1() && chunk1.getY2() <= chunk2.getY1()) {
            return 1;
        }
        if ((!(chunk1.getType().equals("title") || chunk1.getType().equals("header"))) && (chunk2.getType().equals("title") || chunk2.getType().equals("header"))) {
            return 2;
        } else if ((chunk1.getType().equals("title") || chunk1.getType().equals("header")) && (chunk2.getType().equals("title") || chunk2.getType().equals("header"))) {
            if (chunk1.getMostPopularWordHeight() == chunk2.getMostPopularWordHeight()) {
                return 0;
            } else if (chunk1.getMostPopularWordHeight() > chunk2.getMostPopularWordHeight()) {
                return 1;
            } else {
                return 2;
            }
        } else if (abs(chunk2.getX1() - chunk1.getX1()) < 10) {
            return 0;
        } else {
            return 2;
        }

    }

    /**
     * Fonction d'établissement de la hiérarchie : basée sur un alogrithme shift-reduce
     */
    public void hierarchie() {
        RTChunkBlock def = (RTChunkBlock) pageList.get(0).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE).get(0);
        for (int i = 0; i < pageList.size(); i++) {
            List<ChunkBlock> chunks = pageList.get(i).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE);
            for (int j = 0; j < chunks.size(); j++) {
                RTChunkBlock chunk1 = (RTChunkBlock) chunks.get(j);
                RTChunkBlock chunk2;
                if (j < chunks.size() - 1) {
                    chunk2 = (RTChunkBlock) chunks.get(j + 1);
                } else if (i < pageList.size() - 1) {
                    List<ChunkBlock> chunks2 = pageList.get(i + 1).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE);
                    chunk2 = (RTChunkBlock) chunks2.get(0);
                } else {
                    break;
                }
                chunk2.setFather(def);
                int aux = checkHierarchie(chunk1, chunk2, def);
                while (aux == 2 && !chunk1.getchunkText().equals(def.getchunkText())) {
                    chunk1 = (RTChunkBlock) chunk1.getFather();
                    aux = checkHierarchie(chunk1, chunk2, def);
                }
                if (aux == 0) {
                    chunk2.setBrother(chunk1);
                    chunk1.setOtherbrother(chunk2);
                    if (chunk1.getFather() != null) {
                        chunk2.setFather(chunk1.getFather());
                        ((RTChunkBlock) chunk1.getFather()).addSon(chunk2);
                    }
                } else if (aux == 1) {
                    chunk2.setFather(chunk1);
                    chunk1.addSon(chunk2);
                }
            }
        }
    }

    /**
     * Fonction de rognage
     */
    public void rognerAction() {
        for (int i = 0; i < pageList.size(); i++) {
            HashMap<Integer, ChunkBlock> chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
            Object[] keys = chunks.keySet().toArray();
            for (int k = 0; k < keys.length; k++) {
                Integer key = (Integer) keys[k];
                ChunkBlock value = chunks.get(key);
                if (value != null) {
                    if ((value.getX2() <= this.x1) || (value.getY2() <= this.y1) || (value.getX1() >= this.x2) || (value.getY1() >= this.y2)) {
                        chunks.remove(key);
                    }
                }
            }
        }
    }

    /**
     * Fonction d'analyse pré-rognage
     */
    public void rognerAnalyse() {

        IntegerFrequencyCounter x1, y1, x2, y2;
        x1 = new IntegerFrequencyCounter(0);
        x2 = new IntegerFrequencyCounter(0);
        y1 = new IntegerFrequencyCounter(0);
        y2 = new IntegerFrequencyCounter(0);

        for (int i = 0; i < pageList.size(); i++) {
            List<ChunkBlock> chunks = pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
            for (int j = 0; j < chunks.size(); j++) {
                ChunkBlock ch = chunks.get(j);
                x1.add(ch.getX1());
                x2.add(ch.getX2());
            }
        }
        for (int i = 0; i < pageList.size(); i++) {
            List<ChunkBlock> chunks = pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
            for (int j = 0; j < chunks.size(); j++) {
                ChunkBlock ch = chunks.get(j);
                if ((abs(ch.getX1() - x1.getMostPopular()) <= x1.getMostPopular() * 0.1) && (abs(ch.getX2() - x2.getMostPopular()) <= x2.getMostPopular() * 0.1)) {
                    y1.add(ch.getY1());
                    y2.add(ch.getY2());
                }
            }
        }
        this.x1 = x1.getMostPopular();
        this.x2 = x2.getMostPopular();
        this.y1 = y1.getMin();
        this.y2 = y2.getMax();
    }

    /**
     * Fonction de fusion post-parsing : la fusion est horizontale, verticale, ou liée aux fins de paragraphes.
     * @param vert
     * @param horiz
     * @param fin
     * @param multVert
     * @param multHoriz
     * @return 
     */
    public int joinBlocks(Boolean vert, Boolean horiz, Boolean fin, float multVert, float multHoriz) {
        int x = 0;
        for (int i = 0; i < pageList.size(); i++) {
            HashMap<Integer, ChunkBlock> chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
            Object[] keys = chunks.keySet().toArray();
            for (int k = 0; k < keys.length; k++) {
                Integer key = (Integer) keys[k];
                ChunkBlock value = chunks.get(key);
                if (value != null && chunks.containsKey(key)) {
                    int chY = value.getY1();
                    for (int j = 0; j < keys.length; j++) {
                        Integer key2 = (Integer) keys[j];
                        ChunkBlock value2 = chunks.get(key2);
                        if (value2 != null && chunks.containsKey(key2) && key != key2) {
                            int ch2Y = value2.getY1();
                            //Jointure horizontale
                            if (chY == ch2Y && horiz) {
                                if (abs(value.getX2() - value2.getX1()) <= mostPopularWordHeight * multHoriz) {
                                    //System.out.println("biip");
                                    SpatialEntity spatialEntity = value.union(value2);
                                    value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                    chunks.put(key, value);
                                    pageList.get(i).delete(value2, key2);
                                    chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                }
                                //Jointure verticale
                            } else if ((value.getX1() < value2.getX2() || value.getX2() < value2.getX1()) && vert) {
                                if (abs(value.getY2() - value2.getY1()) <= mostPopularWordHeight * multVert && value.getMostPopularWordFont() != null && value.getMostPopularWordStyle() != null) {
                                    if (value.getMostPopularWordHeight() == value2.getMostPopularWordHeight()) {
                                        if (value.getMostPopularWordFont().equals(value2.getMostPopularWordFont())) {
                                            SpatialEntity spatialEntity = value.union(value2);
                                            value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                            chunks.put(key, value);
                                            pageList.get(i).delete(value2, key2);
                                            chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                        }
                                    }
                                }// Détection de la dernière ou première ligne d'un paragraphe
                            } else if (fin && abs(value.getY2() - value2.getY1()) <= mostPopularWordHeight * multVert) {

                                if (((value.getX1() == value2.getX1()) || (value.getX2() == value2.getX2())) && value.getMostPopularWordFont() != null && value.getMostPopularWordStyle() != null) {
                                    if (value.getMostPopularWordHeight() == value2.getMostPopularWordHeight()) {
                                        if (value.getMostPopularWordFont().equals(value2.getMostPopularWordFont()) && value.getMostPopularWordStyle().equals(value2.getMostPopularWordStyle())) {
                                            //System.out.println("boop");
                                            SpatialEntity spatialEntity = value.union(value2);
                                            value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                            chunks.put(key, value);
                                            //System.out.println("avant " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                            pageList.get(i).delete(value2, key2);
                                            chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                            //System.out.println("après " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                        }
                                    }
                                }

                            } else if ((value.getX2() > value2.getX1()) && (value.getY2() > value2.getY1()) && (value.getX1() < value2.getX2()) && (value.getY1() < value2.getY2())) {
                                //System.out.println("boop");
                                SpatialEntity spatialEntity = value.union(value2);
                                value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                chunks.put(key, value);
                                //System.out.println("avant " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                pageList.get(i).delete(value2, key2);
                                chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                //System.out.println("après " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                            }
                            //System.out.println("chunk1 :" + key + "(" + value.getX1() + "," + value.getX2() + "," + value.getY1() + "," + value.getY2() + ")" + " chnuk2 :" + key2 + "(" + value2.getX1() + "," + value2.getX2() + "," + value2.getY1() + "," + value2.getY2() + ")");
                        }
                    }
                }
            }
            x = x + chunks.size();
        }
        return x;
    }

    /**
     * Fonction de classification des blocs
     * @param rules 
     */
    public void classif(String rules) {
        for (int i = 0; i < pageList.size(); i++) {
            if (pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE) != null && pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE).size() > 0) {
                List<ChunkBlock> chunkList = pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
                RuleBasedChunkClassifier classifier = new RuleBasedChunkClassifier(rules, new RTModelFactory());
                classifier.classify(chunkList);
            }
        }

    }

    /**
     * Fonction d'affichage : affiche la i-ème page sur le panel en question, et affiche les labels si rule est positif
     * @param i
     * @param panel
     * @param rule 
     */
    public void affichage(int i, JPanel panel, boolean rule) {
        //System.out.println(pageList.size());
        panel.removeAll();
        panel.repaint();
        panel.setLayout(null);
        if (pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE) != null && pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE).size() > 0) {
            List<ChunkBlock> chunkList = pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE);
            //System.out.println("PAGE NUMERO " + (i + 1));
            //System.out.println(pageList.get(i).getPageBoxWidth() + " " + pageList.get(i).getPageBoxHeight());

            panel.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
            int j = 0;
            for (int x = 0; x < i; x++) {
                j = j + pageList.get(x).getAllChunkBlocks(SpatialOrdering.VERTICAL_MODE).size();
            }
            for (final ChunkBlock chunk : chunkList) {
                JLabel jlabel;
                if (!words) {
                    if (rule) {
                        jlabel = new JLabel("#" + j + " : " + chunk.getType(), SwingConstants.CENTER);
                    } else {
                        jlabel = new JLabel();
                    }
                    if (((RTChunkBlock) chunk).getSuiv()) {
                        jlabel.setText("<html>" + jlabel.getText() + "<br> Ce bloc possède une suite sur la page suivante.</html>");
                    } else if (((RTChunkBlock) chunk).getPredec()) {
                        jlabel.setText("<html>" + jlabel.getText() + "<br> Ce bloc est la suite du bloc sur la page précédente.</html>");
                    }
                } else {
                    jlabel = new JLabel();
                }
                jlabel.setSize(chunk.getWidth(), chunk.getHeight());
                jlabel.setLocation(chunk.getX1(), chunk.getY1());
                jlabel.setBorder(new LineBorder(Color.black));
                panel.add(jlabel);
                //System.out.println(chunk.getMostPopularWordFont() + " " + chunk.getMostPopularWordStyle() + " " + chunk.getMostPopularWordHeight() + " " + chunk.getX1() + " " + chunk.getX2() + " " + chunk.getY1() + " " + chunk.getY2() + " " + chunk.getchunkText().replaceAll("<[^<>]*>", ""));
                //Ci dessous affichage des mots.

                if (words) {
                    List<SpatialEntity> wordBlockList = ((PageBlock) pageList.get(i))
                            .containsByType(chunk, SpatialOrdering.MIXED_MODE,
                                    WordBlock.class
                            );
                    for (SpatialEntity entity : wordBlockList) {
                        jlabel = new JLabel("mot", SwingConstants.CENTER);
                        if (((RTWordBlock) entity).getWord().contains(".")) {
                            jlabel.setText("point");
                        }
                        jlabel.setSize(entity.getWidth(), entity.getHeight());
                        jlabel.setLocation(entity.getX1(), entity.getY1());
                        jlabel.setBorder(new LineBorder(Color.red));
                        panel.add(jlabel);
                    }
                }
                if (((RTChunkBlock) chunk).getFather() != null) {
                    JLabel lab = new JLabel(/*constructor args here*/) {
                                @Override
                                public void paint(Graphics g) {
                                    super.paint(g);
                                    g.setColor(Color.blue);
                                    g.drawLine(chunk.getX2(), chunk.getY2(), ((RTChunkBlock) chunk).getFather().getX1(), ((RTChunkBlock) chunk).getFather().getY1());

                                }
                            };
                    lab.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
                    lab.setLocation(0, 0);
                    panel.add(lab);

                }
                if (((RTChunkBlock) chunk).getBrother() != null) {
                    JLabel lab = new JLabel(/*constructor args here*/) {
                                @Override
                                public void paint(Graphics g) {
                                    super.paint(g);
                                    g.setColor(Color.red);
                                    g.drawLine(chunk.getX1(), chunk.getY1(), ((RTChunkBlock) chunk).getBrother().getX2(), ((RTChunkBlock) chunk).getBrother().getY2());
                                    //g.drawArc(chunk.getX1(), chunk.getY1(),50, ((RTChunkBlock) chunk).getBrother().getY1()-chunk.getY1(), 45, 0);
                                }
                            };
                    lab.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
                    lab.setLocation(0, 0);
                    panel.add(lab);
                }
                j++;
            }

            JLabel jlabel = new JLabel();
            jlabel.setSize(x2 - x1, y2 - y1);
            jlabel.setLocation(x1, y1);
            jlabel.setBorder(new LineBorder(Color.green, 4));
            panel.add(jlabel);
        } else {
            JLabel jlabel = new JLabel("Cette page est vide.", SwingConstants.CENTER);
            jlabel.setLocation(panel.getWidth() / 2, panel.getHeight() / 2);
            panel.add(jlabel);
        }
    }

    //------------------------Fin des modifications liées à LaToe 2.0-----------------------------------------------------
    //------------------------Fonction originales de la librairie--------------------------------------------------------
    
    public ArrayList<PageBlock> getPageList() {
        return pageList;
    }

    public boolean hasjPedalDecodeFailed() {
        return jPedalDecodeFailed;
    }

    public void setjPedalDecodeFailed(boolean jPedalDecodeFailed) {
        this.jPedalDecodeFailed = jPedalDecodeFailed;
    }

    public Document() {
        this.avgHeightFrequencyCounter = new IntegerFrequencyCounter(1);
        this.words = false;
    }

    public int getTotalNumberOfPages() {
        return this.pageList.size();
    }

    public void addPages(List<PageBlock> pageList) {
        this.pageList = new ArrayList<PageBlock>(pageList);
    }

    public PageBlock getPage(int pageNumber) {

        return pageList.get(pageNumber - 1);
    }

    public ChunkBlock getLastChunkBlock(ChunkBlock chunk) throws InvalidPopularSpaceValueException {
        int pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber();
        PageBlock page = this.getPage(pageNumber);
        if (page.getMostPopularVerticalSpaceBetweenWordsPage() < 0 && page.getMostPopularWordHeightPage() > page.getMostPopularWordWidthPage() * 2) {//page.getMostPopularWordHeightPage()>page.getMostPopularWordWidthPage()*2
            System.err.println("Possible page with vertical text flow at page number +" + pageNumber);
            //throw new InvalidPopularSpaceValueException("Possible page with vertical text flow at page number +"+pageNumber);
        }

        if (chunk.getLastChunkBlock() != null) {
            //System.out.println("Same page");
            return chunk.getLastChunkBlock();
        } else {
            pageNumber = ((PageBlock) chunk.getContainer()).getPageNumber() - 1;

            if (pageNumber == 0) {
                return null;
            }

            page = this.getPage(pageNumber);
            List<ChunkBlock> sortedChunkBlockList = page.getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
            //System.out.println("Page:"+ pageNumber);
            return sortedChunkBlockList.get(sortedChunkBlockList.size() - 1);
        }

    }

    public int getMostPopularWordHeight() {
        if (mostPopularWordHeight != -1) {
            avgHeightFrequencyCounter = null;
            return mostPopularWordHeight;
        }

        int mostPopular = avgHeightFrequencyCounter.getMostPopular();
        double mostPopularCount = avgHeightFrequencyCounter
                .getCount(mostPopular);
        int secondMostPopular = avgHeightFrequencyCounter.getNextMostPopular();
        double secondMostPopularCount = avgHeightFrequencyCounter
                .getCount(secondMostPopular);
        double ratio = secondMostPopularCount / mostPopularCount;
        if (secondMostPopular > mostPopular && ratio > 0.8) {
            mostPopularWordHeight = secondMostPopular;
        } else {
            mostPopularWordHeight = mostPopular;
        }

        return mostPopularWordHeight;
    }

    public void addToWordHeightFrequencyCounter(int height) {
        avgHeightFrequencyCounter.add(height);
    }

}
