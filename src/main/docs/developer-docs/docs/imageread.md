# Reading and Decoding images in Java
Java is unfortunately limited in it's ability to handle images. But there are two basic workarounds.

  - You can convert all images to a format that Java can handle. Just use ImageMagick or some other great tool to batch process your images and convert them all to RGB color JPEGs. This is a non Java approach and definitely the faster one.
  - You can circumvent the ImageIO.read(..) method by using [ImageJ](http://rsbweb.nih.gov/ij/). In ImageJ you've got the ImagePlus class, which supports loading and decoding of various formats and is much more error resilient than the pure Java SDK method. Speed, however, is not increased by this approach. It's more the other way round.
  - Other than that you can use [TwelveMonkeys](https://github.com/haraldk/TwelveMonkeys)

## Using ImageJ to load and decode images
    public class Utils {
        // Loading an image with ImageJ
        public static BufferedImage openImage(String path) {
            ImagePlus imgPlus = new ImagePlus(path);
            // converting the image to RGB
            ImageConverter imageConverter = new ImageConverter(imgPlus);
            imageConverter.convertToRGB();
            // returning the BufferedImage instance
            return imgPlus.getBufferedImage();
        }
    }