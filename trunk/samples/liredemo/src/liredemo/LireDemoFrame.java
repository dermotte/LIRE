/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

/*
 * LireDemoFrame.java
 *
 * Created on 20th of February 2007, 10:37
 */
package liredemo;

import edu.uniklu.itec.mosaix.ImageFunctions;
import edu.uniklu.itec.mosaix.engine.Engine;
import liredemo.flickr.FlickrIndexingThread;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.filter.LsaFilter;
import net.semanticmetadata.lire.filter.RerankFilter;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.lucene.store.FSDirectory.open;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LireDemoFrame extends javax.swing.JFrame {

    private Color highlightHoverColor = Color.decode("#dddddd");
    private Color highlightSelectColor = Color.decode("#eeeeee");
    private SearchResultsTableModel tableModel = new SearchResultsTableModel();
    private IndexReader browseReader = null;

    /**
     * Creates new form LireDemoFrame
     */
    public LireDemoFrame() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
        }
        initComponents();
        try {
            Image icon = ImageIO.read(getClass().getResource("/resources/viewmag16.png"));
            if (icon != null) {
                setIconImage(icon);
            }
        } catch (IOException ex) {
            Logger.getLogger(LireDemoFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        selectboxDocumentBuilder.setSelectedIndex(5);
        buttonSwitchIndex.setBackground(highlightSelectColor);
        DropTarget t = new DropTarget(searchPanel, new DropTargetListener() {

            public void dragEnter(DropTargetDragEvent dtde) {
            }

            public void dragOver(DropTargetDragEvent dtde) {
            }

            public void dropActionChanged(DropTargetDragEvent dtde) {
            }

            public void dragExit(DropTargetEvent dte) {
            }

            public void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable tr = dtde.getTransferable();
                    DataFlavor[] flavors = tr.getTransferDataFlavors();
                    for (int i = 0; i < flavors.length; i++) {
                        System.out.println("Possible flavor: " + flavors[i].getMimeType());
                        if (flavors[i].isFlavorJavaFileListType()) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                            java.util.List list = (java.util.List) tr.getTransferData(flavors[i]);
                            textfieldSearchImage.setText(list.get(0).toString());
                            dtde.dropComplete(true);
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // add my custom image panel at the right place ...
        browseImageContainerPanel.add(browseImagePanel, BorderLayout.CENTER);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sameSearchMenu = new javax.swing.JMenu();
        searchMpeg7Descriptors = new javax.swing.JMenuItem();
        searchColorLayout = new javax.swing.JMenuItem();
        searchEdgeHistogram = new javax.swing.JMenuItem();
        searchScalableColor = new javax.swing.JMenuItem();
        searchAutoColorCorrelation = new javax.swing.JMenuItem();
        mosaicButtons = new javax.swing.ButtonGroup();
        browseImagePanel = new liredemo.ImagePanel();
        topPane = new javax.swing.JPanel();
        controlPane = new javax.swing.JPanel();
        switchButtonsPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        buttonSwitchIndex = new javax.swing.JButton();
        buttonSwitchSearch = new javax.swing.JButton();
        buttonSwitchBrowse = new javax.swing.JButton();
        buttonSwitchMosaic = new javax.swing.JButton();
        buttonSwitchOptions = new javax.swing.JButton();
        buttonSwitchAbout = new javax.swing.JButton();
        cardPanel = new javax.swing.JPanel();
        indexPanel = new javax.swing.JPanel();
        textfieldIndexDir = new javax.swing.JTextField();
        buttonOpenDir = new javax.swing.JButton();
        buttonStartIndexing = new javax.swing.JButton();
        progressBarIndexing = new javax.swing.JProgressBar();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        checkBoxAddToExisintgIndex = new javax.swing.JCheckBox();
        searchPanel = new javax.swing.JPanel();
        textfieldSearchImage = new javax.swing.JTextField();
        buttonOpenImage = new javax.swing.JButton();
        buttonStartSearch = new javax.swing.JButton();
        progressSearch = new javax.swing.JProgressBar();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        browsePanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        labelDocCount = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        spinnerCurrentDocNum = new javax.swing.JSpinner();
        spinnerMaxDocCount = new javax.swing.JSpinner();
        buttonSearchFromBrowse = new javax.swing.JButton();
        browseImageContainerPanel = new javax.swing.JPanel();
        mosaicPanel = new javax.swing.JPanel();
        textfieldMosaicImage = new javax.swing.JTextField();
        buttonOpenMosaicImage = new javax.swing.JButton();
        buttonStartMosaicing = new javax.swing.JButton();
        progressMosaic = new javax.swing.JProgressBar();
        labelMosaicTitle = new javax.swing.JLabel();
        mosaicTileCountSlider = new javax.swing.JSlider();
        jLabel12 = new javax.swing.JLabel();
        mosaicImageLable = new javax.swing.JLabel();
        buttonMosaicSave = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        mosaicAdvanceOptionsPanel = new javax.swing.JPanel();
        panelMosaicOptionsHidden = new javax.swing.JPanel();
        panelMosaicOptionsShown = new javax.swing.JPanel();
        mosaicOptionsColorLayout = new javax.swing.JRadioButton();
        mosaicOptionsAutocolorcorrelogram = new javax.swing.JRadioButton();
        mosaicOptionsCedd = new javax.swing.JRadioButton();
        checkboxMosaicAdvanceOptions = new javax.swing.JCheckBox();
        labelMosaicSliderValue = new javax.swing.JLabel();
        checkboxAvoidDuplicates = new javax.swing.JCheckBox();
        optionsPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        selectboxDocumentBuilder = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        textfieldIndexName = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        textfieldNumSearchResults = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        textFieldFlickrDownloadMax = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        selectboxRerankFeature = new javax.swing.JComboBox();
        resultsCardPane = new javax.swing.JPanel();
        resultsPane = new javax.swing.JScrollPane();
        resultsTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        buttonBackToSearch = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        buttonBackToOptions = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        frameMenu = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        fileMenuExit = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        vieMenuStartPage = new javax.swing.JMenuItem();
        viewMenuResults = new javax.swing.JMenuItem();
        viewMenuOptions = new javax.swing.JMenuItem();
        devMenu = new javax.swing.JMenu();
        researchMenu = new javax.swing.JMenu();
        accSearch = new javax.swing.JMenuItem();
        ceddSearch = new javax.swing.JMenuItem();
        clSearch = new javax.swing.JMenuItem();
        ehSearch = new javax.swing.JMenuItem();
        fcthSearch = new javax.swing.JMenuItem();
        jcdSearch = new javax.swing.JMenuItem();
        jpegCoeffSearch = new javax.swing.JMenuItem();
        colorhistSearch = new javax.swing.JMenuItem();
        scSearch = new javax.swing.JMenuItem();
        rerankMenu = new javax.swing.JMenu();
        rerankFeature = new javax.swing.JMenuItem();
        rerankLsa = new javax.swing.JMenuItem();
        bovwMenu = new javax.swing.JMenu();
        indexAll = new javax.swing.JMenuItem();
        indexMissing = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuAbout = new javax.swing.JMenuItem();
        helpMenuHomepage = new javax.swing.JMenuItem();
        helpMenuWiki = new javax.swing.JMenuItem();
        helpMenuMailinglist = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        helpMenuDonate = new javax.swing.JMenuItem();

        sameSearchMenu.setText("Search again using ...");

        searchMpeg7Descriptors.setText("MPEG-7 descripors");
        searchMpeg7Descriptors.setActionCommand("all");
        searchMpeg7Descriptors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMpeg7DescriptorsActionPerformed(evt);
            }
        });
        sameSearchMenu.add(searchMpeg7Descriptors);

        searchColorLayout.setText("MPEG-7 color layout");
        searchColorLayout.setActionCommand("cl");
        searchColorLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMpeg7DescriptorsActionPerformed(evt);
            }
        });
        sameSearchMenu.add(searchColorLayout);

        searchEdgeHistogram.setText("MPEG-7 edge histogram");
        searchEdgeHistogram.setActionCommand("eh");
        searchEdgeHistogram.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMpeg7DescriptorsActionPerformed(evt);
            }
        });
        sameSearchMenu.add(searchEdgeHistogram);

        searchScalableColor.setText("MPEG-7 scalable color");
        searchScalableColor.setActionCommand("sc");
        searchScalableColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMpeg7DescriptorsActionPerformed(evt);
            }
        });
        sameSearchMenu.add(searchScalableColor);

        searchAutoColorCorrelation.setText("Auto color correlogram");
        searchAutoColorCorrelation.setActionCommand("acc");
        searchAutoColorCorrelation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMpeg7DescriptorsActionPerformed(evt);
            }
        });
        sameSearchMenu.add(searchAutoColorCorrelation);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Bundle"); // NOI18N
        setTitle(bundle.getString("liredemo.frame.titel")); // NOI18N
        setIconImages(null);

        topPane.setLayout(new java.awt.CardLayout());

        controlPane.setLayout(new java.awt.BorderLayout());

        switchButtonsPanel.setBackground(new java.awt.Color(255, 255, 255));
        switchButtonsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        switchButtonsPanel.setLayout(new java.awt.BorderLayout());

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setLayout(new java.awt.GridLayout(1, 0));

        buttonSwitchIndex.setBackground(new java.awt.Color(255, 255, 255));
        buttonSwitchIndex.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/run.png"))); // NOI18N
        buttonSwitchIndex.setText("Index");
        buttonSwitchIndex.setBorderPainted(false);
        buttonSwitchIndex.setFocusPainted(false);
        buttonSwitchIndex.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchIndex.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchIndex.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseOver(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseOut(evt);
            }
        });
        buttonSwitchIndex.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchIndex);

        buttonSwitchSearch.setBackground(new java.awt.Color(255, 255, 255));
        buttonSwitchSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/viewmag.png"))); // NOI18N
        buttonSwitchSearch.setText("Search");
        buttonSwitchSearch.setBorderPainted(false);
        buttonSwitchSearch.setFocusPainted(false);
        buttonSwitchSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseOver(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseOut(evt);
            }
        });
        buttonSwitchSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchSearch);

        buttonSwitchBrowse.setBackground(new java.awt.Color(255, 255, 255));
        buttonSwitchBrowse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/contents.png"))); // NOI18N
        buttonSwitchBrowse.setText("Browse");
        buttonSwitchBrowse.setBorderPainted(false);
        buttonSwitchBrowse.setFocusPainted(false);
        buttonSwitchBrowse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchBrowse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchBrowse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseOver(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseOut(evt);
            }
        });
        buttonSwitchBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchBrowse);

        buttonSwitchMosaic.setBackground(new java.awt.Color(254, 254, 254));
        buttonSwitchMosaic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/paint.png"))); // NOI18N
        buttonSwitchMosaic.setText("Mosaic");
        buttonSwitchMosaic.setBorderPainted(false);
        buttonSwitchMosaic.setFocusPainted(false);
        buttonSwitchMosaic.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchMosaic.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchMosaic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchMosaic);

        buttonSwitchOptions.setBackground(new java.awt.Color(255, 255, 255));
        buttonSwitchOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/configure.png"))); // NOI18N
        buttonSwitchOptions.setText("Options");
        buttonSwitchOptions.setBorderPainted(false);
        buttonSwitchOptions.setFocusPainted(false);
        buttonSwitchOptions.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchOptions.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchOptions.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseOver(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseOut(evt);
            }
        });
        buttonSwitchOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchOptions);

        buttonSwitchAbout.setBackground(new java.awt.Color(255, 255, 255));
        buttonSwitchAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/help.png"))); // NOI18N
        buttonSwitchAbout.setText("About");
        buttonSwitchAbout.setBorderPainted(false);
        buttonSwitchAbout.setFocusPainted(false);
        buttonSwitchAbout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        buttonSwitchAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        buttonSwitchAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonMouseOver(evt);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonMouseOut(evt);
            }
        });
        buttonSwitchAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSwitchIndexActionPerformed(evt);
            }
        });
        jPanel5.add(buttonSwitchAbout);

        switchButtonsPanel.add(jPanel5, java.awt.BorderLayout.WEST);

        controlPane.add(switchButtonsPanel, java.awt.BorderLayout.NORTH);

        cardPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        cardPanel.setLayout(new java.awt.CardLayout());

        textfieldIndexDir.setEditable(false);
        textfieldIndexDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textfieldIndexDirActionPerformed(evt);
            }
        });

        buttonOpenDir.setText(bundle.getString("button.open.indexdirectory")); // NOI18N
        buttonOpenDir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpenDirActionPerformed(evt);
            }
        });

        buttonStartIndexing.setText(bundle.getString("button.start.indexing")); // NOI18N
        buttonStartIndexing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStartIndexingActionPerformed(evt);
            }
        });

        progressBarIndexing.setFocusable(false);
        progressBarIndexing.setName(bundle.getString("progressbar.indexing.name")); // NOI18N
        progressBarIndexing.setString(bundle.getString("progressbar.indexing.name")); // NOI18N
        progressBarIndexing.setStringPainted(true);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel6.setText("Image Indexing:");

        jLabel8.setText("<html><b>Hints:</b>\n<ul>\n<li> JPEG images in the selected directory and all <i>subdirectories</i> will be indexed.\n<li> If you don't specify a directory and press <i>start</i> photos are downloaded from <i>Flickr</i>.Use options panel to configure how many.\n</ul>");

        checkBoxAddToExisintgIndex.setText("add to existing index");
        checkBoxAddToExisintgIndex.setToolTipText(bundle.getString("index.addToExistingIndex")); // NOI18N
        checkBoxAddToExisintgIndex.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkBoxAddToExisintgIndex.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout indexPanelLayout = new javax.swing.GroupLayout(indexPanel);
        indexPanel.setLayout(indexPanelLayout);
        indexPanelLayout.setHorizontalGroup(
                indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel6)
                        .addGroup(indexPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                                .addContainerGap())
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, indexPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(progressBarIndexing, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, indexPanelLayout.createSequentialGroup()
                                                .addComponent(textfieldIndexDir, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonOpenDir, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(indexPanelLayout.createSequentialGroup()
                                                .addComponent(checkBoxAddToExisintgIndex)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonStartIndexing, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        indexPanelLayout.setVerticalGroup(
                indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(indexPanelLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonOpenDir)
                                        .addComponent(textfieldIndexDir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressBarIndexing, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(indexPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonStartIndexing, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(checkBoxAddToExisintgIndex))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(288, 288, 288))
        );

        cardPanel.add(indexPanel, "card3");

        textfieldSearchImage.setEditable(false);
        textfieldSearchImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textfieldSearchImageActionPerformed(evt);
            }
        });

        buttonOpenImage.setText(bundle.getString("button.open.searchimage")); // NOI18N
        buttonOpenImage.setActionCommand(bundle.getString("openImageButton")); // NOI18N
        buttonOpenImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpenImageActionPerformed(evt);
            }
        });

        buttonStartSearch.setText(bundle.getString("button.start.search")); // NOI18N
        buttonStartSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStartSearchActionPerformed(evt);
            }
        });

        progressSearch.setString("Search state ...");
        progressSearch.setStringPainted(true);

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel5.setText("Search for digital images:");

        jLabel7.setText("<html>\n<b>Hints:</b>\n<ul>\n<li> Note that a double click on a row within the search results starts a new search for the clicked image.\n<li>Use Drag'n'Drop to select query image from file explorer\n</ul>\n</html>");

        javax.swing.GroupLayout searchPanelLayout = new javax.swing.GroupLayout(searchPanel);
        searchPanel.setLayout(searchPanelLayout);
        searchPanelLayout.setHorizontalGroup(
                searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel5)
                        .addGroup(searchPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(progressSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
                                        .addGroup(searchPanelLayout.createSequentialGroup()
                                                .addComponent(textfieldSearchImage, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonOpenImage))
                                        .addComponent(buttonStartSearch, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
                        .addGroup(searchPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(162, Short.MAX_VALUE))
        );
        searchPanelLayout.setVerticalGroup(
                searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(searchPanelLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(searchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(textfieldSearchImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonOpenImage))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progressSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttonStartSearch)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(288, Short.MAX_VALUE))
        );

        cardPanel.add(searchPanel, "card2");

        jLabel10.setText("Current document:");

        labelDocCount.setText("/");

        jLabel11.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel11.setText("Browse Index:");

        spinnerCurrentDocNum.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinnerCurrentDocNumStateChanged(evt);
            }
        });

        spinnerMaxDocCount.setEnabled(false);

        buttonSearchFromBrowse.setText("Search");
        buttonSearchFromBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSearchFromBrowseActionPerformed(evt);
            }
        });

        browseImageContainerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout browsePanelLayout = new javax.swing.GroupLayout(browsePanel);
        browsePanel.setLayout(browsePanelLayout);
        browsePanelLayout.setHorizontalGroup(
                browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(browsePanelLayout.createSequentialGroup()
                                .addGroup(browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel11)
                                        .addGroup(browsePanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(jLabel10)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spinnerCurrentDocNum, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(labelDocCount)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(spinnerMaxDocCount, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonSearchFromBrowse)))
                                .addGap(354, 354, 354))
                        .addComponent(browseImageContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 702, Short.MAX_VALUE)
        );
        browsePanelLayout.setVerticalGroup(
                browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(browsePanelLayout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(browsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel10)
                                        .addComponent(spinnerCurrentDocNum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(buttonSearchFromBrowse)
                                        .addComponent(spinnerMaxDocCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(labelDocCount))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(browseImageContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
        );

        cardPanel.add(browsePanel, "card5");

        textfieldMosaicImage.setEditable(false);

        buttonOpenMosaicImage.setText(bundle.getString("button.open.searchimage")); // NOI18N
        buttonOpenMosaicImage.setToolTipText(bundle.getString("mosaic.openImageButton.tooltip")); // NOI18N
        buttonOpenMosaicImage.setActionCommand(bundle.getString("openImageButton")); // NOI18N
        buttonOpenMosaicImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOpenMosaicImageActionPerformed(evt);
            }
        });

        buttonStartMosaicing.setText(bundle.getString("button.start.search")); // NOI18N
        buttonStartMosaicing.setToolTipText(bundle.getString("mosaic.startButton.tooltip")); // NOI18N
        buttonStartMosaicing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonStartMosaicingActionPerformed(evt);
            }
        });

        progressMosaic.setToolTipText(bundle.getString("mosaic.progress.tooltip")); // NOI18N
        progressMosaic.setString(bundle.getString("progressbar.mosaic.name")); // NOI18N
        progressMosaic.setStringPainted(true);

        labelMosaicTitle.setFont(new java.awt.Font("Tahoma", 1, 18));
        labelMosaicTitle.setText("Select image to create Mosaic:");

        mosaicTileCountSlider.setPaintLabels(true);
        mosaicTileCountSlider.setToolTipText(bundle.getString("mosaic.slider.tooltip")); // NOI18N
        mosaicTileCountSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                mosaicTileCountSliderStateChanged(evt);
            }
        });

        jLabel12.setText("Number of tiles:");
        jLabel12.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);

        mosaicImageLable.setBackground(new java.awt.Color(255, 255, 255));
        mosaicImageLable.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        mosaicImageLable.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        mosaicImageLable.setDoubleBuffered(true);
        mosaicImageLable.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        mosaicImageLable.setMinimumSize(new java.awt.Dimension(260, 260));

        buttonMosaicSave.setText("Save result ....");
        buttonMosaicSave.setToolTipText(bundle.getString("mosaic.saveButton.tooltip")); // NOI18N
        buttonMosaicSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMosaicSaveActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("DejaVu Sans", 2, 13));
        jLabel13.setText("Mosaic creation code was contributed by Lukas Esterle & Manuel Warum");

        mosaicAdvanceOptionsPanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout panelMosaicOptionsHiddenLayout = new javax.swing.GroupLayout(panelMosaicOptionsHidden);
        panelMosaicOptionsHidden.setLayout(panelMosaicOptionsHiddenLayout);
        panelMosaicOptionsHiddenLayout.setHorizontalGroup(
                panelMosaicOptionsHiddenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 214, Short.MAX_VALUE)
        );
        panelMosaicOptionsHiddenLayout.setVerticalGroup(
                panelMosaicOptionsHiddenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 191, Short.MAX_VALUE)
        );

        mosaicAdvanceOptionsPanel.add(panelMosaicOptionsHidden, "card2");

        mosaicButtons.add(mosaicOptionsColorLayout);
        mosaicOptionsColorLayout.setText("MPEG-7 features");
        mosaicOptionsColorLayout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mosaicOptionsColorLayoutActionPerformed(evt);
            }
        });

        mosaicButtons.add(mosaicOptionsAutocolorcorrelogram);
        mosaicOptionsAutocolorcorrelogram.setText("AutoColorCorrelogram");

        mosaicButtons.add(mosaicOptionsCedd);
        mosaicOptionsCedd.setSelected(true);
        mosaicOptionsCedd.setText("CEDD");

        javax.swing.GroupLayout panelMosaicOptionsShownLayout = new javax.swing.GroupLayout(panelMosaicOptionsShown);
        panelMosaicOptionsShown.setLayout(panelMosaicOptionsShownLayout);
        panelMosaicOptionsShownLayout.setHorizontalGroup(
                panelMosaicOptionsShownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMosaicOptionsShownLayout.createSequentialGroup()
                                .addGroup(panelMosaicOptionsShownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(panelMosaicOptionsShownLayout.createSequentialGroup()
                                                .addGap(6, 6, 6)
                                                .addComponent(mosaicOptionsColorLayout))
                                        .addGroup(panelMosaicOptionsShownLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(mosaicOptionsAutocolorcorrelogram))
                                        .addGroup(panelMosaicOptionsShownLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(mosaicOptionsCedd)))
                                .addContainerGap(75, Short.MAX_VALUE))
        );
        panelMosaicOptionsShownLayout.setVerticalGroup(
                panelMosaicOptionsShownLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(panelMosaicOptionsShownLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(mosaicOptionsColorLayout)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mosaicOptionsAutocolorcorrelogram)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mosaicOptionsCedd)
                                .addContainerGap(109, Short.MAX_VALUE))
        );

        mosaicAdvanceOptionsPanel.add(panelMosaicOptionsShown, "card3");

        checkboxMosaicAdvanceOptions.setText("advanced options");
        checkboxMosaicAdvanceOptions.setToolTipText(bundle.getString("mosaic.advancedOptions.tooltip")); // NOI18N
        checkboxMosaicAdvanceOptions.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxMosaicAdvanceOptions.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxMosaicAdvanceOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkboxMosaicAdvanceOptionsActionPerformed(evt);
            }
        });

        labelMosaicSliderValue.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        labelMosaicSliderValue.setText("50");

        checkboxAvoidDuplicates.setText("avoid duplicates in mosaic");
        checkboxAvoidDuplicates.setToolTipText("This might reduce the 'quality' of the outcome.");
        checkboxAvoidDuplicates.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        checkboxAvoidDuplicates.setMargin(new java.awt.Insets(0, 0, 0, 0));
        checkboxAvoidDuplicates.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkboxAvoidDuplicatesStateChanged(evt);
            }
        });

        javax.swing.GroupLayout mosaicPanelLayout = new javax.swing.GroupLayout(mosaicPanel);
        mosaicPanel.setLayout(mosaicPanelLayout);
        mosaicPanelLayout.setHorizontalGroup(
                mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(labelMosaicTitle)
                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel13, javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                                .addGroup(mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(mosaicImageLable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                                                        .addComponent(textfieldMosaicImage, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel12)
                                                .addGap(18, 18, 18)
                                                .addComponent(labelMosaicSliderValue, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                                        .addComponent(buttonStartMosaicing, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                        .addComponent(buttonOpenMosaicImage, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                        .addComponent(mosaicTileCountSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                        .addComponent(checkboxAvoidDuplicates)
                                        .addComponent(checkboxMosaicAdvanceOptions)
                                        .addComponent(buttonMosaicSave, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                        .addComponent(progressMosaic, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                        .addComponent(mosaicAdvanceOptionsPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))
                                .addContainerGap())
        );
        mosaicPanelLayout.setVerticalGroup(
                mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                .addGroup(mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                                .addComponent(labelMosaicTitle)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(textfieldMosaicImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(mosaicImageLable, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                                        .addGroup(mosaicPanelLayout.createSequentialGroup()
                                                .addGap(28, 28, 28)
                                                .addComponent(buttonOpenMosaicImage)
                                                .addGap(12, 12, 12)
                                                .addGroup(mosaicPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                        .addComponent(jLabel12)
                                                        .addComponent(labelMosaicSliderValue))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(mosaicTileCountSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonStartMosaicing)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(progressMosaic, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(checkboxAvoidDuplicates)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(checkboxMosaicAdvanceOptions)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(mosaicAdvanceOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 191, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(buttonMosaicSave)))
                                .addGap(6, 6, 6)
                                .addComponent(jLabel13))
        );

        cardPanel.add(mosaicPanel, "card2");

        jLabel1.setText("Type of IndexSearcher:");

        selectboxDocumentBuilder.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Color Layout (MPEG-7)", "Scalable Color (MPEG-7)", "Edge Histogram (MPEG-7)", "Auto Color Correlogram", "CEDD", "FCTH", "JCD", "RGB Color Histogram", "Tamura Texture Features", "GaborTexture Features", "JPEG Coefficients Histogram", "SURF BoVW"}));
        selectboxDocumentBuilder.setToolTipText(bundle.getString("options.tooltip.documentbuilderselection")); // NOI18N
        selectboxDocumentBuilder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectboxDocumentBuilderActionPerformed(evt);
            }
        });

        jLabel2.setText("Use index directory:");

        textfieldIndexName.setText("index");
        textfieldIndexName.setToolTipText(bundle.getString("options.tooltip.indexname")); // NOI18N

        jLabel3.setText("Number of search results:");

        textfieldNumSearchResults.setText("100");
        textfieldNumSearchResults.setToolTipText(bundle.getString("options.tooltip.numsearchresults")); // NOI18N

        jLabel4.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel4.setText("Options:");

        jLabel17.setText("Flickr download maximum:");

        textFieldFlickrDownloadMax.setText("100");
        textFieldFlickrDownloadMax.setToolTipText("If no directory to index is given images can be downloaded from Flickr. This is the maximum of images retrieved in one batch.");

        jLabel14.setText("Re-ranking feature:");

        selectboxRerankFeature.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Color Layout (MPEG-7)", "Scalable Color (MPEG-7)", "Edge Histogram (MPEG-7)", "Auto Color Correlogram", "CEDD", "FCTH", "JCD", "RGB Color Histogram", "Tamura Texture Features", "GaborTexture Features", "JPEG Coefficients Histogram"}));

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
                optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionsPanelLayout.createSequentialGroup()
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel4)
                                        .addGroup(optionsPanelLayout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(jLabel1)
                                                        .addComponent(jLabel2)
                                                        .addComponent(jLabel3)
                                                        .addComponent(jLabel17)
                                                        .addComponent(jLabel14))
                                                .addGap(14, 14, 14)
                                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(selectboxRerankFeature, 0, 544, Short.MAX_VALUE)
                                                        .addComponent(textfieldNumSearchResults, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                                                        .addComponent(selectboxDocumentBuilder, javax.swing.GroupLayout.Alignment.LEADING, 0, 544, Short.MAX_VALUE)
                                                        .addComponent(textfieldIndexName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
                                                        .addComponent(textFieldFlickrDownloadMax, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE))))
                                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
                optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(optionsPanelLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(selectboxDocumentBuilder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(textfieldIndexName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(textfieldNumSearchResults, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel17)
                                        .addComponent(textFieldFlickrDownloadMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(selectboxRerankFeature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel14))
                                .addContainerGap(307, Short.MAX_VALUE))
        );

        cardPanel.add(optionsPanel, "card4");

        controlPane.add(cardPanel, java.awt.BorderLayout.CENTER);

        topPane.add(controlPane, "card2");

        resultsCardPane.setLayout(new java.awt.BorderLayout());

        resultsTable.setModel(tableModel);
        resultsTable.setToolTipText(bundle.getString("table.tooltip")); // NOI18N
        resultsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultsTableMouseClicked(evt);
            }
        });
        resultsPane.setViewportView(resultsTable);

        resultsCardPane.add(resultsPane, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jPanel2.setLayout(new java.awt.BorderLayout());

        buttonBackToSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/viewmag16.png"))); // NOI18N
        buttonBackToSearch.setToolTipText("Go back to search ...");
        buttonBackToSearch.setBorderPainted(false);
        buttonBackToSearch.setFocusPainted(false);
        buttonBackToSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBackToSearchActionPerformed(evt);
            }
        });
        jPanel2.add(buttonBackToSearch, java.awt.BorderLayout.EAST);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 18));
        jLabel9.setText("Search Results");
        jLabel9.setComponentPopupMenu(sameSearchMenu.getPopupMenu());
        jPanel2.add(jLabel9, java.awt.BorderLayout.WEST);

        jPanel1.setLayout(new java.awt.BorderLayout());

        buttonBackToOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/configure16.png"))); // NOI18N
        buttonBackToOptions.setToolTipText("Go  to options ...");
        buttonBackToOptions.setBorderPainted(false);
        buttonBackToOptions.setFocusPainted(false);
        buttonBackToOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonBackToOptionsActionPerformed(evt);
            }
        });
        jPanel1.add(buttonBackToOptions, java.awt.BorderLayout.EAST);

        jPanel3.setLayout(new java.awt.BorderLayout());

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/filter16.png"))); // NOI18N
        jButton1.setToolTipText("Filter search results.");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerankFeatureActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanel3, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel1, java.awt.BorderLayout.CENTER);

        resultsCardPane.add(jPanel2, java.awt.BorderLayout.NORTH);

        topPane.add(resultsCardPane, "card4");

        getContentPane().add(topPane, java.awt.BorderLayout.CENTER);

        fileMenu.setText(bundle.getString("menu.file")); // NOI18N

        fileMenuExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_MASK));
        fileMenuExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/cancel16.png"))); // NOI18N
        fileMenuExit.setText(bundle.getString("menu.file.exit")); // NOI18N
        fileMenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuExitActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuExit);

        frameMenu.add(fileMenu);

        viewMenu.setText("View");

        vieMenuStartPage.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK));
        vieMenuStartPage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/run16.png"))); // NOI18N
        vieMenuStartPage.setText("Start page");
        vieMenuStartPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vieMenuStartPageActionPerformed(evt);
            }
        });
        viewMenu.add(vieMenuStartPage);

        viewMenuResults.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK));
        viewMenuResults.setText("Results");
        viewMenuResults.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuResultsActionPerformed(evt);
            }
        });
        viewMenu.add(viewMenuResults);

        viewMenuOptions.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.ALT_MASK));
        viewMenuOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/configure16.png"))); // NOI18N
        viewMenuOptions.setText("Options");
        viewMenuOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuOptionsActionPerformed(evt);
            }
        });
        viewMenu.add(viewMenuOptions);

        frameMenu.add(viewMenu);

        devMenu.setText("Developer");

        researchMenu.setText("Re-do search with ...");
        researchMenu.setEnabled(false);

        accSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.ALT_MASK));
        accSearch.setText("Auto Color Correlogram");
        accSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithAcc(evt);
            }
        });
        researchMenu.add(accSearch);

        ceddSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.ALT_MASK));
        ceddSearch.setText("CEDD");
        ceddSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithCedd(evt);
            }
        });
        researchMenu.add(ceddSearch);

        clSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.ALT_MASK));
        clSearch.setText("Color Layout");
        clSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithColorLayout(evt);
            }
        });
        researchMenu.add(clSearch);

        ehSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.ALT_MASK));
        ehSearch.setText("Edge Histogram");
        ehSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithEdgeHistogram(evt);
            }
        });
        researchMenu.add(ehSearch);

        fcthSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.ALT_MASK));
        fcthSearch.setText("FCTH");
        fcthSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithFCTH(evt);
            }
        });
        researchMenu.add(fcthSearch);

        jcdSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_6, java.awt.event.InputEvent.ALT_MASK));
        jcdSearch.setText("JCD");
        jcdSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithJcd(evt);
            }
        });
        researchMenu.add(jcdSearch);

        jpegCoeffSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_7, java.awt.event.InputEvent.ALT_MASK));
        jpegCoeffSearch.setText("JPEG Coefficient Histogram");
        jpegCoeffSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithJpegCoeffs(evt);
            }
        });
        researchMenu.add(jpegCoeffSearch);

        colorhistSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_8, java.awt.event.InputEvent.ALT_MASK));
        colorhistSearch.setText("RGB Color Histogram");
        colorhistSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithColorHist(evt);
            }
        });
        researchMenu.add(colorhistSearch);

        scSearch.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_9, java.awt.event.InputEvent.ALT_MASK));
        scSearch.setText("Scalable Color");
        scSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchWithScalableColor(evt);
            }
        });
        researchMenu.add(scSearch);

        devMenu.add(researchMenu);

        rerankMenu.setText("Re-rank results ...");

        rerankFeature.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/filter16.png"))); // NOI18N
        rerankFeature.setText("Using a different feature");
        rerankFeature.setEnabled(false);
        rerankFeature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerankFeatureActionPerformed(evt);
            }
        });
        rerankMenu.add(rerankFeature);

        rerankLsa.setText("Using Latent Semantic Analysis");
        rerankLsa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rerankLsaActionPerformed(evt);
            }
        });
        rerankMenu.add(rerankLsa);

        devMenu.add(rerankMenu);

        bovwMenu.setText("Bag of visual words");

        indexAll.setText("Index all");
        indexAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexAllActionPerformed(evt);
            }
        });
        bovwMenu.add(indexAll);

        indexMissing.setText("Index missing");
        indexMissing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexMissingActionPerformed(evt);
            }
        });
        bovwMenu.add(indexMissing);

        devMenu.add(bovwMenu);

        frameMenu.add(devMenu);

        helpMenu.setText(bundle.getString("menu.help")); // NOI18N

        helpMenuAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        helpMenuAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/help16.png"))); // NOI18N
        helpMenuAbout.setText(bundle.getString("menu.help.about")); // NOI18N
        helpMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuAboutActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuAbout);

        helpMenuHomepage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/browser16.png"))); // NOI18N
        helpMenuHomepage.setText("Homepage");
        helpMenuHomepage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuHomepageActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuHomepage);

        helpMenuWiki.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/editcopy16.png"))); // NOI18N
        helpMenuWiki.setText("Developer Wiki");
        helpMenuWiki.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuWikiActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuWiki);

        helpMenuMailinglist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/mail_generic16.png"))); // NOI18N
        helpMenuMailinglist.setText("Mailing List");
        helpMenuMailinglist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuMailinglistActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuMailinglist);
        helpMenu.add(jSeparator1);

        helpMenuDonate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/money16.png"))); // NOI18N
        helpMenuDonate.setText("Donate");
        helpMenuDonate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuDonateActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuDonate);

        frameMenu.add(helpMenu);

        setJMenuBar(frameMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void checkboxAvoidDuplicatesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_checkboxAvoidDuplicatesStateChanged
        // TODO: set / unset in Engine.
        Engine.setAvoidDuplicateTileImages(checkboxAvoidDuplicates.isSelected());
    }//GEN-LAST:event_checkboxAvoidDuplicatesStateChanged

    private void mosaicTileCountSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_mosaicTileCountSliderStateChanged
        labelMosaicSliderValue.setText(mosaicTileCountSlider.getValue() + "");
    }//GEN-LAST:event_mosaicTileCountSliderStateChanged

    private void checkboxMosaicAdvanceOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkboxMosaicAdvanceOptionsActionPerformed
        CardLayout cl = (CardLayout) mosaicAdvanceOptionsPanel.getLayout();
        if (checkboxMosaicAdvanceOptions.isSelected()) {
            cl.last(mosaicAdvanceOptionsPanel);
        } else {
            cl.first(mosaicAdvanceOptionsPanel);
        }
    }//GEN-LAST:event_checkboxMosaicAdvanceOptionsActionPerformed

    private void buttonMosaicSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMosaicSaveActionPerformed
        try {
            JFileChooser saveIt = new JFileChooser("Save image as PNG ...");
            if (saveIt.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(new java.io.File("result.png")), "png", new java.io.FileOutputStream(saveIt.getSelectedFile()));
            }
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_buttonMosaicSaveActionPerformed

    private void buttonStartMosaicingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartMosaicingActionPerformed
        try {
            if (!IndexReader.indexExists(open(new File(textfieldIndexName.getText())))) {
                JOptionPane.showMessageDialog(this, "Did not find existing index!\n"
                        + "Use the \"Index\" function to create a new one.", "Error", JOptionPane.ERROR_MESSAGE);
            } else if (textfieldMosaicImage.getText().length() > 4) {
                mosaicImage();
            } else {
                JOptionPane.showMessageDialog(this, "Please select an image to create mosaic first.\n"
                        + "Use the \"Open image ...\" button to do this.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }//GEN-LAST:event_buttonStartMosaicingActionPerformed

    private void buttonOpenMosaicImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpenMosaicImageActionPerformed
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Select image to create mosaic ...");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JPG, PNG & GIF Images", "jpg", "gif", "png");
        jfc.setFileFilter(filter);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                textfieldMosaicImage.setText(jfc.getSelectedFile().getCanonicalPath());
                BufferedImage img = ImageIO.read(new java.io.FileInputStream(textfieldMosaicImage.getText()));
                mosaicTileCountSlider.setMinimum(2);
                mosaicTileCountSlider.setValue(Math.min(img.getWidth() / 8, 10));
                mosaicImageLable.setIcon(new ImageIcon(ImageUtils.scaleImage(img, 256)));
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_buttonOpenMosaicImageActionPerformed

    private void buttonSearchFromBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSearchFromBrowseActionPerformed
        int docID = ((Integer) spinnerCurrentDocNum.getValue()).intValue();
        if (docID < 0 || docID > browseReader.maxDoc()) ;
        try {
            searchForDocument(browseReader.document(docID));
        } catch (Exception e) {
            System.err.println(e);
        }
    }//GEN-LAST:event_buttonSearchFromBrowseActionPerformed

    private void spinnerCurrentDocNumStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinnerCurrentDocNumStateChanged
        setDocumentImageIcon(((Integer) spinnerCurrentDocNum.getValue()).intValue());
    }//GEN-LAST:event_spinnerCurrentDocNumStateChanged

    private void searchMpeg7DescriptorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMpeg7DescriptorsActionPerformed
        String command = evt.getActionCommand();
        if (command.equals("all")) {
            selectboxDocumentBuilder.setSelectedIndex(0);
        } else if (command.equals("eh")) {
            selectboxDocumentBuilder.setSelectedIndex(2);
        } else if (command.equals("sc")) {
            selectboxDocumentBuilder.setSelectedIndex(1);
        } else if (command.equals("cl")) {
            selectboxDocumentBuilder.setSelectedIndex(3);
        } else if (command.equals("acc")) {
            selectboxDocumentBuilder.setSelectedIndex(4);
        }
        // search for document in selected row ...
        int row = 0;
        if (resultsTable.getSelectedRow() > -1) {
            row = resultsTable.getSelectedRow();
        }
        searchForDocument(row);
    }//GEN-LAST:event_searchMpeg7DescriptorsActionPerformed

    private void buttonBackToSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBackToSearchActionPerformed
        ((CardLayout) topPane.getLayout()).first(topPane);
    }//GEN-LAST:event_buttonBackToSearchActionPerformed

    private void viewMenuResultsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMenuResultsActionPerformed
        ((CardLayout) topPane.getLayout()).last(topPane);
    }//GEN-LAST:event_viewMenuResultsActionPerformed

    private void vieMenuStartPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vieMenuStartPageActionPerformed
        ((CardLayout) topPane.getLayout()).first(topPane);
    }//GEN-LAST:event_vieMenuStartPageActionPerformed

    private void buttonSwitchIndexActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSwitchIndexActionPerformed
        JButton button = (JButton) evt.getSource();
        if (!button.equals(buttonSwitchAbout)) {
            resetColor();
            button.setBackground(highlightSelectColor);
            CardLayout cl = (CardLayout) cardPanel.getLayout();
            cl.first(cardPanel);
            if (button.equals(buttonSwitchOptions)) {
                cl.last(cardPanel);
            } else if (button.equals(buttonSwitchMosaic)) {
                cl.next(cardPanel);
                cl.next(cardPanel);
                cl.next(cardPanel);
            } else if (button.equals(buttonSwitchBrowse)) {
                cl.next(cardPanel);
                cl.next(cardPanel);
                initReader();
            } else if (button.equals(buttonSwitchSearch)) {
                cl.next(cardPanel);
            }
        } else {
            showAbout();
        }
    }//GEN-LAST:event_buttonSwitchIndexActionPerformed

    private void initReader() {
        try {
            if (browseReader == null) {
                browseReader = org.apache.lucene.index.IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())));
            } else {
                browseReader.close();
                browseReader = org.apache.lucene.index.IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())));
            }
            spinnerMaxDocCount.setValue(browseReader.maxDoc());
            setDocumentImageIcon(((Integer) spinnerCurrentDocNum.getValue()).intValue());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not open index. Please ensure that an index has been created.", "Error opening index", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setDocumentImageIcon(int docID) {
        if (docID < 0) {
            return;
        }
        if (docID > browseReader.maxDoc()) {
            return;
        }
        try {
            Document d = browseReader.document(docID);
            BufferedImage img = null;
            String file = d.getFieldable(net.semanticmetadata.lire.DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
            if (!file.startsWith("http:")) {
                img = ImageIO.read(new java.io.FileInputStream(file));
            } else {
                img = ImageIO.read(new URL(file));
            }
            browseImagePanel.setImage(img);
            browseImagePanel.repaint();
        } catch (Exception e) {
            JOptionPane.showConfirmDialog(this, "Error loading image:\n" + e.toString(), "An error occurred", JOptionPane.ERROR_MESSAGE);
            System.err.println(e.toString());
        }

    }

    private void resetColor() {
        buttonSwitchIndex.setBackground(Color.white);
        buttonSwitchSearch.setBackground(Color.white);
        buttonSwitchOptions.setBackground(Color.white);
        buttonSwitchBrowse.setBackground(Color.white);
        buttonSwitchMosaic.setBackground(Color.white);
    }

    private void buttonMouseOut(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseOut
        JButton button = (JButton) evt.getSource();
        if (button.getBackground().equals(highlightHoverColor)) {
            button.setBackground(Color.white);
        }
    }//GEN-LAST:event_buttonMouseOut

    private void buttonMouseOver(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_buttonMouseOver
        JButton button = (JButton) evt.getSource();
        if (button.getBackground().equals(Color.white)) {
            button.setBackground(highlightHoverColor);
        }
    }//GEN-LAST:event_buttonMouseOver

    private void resultsTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsTableMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int imageID = resultsTable.rowAtPoint(evt.getPoint()) * 3 + resultsTable.columnAtPoint(evt.getPoint());
            if (imageID >= 0 && imageID < tableModel.getHits().length()) {
                String file = tableModel.getHits().doc(imageID).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();

                try {
                    Desktop.getDesktop().open(new File(file));
                } catch (IOException ex) {
                    Logger.getLogger(LireDemoFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (evt.getClickCount() == 2) {
            searchForDocument(resultsTable.getSelectedRow() * 3 + resultsTable.getSelectedColumn());
        }
    }//GEN-LAST:event_resultsTableMouseClicked

    private void searchForDocument(int tableRow) {
        searchForDocument(tableModel.getHits().doc(tableRow));
    }

    private void mosaicImage() {
        final int numTiles = mosaicTileCountSlider.getValue();
        progressMosaic.setString("Running ...");
        enableMosaicControls(false);
        final ProgressMonitor progressMonitor = new ProgressMonitor(progressMosaic);
        Runnable r = new Runnable() {

            public void run() {
                try {
                    float colorhist = 0f;
                    float colordist = 0f;
                    float texture = 0f;
                    if (mosaicOptionsAutocolorcorrelogram.isSelected()) {
                        texture = 1f;
                    } else if (mosaicOptionsCedd.isSelected()) {
                        colorhist = 1f;
                    } else {
                        colordist = 1f;
                    }
                    ImageFunctions iFunc = new ImageFunctions();
                    BufferedImage mosaic = iFunc.getMosaic(javax.imageio.ImageIO.read(new java.io.FileInputStream(textfieldMosaicImage.getText())),
                            new java.awt.Dimension(320, 320), new java.awt.Dimension(numTiles, numTiles),
                            colorhist,
                            colordist,
                            texture, textfieldIndexName.getText(),
                            progressMonitor);
                    mosaicImageLable.setIcon(new ImageIcon(ImageUtils.scaleImage(mosaic, Math.min(mosaicImageLable.getSize().width, mosaicImageLable.getSize().height))));
                    ImageIO.write(mosaic, "png", new FileOutputStream("result.png"));
                    progressMosaic.setString("Finished");
                    enableMosaicControls(true);
                } catch (IOException ex) {
                    Logger.getLogger("global").log(Level.SEVERE, null, ex);
                }
            }
        };
        new Thread(r).start();
    }

    private void enableMosaicControls(boolean enable) {
        buttonOpenMosaicImage.setEnabled(enable);
        buttonStartMosaicing.setEnabled(enable);
        mosaicTileCountSlider.setEnabled(enable);
        buttonMosaicSave.setEnabled(enable);
        checkboxAvoidDuplicates.setEnabled(enable);
        checkboxMosaicAdvanceOptions.setEnabled(enable);
        mosaicOptionsAutocolorcorrelogram.setEnabled(enable);
        mosaicOptionsCedd.setEnabled(enable);
        mosaicOptionsColorLayout.setEnabled(enable);
        //spinnerMosaicOptionCl.setEnabled(enable);
        //spinnerMosaicOptionEh.setEnabled(enable);
        //spinnerMosaicOptionSc.setEnabled(enable);
    }

    private void searchForDocument(Document d) {
        final Document myDoc = d;
        // setting to search panel:
        ((CardLayout) cardPanel.getLayout()).first(cardPanel);
        ((CardLayout) cardPanel.getLayout()).next(cardPanel);
        // switching away from search results ...
        ((CardLayout) topPane.getLayout()).first(topPane);
        resultsTable.setEnabled(false);
        final JPanel frame = topPane;
        Thread t = new Thread() {

            public void run() {
                try {
                    progressSearch.setValue(0);
                    IndexReader reader = IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())), true);
                    ImageSearcher searcher = getSearcher();
                    // System.out.println(searcher.getClass().getName() + " " + searcher.toString());
                    progressSearch.setString("Searching for matching images: " + searcher.getClass().getName());
                    ImageSearchHits hits = searcher.search(myDoc, reader);
                    tableModel.setHits(hits, progressSearch);
                    reader.close();
                    // scroll to first row:
                    Rectangle bounds = resultsTable.getCellRect(0, 0, true);
                    resultsPane.getViewport().setViewPosition(bounds.getLocation());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "An error occurred while searching: " + e.getMessage(), "Error while searching", JOptionPane.ERROR_MESSAGE);

                } finally {
                    resultsTable.setRowHeight(220);
                    // resultsTable.getColumnModel().getColumn(0).setMaxWidth(64);
                    // resultsTable.getColumnModel().getColumn(0).setMinWidth(64);
                    // resultsTable.getColumnModel().getColumn(1).setMaxWidth(150);
                    // resultsTable.getColumnModel().getColumn(1).setMinWidth(150);
                    resultsTable.setShowGrid(false);
                    resultsTable.setTableHeader(null);
                    ((CardLayout) topPane.getLayout()).last(frame);
                    resultsTable.setEnabled(true);
                    // enable the menu for searching & re-ranking using alternative descriptors.
                    researchMenu.setEnabled(true);
                    rerankFeature.setEnabled(true);
                }
            }
        };
        t.start();
    }

    private void buttonStartSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartSearchActionPerformed
        if (textfieldSearchImage.getText().length() > 4) {
            try {
                searchForImage(textfieldSearchImage.getText());
            } catch (FileNotFoundException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }

        } else {
            JOptionPane.showMessageDialog(this, "Please select a query image first.\n"
                    + "Use the \"Open image ...\" button to do this.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_buttonStartSearchActionPerformed

    private void buttonStartIndexingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonStartIndexingActionPerformed
        if (textfieldIndexDir.getText().length() > 1) {
            IndexingThread t = new IndexingThread(this);
            buttonStartIndexing.setEnabled(false);
            t.start();
        } else {
            int result = JOptionPane.showConfirmDialog(this, "You did not specify images to index.\n"
                    + "Should LireDemo download random Flickr images for indexing?.\n"
                    + "Note that this is rather slow an consumes a lot of bandwidth.");
            if (result == JOptionPane.OK_OPTION) {
                FlickrIndexingThread t = new FlickrIndexingThread(this, Integer.parseInt(textFieldFlickrDownloadMax.getText()));
                buttonStartIndexing.setEnabled(false);
                t.start();
            }

//            JOptionPane.showMessageDialog(this, "Please select a directory to index first.\n" +
//                    "At least some digital image should be found there.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_buttonStartIndexingActionPerformed

    private void buttonOpenDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpenDirActionPerformed
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Select directory to index ...");
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                textfieldIndexDir.setText(jfc.getSelectedFile().getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_buttonOpenDirActionPerformed

    private void helpMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuAboutActionPerformed
        JOptionPane.showMessageDialog(this, "<html><center><b>Simple demo for Lucene Image Retrieval (LIRe) library.</b><br>"
                + "<br>Visit http://www.semanticmetadata.net/lire for more information.<br>"
                + "<br>&copy; 2007-2011 by Mathias Lux<br>"
                + "mathias@juggle.at<br></center></html>",
                "About LIRe demo", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_helpMenuAboutActionPerformed

    private void showAbout() {
        JOptionPane.showMessageDialog(this, "<html><center><b>Simple demo for<br>Lucene Image Retrieval (LIRe) library.</b><br>"
                + "<br>Visit http://www.semanticmetadata.net/lire<br>for more information.<br>"
                + "<br>&copy; 2007-2011 by Mathias Lux<br>"
                + "mathias@juggle.at<br></center></html>",
                "About LIRe demo", JOptionPane.PLAIN_MESSAGE);
    }

    private void fileMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_fileMenuExitActionPerformed

    private void buttonOpenImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOpenImageActionPerformed
        JFileChooser jfc = new JFileChooser(".");
        jfc.setDialogTitle("Select image to search for ...");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "JPG, PNG & GIF Images", "jpg", "gif", "png");
        jfc.setFileFilter(filter);
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                textfieldSearchImage.setText(jfc.getSelectedFile().getCanonicalPath());
            } catch (IOException ex) {
                Logger.getLogger("global").log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_buttonOpenImageActionPerformed

    private void selectboxDocumentBuilderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectboxDocumentBuilderActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_selectboxDocumentBuilderActionPerformed

    private void textfieldIndexDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfieldIndexDirActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_textfieldIndexDirActionPerformed

    private void mosaicOptionsColorLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mosaicOptionsColorLayoutActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_mosaicOptionsColorLayoutActionPerformed

    private void buttonBackToOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonBackToOptionsActionPerformed
        // change to options tab ...
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.first(cardPanel);
        cl.last(cardPanel);
        // hide results ...
        ((CardLayout) topPane.getLayout()).first(topPane);
    }//GEN-LAST:event_buttonBackToOptionsActionPerformed

    private void viewMenuOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMenuOptionsActionPerformed
// TODO add your handling code here:
        buttonBackToOptionsActionPerformed(evt);
    }//GEN-LAST:event_viewMenuOptionsActionPerformed

    private void searchWithJcd(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithJcd
        selectboxDocumentBuilder.setSelectedIndex(6);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithJcd

    private void searchWithAcc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithAcc
        selectboxDocumentBuilder.setSelectedIndex(3);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithAcc

    private void searchWithCedd(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithCedd
        selectboxDocumentBuilder.setSelectedIndex(4);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithCedd

    private void searchWithFCTH(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithFCTH
        selectboxDocumentBuilder.setSelectedIndex(5);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithFCTH

    private void searchWithColorHist(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithColorHist
        selectboxDocumentBuilder.setSelectedIndex(7);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithColorHist

    private void searchWithColorLayout(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithColorLayout
        selectboxDocumentBuilder.setSelectedIndex(0);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithColorLayout

    private void searchWithEdgeHistogram(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithEdgeHistogram
        selectboxDocumentBuilder.setSelectedIndex(2);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithEdgeHistogram

    private void searchWithJpegCoeffs(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithJpegCoeffs
        selectboxDocumentBuilder.setSelectedIndex(10);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithJpegCoeffs

    private void searchWithScalableColor(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchWithScalableColor
        selectboxDocumentBuilder.setSelectedIndex(1);
        searchForDocument(0);
    }//GEN-LAST:event_searchWithScalableColor

    private void rerankFeatureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rerankFeatureActionPerformed
        RerankFilter filter = null;
        filter = new RerankFilter(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
        if (selectboxRerankFeature.getSelectedIndex() == 1) { // ScalableColor
            filter = new RerankFilter(ScalableColor.class, DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
        } else if (selectboxRerankFeature.getSelectedIndex() == 2) { // EdgeHistogram
            filter = new RerankFilter(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
        } else if (selectboxRerankFeature.getSelectedIndex() == 3) { // ACC
            filter = new RerankFilter(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        } else if (selectboxRerankFeature.getSelectedIndex() == 4) { // CEDD
            filter = new RerankFilter(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
        } else if (selectboxRerankFeature.getSelectedIndex() == 5) { // FCTH
            filter = new RerankFilter(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
        } else if (selectboxRerankFeature.getSelectedIndex() == 6) { // JCD
            filter = new RerankFilter(JCD.class, DocumentBuilder.FIELD_NAME_JCD);
        } else if (selectboxRerankFeature.getSelectedIndex() == 7) { // SimpleColorHistogram
            filter = new RerankFilter(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        } else if (selectboxRerankFeature.getSelectedIndex() == 8) { // Tamura
            filter = new RerankFilter(Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
        } else if (selectboxRerankFeature.getSelectedIndex() == 9) { // Gabor
            filter = new RerankFilter(Gabor.class, DocumentBuilder.FIELD_NAME_GABOR);
        } else if (selectboxRerankFeature.getSelectedIndex() >= 10) {  // JpegCoeffs
            filter = new RerankFilter(JpegCoefficientHistogram.class, DocumentBuilder.FIELD_NAME_JPEGCOEFFS);
        }
        tableModel.setHits(filter.filter(tableModel.hits, tableModel.hits.doc(0)), null);
    }//GEN-LAST:event_rerankFeatureActionPerformed

    private void helpMenuHomepageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuHomepageActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("http://www.semanticmetadata.net/lire"));
        } catch (Exception ex) {
            Logger.getLogger(LireDemoFrame.class.getName()).log(Level.INFO, null, ex);
        }
    }//GEN-LAST:event_helpMenuHomepageActionPerformed

    private void helpMenuWikiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuWikiActionPerformed
        // http://www.semanticmetadata.net/wiki/doku.php?id=start
        try {
            Desktop.getDesktop().browse(new URI("http://www.semanticmetadata.net/wiki/doku.php?id=start"));
        } catch (Exception ex) {
            Logger.getLogger(LireDemoFrame.class.getName()).log(Level.INFO, null, ex);
        }
    }//GEN-LAST:event_helpMenuWikiActionPerformed

    private void helpMenuMailinglistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuMailinglistActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("http://groups.google.com/group/lire-dev"));
        } catch (Exception ex) {
            Logger.getLogger(LireDemoFrame.class.getName()).log(Level.INFO, null, ex);
        }
    }//GEN-LAST:event_helpMenuMailinglistActionPerformed

    private void helpMenuDonateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuDonateActionPerformed
        try {
            Desktop.getDesktop().browse(new URI("https://sourceforge.net/donate/index.php?group_id=105915"));
        } catch (Exception ex) {
            Logger.getLogger(LireDemoFrame.class.getName()).log(Level.INFO, null, ex);
        }
    }//GEN-LAST:event_helpMenuDonateActionPerformed

    private void rerankLsaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rerankLsaActionPerformed
        LsaFilter filter = null;
        filter = new LsaFilter(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
        if (selectboxDocumentBuilder.getSelectedIndex() == 1) { // ScalableColor
            filter = new LsaFilter(ScalableColor.class, DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 2) { // EdgeHistogram
            filter = new LsaFilter(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 3) { // ACC
            filter = new LsaFilter(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 4) { // CEDD
            filter = new LsaFilter(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 5) { // FCTH
            filter = new LsaFilter(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 6) { // JCD
            filter = new LsaFilter(JCD.class, DocumentBuilder.FIELD_NAME_JCD);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 7) { // SimpleColorHistogram
            filter = new LsaFilter(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 8) { // Tamura
            filter = new LsaFilter(Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 9) { // Gabor
            filter = new LsaFilter(Gabor.class, DocumentBuilder.FIELD_NAME_GABOR);
        } else if (selectboxDocumentBuilder.getSelectedIndex() >= 10) {  // JpegCoeffs
            filter = new LsaFilter(JpegCoefficientHistogram.class, DocumentBuilder.FIELD_NAME_JPEGCOEFFS);
        }

        tableModel.setHits(filter.filter(tableModel.hits, tableModel.hits.doc(0)), null);
    }//GEN-LAST:event_rerankLsaActionPerformed

    private void indexAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexAllActionPerformed
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())), true);
            final SurfFeatureHistogramBuilder builder = new SurfFeatureHistogramBuilder(reader);
            builder.setProgressMonitor(new javax.swing.ProgressMonitor(this, "Progress of BoVW indexing (~)", "", 0, 100));
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        builder.index();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_indexAllActionPerformed

    private void indexMissingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexMissingActionPerformed
        try {
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())), true);
            SurfFeatureHistogramBuilder builder = new SurfFeatureHistogramBuilder(reader, reader.maxDoc() / 10, 2000);
            builder.indexMissing();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }//GEN-LAST:event_indexMissingActionPerformed

    private void textfieldSearchImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textfieldSearchImageActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textfieldSearchImageActionPerformed

    private void searchForImage(String imagePath) throws FileNotFoundException, IOException {
        // setting to search panel:
        ((CardLayout) cardPanel.getLayout()).first(cardPanel);
        ((CardLayout) cardPanel.getLayout()).next(cardPanel);
        // switching away from search results ...
        ((CardLayout) topPane.getLayout()).first(topPane);
        final String path = imagePath;
        final JPanel frame = topPane;
        Thread t = new Thread() {

            public void run() {
                try {
                    IndexReader reader = IndexReader.open(FSDirectory.open(new File(textfieldIndexName.getText())), true);
                    int numDocs = reader.numDocs();
                    System.out.println("numDocs = " + numDocs);
                    ImageSearcher searcher = getSearcher();
                    ImageSearchHits hits = searcher.search(ImageIO.read(new FileInputStream(path)), reader);
                    tableModel.setHits(hits, progressSearch);
                    reader.close();
                    Rectangle bounds = resultsTable.getCellRect(0, 0, true);
                    resultsPane.getViewport().setViewPosition(bounds.getLocation());
                } catch (Exception e) {
                    // Nothing to do here ....
                } finally {
                    resultsTable.setRowHeight(150);
                    // resultsTable.getColumnModel().getColumn(0).setMaxWidth(64);
                    // resultsTable.getColumnModel().getColumn(0).setMinWidth(64);
                    resultsTable.getColumnModel().getColumn(1).setMaxWidth(150);
                    resultsTable.getColumnModel().getColumn(1).setMinWidth(150);
                    ((CardLayout) topPane.getLayout()).last(frame);
                    resultsTable.setEnabled(true);
                }
            }
        };
        t.start();

    }

    private ImageSearcher getSearcher() {
        int numResults = 50;
        try {
            numResults = Integer.parseInt(textfieldNumSearchResults.getText());
        } catch (Exception e) {
            // nothing to do ...
        }
        ImageSearcher searcher = ImageSearcherFactory.createColorLayoutImageSearcher(numResults);
        if (selectboxDocumentBuilder.getSelectedIndex() == 1) {
            searcher = ImageSearcherFactory.createScalableColorImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 2) {
            searcher = ImageSearcherFactory.createEdgeHistogramImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 3) {
            searcher = ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 4) { // CEDD
            searcher = ImageSearcherFactory.createCEDDImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 5) { // FCTH
            searcher = ImageSearcherFactory.createFCTHImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 6) { // JCD
            searcher = ImageSearcherFactory.createJCDImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 7) { // SimpleColorHistogram
            searcher = ImageSearcherFactory.createColorHistogramImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 8) {
            searcher = ImageSearcherFactory.createTamuraImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 9) {
            searcher = ImageSearcherFactory.createGaborImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() == 10) {
            searcher = ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(numResults);
        } else if (selectboxDocumentBuilder.getSelectedIndex() > 10) {
            searcher = new VisualWordsImageSearcher(numResults, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        }
        return searcher;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new LireDemoFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem accSearch;
    private javax.swing.JMenu bovwMenu;
    private javax.swing.JPanel browseImageContainerPanel;
    private liredemo.ImagePanel browseImagePanel;
    private javax.swing.JPanel browsePanel;
    private javax.swing.JButton buttonBackToOptions;
    private javax.swing.JButton buttonBackToSearch;
    private javax.swing.JButton buttonMosaicSave;
    private javax.swing.JButton buttonOpenDir;
    private javax.swing.JButton buttonOpenImage;
    private javax.swing.JButton buttonOpenMosaicImage;
    private javax.swing.JButton buttonSearchFromBrowse;
    public javax.swing.JButton buttonStartIndexing;
    private javax.swing.JButton buttonStartMosaicing;
    private javax.swing.JButton buttonStartSearch;
    private javax.swing.JButton buttonSwitchAbout;
    private javax.swing.JButton buttonSwitchBrowse;
    private javax.swing.JButton buttonSwitchIndex;
    private javax.swing.JButton buttonSwitchMosaic;
    private javax.swing.JButton buttonSwitchOptions;
    private javax.swing.JButton buttonSwitchSearch;
    private javax.swing.JPanel cardPanel;
    private javax.swing.JMenuItem ceddSearch;
    public javax.swing.JCheckBox checkBoxAddToExisintgIndex;
    private javax.swing.JCheckBox checkboxAvoidDuplicates;
    private javax.swing.JCheckBox checkboxMosaicAdvanceOptions;
    private javax.swing.JMenuItem clSearch;
    private javax.swing.JMenuItem colorhistSearch;
    private javax.swing.JPanel controlPane;
    private javax.swing.JMenu devMenu;
    private javax.swing.JMenuItem ehSearch;
    private javax.swing.JMenuItem fcthSearch;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem fileMenuExit;
    private javax.swing.JMenuBar frameMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuAbout;
    private javax.swing.JMenuItem helpMenuDonate;
    private javax.swing.JMenuItem helpMenuHomepage;
    private javax.swing.JMenuItem helpMenuMailinglist;
    private javax.swing.JMenuItem helpMenuWiki;
    private javax.swing.JMenuItem indexAll;
    private javax.swing.JMenuItem indexMissing;
    private javax.swing.JPanel indexPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenuItem jcdSearch;
    private javax.swing.JMenuItem jpegCoeffSearch;
    private javax.swing.JLabel labelDocCount;
    private javax.swing.JLabel labelMosaicSliderValue;
    private javax.swing.JLabel labelMosaicTitle;
    private javax.swing.JPanel mosaicAdvanceOptionsPanel;
    private javax.swing.ButtonGroup mosaicButtons;
    private javax.swing.JLabel mosaicImageLable;
    private javax.swing.JRadioButton mosaicOptionsAutocolorcorrelogram;
    private javax.swing.JRadioButton mosaicOptionsCedd;
    private javax.swing.JRadioButton mosaicOptionsColorLayout;
    private javax.swing.JPanel mosaicPanel;
    private javax.swing.JSlider mosaicTileCountSlider;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JPanel panelMosaicOptionsHidden;
    private javax.swing.JPanel panelMosaicOptionsShown;
    public javax.swing.JProgressBar progressBarIndexing;
    private javax.swing.JProgressBar progressMosaic;
    private javax.swing.JProgressBar progressSearch;
    private javax.swing.JMenuItem rerankFeature;
    private javax.swing.JMenuItem rerankLsa;
    private javax.swing.JMenu rerankMenu;
    private javax.swing.JMenu researchMenu;
    private javax.swing.JPanel resultsCardPane;
    private javax.swing.JScrollPane resultsPane;
    private javax.swing.JTable resultsTable;
    private javax.swing.JMenu sameSearchMenu;
    private javax.swing.JMenuItem scSearch;
    private javax.swing.JMenuItem searchAutoColorCorrelation;
    private javax.swing.JMenuItem searchColorLayout;
    private javax.swing.JMenuItem searchEdgeHistogram;
    private javax.swing.JMenuItem searchMpeg7Descriptors;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JMenuItem searchScalableColor;
    public javax.swing.JComboBox selectboxDocumentBuilder;
    private javax.swing.JComboBox selectboxRerankFeature;
    private javax.swing.JSpinner spinnerCurrentDocNum;
    private javax.swing.JSpinner spinnerMaxDocCount;
    private javax.swing.JPanel switchButtonsPanel;
    private javax.swing.JTextField textFieldFlickrDownloadMax;
    public javax.swing.JTextField textfieldIndexDir;
    public javax.swing.JTextField textfieldIndexName;
    private javax.swing.JTextField textfieldMosaicImage;
    private javax.swing.JTextField textfieldNumSearchResults;
    private javax.swing.JTextField textfieldSearchImage;
    private javax.swing.JPanel topPane;
    private javax.swing.JMenuItem vieMenuStartPage;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewMenuOptions;
    private javax.swing.JMenuItem viewMenuResults;
    // End of variables declaration//GEN-END:variables
}
