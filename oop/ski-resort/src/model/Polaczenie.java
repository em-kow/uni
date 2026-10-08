package model;
public abstract class Polaczenie {

    //  Numer połączenia.
    protected int id;

    //  Węzeł, z którego połączenie wychodzi.
    protected Wezel poczatek;

    //  Węzeł, do którego połączenie wchodzi.
    protected Wezel koniec;

    //  Czas przejazdu w sekundach.
    protected int czasPrzejazdu;

    //  Liczba sportowców, którzy użyli tego połączenia.
    protected int liczbaPrzejazdow;

    public Polaczenie(
            int id,
            Wezel poczatek, 
            Wezel koniec, 
            int czasPrzejazdu
    ) {
        this.id = id;
        this.poczatek = poczatek;
        this.koniec = koniec;
        this.czasPrzejazdu = czasPrzejazdu;

        //  Na początek liczba przejazdów danym połączeniem wynosi 0.
        this.liczbaPrzejazdow = 0;
    }

    public int getId() {
        return id;
    }

    public Wezel getKoniec() {
        return koniec;
    }

    public void zwiekszLiczbePrzejazdow() {
        liczbaPrzejazdow ++;
    }

    public int getCzasPrzejazdu() {
        return czasPrzejazdu;
    }

    public int getLiczbaPrzejazdow() {
        return liczbaPrzejazdow;
    }
}
