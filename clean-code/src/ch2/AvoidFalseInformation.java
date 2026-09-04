package ch2;

public class AvoidFalseInformation {

    public static void main(String[] args) {

        int l = 1;      // 소문자 L은 숫자 1처럼 보인다.
        int O = 0;      // 대문자 O은 숫자 0처럼 보인다.
        int O1 = 10;

        int a = l;

        if (O == 1) {
            a = O;
        } else {
            l = O1;
        }

        System.out.println(a);
        System.out.println(l);
    }
}
