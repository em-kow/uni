// Autor rozwiązania: Mikołaj Kowalski (479520)



#include <stdio.h>
#include <errno.h>
#include <stdbool.h>
#include <stdlib.h>
#include <ctype.h>
#include <inttypes.h>
#include "rstack.h"



// Typ elementu na stosie:
// TYPE_VALUE - wartość,
// TYPE_STACK - referencja do innego stosu.
typedef enum {
    TYPE_VALUE,
    TYPE_STACK
} elem_type_t;


// Element listy elementów na stosie.
typedef struct elem {
    elem_type_t type; // Typ elementu.

    union {
        uint64_t value; // Wartość w przypadku gdy element jest liczbą.

        struct rstack *stack; // Referencja do innego stosu wpp.
    } data;

    struct elem* next; // Wskaźnik na o jeden głębszy element stosu.
} elem_t;

// Kolory używane przez pomocniczy algorytm wykrywania i usuwania nieosiągalnych
// stosów oraz tzw. "martwych" cykli, tj. takich komponentów grafu referencji,
// które nie są już osiągalne z poziomu użytkownika i utrzymują się wyłącznie
// dzięki referencjom wewnętrznym między swoimi elementami oraz 
// nieosiągalnych stosów. 
typedef enum {
    COLOR_BLACK, // Obiekt nie jest kandydatem do usunięcia.

    COLOR_GRAY, // Obiekt aktualnie badany.

    COLOR_WHITE, // Obiekt nieosiągalny.

    COLOR_PURPLE // Obiekt nieosiągalny już zebrany do usunięcia.
} color_t;


// Struktura opisująca stos rekurencyjny.
struct rstack {
    size_t refcount; // Licznik wszystkich referencji do stosu.

    elem_t *top; // Wskaźnik na wierzchołek listy elementów stosu.

    bool visited; // Znacznik odwiedzenia używany podczas przechodzenia po 
    // strukturze stosów algorytmem DFS.

    color_t color; // Aktualny stan stosu w algorytmie wykrywania i usuwania
    // nieosiągalnych stosów i martwych cykli w grafie referencji.

    bool in_write; // Znacznik informujący, czy stos jest aktualnie
    // przetwarzany podczas zapisu do pliku.

    struct rstack *collect_next; // Jeśli stos został uznany za nieosiągalny,
    // ten wskaźnik wskazuje na kolejny nieosiągalny stos do usunięcia.
};


// r_stack_new() - Funkcja tworząca nowy pusty stos.
rstack_t *rstack_new() {
    rstack_t *rs = malloc(sizeof(rstack_t));

    if (rs == nullptr) {
        errno = ENOMEM;
        return nullptr;
    }

    rs->refcount = 1; // Stos ma jedną referencję.

    rs->top = nullptr; // Stos nie zawiera żadnych elementów.

    rs->visited = false; // Domyślnie stos nie jest odwiedzony w 
    // algorytmie DFS.

    rs->color = COLOR_BLACK; // Domyślny kolor stosu to COLOR_BLACK.

    rs->in_write = false; // Domyślnie stos nie jest na
    // ścieżce rekurencji.

    rs->collect_next = nullptr; // Domyślnie stos nie jest
    // uznany za nieosiągalny.

    return rs;
}


// rstack_push_value() - Funkcja odkładjąca lizcbę na stos.
int rstack_push_value(rstack_t *rs, uint64_t value) {
    if (rs == nullptr) {
        errno = EINVAL;
        return -1;
    }

    // Alokujemy pamięć na nowy element stosu.
    elem_t *e = malloc(sizeof(elem_t));
    if (e == nullptr) {
        errno = ENOMEM;
        
        return -1;
    }

    // Nowy element reprezentuje zwykłą wartość liczbową.
    e->type = TYPE_VALUE;
    e->data.value = value;

    // Nowy element ma wskazywać na poprzedni wierzchołek stosu,
    // dzięki czemu stanie się nowym początkiem listy.
    e->next = rs->top;

    // Aktualizujemy wierzchołek stosu.
    rs->top = e;

    return 0;
}


