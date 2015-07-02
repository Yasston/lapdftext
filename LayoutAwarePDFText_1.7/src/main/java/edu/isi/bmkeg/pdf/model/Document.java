package edu.isi.bmkeg.pdf.model;

import edu.isi.bmkeg.pdf.classification.ruleBased.RuleBasedChunkClassifier;
import java.util.ArrayList;
import java.util.List;

import edu.isi.bmkeg.pdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.pdf.model.RTree.RTChunkBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.pdf.model.RTree.RTPageBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTWordBlock;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;
import java.awt.Color;
import java.io.IOException;
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

    public void setWords(boolean words) {
        this.words = words;
    }

    public void affichage(int i, JPanel panel) {
        JFrame window;
        RuleBasedChunkClassifier classifier = new RuleBasedChunkClassifier("src/main/resources/rules/rules.drl", new RTModelFactory());
        classifier.classify(pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE));
        //System.out.println("PAGE NUMERO " + (i + 1));
        //System.out.println(pageList.get(i).getPageBoxWidth() + " " + pageList.get(i).getPageBoxHeight());
        List<ChunkBlock> chunkList = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);
        panel.removeAll();
        panel.repaint();
        panel.setLayout(null);
        panel.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
        for (ChunkBlock chunk : chunkList) {
            JLabel jlabel;
            if (!words) {
                if (((RTChunkBlock) chunk).getSuiv()) {
                    jlabel = new JLabel("<html>" + chunk.getType() + "<br> Ce bloc possède une suite sur la page suivante.</html>", SwingConstants.CENTER);
                } else if (((RTChunkBlock) chunk).getPredec()) {
                    jlabel = new JLabel("<html>" + chunk.getType() + "<br> Ce bloc est la suite du bloc sur la page précédente.</html>", SwingConstants.CENTER);
                } else {
                    jlabel = new JLabel(chunk.getType(), SwingConstants.CENTER);

                }
            } else {
                jlabel=new JLabel("",SwingConstants.CENTER);
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
                                WordBlock.class);
                for (SpatialEntity entity : wordBlockList) {
                    jlabel = new JLabel("mot", SwingConstants.CENTER);
                    if (((RTWordBlock) entity).getWord().contains(".")) {
                        jlabel.setText("point");
                    }
                    jlabel.setSize(entity.getWidth(), entity.getHeight());
                    jlabel.setLocation(entity.getX1(), entity.getY1());
                    jlabel.setBorder(new LineBorder(Color.red));
                    panel.add(jlabel);
                    //System.out.println("X1 = "+entity.getX1());
                    //System.out.println("Y1 = "+entity.getY1());
                    //System.out.println("X2 = "+entity.getX2());
                    //System.out.println("Y2 = "+entity.getY2());
                    //System.out.println("font = "+((RTWordBlock)entity).getFont());
                    //System.out.println("style = "+((RTWordBlock)entity).getFontStyle());
                    //System.out.println("text = "+((RTWordBlock)entity).getWord().replaceAll("<[^<>]*>", ""));
                }
            }
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
