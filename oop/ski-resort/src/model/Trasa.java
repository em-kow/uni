package model;

public class Trasa extends Polaczenie{

    //  Poziom trudności trasy.
    private int poziomTrudnosci;

    //  Wyrównanie nawierzchni trasy na początku.
    private double bazoweWyrownanieNawierzchni;

    //  Wyrównanie nawierzchni trasy w tym momencie.
    private double aktualneWyrownanieNawierzchni;

    //  Odporność trasy na zużycie.
    private double odpornosc;

    public Trasa(
            int id,
            Wezel poczatek,
            Wezel koniec,
            int czasPrzejazdu,
            int poziomTrudnosci,
            double bazoweWyrownanieNawierzchni,
            double odpornosc
    ) {
        super(id, poczatek, koniec, czasPrzejazdu);

        this.poziomTrudnosci = poziomTrudnosci;
        this.bazoweWyrownanieNawierzchni = bazoweWyrownanieNawierzchni;
        this.odpornosc = odpornosc;

        //  Podstawiając do wzoru liczbę przejazdów równą 0 otrzymujemy, że 
        //  początkowe wyrównanie nawierzchni wynosi 1.0
        this.aktualneWyrownanieNawierzchni = 1.0;
    }

    @Override
    public void zwiekszLiczbePrzejazdow(){
        super.zwiekszLiczbePrzejazdow();

        //  Aktualizujemy wyrównanie trasy na podstawie przerobionego wzoru z 
        //  treści.
        //  aktualne = bazowe + (1 - bazowe) * exp(odpornosc, liczba przejazdow)
        //  przerobione algebraicznie na:
        //  aktualne = bazowe + (aktualne - bazowe) * odpornosc
        aktualneWyrownanieNawierzchni = bazoweWyrownanieNawierzchni + 
        (aktualneWyrownanieNawierzchni - bazoweWyrownanieNawierzchni) * 
        odpornosc;
    }

    //  Oblicza dopasowanie trudności tej trasy do poziomu sportowca.
    private double obliczDopasowanieTrudnosci(Sportowiec sportowiec) {
        int poziomSportowca = sportowiec.getPoziom();

        if(poziomTrudnosci >= poziomSportowca + 5) {
            return 0.0;
        }

        if(poziomTrudnosci >= poziomSportowca) {
            return 1.0 - (poziomTrudnosci - poziomSportowca) / 5.0;
        }

        return Math.max(
            0.2,
            1.0 - (poziomSportowca - poziomTrudnosci) / 7.0
        );
    }

    //  Oblicza atrakcyjność tej trasy dla danego sportowca.
    public double obliczAtrakcyjnosc(Sportowiec sportowiec) {
        double dopasowanieTrudnosci = obliczDopasowanieTrudnosci(sportowiec);

        return sportowiec.getWagaNawierzchni() * aktualneWyrownanieNawierzchni 
            + sportowiec.getWagaTrudnosci() * dopasowanieTrudnosci;
    }
}