// rstack_push_rstack() - Funkcja odkładająca stos na stos.
int rstack_push_rstack(rstack_t *rs1, rstack_t *rs2) {
    if (rs1 == nullptr || rs2 == nullptr) {
        errno = EINVAL;
        return -1;
    }

    // Alokujemy pamięć na nowy element stosu.
    elem_t *e = malloc(sizeof(elem_t));
    if (e == nullptr) {
        errno = ENOMEM;
        return -1;
    }

    // Nowy element będzie przechowywał refrencję do innego stosu.
    e->type = TYPE_STACK;
    e->data.stack = rs2;

    // Nowy element ma wskazywać na poprzedni wierzchołek stosu rs1,
    // dzięki czemu stanie się nowym początkiem listy.
    e->next = rs1->top;

    // Aktualizujemy wierzchołek stosu rs1.
    rs1->top = e;

    // Odkładany stos rs2 zyskuje nową refrencję.
    rs2->refcount++;

    return 0;
}

// rstack_empty() - Funkcja sprawdzająca rekurencyjnie, 
// czy stos zawiera liczbę.
// Korzysta ona z dwóch funkcji pomocniczych:
// rstack_empty_dfs() - Funkcja przeszukująca graf referencji algorytmem DFS
// w celu znalezienia liczby na którymś ze stosów.
// rstack_clear_visited() - Funkcja ustawiająca z powrotem znaczniki visited
// stosów na false.

static bool rstack_empty_dfs(rstack_t *rs) {
    if (rs == nullptr) {
        return true;
    }

    // Ustawiamy stos jako odwiedzony, 
    // aby nie wywoływać się w nim ponownie.
    rs->visited = true;

    // Iterujemy się po elementach stosu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // Jeśli element stosu jest liczbą, to znaczy,
        // że nie jest on pusty.
        if (e->type == TYPE_VALUE) {
            return false;
        }

        // Jeśli element stosu jest innym stosem, rekurencyjnie
        // się w nim wywołujemy, o ile nie zrobiliśmy tego wcześniej.
        if (e->type == TYPE_STACK) {
            // child wskazuje na aktualnie analizowany podstos 
            // (stos leżący na naszym stosie).
            rstack_t *child = e->data.stack;

            // Sprawdzamy czy wywołaliśmy się już wcześniej na
            // tym rozpatrywanym podstosie, aby uniknąć zapętlenia
            if (child != nullptr && !child->visited) {
                // Jeśli znaleźliśmy na nim liczbę, 
                // to nasz stos nie jest pusty.
                if (!rstack_empty_dfs(child)) {
                    return false;
                }
            }
        }
    }

    // Jeśli nie udało nam się znaleźć liczby na naszym stosie ani 
    // rekurencyjnie na stosach na nim leżących, to stos jest pusty.
    return true;
}

static void rstack_clear_visited(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // Ustawiamy z powrotem stos jako nieodwiedzony.
    rs->visited = false;

    // Iterujemy się po elementach stosu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // child wskazuje na aktualnie rozpatrywany podstos
            //  (stos leżący na naszym stosie).
            rstack_t *child = e->data.stack;

            // Sprawdzamy, czy rozpatrywany podstos jest odwiedzony. 
            // Jeśli tak, wywołujemy się w nim.
            if (child != nullptr && child->visited) {
                rstack_clear_visited(e->data.stack);
            }
        }
    }
}

bool rstack_empty(rstack_t *rs) {
    // Szukamy rekurencyjnie na stosie liczby 
    // za pomocą dedykowanej funkcji.
    bool result = rstack_empty_dfs(rs);

    // Przywracamy rekurencyjnie stosom
    // status nieodwiedzonych.
    rstack_clear_visited(rs);

    // Zwracamy znaleziony wcześniej wynik.
    return result;
}


// rstack_front() - Funkcja znajdująca rekurencyjnie liczbę, która 
// jest najbliżej wierzchołka stosu.
// Korzysta ona z dwóch funkcji pomocniczych:
// rstack_front_dfs() - Funkcja przeszukująca graf referencji algorytmem
// DFS do momentu, aż znajdzie liczbę - to będzie liczba najbliżej
// wierzchołka stosu.
// rstack_clear_visited() - Funkcja pomocnicza użyta przy implementacji
// funkcji rstack_empty().


