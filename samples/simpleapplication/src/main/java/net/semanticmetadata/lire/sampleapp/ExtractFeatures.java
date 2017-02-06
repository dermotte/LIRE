package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by mlux on 2/6/17.
 */
public class ExtractFeatures {
    static String helpMessage = "$> ExtractFeatures -i <directory> [-f <feature>]\n" +
            "\n" +
            "Extracts a features vector from each file in a directory and outputs to stdout.\n" +
            "\n" +
            "Options\n" +
            "=======\n" +
            "-i ... the directory with the images, files with .jpg and .png are read. \n" +
            "-f ... the feature vector, FCTH is default. Example -g net.semanticmetadata.lire.imageanalysis.features.global.CEDD\n";

    public static void main(String[] args) throws IOException {
        Properties p = CommandLineUtils.getProperties(args, helpMessage, new String[]{"-i"});
        // input directory ...
        File dir = new File(p.getProperty("-i"));
        if (!dir.exists() || !dir.isDirectory()) System.exit(1);
        Iterator<File> fileIterator = FileUtils.iterateFiles(dir, new SuffixFileFilter(new String[]{"png", "jpg"}, IOCase.INSENSITIVE), null);
        // feature ...
        GlobalFeature f = new FCTH();
        if (p.get("-f") !=null) {
            try {
                f = (GlobalFeature) Class.forName(p.getProperty("-f")).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        while (fileIterator.hasNext()) {
            try {
                File next = fileIterator.next();
                f.extract(ImageIO.read(next));
                System.out.print(next.getAbsolutePath() + '\t');
                for (int i = 0; i < f.getFeatureVector().length; i++) {
                    double v = f.getFeatureVector()[i];
                    System.out.print(v + "\t");
                }
                System.out.print('\n');
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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