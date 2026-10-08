package symulacja;

import java.util.Random;

import model.Sportowiec;
import model.Trasa;
import model.Wezel;
import model.Wyciag;
import zdarzenia.RozpoczeciePrzejazduTrasa;
import zdarzenia.WejscieDoKolejkiWyciagu;

public class WyborKolejnegoRuchu {
    
    private Random random;

    public WyborKolejnegoRuchu() {
        this.random = new Random();
    }

    //  Wybór następnego ruchu sportowca znajdującego się w węźle podczas
    //  danej symualcji.
    public void wybierzKolejnyRuch(
        Sportowiec sportowiec, 
        int czas, Symulacja symulacja
    ) {
        //  Po końcu czasu obsługiwania początków przejazdów sportowiec
        //  już nic nie może zrobić.
        if(!symulacja.czyDoKoncaJazdy(czas)) {
            return;
        }

        Wezel aktualnyWezel = sportowiec.getAktualnyWezel();

        //  Rozpatrujemy wszystkie możliwe połączenia z węzła, w którym
        //  jest sportowiec.
        Trasa[] trasy = aktualnyWezel.getTrasy();
        Wyciag[] wyciagi = aktualnyWezel.getWyciagi();

        //  Jeśli losowa liczba jest mniejsza od spontaniczności sportowca,
        //  to wybiera on losowo, bo prawdopodobieństwo wybrania losowo
        //  to jego spontaniczność.
        if(random.nextDouble() < sportowiec.getSpontanicznosc()) {
            wybierzSpontanicznie(sportowiec, czas, trasy, wyciagi, symulacja);
        }
        //  W przeciwnym wypadku wybieramy najlepszy możliwy ruch.
        else {
            wybierzWedlugAtrakcyjnosci(sportowiec, czas, trasy, 
                                                    wyciagi, symulacja);
        }
    }

    //  Losowy wybór trasy lub wyciągu.
    private void wybierzSpontanicznie(
        Sportowiec sportowiec,
        int czas,
        Trasa[] trasy,
        Wyciag[] wyciagi,
        Symulacja symulacja
    ) {
        //  Losujemy spośród wszystkich połączeń.
        int liczbaMozliwosci = trasy.length + wyciagi.length;
        int indeks = random.nextInt(liczbaMozliwosci);

        //  Przypadek, gdy wylosowaliśmy trasę.
        if(indeks < trasy.length) {
            //  Wylosowana trasa.
            Trasa trasa = trasy[indeks];

            rozpocznijPrzejazdTrasa(sportowiec, czas, trasa, symulacja);
        }
        //  Przypadek, gdy wylosowaliśmy wyciąg.
        else {
            //  Wylosowany wyciąg.
            Wyciag wyciag = wyciagi[indeks - trasy.length];

            wejdzDoKolejkiWyciagu(sportowiec, czas, wyciag, symulacja);
        }
    }

    //  Wybór trasy lub wyciągu kierując się atrakcyjnością.
    private void wybierzWedlugAtrakcyjnosci(
        Sportowiec sportowiec,
        int czas,
        Trasa[] trasy,
        Wyciag[] wyciagi,
        Symulacja symulacja
    ) {
        //  Aktualnie najlepsza trasa z rozpatrzonych i jej atrakcyjność.
        //  Początkowo atrakcyjność jest równa -1, ponieważ jest ona z
        //  definicji nieujemna, więc -1 jest mniejsze od wszystkich
        //  możliwych atrakcyjności.
        Trasa najlepszaTrasa = null;
        double najlepszaAtrakcyjnosc = -1.0;

        //  Wyciąg prowadzący do najlepszej trasy, jeśli nie wychodzi ona
        //  bezpośrednio z węzła. W przeciwnym wypadku zmienna pozostaje null.
        Wyciag najlepszyWyciag = null;

        //  Rozważamy trasy bezpośrednio wychodzące z aktualnego węzła.
        for(int i = 0; i < trasy.length; i++) {
            Trasa trasa = trasy[i];

            double atrakcyjnosc = trasa.obliczAtrakcyjnosc(sportowiec);

            //  Polepszamy wynik jeśli możemy.
            if(najlepszaTrasa == null || atrakcyjnosc > najlepszaAtrakcyjnosc) {
                najlepszaTrasa = trasa;
                najlepszaAtrakcyjnosc = atrakcyjnosc;
            }
        }

        //  Rozważamy wyciągi wychodzące z aktualnego węzła i dla każdego
        //  z nich ropzatrujemy wszystkie trasy wychodzące z jego końca.
        for(int i = 0; i < wyciagi.length; i++) {
            Wyciag wyciag = wyciagi[i];

            Wezel koncowaStacja = wyciag.getKoniec();
            Trasa[] trasyZKonca = koncowaStacja.getTrasy();

            for(int j = 0; j < trasyZKonca.length; j++) {
                Trasa trasa = trasyZKonca[j];

                double atrakcyjnosc = trasa.obliczAtrakcyjnosc(sportowiec);

                //  Polepszamy wynik jeśli możemy.
                if(najlepszaTrasa == null 
                            || atrakcyjnosc > najlepszaAtrakcyjnosc) {
                    najlepszaTrasa = trasa;
                    najlepszaAtrakcyjnosc = atrakcyjnosc;

                    //  Ta trasa jest dostępna dopiero po wjechaniu
                    //  tym wyciągiem.
                    najlepszyWyciag = wyciag;
                }
            }
        }

        //  Jeśli nie znaleźlismy żadnej trasy, to mamy zagwarantowane, że
        //  z węzła prowadzą wyciągi, więc możemy użyć dowolnego.
        if(najlepszaTrasa == null) {
            wejdzDoKolejkiWyciagu(sportowiec, czas, wyciagi[0], symulacja);
        }

        //  Jeśli najlepsa znaleziona trasa pochodzi bezpośrednio z
        //  aktualnego węzła, sportowiec od razu nią zjeżdża.
        if(najlepszyWyciag == null) {
            rozpocznijPrzejazdTrasa(sportowiec, czas, 
                                                najlepszaTrasa, symulacja);
        }
        //  W przeciwnym wypadku sportowiec wchodzi do kolejki wyciągu
        //  prowadzącego do najlepszej trasy.
        else {
            wejdzDoKolejkiWyciagu(sportowiec, czas, najlepszyWyciag, symulacja);
        }
    }

    //  Dodaje zdarzenie rozpoczęcia przejazdu trasą.
    private void rozpocznijPrzejazdTrasa(
        Sportowiec sportowiec,
        int czas,
        Trasa trasa,
        Symulacja symulacja
    ) {
        symulacja.dodajZdarzenie(new RozpoczeciePrzejazduTrasa(
        czas,
        sportowiec,
        trasa
    ));
    }

    //  Dodaje zdarzenie wejścia do kolejki wyciągu.
    private void wejdzDoKolejkiWyciagu(
        Sportowiec sportowiec,
        int czas,
        Wyciag wyciag,
        Symulacja symulacja
    ) {
        symulacja.dodajZdarzenie(new WejscieDoKolejkiWyciagu(
            czas,
            sportowiec,
            wyciag));
    }
}
