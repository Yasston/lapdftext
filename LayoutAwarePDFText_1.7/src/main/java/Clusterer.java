
import edu.isi.bmkeg.pdf.model.ChunkBlock;
import edu.isi.bmkeg.pdf.model.Document;
import java.util.ArrayList;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Cobweb;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author Samih
 */
public class Clusterer {

    private Document pdf;
    private Instances instances;
    private Attribute numero, x1, x2, textsize, fontstyle, lineNumb, height, width;

    public Clusterer(Document doc) {
        this.pdf = doc;

        //numero = new Attribute("numero");
        x1 = new Attribute("x1");
        //x2 = new Attribute("x2");
        textsize = new Attribute("textsize");
        fontstyle=new Attribute("fontstyle");
        height=new Attribute("height");
        lineNumb=new Attribute("lineNumb");
        width=new Attribute("width");
        FastVector vec = new FastVector(6);
        //vec.addElement(numero);
        vec.addElement(x1);
        //vec.addElement(x2);
        vec.addElement(textsize);
        vec.addElement(fontstyle);
        vec.addElement(height);
        vec.addElement(lineNumb);
        vec.addElement(width);
        instances = new Instances("instances", vec, 0);
    }

    public void fillInstances() {
        ArrayList<ChunkBlock> chunks = pdf.returnAllBlocks();
        int index = 0;
        for (ChunkBlock ch : chunks) {
            Instance i = new Instance(6);
            //i.setValue(numero, index);
            i.setValue(x1, -1*ch.getX1());
            //  i.setValue(x2, ch.getX2());
            i.setValue(textsize, ch.getMostPopularWordHeight());
            i.setValue(lineNumb, ch.getNumberOfLine());
            i.setValue(height, ch.getHeight());
            i.setValue(width, ch.getWidth());
            if (ch.getMostPopularWordStyle().contains("bold")) {
                i.setValue(fontstyle, 1);
            } else if (ch.getMostPopularWordStyle().contains("italic")) {
                i.setValue(fontstyle, 2);
            } else {
                i.setValue(fontstyle, 0);
            }
            instances.add(i);
            
            index++;
        }
    }

    public String classify() throws Exception {
        Cobweb cw = new Cobweb();
        cw.buildClusterer(instances);
        for (int i = 0; i < instances.numInstances(); i++) {
            cw.updateClusterer(instances.instance(i));
        }
        cw.updateFinished();
        
        for (int i = 0; i< instances.numInstances(); i++) {
            System.out.println("Chunk  : "+i+ " Node : " + cw.clusterInstance(instances.instance(i)));
        }
        return cw.graph();
    }

}
