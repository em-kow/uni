package symulacja;

import dane.DaneSymulacji;
import model.Trasa;
import model.Wyciag;

public class WypisywaczStatystyk {

    //  Klasa ma tylko metody statyczne, więc konstruktor jest prywatny.
    private WypisywaczStatystyk() {

    }

    //  Wypisuje statystyki dotyczące tras i wyciągów.
    public static void wypisz(DaneSymulacji dane) {
        wypiszStatystykiTras(dane);

        wypiszStatystykiWyciagow(dane);
    }

    private static void wypiszStatystykiTras(DaneSymulacji dane) {
        Trasa[] trasy = dane.getTrasy();

        System.out.println("Statystyki tras:");

        for (int i = 0; i < trasy.length; i++) {
            System.out.println(
                    "Trasa " + trasy[i].getId()
                            + ": liczba przejazdów = "
                            + trasy[i].getLiczbaPrzejazdow()
            );
        }
    }

    private static void wypiszStatystykiWyciagow(DaneSymulacji dane) {
        Wyciag[] wyciagi = dane.getWyciagi();

        System.out.println("Statystyki wyciągów:");

        for (int i = 0; i < wyciagi.length; i++) {
            System.out.println(
                    "Wyciąg " + wyciagi[i].getId()
                            + ": liczba przejazdów = "
                            + wyciagi[i].getLiczbaPrzejazdow()
            );
        }
    }
}