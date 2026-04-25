#include <bits/stdc++.h>
#define pb emplace_back
#define pll pair <ll, ll> 
#define st first
#define nd second

using namespace std;
using ll = uint_fast64_t;
using lll = __int128_t;

const ll MOD = (1LL << 61) - 1;
const ll BASE = 911382323;

ll mod_mn(ll a, ll b){  // mnozenie modulo MOD
    lll t = (lll)a * b;
    t = (t >> 61) + (t & ((1LL << 61) -1));
    if (t >= (1LL << 61) - 1) t -= (1LL << 61) - 1;
    return (ll)t;
}

ll mod_dod(ll a, ll b){ // dodawanie modulo MOD
    a += b;
    if (a >= (1LL << 61) - 1) a -= (1LL << 61) - 1;
    return a;
}

void wczytaj(int &n, vector <int> &x, vector <int> &y){
    cin >> n;
    x.resize(n); y.resize(n);
    for(int i = 0; i < n; i ++) cin >> x[i] >> y[i];
}

void wyczysc(int &n, vector <int> &x, vector <int> &y){ // pozbywamy sie zbednych szklanek o pojemnosci 0 
    vector <int> nowe_x, nowe_y;
    for(int i = 0; i < n; i ++){
        if(x[i] != 0){
            nowe_x.pb(x[i]);
            nowe_y.pb(y[i]);
        }
    }
    n = (int)nowe_x.size();
    x = nowe_x;
    y = nowe_y;
}

bool sprawdz_warunki_konieczne(int n, vector <int> &x, vector <int> &y, int &nwd){ // aby istnialo rozwiazanie musi zachodzic jeden
    // z dwoch warunkow: 1. istnieje taki indeks i ze x_i = y_i lub y_i = 0, 2. dla kazdego i nwd(x1, ..., x_n) dzieli y_i
    // jest tak poniewaz mozna udowodnic ze ten warunek jest niezmiennikiem, tzn w kazdym momencie ciagu dozwolonych operacji
    // bedzie on spelniony poprzed stany wody w szklankach
    bool czy = 0;
    for(int i = 0; i < n; i ++){
        if(y[i] == 0 || x[i] == y[i]) czy = 1;
        nwd = gcd(nwd, x[i]);
    }
    if(nwd == 0) return 1;
    for(int i = 0; i < n; i ++){
        if(y[i] % nwd != 0) return 0;
    }
    return czy;
}

int tryw(int n, vector <int> &x, vector <int> &y){ // jezeli mamy do czynienia z trywialnym przypadkiem ze kazdy kubek ma byc
    // albo pelny albo pusty to odpowiedz mozna prosto zliczyc w czasie liniowym
    bool czy = 1;
    int wyn = 0;
    for(int i = 0; i < n; i ++){
        if(y[i] != 0 && y[i] != x[i]) czy = 0;
        if(y[i] == x[i]) ++ wyn;
    }
    if(!czy) return -1;
    return wyn;
}

void gen_pot(int n, vector <ll> &pot){ // funkcja generująca potegi bazy potrzebne do hashowania
    pot[0] = 1;
    for(int i = 1; i < n; i ++) pot[i] = mod_mn(pot[i - 1], BASE);
}

// problem w ogolnosci rozwiazaujemy przy pomocy algorytmu BFS. kazdy osiagalny stan jest wierzcholkiem, w grafie
// ktory przeszukujemy warstwami od stanu (0, ..., 0). gdy dojdziemy do stanu (y_1, ..., y_n) mamy rozwiazanie

// stany tego ile aktualnie wody jest w kazdej szklance bedziemy reprezentowac za pomocą wielomianowego hashowania


