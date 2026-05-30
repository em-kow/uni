package kolejki;
//  Implementacja kolejki zdarzeń za pomocą posortowanej tablicy.

import zdarzenia.Zdarzenie;

public class TablicowaKolejkaZdarzen implements KolejkaZdarzen {
    
    //  Tablica przechowuje zdarzenia posortowane rosnąco po czasie (w 
    //  przypadku remisów decyduje moment dodania do kolejki).
    private Zdarzenie[] tab;

    //  Rozmiar kolejki - liczba aktualnie przechowywanych zdarzeń.
    private int rozmiar;

    public TablicowaKolejkaZdarzen() {
        this.tab = new Zdarzenie[128];

        //  Początkowo kolejka jest pusta.
        this.rozmiar = 0;
    }

    //  Metoda wywoływana, gdy chcemy coś dodać do kolejki, a tablica jest
    //  cała wypełniona.
    private void powieksz() {
        //  Zwiększamy rozmiar tablicy dwukrotnie, aby nie musieć tego robić
        //  zbyt często, jednocześnie nie marnując dużo pamięci.
        Zdarzenie[] nowa = new Zdarzenie[tab.length * 2];

        for(int i = 0; i < tab.length; i++) nowa[i] = tab[i];

        tab = nowa;
    }

    @Override
    public void dodaj(Zdarzenie zdarzenie) {
        //  W przypadku gdy tablica jest cała wypełniona zwiększamy ją.
        if(rozmiar == tab.length) {
            powieksz();
        }

        //  Miejsce w tablicy dodawanego zdarzenia.
        int indeks = 0;

        //  Szukamy poprawnego indeksu. Używamy nieostrej nierówności, aby
        //  poprawnie rozstrzygnąć remisy.
        while(indeks < rozmiar 
                && tab[indeks].getCzas() <= zdarzenie.getCzas()) {
            indeks++;
        }

        //  Przesuwamy w prawo elementy za dodawanym zdarzeniem, aby zrobić
        //  dla niego miejsce.
        for(int i = rozmiar; i > indeks; i--) {
            tab[i] = tab[i - 1];
        }

        tab[indeks] = zdarzenie;

        rozmiar++;
    }

    @Override
    public boolean czyPusta() {
        return rozmiar == 0;
    }

    @Override
    public Zdarzenie wezPierwsze() {
        //  Sprawdzenie, czy nie próbujemy pobrać zdarzenia z pustej kolejki.
        if(czyPusta()) {
            throw new IllegalStateException("Kolejka zdarzeń jest pusta");
        }

        Zdarzenie pierwsze = tab[0];

        //  Usuwamy pierwszy element przesuwając pozostałe w lewo.
        for(int i = 1; i < rozmiar; i++) {
            tab[i - 1] = tab[i];
        }

        rozmiar--;
        tab[rozmiar] = null;
        
        return pierwsze;
    }
}
