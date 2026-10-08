package dane;
import model.Sportowiec;
import model.Trasa;
import model.Wezel;
import model.Wyciag;

public class DaneSymulacji {

    //  Wszystkie węzły w ośrodku.
    private Wezel[] wezly;

    //  Wszystkie wyciągi w ośrodku.
    private Wyciag[] wyciagi;

    //  Wszystkie trasy w ośrodku.
    private Trasa[] trasy;

    //  Wszyscy sportowcy.
    private Sportowiec[] sportowcy;

    public DaneSymulacji(
        Wezel[] wezly,
        Wyciag[] wyciagi,
        Trasa[] trasy,
        Sportowiec[] sportowcy
    ) {
        this.wezly = wezly;
        this.wyciagi = wyciagi;
        this.trasy = trasy;
        this.sportowcy = sportowcy;
    }

    public Wyciag[] getWyciagi() {
        return wyciagi;
    }

    public Trasa[] getTrasy() {
        return trasy;
    }

    public Sportowiec[] getSportowcy() {
        return sportowcy;
    }
}
