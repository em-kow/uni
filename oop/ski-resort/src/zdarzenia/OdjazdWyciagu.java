package zdarzenia;
import model.Sportowiec;
import model.Wyciag;
import symulacja.Symulacja;

public class OdjazdWyciagu extends ZdarzenieWyciagu {
    
    public OdjazdWyciagu(int czas, Wyciag wyciag) {
        super(czas, wyciag);
    }

    @Override
    public void wykonaj(Symulacja symulacja) {
        //  Wyciąg może zabrać nie więcej osób niż ma pojemność oraz nie
        //  więcej osób niż oczekuje.
        int liczbaZabranych = Math.min(
            wyciag.podajLiczbeOczekujacych(),
            wyciag.getPojemnosc()
        );

        for(int i = 0; i < liczbaZabranych; i++) {
            //  Bierzemy aktualnie pierwszego sportowca na kolejce i go
            //  z niej zdejmujemy.
            Sportowiec sportowiec = wyciag.wezZKolejki();

            //  Dodajemy zdarzenie rozpoczęcia przejazdu tym wyciągiem.
            symulacja.dodajZdarzenie(new RozpoczeciePrzejazduWyciagiem(
                czas,
                sportowiec,
                wyciag
            ));

        }

        //  Planujemy następny odjazd tego wyciągu.
        symulacja.dodajZdarzenie(new OdjazdWyciagu(
            czas + wyciag.getOdstep(),
            wyciag
        ));
    }
}
