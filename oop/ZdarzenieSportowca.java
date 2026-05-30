package zdarzenia;

import model.Sportowiec;
import symulacja.Symulacja;

//  Zdarzenie dotyczące sportowca.
public abstract class ZdarzenieSportowca extends Zdarzenie {
    
    //  Sportowiec, którego dotyczy to zdarzenie.
    protected Sportowiec sportowiec;

    public ZdarzenieSportowca(int czas, Sportowiec sportowiec) {
        super(czas);

        this.sportowiec = sportowiec;
    }

    @Override
    public final void wykonaj(Symulacja symulacja) {
        //  Podczas wykonywania zdarzenia sportowca najpierw wykonujemy
        //  właściwą część.
        wykonajWlasciwaCzesc(symulacja);

        //  Wypisujemy informacje o zdarzeniu, jeśli sportowiec jest śledzony.
        if (sportowiec.czySledzony()) {
            symulacja.wypisz(getCzas(), toString());
        }

        //  Wykonujemy ewentualną część zdarzenia po wypisaniu.
        wykonajPoWypisaniu(symulacja);
    }

    //  Właściwa część zdarzenia zależna od konkretnej podklasy.
    protected abstract void wykonajWlasciwaCzesc(Symulacja symulacja);

    //  Zazwyczaj po wypisaniu nic się nie dzieje, ale niektóre podklasy
    //  mogą to nadpisać.
    protected void wykonajPoWypisaniu(Symulacja symulacja) {

    }

}
