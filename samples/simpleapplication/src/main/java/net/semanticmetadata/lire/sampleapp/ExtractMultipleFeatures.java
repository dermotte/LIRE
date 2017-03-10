package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * Created by mlux on 2/6/17.
 */
public class ExtractMultipleFeatures {
    static String helpMessage = "$> ExtractMultipleFeatures -i <directory> [-f <feature>]\n" +
            "\n" +
            "Extracts a features vector from each file in a directory and outputs to stdout.\n" +
            "\n" +
            "Options\n" +
            "=======\n" +
            "-i ... the directory with the images, files with .jpg and .png are read. \n" +
            "-m ... Max norm feature vectors, default is false. \n" +
            "-o ... Output file, default is outstream \n" +
            "-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD\n";

    public static void main(String[] args) throws IOException {
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{"-i"});
        // input directory ...
        File dir = new File(p.getProperty("-i"));
        boolean maxNorm = false;
        PrintStream outstream = System.out;
        if (!dir.exists() || !dir.isDirectory()) System.exit(1);
        Iterator<File> fileIterator = FileUtils.iterateFiles(dir, new SuffixFileFilter(new String[]{"png", "jpg"}, IOCase.INSENSITIVE), null);
        // feature ...
        List<GlobalFeature> f = new LinkedList<>();
        if (p.get("-m") != null) {
            maxNorm = true;
        }
        if (p.get("-o") != null) {
            outstream = new PrintStream(new FileOutputStream(p.getProperty("-o")));
        }
        if (p.get("-f") != null) {
            String argf = p.getProperty("-f");
            String[] features = new String[1];
            if (argf.contains(",")) {
                // split features
                features = argf.split(",");
            } else {
                features[0] = argf;
            }
            for (int i = 0; i < features.length; i++) {
                String feature = features[i];
                if (!feature.contains("."))
                    feature = "net.semanticmetadata.lire.imageanalysis.features.global." + feature;
                try {
                    f.add((GlobalFeature) Class.forName(feature).newInstance());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if (f.size() < 1) f.add(new FCTH());

        boolean headerPrinted = false;
        // print data
        while (fileIterator.hasNext()) {
            try {
                File next = fileIterator.next();
                if (!headerPrinted) {
                    printHeader(next, f, outstream);
                    headerPrinted = true;
                }
                StringBuilder sb = new StringBuilder();
                // file name
                sb.append(next.getAbsolutePath() + '\t');
                // each feature
                BufferedImage read = ImageUtils.createWorkingCopy(ImageIO.read(next));
                for (Iterator<GlobalFeature> iterator = f.iterator(); iterator.hasNext(); ) {
                    GlobalFeature gf = iterator.next();
                    gf.extract(read);
                    double[] hist = gf.getFeatureVector();
                    if (maxNorm) hist = MetricsUtils.normalizeMax(hist);
                    for (int i = 0; i < hist.length; i++) {
                        double v = hist[i];
                        sb.append(v + "\t");
                    }
                }
                outstream.print(sb.toString().trim() + '\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void printHeader(File image, List<GlobalFeature> f, PrintStream outstream) throws IOException {
        // print header
        StringBuilder sb = new StringBuilder();
        sb.append("# filename\t");
        BufferedImage read = ImageUtils.createWorkingCopy(ImageIO.read(image));
        for (Iterator<GlobalFeature> iterator = f.iterator(); iterator.hasNext(); ) {
            GlobalFeature gf = iterator.next();
            gf.extract(read);
            for (int i = 0; i < gf.getFeatureVector().length; i++) {
                double v = gf.getFeatureVector()[i];
                sb.append(gf.getFeatureName().replaceAll("\\s+", "_") + i + "\t");
            }
        }
        outstream.print(sb.toString().trim() + "\n");
    }
}
/*
$> ExtractFeatures -i <directory> [-f <feature>]

Extracts a features vector from each file in a directory and outputs to stdout.

Options
=======
-i ... the directory with the images, files with .jpg and .png are read.
-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD
 */