static result_t rstack_front_dfs(rstack_t *rs) {
    if (rs == nullptr) {
        return (result_t){ .flag = false, .value = 0 };
    }

    // Ustawiamy stos jako odwiedzony, aby nie wywoływać się w nim ponownie.
    rs->visited = true;

    // Iterujemy się po elementach stosu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // Jeśli element stosu jest liczbą, zwracamy ją
        // jako liczbę najbliżej wierzchołka.
        if (e->type == TYPE_VALUE) {
            return (result_t){ .flag = true, .value = e->data.value };
        }

        // Jeśli element stosu jest innym stosem, rekurencyjnie
        // się w nim wywołujemy, o ile nie zrobiliśmy tego wcześniej.
        if (e->type == TYPE_STACK) {
            // child wskazuje na aktualnie rozpatrywany podstos 
            // (stos leżący na naszym stosie).
            rstack_t *child = e->data.stack;

            // Sprawdzamy, czy wywołaliśmy się już wcześniej na
            // tym rozpatrywanym podstosie, aby uniknąć zapętlenia.
            if (child != nullptr && !child->visited) {
                result_t sub = rstack_front_dfs(child);

                // Jeśli w rozpatrywanym podstosie znaleziono 
                // jakąś liczbę, zgodnie z działaniem algorytmu
                // DFS, zwracamy ją jako liczbę najbliżej wierzchołka stosu.
                if (sub.flag) {
                    return sub;
                }
            }
        }
    }

    // Jeśli nie znaleziono żadnej liczby ani w rozpatrywanym stosie,
    // ani w jego podstosach, zwracamy informację o braku wyniku.
    return (result_t){ .flag = false, .value = 0 };
}

result_t rstack_front(rstack_t *rs) {
    // Szukamy rekurencyjnie liczby najbliżej wierzchołka stosu
    //  za pomocą dedykowanej funkcji
    result_t result = rstack_front_dfs(rs);

    // Przywracamy rekurencyjnie stosom status nieodwiedzonych.
    rstack_clear_visited(rs);

    // Zwracamy znaleziony wcześniej wynik.
    return result;
}

// rstack_delete() - Funkcja usuwająca jedną referencję do stosu. 
// Wywołana przez użytkownika usuwa dostęp do stosu z poziomu
// użytkownika (kasuje go). Dba również o brak wycieków pamięci,
// zwalnia pamięć po nieosiągalnych stosach oraz wykrywa 
// martwe cykle i je usuwa. 
//
// Korzysta z funkcji pomocniczych:
// rstack_destroy() - Funkcja niszcząca stos, zwalnia pamięć
// po nim i odejmuje referencje podstosom będącymi jego elementami.
static void rstack_destroy(rstack_t *rs);
//
// rstack_trial_unlink() - Funkcja realizująca pierwszą fazę algorytmu
// wykrywania nieosiągalnych stosów i martwych cykli. Tymczasowo odłącza
// badany stos od stosów, do których prowadzą jego referencje,
// zmniejszając ich liczniki referencji i rekurencyjnie wykonuje dla nich
// to samo. Ustawia ona również kolor tych stosów jako szare (patrz ).
static void rstack_trial_unlink(rstack_t *rs);
//
// rstack_check() - Funkcja realizująca drugą fazę algorytmu wykrywania
// nieosiągalnych stosów i martwych cykli. Sprawdza, czy po tymczasowym
// odłączeniu referencji badany stos ma dodatni licznik referencji.
// Jeśli tak, to przywraca wcześniej odłączone referencje stosom, do których
// prowadzą jego referencje i rekurencyjnie wykonuje dla nich to samo.
// W przeciwnym razie oznacza stos jako nieosiągalny i rekurencyjnie
// wywołuje się w stosach, do których prowadzą jego referencje.
static void rstack_check(rstack_t *rs);
//
// rstack_restore() - Funkcja pomocnicza drugiej fazy algorytmu. Przywraca
// wcześniej tymczasowo odłączone referencje w osiągalnej części struktury:
// oznacza odwiedzane stosy z powrotem jako czarne, odtwarza zmniejszone
// liczniki referencji i rekurencyjnie wykonuje tę samą operację dla
// stosów, do których prowadzą ich referencje.
static void rstack_restore(rstack_t *rs);
//
// rstack_collect_mark() - Funkcja pomocnicza trzeciej fazt algorytmu.
// Przechodzi rekurencyjnie po białych stosach osiągalnych z zadanego stosu,
// oznacza je jako przeznaczone do zebrania oraz wpina je na listę stosów
// przeznaczonych do usunięcia.
static void rstack_collect_mark(rstack_t *rs, rstack_t **head);
//
// rstack_collect() - Funkcja realizująca trzecią fazę algorytmu,
// czyli usuwająca nieosiągalne stosy oznaczone jako białe.
static void rstack_collect(rstack_t *rs);


