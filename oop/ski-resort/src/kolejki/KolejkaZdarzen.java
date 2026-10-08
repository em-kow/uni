package kolejki;
import zdarzenia.Zdarzenie;

public interface KolejkaZdarzen {
    
    //  Dodanie nowego zdarzenia do kolejki.
    void dodaj(Zdarzenie zdarzenie);

    //  Pobieranie i usunięcie pierwszego elementu z kolejki, tj. zdarzenia o 
    //  najwcześniejszym momencie wykonania, a w przypadku remisów, tego 
    //  wcześniej dodanego.
    Zdarzenie wezPierwsze();

    //  Sprawdza, czy kolejka jest pusta tj. nie ma na niej żadnych zdarzeń.
    boolean czyPusta();
}
