public class NumberToWords {

    private static final String[] units = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] tens = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private static String convertBelowThousand(int number) {
        if (number < 20) {
            return units[number];
        } else if (number < 100) {
            return tens[number / 10] + (number % 10 != 0 ? " " + units[number % 10] : "");
        } else {
            return units[number / 100] + " Hundred" + (number % 100 != 0 ? " and " + convertBelowHundred(number % 100) : "");
        }
    }

    private static String convertBelowHundred(int number) {
        if (number < 20) {
            return units[number];
        } else {
            return tens[number / 10] + (number % 10 != 0 ? " " + units[number % 10] : "");
        }
    }

    public static String convert(int number) {
        if (number == 0) {
            return "Zero";
        }

        String words = "";

        if ((number / 1000000) > 0) {
            words += convertBelowThousand(number / 1000000) + " Million ";
            number %= 1000000;
        }

        if ((number / 1000) > 0) {
            words += convertBelowThousand(number / 1000) + " Thousand ";
            number %= 1000;
        }

        if (number > 0) {
            words += convertBelowThousand(number);
        }

        return words.trim();
    }

    
}
