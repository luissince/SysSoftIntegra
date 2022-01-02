

package controller.tools;

public class Number {

    private static int firstDot(String s) {
        int first = s.indexOf(",");
        int second = s.indexOf(".");

        if (first < second) {
            return second;
        } else {
            return first;
        }
    }

    private static int howManyDots(String s) {
        int dots = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isDot(s.charAt(i))) {
                dots++;
            }
        }
        return dots;
    }

    private static int howManyNumbers(String s) {
        int numbers = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isNumber(s.charAt(i))) {
                numbers++;
            }
        }
        return numbers;
    }

    private static boolean isDot(char c) {
        return c == '.';
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private static String removeNotFirstDot(String s) {
        String front = s.substring(0, firstDot(s) + 1);
        String sub = s.substring(firstDot(s) + 1);
        StringBuffer sb = new StringBuffer(sub);
        for (int i = 0; i < sb.length(); i++) {
            if (isDot(sb.charAt(i)) || !isNumber(sb.charAt(i))) {
                sb.deleteCharAt(i);
            }
        }
        return front + sb.toString();
    }

    private static String removeAllDots(String s) {
        StringBuffer sb = new StringBuffer(s);
        int length = sb.length();
        for (int i = 0; i < length; i++) {
            if (isDot(sb.charAt(i))) {
                sb.deleteCharAt(i);
                length--;
                i--;
            }
        }
        return sb.toString();
    }

    private static String addLeadingZero(String s) {
        if (firstDot(s) == 0)
            return "0" + s;
        else
            return s;
    }

    private static String addNumber(String s) {
        if (howManyNumbers(s) == 0)
            return "0";
        else
            return s;
    }

    private static String format(String s) {
        s = s.replaceAll(",", ".");
        s = s.replaceAll("[^.;0-9]*", "");
        s = removeNotFirstDot(s);
        s = addNumber(s);
        return s;
    }

    public static String formatLikeInt(String s) {
        s = format(s);
        if (firstDot(s) != -1) {
            s = s.substring(0, firstDot(s) + 1);
        }
        s = removeAllDots(s);
        return s;
    }

    public static String formatLikeDouble(String s) {
        s = format(s);
        s = addLeadingZero(s);
        while (howManyDots(s) > 1) {
            s = removeNotFirstDot(s);
        }
        return s;
    }
}