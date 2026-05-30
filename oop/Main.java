import dane.DaneSymulacji;
import dane.WczytywaczDanych;
import symulacja.Symulacja;

public class Main {

    public static void main(String[] args) {
        WczytywaczDanych wczytywacz = new WczytywaczDanych();

        DaneSymulacji dane = wczytywacz.wczytaj();

        Symulacja symulacja = new Symulacja(dane);

        symulacja.uruchom();

        symulacja.wypiszStatystyki();
    }
}