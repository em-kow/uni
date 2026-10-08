package zdarzenia;
import model.Sportowiec;
import model.Wyciag;
import symulacja.Symulacja;

public class WejscieDoKolejkiWyciagu extends ZdarzenieSportowca {
    
    //  Wyciąg, do którego kolejki sportowiec wszedł.
    private Wyciag wyciag;

    public WejscieDoKolejkiWyciagu(
            int czas,
            Sportowiec sportowiec,
            Wyciag wyciag
    ) {
        super(czas, sportowiec);

        this.wyciag = wyciag;
    }

    @Override
    protected void wykonajWlasciwaCzesc(Symulacja symulacja) {
        //  Sportowiec trafia do kolejki oczekujących na wyciąg.
        wyciag.dodajDoKolejki(sportowiec);
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId() +
            " wszedł do kolejki wyciągu " + wyciag.getId() + ".";
    }
}