void rstack_delete(rstack_t *rs) {
    if (rs == nullptr) { 
        return;
    }

    // Zmniejszamy licznik referencji.
    rs->refcount--;

    // Jeśli licznik referencji spadł do zera, to naturalnie
    // musimy zniszczyć ten stos.
    if (rs->refcount == 0) {
        rstack_destroy(rs);
    } 
    
    else {
        // Faza 1.
        rstack_trial_unlink(rs);

        // Faza 2.
        rstack_check(rs);

        // Faza 3.
        rstack_collect(rs);
    }
}

static void rstack_destroy(rstack_t *rs) {
    if (rs == nullptr) { 
        return;
    }

    // e - wskaźnik do początku listy odpowiadającej elementom tego stosu,
    // zwalniamy po nim pamięć
    elem_t *e = rs->top;
    rs->top = nullptr;

    // Zwalniamy pamięć po każdym elemencie tej listy idąc od wierzchu.
    while (e != nullptr) {
        // next - wskaźnik do następnego elementu względem naszego aktualnego 
        // nieusuniętego frontu
        elem_t *next = e->next;

        // Jeśli aktualny front, który usuwamy, jest referencją
        // do innego stosu, to znaczy, że ten stos traci jedną referencję,
        // więc musimy ją usunąć dedykowaną funkcją.

        if (e->type == TYPE_STACK) {
            rstack_delete(e->data.stack);
        }

        free(e);
        e = next;
    }

    // Na koniec, po zwolnieniu pamięci po elementach stosu, 
    // zwalniamy pamięć po samym stosie.
    free(rs);
}


// Funkcja obsługująca fazę 1 algorytmu (opis: linijki )
static void rstack_trial_unlink(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // Jeśli stos już ma szary kolor, to nie wchodzimy do niego ponownie,
    // bo nie chcemy trafić w nieskończoną rekurencję wynikającą
    // z istnienia cyklu.
    if (rs->color == COLOR_GRAY) {
        return;
    }

    // Ustawiamy kolor stosu na szary.
    rs->color = COLOR_GRAY;

    // Iterujemy się po elementach leżących na stosie.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // Jeśli aktualnie rozpatrywany element stosu (child) to stos,
            // to zgodnie z działaniem algorytmu tymczasowo usuwamy mu
            // referencję i rekurencyjnie się w nim wywołujemy.
            rstack_t *child = e->data.stack;
            child->refcount--;
            rstack_trial_unlink(child);
        }
    }
}


static void rstack_restore(rstack_t *rs) {
    if (rs == nullptr) {
        return;
    }

    // Jeżeli stos jest czarny, to znaczy, że został
    // juz wcześniej przywrócony.
    if (rs->color == COLOR_BLACK) {
        return;
    }

    // Ustawiamy kolor stosu z powrotem na czarny.
    rs->color = COLOR_BLACK;

    // Iterujemy się po elementach stosu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            rstack_t *child = e->data.stack;
            child->refcount++;
            rstack_restore(child);
        }
    }
}


static void rstack_check(rstack_t *rs) {
    if(rs == nullptr) {
        return;
    }

    // Jeśli kolor stosu nie jest szary, to znaczy, że został już rozpatrzony.
    if (rs->color != COLOR_GRAY) {
        return;
    }

    // Jeśli licznik referencji rozpatrywanego stosu jest dodatni, to zgodnie
    // z algorytmem uruchamiamy procedurę przywracania.
    if (rs->refcount > 0) {
        rstack_restore(rs);
        return;
    }

    // W przeciwnym wypadku wiemy, że aktualnie rozpatrywany
    // stos jest śmieciem, więc ustawiamy jego kolor jako biały.
    rs->color = COLOR_WHITE;

    // Iterujemy się po elementach leżących na aktualnie rozpatrywanym stosie,
    // aby kontynuować drugą fazę algorytmu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        // Jeśli dany element jest stosem, wywołujemy na nim funkcję.
        if (e->type == TYPE_STACK) {
            rstack_check(e->data.stack);
        }
    }
}


