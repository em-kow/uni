package zdarzenia;

import model.Sportowiec;
import model.Wyciag;

public class ZakonczeniePrzejazduWyciagiem extends ZakonczeniePrzejazdu {

    public ZakonczeniePrzejazduWyciagiem(
            int czas,
            Sportowiec sportowiec,
            Wyciag wyciag
    ) {
        super(czas, sportowiec, wyciag);
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId() +
            " zakończył przejazd wyciągiem " + polaczenie.getId() + ".";

    }
}
