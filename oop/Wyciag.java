package model;
import kolejki.KolejkaSportowcow;

public class Wyciag extends Polaczenie{
    
    //  Odstęp czasowy między zabieraniem grup pasażerów.
    private int odstep;

    //  Maksymalna wielkość grupy, którą jest w stanie pomieścić wyciąg.
    private int pojemnosc;

    //  Kolejka sportowców czekających na wyciąg.
    private KolejkaSportowcow kolejka;

    public Wyciag(
            int id,
            Wezel poczatek,
            Wezel koniec,
            int czasPrzejazdu,
            int odstep,
            int pojemnosc
    ) {
        super(id, poczatek, koniec, czasPrzejazdu);

        this.odstep = odstep;
        this.pojemnosc = pojemnosc;

        //  Początkowo kolejka sportowców czekających na wyciąg jest pusta.
        this.kolejka = new KolejkaSportowcow();
    }

    public void dodajDoKolejki(Sportowiec sportowiec) {
        kolejka.dodaj(sportowiec);
    }

    public int podajLiczbeOczekujacych() {
        return kolejka.getRozmiar();
    }

    public int getPojemnosc() {
        return pojemnosc;
    }

    public Sportowiec wezZKolejki() {
        return kolejka.wezPierwszego();
    }
    
    public int getOdstep() {
        return odstep;
    }
}