static void rstack_collect_mark(rstack_t *rs, rstack_t **head) {
    if (rs == nullptr || rs->color != COLOR_WHITE) {
        return;
    }

    // Oznaczamy stos jako zebrany do usunięcia, aby 
    // nie trafić w nieskończoną pętlę.
    rs->color = COLOR_PURPLE;

    // Wpinamy stos na początek listy do zwolnienia.
    rs->collect_next = *head;
    *head = rs;

    // Iterujemy się po elementach stosu.
    for (elem_t *e = rs->top; e != nullptr; e = e->next) {
        if (e->type == TYPE_STACK) {
            // Child wskazuje na aktualnie rozpatrywany podstos
            // (stos leżący na naszym stosie).
            rstack_t *child = e->data.stack;

            // Sprawdzamy, czy rozpatrywany podstos istnieje i jest biały.
            // Jeśli tak, wywołujemy się w nim rekurencyjnie.
            if (child != nullptr && child->color == COLOR_WHITE) {
                rstack_collect_mark(child, head);
            }
        }
    }
}


static void rstack_collect(rstack_t *rs) {
    // head wskazuje na początek listy stosów przeznaczonych do usunięcia.
    rstack_t *head = nullptr;

    // Zbieramy wszystkie białe stosy na listę za pomocą dedykowanej funkcji.
    rstack_collect_mark(rs, &head);

    // Iterujemy się po liście stosów przeznaczonych do usunięcia.
    for (rstack_t *p = head; p != nullptr; p = p->collect_next) {
        // e wskazuje na pierwszy element aktualnie rozpatrywanego stosu.
        elem_t *e = p->top;
        
        // Iterujemy się po elementach aktualnie rozpatrywanego stosu.
        while (e != nullptr) {
            // next wskazuje na kolejny element stosu.
            elem_t *next = e->next;

            // Zwalniamy aktualnie rozpatrywany element.
            free(e);

            // Przechodzimy do kolejnego elementu stosu.
            e = next;
        }
    }

    // Iterujemy się po liście stosów przeznaczonych do usunięcia.
    while (head != nullptr) {
        // next wskazuje na kolejny stos z listy do usunięcia.
        rstack_t *next = head->collect_next;

        // Zwalniamy aktualnie rozpatrywany stos.
        free(head);

        // Przechodzimy do kolejnego stosu z listy.
        head = next;
    }
}

// rstack_pop() - Funkcja zdejmująca nierekurencyjnie wierzchołek stosu.
void rstack_pop(rstack_t *rs) {
    // Sprawdzamy czy stos jest pusty.
    if (rs == nullptr || rs->top == nullptr) {
        return;
    }

    // Zapamiętujemy aktualny wierzchołek stosu.
    elem_t *e = rs->top;
    
    // Ustawiamy wierzchołek jako kolejny element względem poprzedniego
    // wierzchołka.
    rs->top = e->next;

    // W przypadku, gdy zdejmowany wierzchołek był stosem, musimy usunąć
    // mu referencję od stosu, który obsługujemy.
    if (e->type == TYPE_STACK) {
        rstack_delete(e->data.stack);
    }

    free(e);
}


// rstack_read() - Funkcja tworząca nowy stos, na którym odłożone są liczby 
// podane w pliku.

