#include <bits/stdc++.h>

using namespace std;

struct kolejka{ // struktura umozliwiająca uzyskanie minimum ciagu na monotonicznie przesuwajacym sie przedziale
    deque <pair<int, int>> dq;
    void dodaj(int wartosc, int indeks){ // przesuwamy prawy koniec przedzialu w prawo dodajac element o danej wartosci i indeksie
        while(!dq.empty() && dq.front().first >= wartosc){ // usuwamy elementy ktore na pewno juz nigdy nie beda optymalne
            dq.pop_front();
        }
        dq.emplace_front(wartosc, indeks);
    }
    void usun(int indeks){ // przesuwamy lewy koniec przedzialu w prawo usuwajac element na danym indeksie
        assert(!dq.empty());
        if(dq.back().second == indeks) dq.pop_back(); // jesli element nie byl usuniety wczesniej to go usuwamy z kolejki
    }
    int zapytanie(){ // zapytanie o minimum
        assert(!dq.empty());
        return dq.back().first; // minimum zgodnie ze struktura kolejki zawsze znajduje sie na jej tyle
    }
};

void wczytaj(int &n, int &u, vector <int> &x, vector <int> &y){ // wczytywanie zmiennych
    cin >> n >> u;
    x.resize(n); y.resize(n);
    for(int i = 0; i < n; i ++) cin >> x[i] >> y[i];
}

vector<pair<int, int>> znajdz_przedzialy(int &n, int &u, vector <int> &y){ // zwraca wektor maksymalnych przedzialow u-scislych
    vector <pair<int, int>> przedzialy;
    kolejka minimum, maksimum; // struktury pozwalajace na uzyskanie minimum i maksimum na danym przedziale
    // do uzyskania maksimum wystarczy wrzucac elementy przeciwne i brac zawsze element z minusem
    int prawy_koniec = 0, stary_prawy_koniec = -1; // prawy koniec aktualnego i poprzedniego przedzialu
    for(int i = 0; i < n; i ++){ // dla kazdego i szukamy jak najdluzszego max przedzialu postaci [i, prawy_koniec]
        if(minimum.dq.empty()){ // jesli kolejka jest pusta to znaczy ze przedzial zaczynajacy sie w i bedzie rozlaczny
            // ze wszystkimi wczesniejszymi przedzialami, wiec nie mamy jeszcze elementu i na kolejce
            prawy_koniec = i; // przedzial [i, i] jest zawsze kandydatem na max dobry przedzial
            minimum.dodaj(y[i], i);
            maksimum.dodaj(-y[i], i); // pamietamy ze na kolejke maksimum wrzucamy ujemne
        }
        while(prawy_koniec < n - 1 && 
                max(-maksimum.zapytanie(), y[prawy_koniec + 1]) - min(minimum.zapytanie(), y[prawy_koniec + 1]) <= u){
            // przedzial jest dobry jesli max - min na jego przedziale jest <= U, wiec rozszerzamy przedzial tak dlugo
            // jak jest dobry
            ++ prawy_koniec;
            minimum.dodaj(y[prawy_koniec], prawy_koniec);
            maksimum.dodaj(-y[prawy_koniec], prawy_koniec);
        }
        if(prawy_koniec != stary_prawy_koniec){ // jesli zmienil nam sie prawy koniec, to aktualny przedzial jest maksymalny
            // gdyby prawy koniec by sie nie zmienil, to aktualny przedzial zawieralby sie w poprzednim wiec nie bylby maksymalny
            przedzialy.emplace_back(i, prawy_koniec);
            stary_prawy_koniec = prawy_koniec;
        }
        // usuwamy elementy i z kolejek bo nie bedzie on na pewno juz nalezal do zadnego nowego przedzialu
        minimum.usun(i);
        maksimum.usun(i);
    }
    return przedzialy;
}

bool mniejsza_jakosc(pair<int, int> przedzial_1, pair<int, int> przedzial_2, vector<int> &x){ // czy przedzial_1 ma mniejsza jakosc
    // niz przedzial_2
    int l1 = przedzial_1.first, r1 = przedzial_1.second;
    int l2 = przedzial_2.first, r2 = przedzial_2.second;
    // jako ze jakosci sa dodatnie wystarczy porownac ich kwadraty
    __int128_t licznik_1 = (__int128_t)(x[r1] - x[l1]) * (__int128_t)(x[r1] - x[l1]);
    __int128_t mianownik_1= (r1 - l1 + 1);
    __int128_t licznik_2 = (__int128_t)(x[r2] - x[l2]) * (__int128_t)(x[r2] - x[l2]);
    __int128_t mianownik_2 = (r2 - l2 + 1);
    // standardowe porownywanie ulamkow po wymnozeniu na krzyz
    return ((__int128_t)(licznik_1 * mianownik_2) < (__int128_t)(licznik_2 * mianownik_1));
}

vector <pair<int, int>> znajdz_optymalny_przedzial(int &n, vector <pair<int, int>> &przedzialy, vector <int> &x){
    // dla kazdego indeksu zwraca maksymalny przedzial u-bliski o optymalnej jakosci
    // jako ze maksymalne przedzialy u-bliskie sa posortowane po poczatkach oraz ich konce sa monotoniczne, 
    // przedzial przedzialow pokrywajacych indeks i bedzie spojny oraz dla wiekszych i oba konce beda niemniejsze
    vector <pair<int, int>> odpowiedz(n);
    deque <int> dq;
    int l = 0, r = -1; // aktualne konce przedzialu przedzialow pokrywajacych indeks i
    for(int i = 0; i < n; i ++){
        while(r + 1 < (int)przedzialy.size() && przedzialy[r + 1].first <= i){ // rozszerzamy dopoki mozemy
            ++ r;
            // dodajemy na kolejke podobnie jak w strukturze, ale tu porownujemy przedzialy po jakosci
            // interesuje nas tylko ostro wieksza jakosc, bo w przypadku remisu decydują indeksy
            // wiec wyrzucamy te przedzialy ktore maja ostro mniejszą jakosc
            while(!dq.empty() && mniejsza_jakosc(przedzialy[dq.front()], przedzialy[r], x)) dq.pop_front();
            dq.push_front(r);
        }
        while(l <= r && przedzialy[l].second < i){ // wyrzucamy przedzialy ktore juz nie pokrywają i
            if(!dq.empty() && dq.back() == l) dq.pop_back();
            ++ l;
        }
        assert(l <= r);
        assert(!dq.empty());
        odpowiedz[i] = przedzialy[dq.back()]; // odpowiedzią dla indeksu i bedzie przedzial z tylu kolejki bo ma maksymalną jakosc
    }
    return odpowiedz;
}

void rozwiaz(int &n, int &u, vector <int> &x, vector <int> &y){
    auto przedzialy = znajdz_przedzialy(n, u, y);
    auto odpowiedz = znajdz_optymalny_przedzial(n, przedzialy, x);
    for(auto [l, r]: odpowiedz) cout << l + 1 << ' ' << r + 1 << '\n';
}

int main(){
    ios_base::sync_with_stdio(0); cin.tie(0);
    int n, u; vector <int> x, y;
    wczytaj(n, u, x, y);
    rozwiaz(n, u, x, y);
    return 0;
}
