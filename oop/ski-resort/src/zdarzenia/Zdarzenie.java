package zdarzenia;

import symulacja.Symulacja;

public abstract class Zdarzenie {
    
    //  Czas zdarzenia liczony w sekundach od 9:00.
    protected int czas;

    public Zdarzenie(int czas) {
        this.czas = czas;
    }

    public int getCzas() {
        return czas;
    }

    //  Obsłużenie zdarzenia.
    public abstract void wykonaj(Symulacja symulacja);

    //  Sprawdza, czy zdarzenie może zostać dodane do symulacji, czyli czy 
    //  wydarzyło się ono w odpowiednim przedziale czasowym. Domyślnie jest
    //  to koniec jazdy ustawiony w symulacji, ale niektóre podklasy
    //  są wyjątkami więc nadpiszą tę metodę.
    public void sprobujDodacDoSymulacji(Symulacja symulacja) {
        if(symulacja.czyDoKoncaJazdy(czas)) {
            symulacja.dodajDoKolejkiZdarzen(this);
        }
    }
}