rstack_t *rstack_read(char const *path) {
    // Rozpatrujemy przypadek błędnie podanej ścieżki i ustawiamy
    // odpowiednie errno.
    if (path == nullptr) {
        errno = EINVAL;

        return nullptr;
    }

    // Zachowujemy poprzednią wartość errno, 
    // aby przy sukcesie jej nie zmieniać.
    int entry_errno = errno;

    // Otwieramy plik w trybie tekstowym do odczytu.
    FILE *f = fopen(path, "r");
    if (f == nullptr) {
        return nullptr;
    }

    // Tworzymy nowy pusty stos, do którego będą trafiały odczytane wartości.
    rstack_t *rs = rstack_new();

    // Jeśli nie udało się utworzyć stosu, zamykamy wcześniej otwarty plik
    // i zachowujemy kod błędu ustawiony przez rstack_new().
    if (rs == nullptr) {
        int saved_errno = errno;
        fclose(f);
        errno = saved_errno;
        return nullptr;
    }

    // Zmienna przehowująca aktualnie rozpatrywany znak odczytywany z pliku.
    int c;

    // Informacja, czy jesteśmy aktualnie w trakcie wczytywania liczby.
    bool in_number = false;

    // Wartość aktualnie budowanej liczby.
    uint64_t value = 0;

    // Zerujemy errno przed odczytem, aby móc rozpoznać przypadek,
    // gdy operacja wejścia/wyjścia zgłosi błąd bez ustawienia errno.
    errno = 0;

    // Iterujemy się po kolejnych znakach z pliku.
    while ((c = fgetc(f)) != EOF) {
        // Rzutujemy aktualnie rozpatrywany znak na unsigned char.
        unsigned char uc = (unsigned char)c;

        // Przypadek, gdy aktualnie rozpatrywany znak jest białym znakiem
        if (isspace(uc)) {
            // Gdy jesteśmy w trakcie wczytywania liczby,
            // to biały znak je kończy, zatem wrzucamy wartość na stos.
            if (in_number) {
                // Rozpatrujemy przypadek błędu przy wrzucaniu liczby na stos.
                if (rstack_push_value(rs, value) == -1) {
                    // Zapamiętujemy errno, aby wynikało ono ze złego
                    // przydzielenia pamięci przy wrzucaniu liczby na stos.
                    int saved_errno = errno;
                    
                    // Zamykamy wcześniej otwarty plik.
                    fclose(f);

                    // Usuwamy utworzony stos.
                    rstack_delete(rs);

                    // Przywracamy poprawne errno.
                    errno = saved_errno;

                    return nullptr;
                }

                // Skoro skończyliśmy wczytywanie liczby,
                // to ustawiamy, że tego aktualnie nie robimy.
                in_number = false;

                // Przygotowujemy zmienną do ewentualnego
                // wczytywania kolejnej liczby.
                value = 0;
            }
        }
        
        // Przypadek, gdy aktualnie rozpatrywany znak jest cyfrą.
        else if (isdigit(uc)) {
            // Wartość aktualnie rozpatrywanej cyfry.
            uint64_t digit = (uint64_t)(uc - '0');

            // Jeśli nie jesteśmy w trakcie wczytywania liczby,
            // to ta cyfra rozpoczyna wczytywanie kolejnej.
            if (!in_number) {
                in_number = true;

                // Wartość aktualnie wczytywanej liczby wynosi zatem tę cyfrę.
                value = digit;
            } 

            // Jeśli jesteśmy w trakcie wczytywania liczby, 
            // to dodajemy do niej kolejną cyfrę.
            else {
                // Operacja dopisania do liczby value cyfry digit to 
                // value = value * 10 + digit.
                // Rozpatrujemy przypadek, gdy przy tej operacji 
                // zostanie przepełniony typ uint64_t.
                if (value > (UINT64_MAX - digit) / 10) {
                    // Zamykamy wcześniej otwarty plik.
                    fclose(f);

                    // Usuwamy utworzony stos.
                    rstack_delete(rs);

                    // Ustawiamy kod błędu na zły zakres wyniku.
                    errno = ERANGE;

                    return nullptr;
                }

                // W przeciwnym wypadku wykonujemy operację dopisania cyfry.
                value = value * 10 + digit;
            }
        }

        // Przypadek, gdy aktualnie rozpatrywany znak nie 
        // jest ani białym znakiem, ani cyfrą.
        // Taki znak jest niedopuszczalny, oznacza to 
        // niepoprawny format pliku.
        else {
            // Zamykamy wcześniej otwarty plik.
            fclose(f);

            // Usuwamy utworzony stos.
            rstack_delete(rs);

            // Ustawiamy poprawne errno.
            errno = EINVAL;

            return nullptr;
        }
    }

    // Sprawdzamy, czy zakończenie odczytu 
    // nastąpiło z powodu błędu pliku.
    if (ferror(f)) {
        // Zapamiętujemy errno, aby nie zostało utracone.
        int saved_errno = errno;

        // Jeśli biblioteka nie ustawiła errno, 
        // przyjmujemy ogólny błąd wejścia/wyjścia.
        if (saved_errno == 0) {
            saved_errno = EIO;
        }

        // Zamykamy wcześniej otwarty plik.
        fclose(f);

        // Usuwamy utworzony stos.
        rstack_delete(rs);

        // Ustawiamy poprawne errno.
        errno = saved_errno;

        return nullptr;
    }

    // W przypadku, gdy koniec pliku został 
    // osiągnięty w trakcie wczytywania liczby,
    // musimy dodać tę liczbę na stos.
    if (in_number) {
        // Rozpatrujemy przypadek błędu przy 
        // wrzucaniu liczby na stos.
        if (rstack_push_value(rs, value) == -1) {
            // Zapamiętujemy errno, aby nie zostało utracone.
            int saved_errno = errno;

            // Zamykamy wcześniej otwarty plik.
            fclose(f);

            // Usuwamy utworzony stos.
            rstack_delete(rs);

            // Ustawiamy poprawne errno.
            errno = saved_errno;

            return nullptr;
        }
    }

    // Po zakończeniu odczytu zamykamy plik.
    // Rozpatrujemy przypadek, gdy zamknięcie 
    // pliku się nie powiedzie.

    // Zerujemy errno, aby móc rozpoznać przypadek,
    // gdy zamknięcie pliku zgłosi błąd bez ustawienia errno.
    errno = 0;

    if (fclose(f) != 0) {
        // Zapamiętujemy errno, aby nie zostało utracone.
        int saved_errno = errno;

        // Jeśli biblioteka nie ustawiła errno, 
        // przyjmujemy ogólny błąd wejścia/wyjścia.
        if (saved_errno == 0) {
            saved_errno = EIO;
        }

        // Usuwamy utworzony stos.
        rstack_delete(rs);

        // Ustawiamy poprawne errno.
        errno = saved_errno;

        return nullptr;
    }
    
    // Jeśli wszystkie operacje zakończyły się powodzeniem, 
    // zwracamy wskaźnik na wczytany stos oraz przywracamy
    // wejściowe errno.
    errno = entry_errno;

    return rs;
}


