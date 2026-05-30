package zdarzenia;

import model.Sportowiec;
import symulacja.Symulacja;

//  Zdarzenie, po którym symulacja wybiera sportowcowi kolejny ruch.

public abstract class ZdarzenieRuchu extends ZdarzenieSportowca {
    
    public ZdarzenieRuchu(int czas, Sportowiec sportowiec) {
        super(czas, sportowiec);
    }

    //  Po zakończeniu tego typu zdarzenia sportowiec znajduje się w węźle,
    //  więc symulacja wybiera mu kolejny ruch.
    @Override
    protected void wykonajPoWypisaniu(Symulacja symulacja) {
        symulacja.wybierzKolejnyRuch(sportowiec, getCzas());
    }
}
