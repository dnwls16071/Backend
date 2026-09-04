package ch2;

import java.util.ArrayList;
import java.util.List;

public class MakeYourIntentionsClear {

    public static void main(String[] args) {

        // 함수가 반환하는 list1을 어떻게 사용하는가?
        List<int[]> list1 = getThem();

        for (int[] arr : list1) {
            System.out.println(arr[0] + ", " + arr[1]);
        }
    }

    static List<int[]> getThem() {
        // theList에 무엇이 들어있는가?
        List<int[]> theList = new ArrayList<>();

        theList.add(new int[]{4, 10});
        theList.add(new int[]{2, 20});
        theList.add(new int[]{4, 30});
        theList.add(new int[]{1, 40});

        List<int[]> result = new ArrayList<>();

        for (int[] x : theList) {
            // theList에서 0번째 값이 왜 중요한가?
            // 값 4가 무슨 의미인가?
            if (x[0] == 4) {
                result.add(x);
            }
        }

        return result;
    }
}
