package kolejki;
import model.Sportowiec;

public class KolejkaSportowcow {
    
    //  Implementujemy kolejkę sportowców oczekujących na wjazd jako bufor 
    //  cykliczny.
    private Sportowiec[] tab;
    private int poczatek;
    private int rozmiar;

    public KolejkaSportowcow() {
        this.tab = new Sportowiec[128];

        //  Najpierw tablica nie ma żadnego cyklicznego przesunięcia, więc
        //  początek jest na swoim miejscu.
        this.poczatek = 0;

        //  Najpierw w kolejce mamy 0 sportowców.
        this.rozmiar = 0;
    }

    //  Metoda wywoływana, gdy chcemy coś dodać do kolejki, a tablica jest
    //  cała wypełniona.
    private void powieksz() {
        //  Zwiększamy rozmiar tablicy dwukrotnie, aby nie musieć tego robić
        //  zbyt często, jednocześnie nie marnując dużo pamięci.
        Sportowiec[] nowa = new Sportowiec[tab.length * 2];

        //  Powiększoną tablicę wypełniamy tak, aby nie miała żadnego cykliczne-
        //  go przesunięcia, czyli początek był na indeksie 0.
        for(int i = 0; i < rozmiar; i ++) {
            nowa[i] = tab[(poczatek + i) % tab.length];
        }
        poczatek = 0;

        tab = nowa;
    }

    public boolean czyPusta() {
        return rozmiar == 0;
    }

    //  Dodawanie sportowca na koniec kolejki.
    public void dodaj(Sportowiec sportowiec) {
        //  Sprawdzenie, czy mamy miejsce w tablicy i ewentualne zwiększenie.
        if(tab.length == rozmiar) powieksz();

        //  Zgodnie z zasadami przesunięcia cyklicznego obliczamy indeks 
        //  miejsca odpowiadającego końcu kolejki.
        int koniec = (poczatek + rozmiar) % tab.length;

        tab[koniec] = sportowiec;
        rozmiar++;
    }

    //  Pobranie i usunięcie pierwszego sportowca z kolejki.
    public Sportowiec wezPierwszego() {
        //  Sprawdzenie, czy nie próbujemy pobrać sportowca z pustej kolejki.
        if(czyPusta()) {
            throw new IllegalStateException("Kolejka sportowców jest pusta");
        }

        Sportowiec pierwszy = tab[poczatek];

        //  Aktualizujemy dane tablicy po usunięciu pierwszego sportowca.
        tab[poczatek] = null;
        poczatek = (poczatek + 1) % tab.length;
        rozmiar--;

        return pierwszy;
    }

    public int getRozmiar() {
        return rozmiar;
    }
}
