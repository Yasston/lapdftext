
import edu.isi.bmkeg.pdf.model.ChunkBlock;
import edu.isi.bmkeg.pdf.model.Document;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Cobweb;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode1;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

/**
 *
 * @author Samih
 */
public class Clusterer {

    /*
     Classe gérant le clustering
     */
    private HashMap<Integer, Integer> associations;
    private HashMap<Integer, Integer> nodes;
    private Document pdf;
    private Instances instances;
    private Attribute numero, x1, x2, textsize, fontstyle, lineNumb, height, width;

    /**
     * Constructeur
     *
     * @param doc
     */
    public Clusterer(Document doc) {
        this.pdf = doc;
        associations = new HashMap<Integer, Integer>();
        nodes = new HashMap<Integer, Integer>();
        //numero = new Attribute("numero");
        x1 = new Attribute("x1");
        //x2 = new Attribute("x2");
        textsize = new Attribute("textsize");
        fontstyle = new Attribute("fontstyle");
        height = new Attribute("height");
        lineNumb = new Attribute("lineNumb");
        width = new Attribute("width");
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

    /**
     * Fonction de création des instances : une instance correspond à un bloc
     */
    public void fillInstances() {
        ArrayList<ChunkBlock> chunks = pdf.returnAllBlocks();
        int index = 0;
        for (ChunkBlock ch : chunks) {
            Instance i = new Instance(6);
            //i.setValue(numero, index);
            i.setValue(x1, -1 * ch.getX1());
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

    /**
     * fonction de classification : renvoie un plan (String) de l'arbre généré
     *
     * @return
     * @throws Exception
     */
    public String classify() throws Exception {
        Cobweb cw = new Cobweb();
        cw.buildClusterer(instances);
        for (int i = 0; i < instances.numInstances(); i++) {
            cw.updateClusterer(instances.instance(i));
        }
        cw.updateFinished();

        for (int i = 0; i < instances.numInstances(); i++) {
            //System.out.println("Chunk  : " + i + " Node : " + cw.clusterInstance(instances.instance(i)));
            associations.put(i, cw.clusterInstance(instances.instance(i)));
        }
        //System.out.println(cw.graph());
        //buildMap(cw.graph());
        return cw.graph();
    }

    /**
     * Fonction de remplissage de la hashmap bloc->noeud
     *
     * @param graph
     */
    public void buildMap(String graph, int j) {
        graph = graph.replaceAll("^.*\\{", "");
        graph = graph.replaceAll("\\}.*", "");
        graph = graph.replaceAll("\\[.*\\]", "");
        graph = graph.replaceAll("N[0-9]+ ", "");
        graph = graph.replaceAll("\\s+", ";");
        graph = graph.replaceAll("N", "");
        String[] tab = graph.split(";");
        //System.out.println(tab);
        for (String s : tab) {
            if (s.length() > 3) {
                String[] aux = s.split("->");
                nodes.put(Integer.parseInt(aux[1]), Integer.parseInt(aux[0]));
            }
        }
        //System.out.println(nodes);
        for (int i = 0; i < associations.size(); i++) {
            iterBuildMap(i,j);
        }
        //System.out.println(associations);
        HashMap<Integer, String> nodes = new HashMap<Integer, String>();
        for (Entry<Integer, Integer> entry : associations.entrySet()) {
            Boolean mark = true;
            for (Entry<Integer, String> entry2 : nodes.entrySet()) {
                if (entry.getValue() == entry2.getKey()) {
                    entry2.setValue(entry2.getValue() + ", " + entry.getKey());
                    mark = false;
                }
            }
            if (mark) {
                nodes.put(entry.getValue(), "" + entry.getKey());
            }
        }
        for (Entry<Integer, String> entry2 : nodes.entrySet()) {
            System.out.println("Node "+entry2.getKey() + " -> " + entry2.getValue());
        }
    }

    /*
     Fonction récurisve de construction de la hashmap
     */
    public void iterBuildMap(int i, int j) {
            if (j>0&&nodes.get(associations.get(i)) == 0) {
            } else if (j>1&&nodes.get(nodes.get(associations.get(i))) == 0) {
            } else if (j>2&&nodes.get(nodes.get(nodes.get(associations.get(i)))) == 0) {
            } else if (j>3&&nodes.get(nodes.get(nodes.get(nodes.get(associations.get(i))))) == 0) {
            } else if (j>4&&nodes.get(nodes.get(nodes.get(nodes.get(nodes.get(associations.get(i)))))) == 0) {
            } else {
                associations.put(i, nodes.get(associations.get(i)));
                iterBuildMap(i,j);
            }
    }

    /*
     Génération du plan 
     */
    public void genPlan(String graph) {
        graph = graph.replaceAll("node", "n");
        graph = graph.replaceAll("leaf", "l");
        final javax.swing.JFrame jf
                = new javax.swing.JFrame("Weka Classifier Tree Visualizer");
        jf.setSize(2500, 1000);
        jf.getContentPane().setLayout(new BorderLayout());
        TreeVisualizer tv = new TreeVisualizer(null,
                graph,
                new PlaceNode2());
        jf.getContentPane().add(tv, BorderLayout.CENTER);
        jf.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                jf.dispose();
            }
        });

        jf.setVisible(true);
        tv.fitToScreen();
    }

}