// rstack_write() - Funkcja zapisująca do pliku liczby odłożone na stosie.
// Przy wykryciu cyklu kończy ona zapisywanie.

// Korzysta ona z dwóch funkcji pomocniczych: 

// rstack_write_elems_from_bottom() - Funkcja zapisująca elementy 
// pojedynczego stosu w kolejności od elementu położonego najniżej
// do elementu na szczycie stosu.
static int rstack_write_elems_from_bottom(FILE *f, elem_t *e, bool *stop);

// rstack_write_dfs() - Przechodzi rekurencyjnie za pomocą algorytmu 
// DFS po strukturze stosów i dla każdego z nich rozpoczyna
// zapis elementów w odpowiedniej kolejności.
static int rstack_write_dfs(FILE *f, rstack_t *rs, bool *stop);

// (Opis linijki)
static int rstack_write_elems_from_bottom(FILE *f, elem_t *e, bool *stop) {
    // Jeśli dany element stosu nie istnieje lub zapis został
    // zatrzymany przez znaleziony stos nie wykonujemy zmian.
    if (e == nullptr || *stop) {
        return 0;
    }

    // Najpierw, zgodnie z koolejnością zapisywania,
    // przechodzimy do kolejnego elementu stosu, o ile on istnieje.
    if (e->next != nullptr) {
        if (rstack_write_elems_from_bottom(f, e->next, stop) == -1) {
            return -1;
        }
    }

    // Jeżeli wczesniej wykryto cykl, przerywamy dalsze zapisywanie.
    if (*stop) {
        return 0;
    }

    // Jeżeli aktualnie rozpatrywany element to liczba,
    // to zapisujemy do pliku jej wartość w osobnej linii.
    if (e->type == TYPE_VALUE) {
        // Zachowujemy poprzednią wartość errno, 
        // aby przy sukcesie jej nie zmieniać.
        int saved_errno = errno;

        // Zerujemy errno, żeby rozpoznać przypadek,
        // gdy biblioteka zgłosi błąd, ale nie ustawi errno.
        errno = 0;

        // Rozpatrujemy przypadek, gdy zapis się nie powiedzie.
        if (fprintf(f, "%" PRIu64 "\n", e->data.value) < 0) {
            // Jeśli biblioteka nie ustawiła errno, 
            // przyjmujemy ogólny błąd wejścia/wyjścia.
            if (errno == 0) {
                errno = EIO;
            }

            // Zwracamy błąd.
            return -1;
        }

        // Przy sukcesie przywracamy poprzednie errno.
        errno = saved_errno;
    } 
    
    // W przeciwnym wypadku, gdy element to stos, 
    // używamy dedykowanej do rekurencyjnego wywołania się w tym
    // stosie w celu zapisania elementów w odpowiedniej kolejności.
    else {
        // child wskazuje na podstos będący 
        // rozpatrywanem elementem stosu.
        rstack_t *child = e->data.stack;

        if (rstack_write_dfs(f, child, stop) == -1) {
            return -1;
        }
    }

    return 0;
}


