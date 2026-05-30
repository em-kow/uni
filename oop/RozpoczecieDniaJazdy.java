package zdarzenia;
import model.Sportowiec;
import symulacja.Symulacja;

public class RozpoczecieDniaJazdy extends ZdarzenieRuchu {
    
    public RozpoczecieDniaJazdy(int czas, Sportowiec sportowiec) {
        super(czas, sportowiec);
    }

    @Override
    protected void wykonajWlasciwaCzesc(Symulacja symulacja) {
        //  Sportowiec zaczyna w swoim węźle startowym.
        sportowiec.setAktualnyWezel(sportowiec.getWezelStartowy());
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId() +
            " rozpoczął dzień jazdy.";
    }
}
