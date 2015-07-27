package edu.isi.bmkeg.pdf.model;

import edu.isi.bmkeg.pdf.classification.ruleBased.RuleBasedChunkClassifier;
import java.util.ArrayList;
import java.util.List;

import edu.isi.bmkeg.pdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.pdf.model.RTree.RTChunkBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.pdf.model.RTree.RTPageBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTSpatialRepresentation;
import edu.isi.bmkeg.pdf.model.RTree.RTWordBlock;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;
import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import static java.lang.Math.abs;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

public class Document {

    private ArrayList<PageBlock> pageList;
    private IntegerFrequencyCounter avgHeightFrequencyCounter;
    private int mostPopularWordHeight = -1;
    private boolean jPedalDecodeFailed;
    private boolean words;
    private int x1, x2, y1, y2;
    private Boolean analyse;

    public void setAnalyse(Boolean analyse) {
        this.analyse = analyse;
    }

    public void setWords(boolean words) {
        this.words = words;
    }

    public void verifSuite() { //Fonction vérifiant s'il existe des pages dont le premier bloc est la suite d'un bloc antérieur
        ChunkBlock chunk1, chunk2;
        for (int i = 1; i < pageList.size(); i++) {
            if (pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() > 0 && pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() > 0) {
                chunk1 = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(0);
                //System.out.println(" 1 "+chunk1.getchunkText());
                chunk2 = pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(pageList.get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() - 1);
                //System.out.println(" 2 "+chunk2.getchunkText());
                if (abs(chunk1.getX1() - chunk2.getX1()) < this.getMostPopularWordHeight() && abs(chunk1.getX2() - chunk2.getX2()) < this.getMostPopularWordHeight()) {
                    //System.out.println("tota");
                    if (chunk1.getMostPopularWordFont().equals(chunk2.getMostPopularWordFont())) {
                        //System.out.println("toti");
                        if (chunk1.getMostPopularWordHeight() == chunk2.getMostPopularWordHeight()) {
                            //System.out.println("totu");
                            //System.out.println("continuité sur la page " + (i + 1));
                            ((RTChunkBlock) chunk2).setSuiv(true);
                            ((RTChunkBlock) chunk1).setPredec(true);
                        }
                    }
                }
            }
        }
    }

    public void hierarchie() {
        for (int i = 0; i < pageList.size(); i++) {
            List<ChunkBlock> chunks = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
            for (int j = 0; j < chunks.size() - 1; j++) {
                RTChunkBlock chunk1 = (RTChunkBlock) chunks.get(j);
                RTChunkBlock chunk2 = (RTChunkBlock) chunks.get(j + 1);
                if (chunk2.getX1() > chunk1.getX1()) {
                    chunk2.setFather(chunk1);
                } else if (chunk2.getX1() == chunk1.getX1()) {
                    chunk2.setFather(chunk1.getFather());
                }
                chunk2.setBrother(chunk1);
            }
            if (i < pageList.size()) {
                RTChunkBlock chunk1 = (RTChunkBlock) chunks.get(chunks.size() - 1);
                List<ChunkBlock> chunks2 = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
                RTChunkBlock chunk2 = (RTChunkBlock) chunks2.get(0);
                if (chunk2.getX1() > chunk1.getX1()) {
                    chunk2.setFather(chunk1);
                } else if (chunk2.getX1() == chunk1.getX1()) {
                    chunk2.setFather(chunk1.getFather());
                }
                chunk2.setBrother(chunk1);
            }
        }
    }

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
        this.analyse = true;
    }

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
                        if (value2 != null && chunks.containsKey(key2) && key != key2 && !((RTChunkBlock) value2).getFrontiere()) {
                            int ch2Y = value2.getY1();
                            //Jointure horizontale
                            if (chY == ch2Y && horiz) {
                                if (abs(value.getX2() - value2.getX1()) <= mostPopularWordHeight * multHoriz) {
                                    //System.out.println("biip");
                                    SpatialEntity spatialEntity = value.union(value2);
                                    value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                    chunks.put(key, value);
                                    //System.out.println("avant " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                    pageList.get(i).delete(value2, key2);
                                    chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                    //System.out.println("après " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                }
                                //Jointure verticale
                            } else if ((value.getX1() < value2.getX2() || value.getX2() < value2.getX1()) && vert) {
                                if (abs(value.getY2() - value2.getY1()) <= mostPopularWordHeight * multVert && value.getMostPopularWordFont() != null && value.getMostPopularWordStyle() != null) {
                                    if (value.getMostPopularWordHeight() == value2.getMostPopularWordHeight()) {
                                        if (value.getMostPopularWordFont().equals(value2.getMostPopularWordFont())) {
                                            //System.out.println("bap");
                                            SpatialEntity spatialEntity = value.union(value2);
                                            value.resize(spatialEntity.getX1(), spatialEntity.getY1(), spatialEntity.getWidth(), spatialEntity.getHeight());
                                            chunks.put(key, value);
                                            //System.out.println("avant " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
                                            pageList.get(i).delete(value2, key2);
                                            chunks = ((RTSpatialRepresentation) pageList.get(i)).getIndexToChunkBlockMap();
                                            //System.out.println("après " + pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE).size());
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

    public void affichage(int i, JPanel panel, String rules, Boolean rule) {
        //System.out.println(pageList.size());
        panel.removeAll();
        panel.repaint();
        panel.setLayout(null);
        if (pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE) != null && pageList.get(i).getAllWordBlocks(SpatialOrdering.MIXED_MODE).size() > 0) {
            List<ChunkBlock> chunkList = pageList.get(i).getAllChunkBlocks(SpatialOrdering.MIXED_MODE);

            if (rule) {
                RuleBasedChunkClassifier classifier = new RuleBasedChunkClassifier(rules, new RTModelFactory());
                classifier.classify(chunkList);
            }
            //System.out.println("PAGE NUMERO " + (i + 1));
            //System.out.println(pageList.get(i).getPageBoxWidth() + " " + pageList.get(i).getPageBoxHeight());

            panel.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
            for (final ChunkBlock chunk : chunkList) {
                JLabel jlabel;
                if (!words) {
                    if (rule) {
                        jlabel = new JLabel(chunk.getType(), SwingConstants.CENTER);
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
                                    g.drawLine(chunk.getX1(), chunk.getY1(), ((RTChunkBlock) chunk).getBrother().getX1(), ((RTChunkBlock) chunk).getBrother().getY1());
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
                                    g.drawLine(chunk.getX1(), chunk.getY1(), ((RTChunkBlock) chunk).getBrother().getX1(), ((RTChunkBlock) chunk).getBrother().getY1());
                                }
                            };
                    lab.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
                    lab.setLocation(0, 0);
                    panel.add(lab);
                }
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
        this.analyse = false;
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
