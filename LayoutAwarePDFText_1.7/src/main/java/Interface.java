
import edu.isi.bmkeg.pdf.classification.ruleBased.RuleBasedChunkClassifier;
import edu.isi.bmkeg.pdf.extraction.exceptions.AccessException;
import edu.isi.bmkeg.pdf.extraction.exceptions.EncryptionException;
import edu.isi.bmkeg.pdf.model.ChunkBlock;
import edu.isi.bmkeg.pdf.model.Document;
import edu.isi.bmkeg.pdf.model.RTree.RTChunkBlock;
import edu.isi.bmkeg.pdf.model.RTree.RTModelFactory;
import edu.isi.bmkeg.pdf.model.ordering.SpatialOrdering;
import edu.isi.bmkeg.pdf.parser.RuleBasedParser;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
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
public class Interface extends javax.swing.JFrame {

    private int pageNumb;
    private Document pdf;

    /**
     * Creates new form Interface
     */

    public void verifSuite() { //Fonction v�rifiant s'il existe des pages dont le premier bloc est la suite d'un bloc ant�rieur
        ChunkBlock chunk1, chunk2;
        for (int i = 1; i < pdf.getPageList().size(); i++) {
            chunk1 = pdf.getPageList().get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(0);
            chunk2 = pdf.getPageList().get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).get(pdf.getPageList().get(i - 1).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE).size() - 1);
            //System.out.println(chunk1.getMostPopularWordFont() + " " + chunk1.getMostPopularWordStyle() + " " + chunk1.getMostPopularWordHeight() + " " + chunk1.getX1() + " " + chunk1.getX2() + " " + chunk1.getY1() + " " + chunk1.getY2() + " " + chunk1.getchunkText().replaceAll("<[^<>]*>", ""));
            //System.out.println(chunk2.getMostPopularWordFont() + " " + chunk2.getMostPopularWordStyle() + " " + chunk2.getMostPopularWordHeight() + " " + chunk2.getX1() + " " + chunk2.getX2() + " " + chunk2.getY1() + " " + chunk2.getY2() + " " + chunk2.getchunkText().replaceAll("<[^<>]*>", ""));
            if (chunk1.getX1() == chunk2.getX1() && chunk1.getX2() == chunk2.getX2()) {
                if (chunk1.getMostPopularWordFont().equals(chunk2.getMostPopularWordFont())) {
                    if (chunk1.getMostPopularWordHeight() == chunk2.getMostPopularWordHeight()) {
                        System.out.println("continuit� sur la page " + (i + 1));
                        ((RTChunkBlock) chunk2).setSuiv(true);
                        ((RTChunkBlock) chunk1).setPredec(true);
                    }
                }
            }
        }
    }

    public void classify() {
        RuleBasedChunkClassifier classifier = new RuleBasedChunkClassifier("src/main/resources/rules/rules.drl", new RTModelFactory());
        for (int i = 0; i < pdf.getPageList().size(); i++) {
            classifier.classify(pdf.getPageList().get(i).getAllChunkBlocks(SpatialOrdering.COLUMN_AWARE_MIXED_MODE));
        }
    }

    public Interface() {
        initComponents();
        pageNumb = 0;
        filepath.setText(System.getProperty("user.dir") + "/test.pdf");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        filepath = new javax.swing.JTextField();
        parc = new javax.swing.JButton();
        charg = new javax.swing.JButton();
        prec = new javax.swing.JButton();
        suiv = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        panel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        parc.setText("Parcourir");
        parc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                parcActionPerformed(evt);
            }
        });

        charg.setText("Charger le PDF");
        charg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chargActionPerformed(evt);
            }
        });

        prec.setText("Pr�c.");
        prec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                precActionPerformed(evt);
            }
        });

        suiv.setText("Suiv.");
        suiv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                suivActionPerformed(evt);
            }
        });

        jLabel1.setText("LATOE 2.0");

        panel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        panel.setAlignmentX(324.0F);

        javax.swing.GroupLayout panelLayout = new javax.swing.GroupLayout(panel);
        panel.setLayout(panelLayout);
        panelLayout.setHorizontalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panelLayout.setVerticalGroup(
            panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 810, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(122, 122, 122)
                        .addComponent(prec)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(charg, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(suiv)
                        .addContainerGap(133, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(filepath, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(parc)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addGap(56, 56, 56))
                    .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filepath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(parc)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(charg)
                    .addComponent(suiv)
                    .addComponent(prec))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void chargActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chargActionPerformed
        // TODO add your handling code here:
        RuleBasedParser rbp = new RuleBasedParser(new RTModelFactory());
        try {
            this.pdf = rbp.parse(filepath.getText());
        } catch (PdfException ex) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
        } catch (EncryptionException ex) {
            Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
        }
        verifSuite();
        pageNumb=0;
        pdf.affichage(0, panel);
    }//GEN-LAST:event_chargActionPerformed

    private void precActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_precActionPerformed
        // TODO add your handling code here:
        if (pageNumb > 0) {
            pageNumb--;
            pdf.affichage(pageNumb, panel);
        }
    }//GEN-LAST:event_precActionPerformed

    private void suivActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_suivActionPerformed
        // TODO add your handling code here:
        if (pageNumb < pdf.getPageList().size() - 1) {
            pageNumb++;
            pdf.affichage(pageNumb, panel);
        }
    }//GEN-LAST:event_suivActionPerformed

    private void parcActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_parcActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(fc) == JFileChooser.APPROVE_OPTION) {
            filepath.setText(fc.getSelectedFile().getAbsolutePath());
        }

    }//GEN-LAST:event_parcActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Interface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Interface().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton charg;
    private javax.swing.JTextField filepath;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel panel;
    private javax.swing.JButton parc;
    private javax.swing.JButton prec;
    private javax.swing.JButton suiv;
    // End of variables declaration//GEN-END:variables
}