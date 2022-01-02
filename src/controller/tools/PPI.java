
package controller.tools;

public class PPI {

    private double x, y, z;

    private String width, height, size;

    public PPI(String breite, String hoehe, String groesse) {

        width = String.valueOf(removeAllNonNumber(String.valueOf(breite), "int"));
        height = String.valueOf(removeAllNonNumber(String.valueOf(hoehe), "int"));
        size = String.valueOf(removeAllNonNumber(String.valueOf(groesse), "double"));

        try {
            x = Double.parseDouble(width);
        } catch (NumberFormatException n) {
            System.out.println(n.getMessage());
            x = 0;
        }
        try {
            y = Double.parseDouble(height);
        } catch (NumberFormatException n) {
            System.out.println(n.getMessage());
            y = 0;
        }
        try {
            z = Double.parseDouble(size);
        } catch (NumberFormatException n) {
            System.out.println(n.getMessage());
            z = 1;
        }
    }

    public double calc() throws IllegalArgumentException {

        if (x <= 0) {
            if (x < 1 || width == null) {
                throw new IllegalArgumentException("The width can not be smaller than 1.");
            } else {
                x = x * (-1);
                x = Double.parseDouble(x + "");
                x = Math.abs(x);
            }
        }

        if (y <= 0) {
            if (y < 1 || height == null) {
                throw new IllegalArgumentException("The height can not be smaller than 1.");
            } else {
                y = y * (-1);
                y = Double.parseDouble(y + "");
                y = Math.abs(y);
            }
        }

        if (z <= 0) {
            if (z <= 0) {
                throw new ArithmeticException("The size can not be smaller than 0.");
            } else {
                z = z * (-1);
                z = Double.parseDouble(z + "");
                z = Math.abs(z);
            }
        }

        return (double) Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)) / z * 100000) / 100000;
    }

    public static double removeAllNonNumber(String str, String type) {
        try {
            if ("int".equals(type)) {
                return Double.parseDouble(Number.formatLikeInt(str));
            } else {
                return Double.parseDouble(Number.formatLikeDouble(str));
            }
        } catch (NumberFormatException n) {
            return 0;
        }
    }
}
