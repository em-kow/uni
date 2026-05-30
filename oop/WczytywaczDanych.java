package dane;
import java.util.Scanner;

import model.Sportowiec;
import model.Trasa;
import model.Wezel;
import model.Wyciag;
import narzedzia.Czas;

import java.util.Locale;

public class WczytywaczDanych {

    //  Scanner czytający wejście linia po linii.
    private Scanner scannerWejscia;

    public WczytywaczDanych() {
        this.scannerWejscia = new Scanner(System.in);
    }

    private String wczytajNiepustaLinie() {
        //  .trim() usuwa zbędne białe znaki z początku i końca linii.
        String linia = scannerWejscia.nextLine().trim();

        while (linia.isEmpty()) {
            linia = scannerWejscia.nextLine().trim();
        }

        return linia;
    }

    //  Scanner czytający jedną konkretną linię.
    private Scanner utworzScannerLinii(String linia) {
        Scanner scannerLinii = new Scanner(linia);

        //  Liczby zmiennoprzecinkowe używają kropek.
        scannerLinii.useLocale(Locale.ENGLISH);

        return scannerLinii;
    }

    //  Główna metoda wczytująca wszystkie sekcje wejścia.
    public DaneSymulacji wczytaj() {
        Wezel[] wezly = wczytajWezly();
        Wyciag[] wyciagi = wczytajWyciagi(wezly);
        Trasa[] trasy = wczytajTrasy(wezly);
        Sportowiec[] sportowcy = wczytajSportowcow(wezly);

        //  Zwraca obiekt z danymi całej symulacji.
        return new DaneSymulacji(wezly, wyciagi, trasy, sportowcy);
    }

    private Wezel[] wczytajWezly() {
        //  Pierwsza liczba mówi ile węzłów wczytujemy.
        int liczbaWezlow = Integer.parseInt(wczytajNiepustaLinie());

        //  Tworzymy tablicę na wszystkie węzły.
        Wezel[] wezly = new Wezel[liczbaWezlow];

        for(int i = 0; i < liczbaWezlow; i++) {
            //  Scanner czytający opis jednego węzła.
            Scanner scannerLinii = utworzScannerLinii(wczytajNiepustaLinie());

            //  Opis węzła: wysokość bezwzględna, para współrzędnych, 
            //  "s" jeśli jest skomunikowany.
            int wysokosc = scannerLinii.nextInt();

            int x = scannerLinii.nextInt();
            int y = scannerLinii.nextInt();

            //  Jeśli na końcu linii jest "s" to węzeł jest skomunikowany.
            boolean skomunikowany = false;

            if (scannerLinii.hasNext()) {
                String oznaczenie = scannerLinii.next();

                if(oznaczenie.equals("s")) {
                    skomunikowany = true;
                }
            }

            // i to numer węzła bo wczytujemy je po kolei.
            wezly[i] = new Wezel(i, wysokosc, x, y, skomunikowany);

            scannerLinii.close();
        }

        return wezly;
    }

    private Wyciag[] wczytajWyciagi(Wezel[] wezly) {
        //  Pierwsza liczba mówi ile wyciągów wczytujemy.
        int liczbaWyciagow = Integer.parseInt(wczytajNiepustaLinie());

        Wyciag[] wyciagi = new Wyciag[liczbaWyciagow];

        for(int i = 0; i < liczbaWyciagow; i ++) {
            //  Scanner czytający opis jednego wyciągu.
            Scanner scannerLinii = utworzScannerLinii(wczytajNiepustaLinie());

            //  Opis wyciągu: numer początku, numer końca, odstęp między
            //  odjazdami, pojemność, czas przejazdu.
            int numerPoczatku = scannerLinii.nextInt();
            int numerKonca = scannerLinii.nextInt();
            int odstep = scannerLinii.nextInt();
            int pojemnosc = scannerLinii.nextInt();
            int czasPrzejazdu = scannerLinii.nextInt();

            Wezel poczatek = wezly[numerPoczatku];
            Wezel koniec = wezly[numerKonca];

            //  i to numer wyciągu bo wczytujemy je po kolei.
            wyciagi[i] = new Wyciag(
                i,
                poczatek, 
                koniec, 
                czasPrzejazdu, 
                odstep, 
                pojemnosc);
            
            //  Ten wyciąg wychodzi ze swojego węzła początkowego.
            poczatek.dodajWyciag(wyciagi[i]);

            scannerLinii.close();

        }

        return wyciagi;
    }

    private Trasa[] wczytajTrasy(Wezel[] wezly) {
        //  Pierwsza liczba mówi ile tras wczytujemy.
        int liczbaTras = Integer.parseInt(wczytajNiepustaLinie());

        Trasa[] trasy = new Trasa[liczbaTras];

        for (int i = 0; i < liczbaTras; i++) {
            //  Scanner czytający opis jednej trasy.
            Scanner scannerLinii = utworzScannerLinii(wczytajNiepustaLinie());

            //  Opis trasy: numer początku, numer końca, poziom trudności,
            //  czas przejazdu, bazowe wyrównanie nawierzchni, odporność
            //  na nierówności.
            int numerPoczatku = scannerLinii.nextInt();
            int numerKonca = scannerLinii.nextInt();
            int poziomTrudnosci = scannerLinii.nextInt();
            int czasPrzejazdu = scannerLinii.nextInt();
            double bazoweWyrownanieNawierzchni = scannerLinii.nextDouble();
            double odpornosc = scannerLinii.nextDouble();

            Wezel poczatek = wezly[numerPoczatku];
            Wezel koniec = wezly[numerKonca];

            //  i to numer trasy bo wczytujemy je po kolei.
            trasy[i] = new Trasa(
                    i,
                    poczatek,
                    koniec,
                    czasPrzejazdu,
                    poziomTrudnosci,
                    bazoweWyrownanieNawierzchni,
                    odpornosc
            );

            //  Ta trasa wychodzi ze swojego węzła początkowego.
            poczatek.dodajTrase(trasy[i]);

            scannerLinii.close();
        }

        return trasy;
    }

