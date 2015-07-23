package edu.isi.bmkeg.pdf.model.RTree;

import edu.isi.bmkeg.pdf.model.Block;
import edu.isi.bmkeg.pdf.model.PageBlock;
import edu.isi.bmkeg.pdf.model.WordBlock;

public class RTWordBlock extends RTSpatialEntity implements WordBlock {

    private String font;
    private String fontStyle;
    private String word;
    private Boolean frontiere;

    public Boolean getFrontiere() {
        return frontiere;
    }

    public void setFrontiere(Boolean frontiere) {
        this.frontiere = frontiere;
    }

    private int spaceWidth;
    private Block container;
    private String type = Block.TYPE_UNCLASSIFIED;

    public RTWordBlock(int x1, int y1, int x2, int y2, int spaceWidth,
            String font, String fontStyle, String word) {
        super(x1, y1, x2, y2);
        this.font = font;
        this.fontStyle = fontStyle;

        this.frontiere=false;
        this.word = word;
        this.spaceWidth = spaceWidth;

    }

    @Override
    public String getFont() {

        return font;
    }

    @Override
    public String getFontStyle() {

        return fontStyle;

    }

    @Override
    public String getWord() {

        return this.word;
    }

    @Override
    public int getSpaceWidth() {

        return this.spaceWidth;
    }

    @Override
    public Block getContainer() {
        return container;
    }

    @Override
    public String getLeftRightMedLine() {
        PageBlock parent = (PageBlock) this.getContainer().getContainer();
        int median = parent.getMedian();
        int X1 = this.getX1();
        int width = this.getWidth();
        int averageWordHeightForTheDocument = parent.getDocument().getMostPopularWordHeight();

        // Conditions for left
        if (X1 < median
                && (X1 + width) < (median + averageWordHeightForTheDocument)) {
            return LEFT;
        }
        // conditions for right
        if (X1 > median) {
            return RIGHT;
        }
        // conditions for medline
        int left = median - X1;
        int right = X1 + width - median;
        /*
         * Doubtful code if(right <= 0) return LEFT;
         */
        double leftIsToRight = (double) left / (double) right;
        double rightIsToLeft = (double) right / (double) left;
        if (leftIsToRight < 0.05) {
            return RIGHT;
        } else if (rightIsToLeft < 0.05) {
            return LEFT;
        } else {
            return MIDLINE;
        }

    }

    public boolean isFlush(String condition, int value) {
        PageBlock parent = (PageBlock) this.getContainer();
        int median = parent.getMedian();
        String leftRightMidline = this.getLeftRightMedLine();

        int x1 = this.getX1();
        int x2 = this.getX2();
        int marginX1 = parent.getMargin()[0];
        int marginX2 = parent.getMargin()[3];

        if (condition.equals(MIDLINE)) {
            if (leftRightMidline.equals(MIDLINE)) {
                return false;
            } else if (leftRightMidline.equals(LEFT)
                    && Math.abs(x2 - median) < value) {
                return true;
            } else if (leftRightMidline.equals(RIGHT)
                    && Math.abs(x1 - median) < value) {
                return true;
            }
        } else if (condition.equals(LEFT)) {
            if (leftRightMidline.equals(MIDLINE)
                    && Math.abs(x1 - marginX1) < value) {
                return true;
            } else if (leftRightMidline.equals(LEFT)
                    && Math.abs(x1 - marginX1) < value) {
                return true;
            } else if (leftRightMidline.equals(RIGHT)) {
                return false;
            }
        } else if (condition.equals(RIGHT)) {
            if (leftRightMidline.equals(MIDLINE)
                    && Math.abs(x2 - marginX2) < value) {
                return true;
            } else if (leftRightMidline.equals(LEFT)) {
                return false;
            } else if (leftRightMidline.equals(RIGHT)
                    && Math.abs(x2 - marginX2) < value) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getId() {

        return super.getId();
    }

    @Override
    public void setContainer(Block block) {
        this.container = block;

    }

    @Override
    public String getType() {

        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;

    }

}
