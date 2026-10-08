package zdarzenia;

import model.Sportowiec;
import model.Trasa;

public class ZakonczeniePrzejazduTrasa extends ZakonczeniePrzejazdu {

    public ZakonczeniePrzejazduTrasa(
            int czas,
            Sportowiec sportowiec,
            Trasa trasa
    ) {
        super(czas, sportowiec, trasa);
    }

    @Override
    public String toString() {
        return "Sportowiec " + sportowiec.getId() +
            " zakończył przejazd trasą " + polaczenie.getId() + ".";

    }
}