    private Sportowiec[] wczytajSportowcow(Wezel[] wezly) {
        //  Pierwsza liczba mówi, ile grup sportowców wczytujemy.
        int liczbaGrup = Integer.parseInt(wczytajNiepustaLinie());

        //  Nie znamy od razu łącznej liczby sportowców, więc tworzymy tablicę
        //  z tymczasowym rozmiarem, którą w razie wypadku będziemy zwiększać.
        Sportowiec[] sportowcy = new Sportowiec[128];
        int liczbaSportowcow = 0;

        for (int i = 0; i < liczbaGrup; i++) {
            //  Scanner czytający pierwszą linię opisu jednej grupy sportowców.
            Scanner pierwszaLinia = utworzScannerLinii(wczytajNiepustaLinie());

            //  Pierwsza linia opisu: liczba sportowców w grupie, poziom 
            //  zaawansowania, współczynnik spontaniczności, "s" jeśli mają
            //  być śledzeni.
            int liczbaSportowcowWGrupie = pierwszaLinia.nextInt();
            int poziom = pierwszaLinia.nextInt();
            double spontanicznosc = pierwszaLinia.nextDouble();

            boolean sledzeni = false;

            //  Jeśli na końcu linii jest "s" to grupa jest śledzona.
            if (pierwszaLinia.hasNext()) {
                String oznaczenie = pierwszaLinia.next();

                if (oznaczenie.equals("s")) {
                    sledzeni = true;
                }
            }

            pierwszaLinia.close();

            //  Scanner czytający drugą linię opisu jednej grupy sportowców.
            Scanner drugaLinia = utworzScannerLinii(wczytajNiepustaLinie());

            //  Druga linia opisu: waga poszczególnych aspektów atrakcyjności:
            //  dopasowania poziomem i wyrównania nawierzchni.
            double wagaTrudnosci = drugaLinia.nextDouble();
            double wagaNawierzchni = drugaLinia.nextDouble();

            drugaLinia.close();

            //  Scanner czytający trzecią linię opisu jednej grupy sportowców.
            Scanner trzeciaLinia = utworzScannerLinii(wczytajNiepustaLinie());

            //  Trzecia linia opisu: numer startowego węzła, godzina rozpoczę-
            //  cia dnia na stoku przez pierwszego z grupy, odstęp czasowy 
            //  między rozpoczęciami (jeśli jest więcej niż jeden sportowiec
            //  w grupie).
            int numerWezlaStartowego = trzeciaLinia.nextInt();
            String godzinaRozpoczeciaPierwszego = trzeciaLinia.next();

            //  Konwertujemy godzinę rozpoczęcia dnia na stoku przez pierwszego
            //  sportowca z grupy na czas w sekundach od 9:00.
            int czasRozpoczeciaPierwszego = 
                    Czas.hmsNaSekundy(godzinaRozpoczeciaPierwszego);

            //  Odstęp między rozpoczęciami jest podany tylko dla grup składa-
            //  jących się z więcej niż jednego sportowca. Dla przejrzystości
            //  obliczeń, gdy mamy tylko jednego sportowca zakładamy, że odstęp
            //  wynosi 0.
            int odstepMiedzyStartami = 0;

            if (trzeciaLinia.hasNextInt()) {
                odstepMiedzyStartami = trzeciaLinia.nextInt();
            }

            trzeciaLinia.close();

            Wezel wezelStartowy = wezly[numerWezlaStartowego];

            // Na podstawie grupy tworzymy podaną liczbę sportowców.
            for (int j = 0; j < liczbaSportowcowWGrupie; j++) {
                //  Jeśli cała tablica sportowców jest wypełniona
                //  powiększamy ją.
                if (liczbaSportowcow == sportowcy.length) {
                    //  Zwiększamy rozmiar tablicy dwukrotnie, aby nie musieć 
                    //  tego robić zbyt często, jednocześnie nie marnując 
                    //  dużo pamięci.
                    Sportowiec[] nowa = new Sportowiec[sportowcy.length * 2];

                    for (int k = 0; k < sportowcy.length; k++) {
                        nowa[k] = sportowcy[k];
                    }

                    sportowcy = nowa;

                }

                //  Obliczamy czas rozoczęcia na podstawie odstępu.
                int czasStartu = czasRozpoczeciaPierwszego 
                                    + j * odstepMiedzyStartami;

                //  Liczba aktualnie utworzonych sportowców to numer nowo
                //  utworzonego sportowca, bo tworzymy ich po kolei.
                sportowcy[liczbaSportowcow] = new Sportowiec(
                        liczbaSportowcow,
                        poziom,
                        spontanicznosc,
                        sledzeni,
                        wagaTrudnosci,
                        wagaNawierzchni,
                        wezelStartowy,
                        czasStartu
                );

                liczbaSportowcow++;
            }
        }

        // Zwracamy tablicę sportowców dokładnej długości, bez pustych miejsc.
        Sportowiec[] wynik = new Sportowiec[liczbaSportowcow];

        for (int i = 0; i < liczbaSportowcow; i++) {
            wynik[i] = sportowcy[i];
        }

        return wynik;
    }
}
