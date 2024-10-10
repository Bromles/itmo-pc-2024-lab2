public class MandelbrotFractal {

    public static void main(String[] args) {
        int width = 80;
        int height = 40;
        int maxIterations = 50;
        double zoom = 20;
        double xShift = -0.5;
        double yShift = 0;

        for (int yPixel = 0; yPixel < height; yPixel++) {
            StringBuilder row = new StringBuilder();
            for (int xPixel = 0; xPixel < width; xPixel++) {
                double zx = 0;
                double zy = 0;
                double cX = (xPixel - width / 2) / zoom + xShift;
                double cY = (yPixel - height / 2) / zoom + yShift;
                int iter = maxIterations;
                while (zx * zx + zy * zy < 4 && iter > 0) {
                    double tmp = zx * zx - zy * zy + cX;
                    zy = 2.0 * zx * zy + cY;
                    zx = tmp;
                    iter--;
                }
                char colorChar = (iter == 0) ? '*' : ' ';
                row.append(colorChar);
            }
            System.out.println(row);
        }
    }
}