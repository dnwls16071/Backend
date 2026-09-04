package ch2;

import java.util.Arrays;

public class MakeMeaningfulDistinction {

    public static void main(String[] args) {
        char[] source = {'C', 'L', 'E', 'A', 'N'};
        char[] destination = new char[source.length];

        copyChars(source, destination);
        System.out.println(Arrays.toString(destination));
    }

    public static void copyChars(char[] a1, char[] a2) {
        for (int i = 0; i < a1.length; i++) {
            a2[i] = a1[i];
        }
    }
}
