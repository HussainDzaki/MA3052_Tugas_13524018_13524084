#include <fstream>
#include <iostream>
using namespace std;

int main()
{
    int N;
    cin >> N;

    ofstream cout(to_string(N) + "-node.txt");
    cout << N << " " << N * (N - 1) / 2 << endl;
    for (int i = 1; i < N; i++) {
        for (int j = 2; j <= N; j++) {
            cout << i << " " << j << endl;
        }
    }
    cout.close();

    return 0;
}