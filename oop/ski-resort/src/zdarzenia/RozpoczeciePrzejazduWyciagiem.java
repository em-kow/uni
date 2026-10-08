package zdarzenia;

import model.Sportowiec;
import model.Wyciag;
import symulacja.Symulacja;

public class RozpoczeciePrzejazduWyciagiem extends RozpoczeciePrzejazdu {

    //  Wyciąg, którym sportowiec rozpoczyna przejazd.
    private Wyciag wyciag;

    public RozpoczeciePrzejazduWyciagiem(
            int czas,
            Sportowiec sportowiec,
            Wyciag wyciag
    ) {
        super(czas, sportowiec);

        this.wyciag = wyciag;
    }

    @Override
    protected void zwiekszLiczbePrzejazdow() {
        wyciag.zwiekszLiczbePrzejazdow();
    }

    @Override
    protected void zaplanujZakonczeniePrzejazdu(Symulacja symulacja) {
        symulacja.dodajZdarzenie(new ZakonczeniePrzejazduWyciagiem(
                czas + wyciag.getCzasPrzejazdu(),
                sportowiec,
                wyciag
        ));
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId()
                + " rozpoczął przejazd wyciągiem " + wyciag.getId() + ".";
    }
}