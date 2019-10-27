package ranker;

public class Sort {

    public static void bubbleSort(OkapiBM25.DocumentScore arr[]) {

        int n = arr.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr[j].score < arr[j + 1].score) {

                    OkapiBM25.DocumentScore temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
    }
}
