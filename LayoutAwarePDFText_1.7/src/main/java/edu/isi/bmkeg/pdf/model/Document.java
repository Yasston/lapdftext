package edu.isi.bmkeg.pdf.model;

import java.util.ArrayList;
import java.util.List;

import edu.isi.bmkeg.pdf.extraction.exceptions.InvalidPopularSpaceValueException;
import edu.isi.bmkeg.pdf.model.RTree.RTWordBlock;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.model.spatial.SpatialEntity;
import edu.isi.bmkeg.utils.IntegerFrequencyCounter;
import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;

class MyCanvas extends JComponent {

    int x1, x2, x3, x4;

    boolean color;

    public MyCanvas(int x, int a, int b, int c, boolean color) {
        super();
        x1 = x;
        x2 = a;
        x3 = b;
        x4 = c;
        this.color = color;
    }

    public void paint(Graphics g) {
        if (color) {
            g.setColor(Color.red);
        }
        g.drawRect(x1, x2, x3, x4);
    }
}

public class Document {

    private ArrayList<PageBlock> pageList;
    private IntegerFrequencyCounter avgHeightFrequencyCounter;
    private int mostPopularWordHeight = -1;
    private boolean jPedalDecodeFailed;

    public void affichage() {
        JFrame window;
        for (int i = 0; i < pageList.size(); i++) {
            System.out.println("PAGE NUMERO " + (i + 1));
            System.out.println(pageList.get(i).getPageBoxWidth() + " " + pageList.get(i).getPageBoxHeight());
            List<ChunkBlock> chunkList = pageList.get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE);

            window = new JFrame();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setSize(pageList.get(i).getPageBoxWidth(), pageList.get(i).getPageBoxHeight());
            window.setVisible(true);
            for (ChunkBlock chunk : chunkList) {
                window.getContentPane().add(new MyCanvas(chunk.getX1(), chunk.getY1(), chunk.getWidth(), chunk.getHeight(), true));
                window.setVisible(true);
                System.out.println(chunk.getMostPopularWordFont() + " " + chunk.getMostPopularWordStyle() + " " + chunk.getMostPopularWordHeight() + " " + chunk.getX1() + " " + chunk.getX2() + " " + chunk.getY1() + " " + chunk.getY2() + " " + chunk.getchunkText().replaceAll("<[^<>]*>", ""));
                //Ci dessous affichage des mots.
                List<SpatialEntity> wordBlockList = ((PageBlock) pageList.get(i))
                        .containsByType(chunk, SpatialOrdering.MIXED_MODE,
                                WordBlock.class);
                for (SpatialEntity entity : wordBlockList) {
                    window.getContentPane().add(new MyCanvas(entity.getX1(), entity.getY1(), entity.getWidth(), entity.getHeight(), false));
                    window.setVisible(true);
                    //System.out.println("X1 = "+entity.getX1());
                    //System.out.println("Y1 = "+entity.getY1());
                    //System.out.println("X2 = "+entity.getX2());
                    //System.out.println("Y2 = "+entity.getY2());
                    //System.out.println("font = "+((RTWordBlock)entity).getFont());
                    //System.out.println("style = "+((RTWordBlock)entity).getFontStyle());
                    //System.out.println("text = "+((RTWordBlock)entity).getWord().replaceAll("<[^<>]*>", ""));
                }
            }
            try {
                System.in.read();
            } catch (IOException ex) {
                Logger.getLogger(Document.class.getName()).log(Level.SEVERE, null, ex);
            }
            window.setVisible(false);
            window = null;
        }
        System.exit(0);
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