static int rstack_write_dfs(FILE *f, rstack_t *rs, bool *stop) {
    // Jeśli stos nie istnieje lub zapis został
    // zatrzymany przez znaleziony cykl, nie wykonujemy zmian.
    if (rs == nullptr || *stop) {
        return 0;
    }

    // Jeżeli rozpatrywany stos jest w trakcie zapisu, to wykryliśmy cykl.
    // Ustawiamy tę informację i zatrzymujemy dalsze zapisywanie.
    if (rs->in_write) {
        *stop = true;
        return 0;
    }

    // Ustawiamy rozpatrywany stos jako aktualnie zapisywany.
    rs->in_write = true;

    // Zapisujemy elementy rozpatrywanego stosu.
    int result = rstack_write_elems_from_bottom(f, rs->top, stop);

    // Zaznaczamy, że już nie zapisujemy elementów tego stosu.
    rs->in_write = false;

    return result;
}


int rstack_write(char const *path, rstack_t *rs) {
    // Niepoprawna ścieżka lub stos.
    if (path == nullptr || rs == nullptr) {
        // Ustawiamy errno na błędny argument.
        errno = EINVAL;

        // Zwracamy błąd.
        return -1;
    }

    // Zachowujemy poprzednią wartość errno,
    //  aby przy sukcesie jej nie zmieniać.
    int entry_errno = errno;

    // Otwieramy plik do zapisu.
    FILE *f = fopen(path, "w");

    // W przypadku niepowodzenia zwracamy błąd.
    // errno ustawia fopen().
    if (f == nullptr) {
        return -1;
    }

    // Informacja, czy należy przerwać zapisywanie do pliku z powodu
    // znalezionego cyklu.
    bool stop = false;

    // Rozpoczynamy przechodzenie algorytmem DFS.
    //  po stosach od tego przekazanego.
    int result = rstack_write_dfs(f, rs, &stop);

    // Rozpatrujemy przypadek, gdy podczas zapisu nastąpił błąd.
    if (result != 0) {
        // Zapamiętujemy errno.
        int saved_errno = errno;

        // Zamykamy wcześniej otwarty plik.
        fclose(f);

        // Przywracamy poprawne errno.
        errno = saved_errno;

        // Zwracamy błąd.
        return -1;
    }

    // Zerujemy errno, aby rozpatrzeć przypadek,
    // gdy fclose() zgłosi błąd, ale nie ustawi errno.
    errno = 0;

    // Zamykamy plik i rozpatrujemy przypadek, gdy skończyło
    // się to niepowodzeniem.
    if (fclose(f) != 0) {
        // Jeśli biblioteka nie ustawiła errno, 
        // przyjmujemy ogólny błąd wejścia/wyjścia.
        if (errno == 0) {
            errno = EIO;
        }

        // Zwracamy błąd.
        return -1;
    }

    // Przy sukcesie przywracamy poprzednie errno.
    errno = entry_errno;

    return 0;

}
