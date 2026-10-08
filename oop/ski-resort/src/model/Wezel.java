package model;

public class Wezel {
    
    //  Numer węzła.
    private int id;

    //  Wysokość węzła.
    private int wysokosc;

    //  Współrzędne węzła.
    private int x;
    private int y;

    //  Czy węzeł jest skomunikowany.
    private boolean skomunikowany;

    //  Trasy wychodzące z tego węzła i ich liczba.
    private Trasa[] trasy;
    private int liczbaTras;

    //  Wyciągi wychodzące z tego węzła i ich liczba.
    private Wyciag[] wyciagi;
    private int liczbaWyciagow;

    public Wezel(int id, int wysokosc, int x, int y, boolean skomunikowany) {
        this.id = id;
        this.wysokosc = wysokosc;
        this.x = x;
        this.y = y;
        this.skomunikowany = skomunikowany;

        //  Przy tworzeniu węzła liczba tras i wyciągów z niego wychodzących
        //  jest równa 0. Tworzymy na nie małe tablice, które w razie wypadku
        //  odpowiedno zwiększymy.
        this.trasy = new Trasa[16];
        this.liczbaTras = 0;

        this.wyciagi = new Wyciag[16];
        this.liczbaWyciagow = 0;
    }

    public Trasa[] getTrasy() {
        //  Zwracamy tylko faktycznie istniejące trasy.
        Trasa[] wynik = new Trasa[liczbaTras];

        for (int i = 0; i < liczbaTras; i++) {
            wynik[i] = trasy[i];
        }

        return wynik;
    }

    public Wyciag[] getWyciagi() {
        //  Zwracamy tylko faktycznie istniejące wyciągi.
        Wyciag[] wynik = new Wyciag[liczbaWyciagow];

        for (int i = 0; i < liczbaWyciagow; i++) {
            wynik[i] = wyciagi[i];
        }

        return wynik;
    }

    public void dodajTrase(Trasa trasa) {
        //  Jeśli tablica tras jest za mała to ją powiększamy.
        if (liczbaTras == trasy.length) {
            powiekszTabliceTras();
        }

        trasy[liczbaTras] = trasa;
        liczbaTras++;
    }

    public void dodajWyciag(Wyciag wyciag) {
        //  Jeśli tablica wyciągów jest za mała to ją powiększamy.
        if (liczbaWyciagow == wyciagi.length) {
            powiekszTabliceWyciagow();
        }

        wyciagi[liczbaWyciagow] = wyciag;
        liczbaWyciagow++;
    }

    //  Metoda wywoływana, gdy chcemy coś dodać nową trasę, a tablica jest
    //  cała wypełniona.
    private void powiekszTabliceTras() {
        //  Zwiększamy rozmiar tablicy dwukrotnie, aby nie musieć tego robić
        //  zbyt często, jednocześnie nie marnując dużo pamięci.
        Trasa[] nowa = new Trasa[trasy.length * 2];

        for (int i = 0; i < trasy.length; i++) {
            nowa[i] = trasy[i];
        }

        trasy = nowa;
    }

    //  Metoda wywoływana, gdy chcemy coś dodać nowy wyciąg, a tablica jest
    //  cała wypełniona.
    private void powiekszTabliceWyciagow() {
        //  Zwiększamy rozmiar tablicy dwukrotnie, aby nie musieć tego robić
        //  zbyt często, jednocześnie nie marnując dużo pamięci.
        Wyciag[] nowa = new Wyciag[wyciagi.length * 2];

        for (int i = 0; i < wyciagi.length; i++) {
            nowa[i] = wyciagi[i];
        }

        wyciagi = nowa;
    }
}
