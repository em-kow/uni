package symulacja;

import dane.DaneSymulacji;
import kolejki.KolejkaZdarzen;
import kolejki.TablicowaKolejkaZdarzen;
import model.Sportowiec;
import model.Wyciag;
import narzedzia.Czas;
import zdarzenia.OdjazdWyciagu;
import zdarzenia.RozpoczecieDniaJazdy;
import zdarzenia.Zdarzenie;

public class Symulacja {
    
    //  Od 15:00 czasu już nie obsługujemy przejazdów.
    //  Czas liczony w sekundach od 9:00.
    //  Od 9:00 do 15:00 jest 6 godzin, czyli 6 * 60 * 60 sekund.
    private static final int KONIEC_JAZDY = 6 * 60 * 60;

    //  O 16:00 jest całkowity koniec symulacji, tj. koniec pracy wyciągów.
    //  Czas liczony w sekundach od 9:00.
    //  Od 9:00 do 16:00 jest 7 godzin, czyli 7 * 60 * 60 sekund.
    private static final int KONIEC_SYMULACJI = 7 * 60 * 60;

    private DaneSymulacji dane;

    //  Kolejka zdarzeń przechowująca posortowane zaplanowane zdarzenia.
    private KolejkaZdarzen kolejkaZdarzen;

    //  Obiekt odpowiedzialny za wybór kolejnego ruchu sportowca.
    private WyborKolejnegoRuchu wyborKolejnegoRuchu;

    public Symulacja(DaneSymulacji dane) {
        this.dane = dane;

        //  Implementacja kolejki zdarzeń używająca tablicy.
        this.kolejkaZdarzen = new TablicowaKolejkaZdarzen();

        this.wyborKolejnegoRuchu = new WyborKolejnegoRuchu();

        //  Przy tworzeniu nowej symulacji dodaj najpierw jej zdarzenia
        //  początkowe.
        dodajPoczatkoweZdarzenia();
    }

    //  Dodawanie zdarzeń początkowych każdej symulacji, czyli rozpoczynanie
    //  dnia jazdy przez każdego ze sportowców oraz pierwsze odjazdy wyciągów.
    private void dodajPoczatkoweZdarzenia() {
        dodajRozpoczeciaJazdy();

        dodajPierwszeOdjazdyWyciagow();
    }

    private void dodajRozpoczeciaJazdy() {
        Sportowiec[] sportowcy = dane.getSportowcy();

        for (int i = 0; i < sportowcy.length; i++) {
            Sportowiec sportowiec = sportowcy[i];

            dodajZdarzenie(new RozpoczecieDniaJazdy(
                    sportowiec.getCzasRozpoczecia(),
                    sportowiec
            ));
        }
    }

    private void dodajPierwszeOdjazdyWyciagow() {
        Wyciag[] wyciagi = dane.getWyciagi();

        for (int i = 0; i < wyciagi.length; i++) {
            Wyciag wyciag = wyciagi[i];

            //  Pierwsze odjazdy wyciągów są w godzinie rozpoczęcia symulacji.
            dodajZdarzenie(new OdjazdWyciagu(
                    0,
                    wyciag
            ));
        }
    }

    public void dodajDoKolejkiZdarzen(Zdarzenie zdarzenie) {
        kolejkaZdarzen.dodaj(zdarzenie);
    }

    //  Dodajemy zdarzenie do symulacji.
    public void dodajZdarzenie(Zdarzenie zdarzenie) {
        //  Zamiast wrzucać od razu zdarzenie do kolejki, przekazujemy mu 
        //  możliwość podjęcia dezycji, czy można to zrobić, ponieważ różne 
        //  zdarzenia mają różny koniec czasu, w którym mogą być dodane.
        zdarzenie.sprobujDodacDoSymulacji(this);

    }

    //  Sprawdza, czy podany moment mieści się w czasie, w którym obsługiwane są
    //  początki przejazdów.
    public boolean czyDoKoncaJazdy(int czas) {
        return czas < KONIEC_JAZDY;
    }

    //  Sprawdza, czy podany moment mieści się w czasie, w którym wyciągi
    //  pracują (symulacja dalej trwa).
    public boolean czyDoKoncaSymulacji(int czas) {
        return czas < KONIEC_SYMULACJI;
    }

    public void uruchom() {
        //  Rozpatrujemy wszystkie zdarzenia z kolejki, dopóki nie
        //  jest ona pusta.
        while(!kolejkaZdarzen.czyPusta()) {
            //  Pobieramy zdarzenie z początku kolejki i je wykonujemy,
            Zdarzenie zdarzenie = kolejkaZdarzen.wezPierwsze();
            zdarzenie.wykonaj(this);
        }
    }

    //  Wybiera sportowcowi kolejny ruch.
    public void wybierzKolejnyRuch(Sportowiec sportowiec, int czas) {
        wyborKolejnegoRuchu.wybierzKolejnyRuch(sportowiec, czas, this);
    }   

    //  Wypisuje komunikat o zdarzeniu razem z jego czasem.
    public void wypisz(int czas, String komunikat) {
        System.out.println(Czas.sekundyNaHms(czas) + ": " + komunikat);
    }

    //  Wypisywanie statystyk, tj. ile sportowców przejechało każdą
    //  trasą i wyciągiem. Używamy do tego dedykowanej klasy.
    public void wypiszStatystyki() {
        WypisywaczStatystyk.wypisz(dane);
    }
}
