package zdarzenia;

import model.Sportowiec;
import model.Trasa;
import symulacja.Symulacja;

public class RozpoczeciePrzejazduTrasa extends RozpoczeciePrzejazdu {

    //  Trasa, którą sportowiec rozpoczyna przejazd.
    private Trasa trasa;

    public RozpoczeciePrzejazduTrasa(
            int czas,
            Sportowiec sportowiec,
            Trasa trasa
    ) {
        super(czas, sportowiec);

        this.trasa = trasa;
    }

    @Override
    protected void zwiekszLiczbePrzejazdow() {
        trasa.zwiekszLiczbePrzejazdow();
    }

    @Override
    protected void zaplanujZakonczeniePrzejazdu(Symulacja symulacja) {
        symulacja.dodajZdarzenie(new ZakonczeniePrzejazduTrasa(
                czas + trasa.getCzasPrzejazdu(),
                sportowiec,
                trasa
        ));
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId()
                + " rozpoczął przejazd trasą " + trasa.getId() + ".";
    }
}