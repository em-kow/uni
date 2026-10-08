package zdarzenia;

import model.Wyciag;

public abstract class ZdarzenieWyciagu extends Zdarzenie {

    //  Wyciąg, którego dotyczy to zdarzenie.
    protected Wyciag wyciag;

    public ZdarzenieWyciagu(int czas, Wyciag wyciag) {
        super(czas);

        this.wyciag = wyciag;
    }
}