void bfs(int n, vector <int> &x, vector <int> &y, int &wyn){
    queue <pair<vector<int>, ll>> kolejka; // kolejka zawierajaca nastepne stany do rozpoatrzenia w kolejnosci BFS
    unordered_set <ll> odw; // set dający informacje czy dany stan byl juz odwiedzony w grafie

    // generowanie potęg potrzebnych do hashowania
    vector <ll> pot(n);
    gen_pot(n, pot);

    int ile_warstw = 0; // numer aktualnie rozpatrywanej warstwy przy pomocy BFS
    
    // hashowanie stanu (y_1, ..., y_n) zeby moc latwo sprawdzic czy jest osiągniety
    ll koniec = 0;
    for(int i = 0; i < n; i ++) koniec = mod_dod(koniec, mod_mn(y[i], pot[i]));

    // "odwiedzamy" odpowiednio poczatek i koniec grafu
    kolejka.emplace(vector<int>(n, 0), 0);
    odw.insert(0);

    vector <int> sasiad(n); // vector przechowujacy aktualnie tymczasowego sąsiada (uzywane w petli ale deklarowane tutaj aby uniknac
    // wielokrotnego deklarowania)

    while(!kolejka.empty()){
        int rozmiar = (int)kolejka.size();
        while(rozmiar --){ // rozpatrujemy wszystkie stany w aktualnej warstwie od przodu
            auto cur = std::move(kolejka.front());
            kolejka.pop();
            auto &wierz = cur.first;
            ll hash = cur.nd;
            sasiad = wierz;
            // szukamy wszystkich sasiadow czyli stanow do ktorych jestem w stanie dojsc z aktualnego stanu jedną operacją
            for(int i = 0; i < n; i ++){
                if(wierz[i] != 0){ // jesli szklanka i jest pusta mozemy ją oproznic
                    sasiad[i] = 0;
                    ll hash_sasiad = mod_dod(hash, MOD - mod_mn(wierz[i], pot[i]));
                    if(odw.insert(hash_sasiad).second){
                        kolejka.emplace(sasiad, hash_sasiad);
                        if(hash_sasiad == koniec){
                            wyn = ile_warstw + 1;
                            return;
                        }
                    }
                    sasiad[i] = wierz[i];
                }
                if(wierz[i] != x[i]){ // jesli szklanka i nie jest pelna mozemy ją zapelnic
                    sasiad[i] = x[i];
                    ll hash_sasiad = mod_dod( mod_dod(hash, MOD - mod_mn(wierz[i], pot[i])), mod_mn(x[i], pot[i]));
                    if(odw.insert(hash_sasiad).second){
                        kolejka.emplace(sasiad, hash_sasiad);
                        if(hash_sasiad == koniec){
                            wyn = ile_warstw + 1;
                            return;
                        }
                    }
                    sasiad[i] = wierz[i];
                }
                if(wierz[i] == 0) continue; // jesli i nie jest puste mozemy cos z niego przelac do innej szklanki
                for(int j = 0; j < n; j ++){
                    if(i == j || wierz[j] == x[j]) continue; // o ile oczywiscie ta szklanka nie jest pelna
                    sasiad[j] = min(wierz[i] + wierz[j], x[j]);
                    sasiad[i] = wierz[i] + wierz[j] - sasiad[j];
                    ll hash_sasiad = hash;
                    hash_sasiad = mod_dod(hash_sasiad, MOD - mod_mn(wierz[i], pot[i]));
                    hash_sasiad = mod_dod(hash_sasiad, MOD - mod_mn(wierz[j], pot[j]));
                    hash_sasiad = mod_dod(hash_sasiad, mod_mn(sasiad[i], pot[i]));
                    hash_sasiad = mod_dod(hash_sasiad, mod_mn(sasiad[j], pot[j]));  
                    if(odw.insert(hash_sasiad).second){
                        kolejka.emplace(sasiad, hash_sasiad);
                        if(hash_sasiad == koniec){
                            wyn = ile_warstw + 1;
                            return;
                        }
                    }
                    sasiad[i] = wierz[i];
                    sasiad[j] = wierz[j];
                }
                // w kazdym z przypadkow rozpatrzenia kolejnego stanu tworze hash sąsiada na podstawie modyfikacji hashu aktualnego wierzcholka, i jesli ten hash
                // jest rowny hashowi stanu (y_1, ..., y_n) to korzystajac z wlasnosci BFS wynik zostal znaleziony
            }
        }
        ++ ile_warstw; // zwiekszamy licznik warstw bo przechodze do kolejnej
    }
}

void rozwiaz(int &n, vector <int> &x, vector <int> &y){
    int nwd = 0;
    if(!sprawdz_warunki_konieczne(n, x, y, nwd)){ // sprawdzamy warunki konieczne czyli niezmienniki
        cout << "-1\n";
        return;
    }
    if(nwd == 0){ // jesli nwd = 0 to wszystkie x_i = 0, wiec wszystkie y_i = 0, wiec wynik = 0
        cout << "0\n";
        return;
    }
    int wyn = tryw(n, x, y); // sprawdzamy czy mamy do czynienia z jakims trywialnym przypadkiem
    if(wyn != -1){
        cout << wyn << '\n';
        return;
    }
    bfs(n, x, y, wyn); // puszczamy algorytm BFS w celu znalezienia wyniku
    cout << wyn << '\n';
}

int main(){
    ios_base::sync_with_stdio(0); cin.tie(0);
    int n; 
    vector <int> x, y;
    wczytaj(n, x, y);
    wyczysc(n, x, y);
    rozwiaz(n, x, y);
    return 0;
}
