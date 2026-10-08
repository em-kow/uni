package zdarzenia;

import model.Sportowiec;
import symulacja.Symulacja;

//  Zdarzenie będące rozpoczęciem przejazdu trasą albo wyciągiem.
public abstract class RozpoczeciePrzejazdu extends ZdarzenieSportowca {

    public RozpoczeciePrzejazdu(int czas, Sportowiec sportowiec) {
        super(czas, sportowiec);
    }

    @Override
    protected void wykonajWlasciwaCzesc(Symulacja symulacja) {
        //  Zwiększamy licznik przejazdów trasy lub wyciągu w zależności
        //  od konkretnej podklasy.
        zwiekszLiczbePrzejazdow();

        //  Każde rozpoczęcie przejazdu musi zaplanować jego zakończenie.
        zaplanujZakonczeniePrzejazdu(symulacja);
    }

    protected abstract void zwiekszLiczbePrzejazdow();

    protected abstract void zaplanujZakonczeniePrzejazdu(Symulacja symulacja);
}