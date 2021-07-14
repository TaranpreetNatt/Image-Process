import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Image implements ActionListener {

    private JButton openPictureButton = new JButton("Open Picture");
    private JButton nextButton = new JButton("Next");
    private int counter = 0;
    private JFrame applicationFrame;
    private JPanel panel;
    private JLabel imageLeft = new JLabel();
    private JLabel imageRight = new JLabel();
    private File originalFile;

    Image() {
        applicationFrame = new JFrame();
        panel = new JPanel();

        panel.setLayout(new GridBagLayout());

        applicationFrame.getContentPane().add(panel, BorderLayout.NORTH);
        applicationFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        applicationFrame.setTitle("Image");

        GridBagConstraints left = new GridBagConstraints();
        left.gridx = 0;
        left.gridy = 0;
        left.insets = new Insets(10, 10 ,10 ,10);
        panel.add(imageLeft, left);

        GridBagConstraints right = new GridBagConstraints();
        right.gridx = 1;
        right.gridy = 0;
        right.insets = new Insets(10, 10 ,10 ,10);
        panel.add(imageRight, right);

        GridBagConstraints open = new GridBagConstraints();
        open.gridx = 1;
        open.gridy = 1;
        open.insets = new Insets(10, 10 ,10 ,10);
        panel.add(openPictureButton, open);

        GridBagConstraints nextConstraint = new GridBagConstraints();
        nextConstraint.gridx = 0;
        nextConstraint.gridy = 1;
        nextConstraint.insets = new Insets(10, 10 ,10 ,10);
        panel.add(nextButton, nextConstraint);

        nextButton.addActionListener(this);
        openPictureButton.addActionListener(this);

        applicationFrame.pack();
        applicationFrame.setVisible(true);
    }

    public void setOriginalFile(File file) {
        this.originalFile = file;
    }

    private BufferedImage readImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        }
        catch(IOException io) {
            System.out.println("Problem getting the bmp file");
            System.out.println(io);
            throw new RuntimeException(io);
        }
        return image;
    }

    private void displayImageLeft(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        ImageIcon imageIcon = new ImageIcon(image);
        imageLeft.setIcon(imageIcon);
        imageLeft.setSize(new Dimension(width, height));

        Dimension imageRightSize = imageRight.getSize();
        int rightWidth = (int) imageRightSize.getWidth() + 10;
        int rightHeight = (int) imageRightSize.getHeight() + 10;
        applicationFrame.setSize(new Dimension(rightWidth + width + 300, rightHeight + height + 300));
    }

    private void displayImageRight(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        ImageIcon imageIcon = new ImageIcon(image);
        imageRight.setIcon(imageIcon);
        imageRight.setSize(new Dimension(width, height));

        Dimension imageLeftSize = imageLeft.getSize();
        int rightWidth = (int) imageLeftSize.getWidth() + 10;
        int rightHeight = (int) imageLeftSize.getHeight() + 10;
        applicationFrame.setSize(new Dimension(rightWidth + width + 300, rightHeight + height + 300));
    }

    private void resetImageRight() {
        imageRight.setIcon(null);
    }

    private void resetImageLeft() {
        imageLeft.setIcon(null);
    }

    public void displayOriginalImage() {
        BufferedImage originalImage = readImage(originalFile);
        displayImageLeft(originalImage);
    }

    public BufferedImage createGrayImage() {
        BufferedImage grayImage = readImage(originalFile);
        int height = grayImage.getHeight();
        int width = grayImage.getWidth();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Color pixelColor = new Color(grayImage.getRGB(i, j));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();

                r = (int) (r * 0.299);
                g = (int) (g * 0.587);
                b = (int) (b * 0.114);

                int gray = r + g + b;
                int rgb = new Color(gray, gray, gray).getRGB();
                grayImage.setRGB(i, j, rgb);
            }
        }
        return grayImage;
    }

    public BufferedImage createDitherImage() {
        BufferedImage grayImage = createGrayImage();
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        int n = 4;
        // 2x2
//        int[][] ditherMatrix =  {
//                {0, 2},
//                {3, 1},
//        };

        // 4x4
        int[][] ditherMatrix =  {
                                    {0, 8, 2, 10},
                                    {12, 4, 14, 6},
                                    {3, 11, 1, 9},
                                    {15, 7, 13, 5}
                                };

        // 8x8
//        int[][] ditherMatrix =  {
//                                    {0, 32, 8, 40, 2, 34, 10, 42},
//                                    {48, 16, 56, 24, 50, 18, 58, 26},
//                                    {12, 44, 4, 36, 14, 46, 6, 38},
//                                    {60, 28, 52, 20, 62, 30, 54, 22},
//                                    {3, 35, 11, 43, 1, 33, 9, 41},
//                                    {51, 19, 59, 27, 49, 17, 57, 25},
//                                    {15, 47, 7, 39, 13, 45, 5, 37},
//                                    {63, 31, 55, 23, 61, 29, 53, 21}
//                                };


        int divisor = n * n + 1;
        int normalizeValue = Math.floorDiv(256, divisor);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int gray = new Color(grayImage.getRGB(x, y)).getRed();
                gray = Math.floorDiv(gray, normalizeValue);
                grayImage.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = x % n;
                int j = y % n;

                int gray = new Color(grayImage.getRGB(x, y)).getRed();
                int dComp = ditherMatrix[i][j];

                if (gray > dComp) {
                    grayImage.setRGB(x, y, new Color(255, 255, 255).getRGB());
                }
                else {
                    grayImage.setRGB(x, y, new Color(0, 0, 0).getRGB());
                }
            }
        }
        return grayImage;
    }

    public BufferedImage createAutoLevelImage() {
        BufferedImage originalImage = readImage(originalFile);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int level = (int) Math.pow(2, (originalImage.getColorModel().getPixelSize() / 3));
        double totalPixels = width * height;

        double[] pmfR = new double[level];
        double[] pmfG = new double[level];
        double[] pmfB = new double[level];

        // get pmf for R G B values
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();

                pmfR[r] = pmfR[r] + 1;
                pmfG[g] = pmfG[g] + 1;
                pmfB[b] = pmfB[b] + 1;
            }
        }

        double[] cdfR = new double[level];
        double[] cdfG = new double[level];
        double[] cdfB = new double[level];

        cdfR[0] = pmfR[0];
        cdfG[0] = pmfG[0];
        cdfB[0] = pmfB[0];

        for (int i = 1; i < level; i++) {
            cdfR[i] = cdfR[i - 1] + pmfR[i];
            cdfG[i] = cdfG[i - 1] + pmfG[i];
            cdfB[i] = cdfB[i - 1] + pmfB[i];
        }

        for (int i = 0; i < level; i++) {
            cdfR[i] = cdfR[i] / totalPixels;
            cdfG[i] = cdfG[i] / totalPixels;
            cdfB[i] = cdfB[i] / totalPixels;
        }

        int[] newR = new int[level];
        int[] newG = new int[level];
        int[] newB = new int[level];

        for (int i = 0; i < level; i++) {
            newR[i] = (int) Math.floor(cdfR[i] * (level - 1));
            newG[i] = (int) Math.floor(cdfG[i] * (level - 1));
            newB[i] = (int) Math.floor(cdfB[i] * (level - 1));
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color pixelColor = new Color(originalImage.getRGB(x, y));
                int r = pixelColor.getRed();
                int g = pixelColor.getGreen();
                int b = pixelColor.getBlue();

                r = newR[r];
                g = newR[g];
                b = newR[b];
                int rgb = new Color(r, g, b).getRGB();
                originalImage.setRGB(x, y, rgb);
            }
        }

        return originalImage;
    }

    public static void imageIoWrite(BufferedImage bImage) {
        JFileChooser jfc = new JFileChooser();
        int returnVal = jfc.showSaveDialog(null);

        try {
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File outputFile = jfc.getSelectedFile();
                ImageIO.write(bImage, "png", outputFile);
            }
        } catch (IOException e) {
            System.out.println("Exception occured :" + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Open Picture")) {
            FileExplorer fileExplorer = new FileExplorer();
            File file = fileExplorer.openFile();
            if (file != null) {
                this.counter = 0;
                this.originalFile = file;
                resetImageRight();
                displayOriginalImage();
            }
        }
        if (e.getActionCommand().equals("Next")) {
            counter++;
            if (counter == 1) {
                BufferedImage grayImage = createGrayImage();
                displayOriginalImage();
                displayImageRight(grayImage);
            }
            if (counter == 2) {
                BufferedImage ditheredImage = createDitherImage();
                BufferedImage grayImage = createGrayImage();
                displayImageLeft(grayImage);
                displayImageRight(ditheredImage);
            }
            if (counter == 3) {
                BufferedImage autoImage = createAutoLevelImage();
                displayOriginalImage();
                displayImageRight(autoImage);
            }
        }
    }

    public static void main(String args[]) {
        FileExplorer fileExplorer = new FileExplorer();

        File file = fileExplorer.openFile();

        Image gui = new Image();
        if (file != null) {
            gui.setOriginalFile(file);
            gui.displayOriginalImage();
        }
    }

}
