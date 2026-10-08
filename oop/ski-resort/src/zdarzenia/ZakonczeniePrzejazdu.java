package zdarzenia;

import model.Polaczenie;
import model.Sportowiec;
import symulacja.Symulacja;

//  Zdarzenie będące zakończeniem przejazdu wyciągiem lub trasą.
public abstract class ZakonczeniePrzejazdu extends ZdarzenieRuchu {
    
    //  Połączenie, którym sportowiec przejechał (trasa lub wyciąg).
    protected Polaczenie polaczenie;

    public ZakonczeniePrzejazdu(
            int czas,
            Sportowiec sportowiec,
            Polaczenie polaczenie
    ) {
        super(czas, sportowiec);
        
        this.polaczenie = polaczenie;
    }

    //  Zdarzenia będące końcem przejazdu mogą zostać dodane do symulacji
    //  do jej końca.
    @Override
    public void sprobujDodacDoSymulacji(Symulacja symulacja) {
        if(symulacja.czyDoKoncaSymulacji(czas)) {
            symulacja.dodajDoKolejkiZdarzen(this);
        }
    }

    @Override
    protected void wykonajWlasciwaCzesc(Symulacja symulacja) {
        //  Po zakończonym przejeździe sportowiec znajduje się w końcowym 
        //  węźle połączenia.
        sportowiec.setAktualnyWezel(polaczenie.getKoniec());
    }
}
