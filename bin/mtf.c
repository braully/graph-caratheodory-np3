
/* Compilieren mit   "cc -O2 -DMAXN=1024 -DWORDSIZE=32 nauty.c nautil.c mtf.c"  */
/* Thomas Harmuth,  Maerz 1996 */
/* Dieses Programm generiert maximale dreiecksfreie Graphen mit vorgegebener
   Knotenzahl. Es stuetzt sich auf das Programm NAUTY. */
/* In den Kommentaren bezeichnet "Subgraph" und "Untergraph" dasselbe, naem-
   lich einen Graphen, dessen Knotenmenge Teilmenge der Knotenmenge eines
   anderen Graphen ist und dessen Kantenmenge Teilmenge der Kantenmenge eines
   anderen Graphen ist. Der groessere Graph kann jedoch innerhalb der Knoten-
   menge des kleinen Graphen zusaetzliche Kanten haben.
   "Obergraph" ist analog dazu definiert. */
/* In den Kommentaren bezeichnet "recoverfile" und "dumpfile" dasselbe. */

/* 18.11.98:  zusaetzliche Option "groetzsch" */
/* 17.11.98:  zusaetzliche Option "twin" */

/* 9.6.98: Zusaetzliche Option "write_ramseygraph_all [von] " */

/* 16.4.97: Fehler beim Errechnen der Groupsizes beseitigt, der auftrat, wenn
   der Prozess gesplittet wurde. */

/* 19.2.97: Optionen "extinfo", "grpsize" 
   Da bei der Option "grpsize" sehr grosse Zahlen auftreten, wurde der
   nicht-Standard-Typ "unsigned long long", der einen Umfang von 8 Bytes hat,
   benutzt. Bei der Ausgabe einer solchen Zahl ist auf einem System, auf dem
   schon ein "unsigned long" 8 Bytes besitzt, der String %ld zu benutzen.
   Auf einem System, auf dem ein "unsigned long" nur 4 Bytes besitzt, muss
   der String %lld benutzt werden. 
   Fuer den Fall, dass auf einem System selbst "unsigned long long" weniger
   als 8 Bytes besitzt, wird eine Warnung ausgegeben, da grosse Zahlen
   unter Umstaenden fehlerhaft sind. 
   Durch die Umstellung auf 8-Byte-Variablen ist die neue Version INKOMPATIBEL
   zu den alten bzgl. der dump-Files */

/* 10.2.97: repeated_fopen */

/* 10.10.: Option "reverse" - wenn man sie anwendet, so werden die Graphen
   in der umgekehrten Reihenfolge generiert. */

/* 14.8.:  Es sind nur spannende Mengen der Ordnung <= "minval+1" interessant.
   In einem MTF-Graphen mit 2k Knoten ist die minimale Valenz aber <= k
   (naemlich im K(k,k)). Deshalb kann beim Speichern der spannenden Mengen
   Platz eingespart werden.
   Auch fuer die "nautykandidaten" wird jetzt nur noch soviel Speicher
   bereitgestellt, wie auch tatsaechlich benoetigt wird. */ 

/* 4.7.: Zusaetzliche Option "write_ramseygraph" */

/* Im Quellcode wurden alle Abfragen auf logische Fehler blockiert, da das
   Programm offenbar funktioniert. Aus didaktischen Gruenden sind die Abfragen
   jedoch noch als Kommentare im Quellcode enthalten. */


/*****************/
/*   Includes:   */
/*****************/

#include<stdlib.h>
#include<string.h>
#include<unistd.h>
#include<stdio.h>
#include<ctype.h>
#include<limits.h>
#include<time.h>
#include<sys/times.h>
#include<sys/stat.h>
#include <signal.h>
#include <sys/time.h>
#include "nauty.h"



/*****************/
/*   Defines:    */
/*****************/

/*   Fuer die Groesse von Arrays:   */
/*   ============================   */
#define MAXN2    30     /* Maximale Groesse eines erzeugten Graphen. Diese
                           Konstante darf den Wert 32 nicht ueberschreiten,
                           da sonst die Makros geaendert werden muessten
                           (ADDELEMENT statt ADDELEM1 usw.).  Die in der
                           Funktion "mache_nautytest" erzeugten Graphen
                           duerfen jedoch mehr als MAXN2 Knoten besitzen,
                           da fuer sie die passenden Makros benutzt werden. */
#define MAXM2    1      /* darf nicht geaendert werden */
#define MATRIXGROESSE2  (MAXN2*MAXM2)             /* kleine Adjazenzmatrix */
#define LISTENGROESSE   ((MAXN2*(MAXN2-1))>>1)    /* Liste von Kanten */
#define WORKSIZE        (100*MAXM)                /* fuer nauty */
#define MATRIXGROESSE   (MAXN*MAXM)               /* grosse Adjazenzmatrix */
#define TESTGRAPHENLISTENGROESSE USHRT_MAX        /* Liste von Testgraphen */
#define MAXFULLTEST     100    /* maximale Liste von Testgraphen, zu denen ALLE
                                  Ramseygraphen ausgegeben werden sollen */
#define MEMBLOCKSIZE    65536          /* Groesse eines Speicherblocks, der
                                          auf einen Schlag alloziert wird */
#define filenamenlaenge 255            /* sollte vorerst reichen */
#define MULTICODESIZE   (((MAXN2*(MAXN2+1))>>1)+2) 
        /* maximale Groesse eines Graphen im multi_code2_s_old */
#define BRANDTCODESIZE  ((MAXN2*MAXN2)+1)
        /* maximale Groesse eines Graphen im Brandt-Code */
#define OUTPUTCODESIZE  (size_t)(MAX(MULTICODESIZE,BRANDTCODESIZE))
        /* maximale Groesse eines Graphen im Outputcode */

/*   Fuer den Baum, in dem Mengen gespeichert werden:   */
/*   ================================================   */
#define STATICLEN  3    /* Mengen mit hoechstens dieser Laenge sind immer 
                           im Baum repraesentiert. Wichtig: STATICLEN<MAXN2 */

/*   Allgemeines:   */
/*   ============   */
#define MIN(a,b)        ((a)>(b) ? (b) : (a))
#define MAX(a,b)        ((a)>(b) ? (a) : (b)) 
#define False    0
#define True     1
#define nil      0


/*   Fuer die Ausgabe von Graphen:   */
/*   =============================   */  
#define maxentries 255       /* gemeinsame Eintraege fuer multi_code2_s_old */
#define outputbuffergroesse (OUTPUTCODESIZE*MAX((size_t)MAXN2*40L,MAXFULLTEST*5))
     /* Zwischenspeicher fuer die auszugebenden Graphen (da nicht alle Graphen
        maximal sind, passen mehr als 1000 Graphen in den Zwischenspeicher) */
#define output_one_row 2000   /* Anzahl der Bytes, die auf einmal geschrieben
                                 werden (klein, um interrupted system call
                                 bei fwrite zu vermeiden) */

/*   Fuer die Erstellung eines Dumpfiles:   */
/*   ====================================   */ 
#define DATAOUTPUTLEN 2000        /* So viele Bytes werden gesammelt, bevor
                                     sie ausgegeben werden */
#define Sicherungsintervall 1000
              /* At most "Sicherungsintervall" seconds between two savings */

/*   Fuer den local-density-Test:   */
/*   ============================   */
#define MENGENTEST_VERHAELTNIS ((double)0.3)
     /* das Verhaeltnis von kantenzahl/mengengroesse, bei lokal_density, ab wo
        auf den simpeltest umgeschaltet wird */

/*   Fuer den Subgraphtest (und den local-density-Test):   */ 
/*   ===================================================   */
#define leer 0
#define unbelegt 0
#define knoten MAXN2      /* max value 32 -- 31 vertices may be used */
#define DENSE_SATZ (84) 
    /* Prozentsatz der Kanten, die vom vollstaendigen Graphen da sein muessen
       und der Graph wird immer noch als dicht angesehen */
#define P_POPCOUNT(x) ((x) ? POPCOUNT(x) : 0)


/*   Fuer die Zeitnahme:   */
/*   ===================   */
#ifdef __osf__ /* OSF auf den alphas */
#define time_factor CLK_TCK
#endif
#ifdef __sgi /* Silicon Graphics */
#define time_factor CLK_TCK
#endif
#ifdef __linux /* Linux */
#define time_factor sysconf(_SC_CLK_TCK)
#endif
#ifdef sun /* Sun */
#define time_factor 60
#endif
#ifdef __hpux /* Hewlet Packard */
#define time_factor CLK_TCK
#endif
#ifdef __ultrix /* DEC */
#define time_factor 60
#endif

#define ADDELEM1 ADDELEMENT1
#define DELELEM1 DELELEMENT1
#define ISELEM1 ISELEMENT1


/**********************/
/* Typ-Deklarationen: */
/**********************/

/*   Allgemeines:   */
/*   ============   */
typedef char BOOL;               /* 0 entspricht False, 1 entspricht True,
                                 andere Werte sollen nicht benutzt werden */
typedef struct mem {       /* Zeiger auf gro�en Speicherblock */
  struct mem *next;        /* n�chster Speicherblock */
  size_t used;             /* Anzahl der benutzten Bytes - immer durch
                              sizeof(void *) teilbar */
  void *memory;            /* Zeiger auf Block */
} MEMORY;


/*   Basisdefinitionen fuer das Programm:    */
/*   ====================================    */
typedef unsigned char KNOTEN;    /* nicht aendern, sonst ist das dump-File
                                    endianabhaengig */  
typedef struct kante {     /*  enthaelt die adjazenten Knoten einer Kante */
  KNOTEN k1;
  KNOTEN k2;
} KANTE;
typedef KANTE PAAR;       /* nur ein anderer Name (fuer den Vortest M) */

typedef struct permut {       /* speichert einen Automorphismus */
  struct permut *next;
  permutation perm[MAXN2];
} PERMUTATION; 

typedef struct indexelem {    /* Element fuer eine Liste von Indexmengen */
  int len;                    /* Groesse der Menge */
  int *index;                 /* Zeiger auf die Indexmenge */
  struct indexelem *next;     /* naechstes Element in der Liste */
} INDEXELEM;

typedef struct liste {         /* fuer eine Liste von Listen */
  struct liste *prevlist;      /* Vorgaenger */
  struct liste *nextlist;      /* naechste Liste */
  struct elem *first;          /* erstes Element in der Liste */
  struct elem *last;           /* letztes Element in der Liste */
} LISTE;

typedef struct elem {      /* ein Listenelement fuer eine spannende Menge */
  KNOTEN menge[(MAXN2-1)/2+2];    /* siehe Kommentar 14.8.96 */
  KNOTEN len;              /* Groesse der Menge */
  KNOTEN nautykandidaten;  /* Muss nauty ueber die Kanonizitaet des neuen
    Knotens (bei Verknuepfung mit der hiesigen Menge) entscheiden? Wenn ja,
    dann gilt "nautykandidaten>0" und die Elemente "kanon[0]" bis 
    "kanon[nautykandidaten-1]" enthalten die Nummern der anderen Kandidaten. */
  KNOTEN *kanon;          
  struct elem *prev;       /* dieser Zeiger ist nur in der grossen Liste
                              wichtig */
  struct elem *next;       /* naechstes Element in der Liste */
  struct liste *base;      /* Zeiger auf Liste von aequivalenten Mengen,
                              in die das Element eingebunden ist */
} ELEM;

/* Eine spannende Menge wird in einen Baum eingetragen. Die Elemente der Menge
   sind aufsteigend geordnet. Diese Folge bestimmt einen Pfad im Baum, an
   dessen Ende ein Verweis auf das zugehoerige Element einer Liste von
   Mengen steht. */  
typedef struct treenode {   /* Verzweigung im Baum mit den spannenden Mengen */
  ELEM *ptr;               /* Zeiger auf zugehoerigen Eintrag in Liste */
  struct treenode **more;  /* weitere Mengen mit mehr Elementen (Zeiger auf
                              Array) */
} TREENODE;


/*   Fuer den Subgraphtest (und den local-density-Test):   */
/*   ===================================================   */
typedef unsigned char GRAPH[MAXN2+1][MAXN2+1];  /* nicht aendern */
typedef unsigned char ADJAZENZ[MAXN2+1];        /* nicht aendern */

typedef struct graphliste {  /* enthaelt den "wichtigen" Bereich einer Liste */
  unsigned short anf;
  unsigned short end;
} GRAPHLISTE;

/* Die folgende Struktur wird fuer den Subgraphtest gebraucht und enthaelt
   alle nur denkbaren Informationen: */
typedef struct gg
           { GRAPH g;                 /*  Adjazenzliste im GRAPH-Format */
	     ADJAZENZ adj;            /*  Adjazenzen */
	     graph nautyg[knoten+1];  /* AAAAACHTUNG -- muss bei 1 anfangen...
                                         Einfach nur als Bitmuster von g */
	     signed char more_than_half;  /* enthaelt g mehr als die Haelfte
                                             aller moeglichen Kanten ? */
	     GRAPH inv_g;
	     ADJAZENZ inv_adj;
	     graph inv_nautyg[knoten+1];
	     int inv_kantenzahl;
	     signed char dense; /* Ist der Graph dicht (1) oder nicht (0) ? */
           } GRAPH_STRUCT;

typedef struct testgraph {  /* Daten eines eingelesenen Graphen, der im
               Komplement der erzeugten mtf-Graphen enthalten sein soll */
  struct testgraph *next;   /* naechster Testgraph in der Liste der Testgraphen
    (wird nur beim Einlesen und Auswerten gebraucht, nicht jedoch waehrend
     der Konstruktion von mtf-Graphen) */
  unsigned short *obergraphen;   /* Zeiger auf eine Liste von Nummern. Diese
    Nummern repraesentieren die Obergraphen des vorliegenden Graphen. Die
    Liste endet mit einer 0. Es werden nur die MINIMALEN Obergraphen des vor-
    liegenden Graphen G gespeichert, d.h. die Graphen H, fuer die es kein
    H' gibt mit G < H' < H. */
  unsigned short *untergraphen;  /* Zeiger auf eine Liste von Nummern. Diese
    Nummern repraesentieren die Untergraphen des vorliegenden Graphen. Die
    Liste endet mit einer 0. Es werden nur die MAXIMALEN Untergraphen des
    vorliegenden Graphen gespeichert. */
  GRAPH_STRUCT g;
  unsigned long nummer;    /* laufende Nummer (beim Einlesen) */
  KNOTEN n;      /* Knotenzahl */
  KNOTEN ramsey; /* zugehoerige Mindestramseyzahl R(K_3,g) >= ramsey+1
                    (es gibt einen mft-Graphen h mit "ramsey" Knoten, so dass
                     g nicht im Komplement von h enthalten ist). */
  unsigned short kanten;   /* Anzahl der Kanten im Graph */ 
  BOOL maximalzahl_erreicht;   /* True => Ramseyzahl ist > nn  => der Test-
       graph braucht nicht mehr getestet zu werden, da sich das Ergebnis nicht
       mehr aendert. Infolge des Algorithmus wird der Graph auch nach und nach
       aus den Testlisten verschwinden, da er nur uebernommen werden kann,
       wenn er zuvor getestet wurde. */ 
} TESTGRAPH;
  

/*   Fuer die Speicherung der Automorphismengroessen:   */
/*   ================================================   */

typedef struct auto_liste {
  struct auto_liste *next;
  unsigned long long a_size;     /* Groesse eines Automorphismus */
  unsigned long long a_anz;      /* Anzahl der MTF-Graphen mit dieser Groesse */
} AUTOLISTE;


/**********************/
/* globale Variablen: */
/**********************/

/*   Fuer den Graphen, mit dem gearbeitet wird:   */
/*   ==========================================   */
KNOTEN nn;   /* maximale Knotenzahl, bis zu der konstruiert wird */
             /* wird im Verlauf der Konstruktion nicht geaendert */
graph g[MATRIXGROESSE2];    
  /* Um Zeit in der Rekursion zu sparen, ist g global. So muss kein Zeiger
     auf g uebergeben werden. */
  /* Inzidenzmatrix g + Knotenzahl n (wird nicht global gespeichert)
     definieren den Graphen */
  /* Da es sich bei dem Graphen um einen ungerichteten Graphen handelt,
     ist in der Inzidenzmatrix jede Information doppelt vorhanden
     (a mit b verbunden <=> b mit a verbunden). Fuer das
     einfache Aufspueren von Dreiecken ist es jedoch notwendig, dass beide
     Haelften der Matrix ordentlich verwaltet werden. */
ELEM *firstmenge=nil;      /* Zeiger auf Liste mit allen spannenden Mengen */
                           /* (muss global sein) */
TREENODE *tree[MAXN2];      /* fuer jedes kleinste Mengenelement ein Baum */
                            /* zur Speicherung der spannenden Mengen */
/* Die folgenden Arrays sind global. Sie werden waehrend der Konstruktion
   gebraucht. Fuer jede Rekursionstiefe steht ein eigener Teil im Array zur
   Verfuegung. Die groesstmoegliche Rekursionstiefe ist MAXN2-1, denn wenn
   ein Graph MAXN2 Knoten besitzt, wird er ausgegeben. */
KANTE kantenarray[MAXN2][LISTENGROESSE];
   /* fuer entfernte Kanten innerhalb der spannenden Menge */
KNOTEN valarray[MAXN2][MAXN2];     /* fuer Valenzen von Knoten */


/*   Fuer die Ausgabe:   */
/*   =================   */
/* die Arrays, in denen die Ausgabe EINES Graphen vorbereitet wird: */
KNOTEN ausgabe[MAX(MAXN2,MAXFULLTEST)][2][2+MAXN2*(MAXN2+1)];
int anz[MAX(MAXN2+1,MAXFULLTEST)][2];
       /* Anzahl der belegten Bytes im zugehoerigen Array */
char ausgabe_b[MAXN2*MAXN2+1];           /* fuer Brandt-Ausgabe */
int anz_b;                               /* fuer Brandt-Ausgabe */
BOOL brandt = False;                     /* Ausgabeformat */
KNOTEN outputlevel=0;  /* alle Graphen mit mindestens so vielen Knoten werden
                          ausgegeben */
char outputfilename[MAX(MAXN2+2,MAXFULLTEST)][filenamenlaenge]; 
     /* Filenamen der Ausgabefiles */
FILE *outputfile[MAX(MAXN2+2,MAXFULLTEST)]; 
     /* Ausgabefiles fuer Graphen mit entsprechender Knotenzahl 
     (bzw. Ramseyzahl, falls "ramsey"-Option gewaehlt) oder fuer entsprechenden
     Testgraphen (in allen Faellen nur voruebergehend geoeffnet) */
BOOL standardout = False;      /* True => Graphen mit nn Knoten auf stdout */
KNOTEN outputbuffer[outputbuffergroesse];   /* Zwischenspeicher fuer Output.
    Dieser Zwischenspeicher wird fuer die einzelnen Knotenzahlen aufgeteilt:
    Graphen jeder Groesse bekommen ihren eigenen Bereich */
KNOTEN *outputbufferstart[MAX(MAXN2+1,MAXFULLTEST)]; 
    /* Anfang des fuer die zugehoerige 
       Knotenzahl vorgesehenen Zwischenspeichers */
size_t outputbufferlen[MAX(MAXN2+1,MAXFULLTEST)];  
       /* Laenge des fuer die zugehoerige
          Knotenzahl vorgesehenen Zwischenspeichers */
size_t outputbufferused[MAX(MAXN2+1,MAXFULLTEST)];  
       /* Anzahl der benutzten Eintraege im
          fuer die zugehoerige Knotenzahl vorgesehenen Zwischenspeicher */
unsigned long long output_graphenzahl[MAX(MAXN2+1,MAXFULLTEST)];
  /* Anzahl der Graphen, die in das zugehoerige Outputfile ausgegeben wurden */
  /* (dies ist keine Statistik, sondern wichtig fuer die Erstellung des
       Short-Codes "multi_code2_s_old") */ 
BOOL revert = True;              /* False => Option "revert" wird benutzt */
BOOL extinfo = False;            /* True => mehr Infos ins Logfile */
BOOL twin = False;               /* True => nur Graphen, bei denen es zwei
      Knoten gibt, die die gleiche Menge von Nachbarknoten haben, werden
      ausgegeben. Die beiden Knoten sind nicht miteinander verbunden
      (sonst Dreieck). */


/*   Fuer das Logfile:   */
/*   =================   */
char logfilename[filenamenlaenge];


/*   Fuer das Einlesen von Graphen:   */
/*   ==============================   */
FILE *inputfile = nil;       /* Eingabefile, falls gewuenscht */


/*   Bitmasken:   */
/*   ==========   */
graph full[MATRIXGROESSE2];    /* in full[i] sind alle Bits gesetzt, die die 
                                 Adjazenzen mit 0...i anzeigen */ 
graph ALL_MASK[knoten+1];  /* eine Maske, deren Eintrag i alle Knoten 1 bis i
                              enthaelt --- ACHTUNG ! NICHT 0 bis i-1 */
graph ALL_MASK2[MAXN2+1];  /* eine Maske, deren Eintrag i alle Knoten 0 bis
                              i-1 enthaelt (fuer lokale_dichte) */


/*   Fuer die Statistik:   */
/*   ===================   */
unsigned long long graphenzahl[MAXN2+1];       /* produzierte Graphen */
unsigned long long acceptcount=0;      /* zu Testzwecken */
unsigned long long eindeutig=0;        /* zu Testzwecken */
unsigned long long nicht_eindeutig=0;  /* zu Testzwecken */
unsigned long long nautys=0;           /* zu Testzwecken */
unsigned long long spannende=0;        /* Test: spannende Mengen */
unsigned long long gute=0;             /* Test: gute spannende Mengen */


/*   Fuer nauty:   */
/*   ===========   */
BOOL autom;                   /* global, da von "userautomproc" benutzt */
PERMUTATION *firstautom[MAXN2+1];        /* ebenso */
    /* Zeiger auf Listen von Permutationen */
/* Die folgenden Variablen sind aus Zeitgruenden (Speicherallozierung) global.
   Sie werden von nauty benutzt und haben keinerlei globale Bedeutung. */
//optionblk options;
static DEFAULTOPTIONS(options);
statsblk stats;
setword workspace[WORKSIZE];


/*   Fuer die Speicherverwaltung:   */
/*   ============================   */
/* die folgenden sechs Zeiger muessen global sein: */
MEMORY *firstmem=nil;              /* Zeiger auf ersten dynamischen 
                                      Speicherbereich */
MEMORY *currmem=nil;               /* Zeiger auf aktuellen dynamischen
                                  Speicherbereich (der noch nicht voll ist) */
MEMORY *firstfestmem=nil;          /* Zeiger auf ersten halbdynamischen 
                                      Speicherbereich */
MEMORY *currfestmem=nil;           /* Zeiger auf aktuellen halbdynamischen
                                  Speicherbereich (der noch nicht voll ist) */
MEMORY *firstpermmem=nil;          /* Zeiger auf permanenten Speicherbereich */
MEMORY *currpermmem=nil;           /* Zeiger auf aktuellen permanenten
                                  Speicherbereich (der noch nicht voll ist) */
/* Die halbdynamischen Speicherbereiche sind fuer die Listen von aequivalenten
   Mengen. Diese werden erst dann wieder geloescht, wenn ein Rekursions-
   rueckschritt erfolgt. Zu jeder Rekursionstiefe muss gespeichert werden,
   ab wo der zugehoerige Speicherbereich beginnt, um wieder exakt loeschen
   zu koennen. */


/*   Fuer die Aufteilung des Prozesses:   */
/*   ==================================   */
signed int level=-1;  /* fuer Aufteilung auf mehrere Prozesse (<0 => OFF) */   
int count=0;        /* zaehlt Erreichen des level mit */
int count2=0;       /* falls Inputfile:  zaehlt gelesene Graphen mit mehr als
                       "level" Knoten mit */ 
int mod=-1;         /* Modul, in dem beim Erreichen des Level gezaehlt wird */
int rest=-1;        /* Divisionsrest, um den Level zu ueberschreiten */
int recovercount;   /* der count-Wert, der im Recoverfile stand, das geladen
                       wurde */
int recovercount2;  /* dasselbe fuer den count2-Wert */
 

/*   Fuer den local-density-Test:   */
/*   ============================   */
BOOL local_density = False;     /* True => Option wird benutzt */
KNOTEN l_n;            /* Ordnung jedes zu betrachtenden Untergraphen */
int l_k;               /* Mindestanzahl Kanten innerhalb jedes Untergraphen */
signed char nosimpeltest_gb;  /* soll bei "lokale_dichte" der aufwendigere
                                 oder der simple test genommen werden ? */


/*   Fuer den Subgraphentest:   */
/*   ========================   */
BOOL ramsey = False;               /* True => Option wird benutzt */
TESTGRAPH *firsttestgraph = nil;   /* Zeiger auf ersten Testgraphen (wird nur
   beim Einlesen, beim Auswerten und beim Speichern des Dumpfiles gebraucht,
   zwischendurch enthaelt das Array "testgraphen" Zeiger auf alle eingelesenen
   Testgraphen) */
TESTGRAPH **testgraphen = nil;   /* Zeiger auf Array, in dem Zeiger auf die
  Testgraphen stehen, wobei die Reihenfolge im Array die Numerierung der 
  Graphen bestimmt (das erste Arrayelement ist unbelegt). */
unsigned long ramsey_min = 1, ramsey_max = ULONG_MAX;   
   /* Begrenzungen, welche Testgraphen eingelesen werden sollen */
unsigned short anz_testgraphen = 0;         /* Laenge der Liste */
unsigned short *testgraphenliste[MAXN2+1];  /* Zeiger auf Array, in dem die
  Nummern von Testgraphen stehen. Die Liste endet nicht mit einer 0, sondern
  wird durch die Variablen "testgraphenliste_anf" und "-_end" begrenzt.
  Die Numerierung der Testgraphen beginnt mit 1. Fuer jede Knotenzahl der 
  mtf-Graphen (=Rekursionsebene) gibt es eine Liste, in der die Nummern
  derjenigen Testgraphen stehen, die nicht im mtf-Graphen enthalten sind. */
unsigned short testgraphenliste_anf[MAXN2+1],testgraphenliste_end[MAXN2+1];
  /* Nur die Elemente von "anf" bis "end" sind in der zugehoerigen Testgraphen-
     liste belegt. Aus organisatorischen Gruenden ist "anf" nicht immer 1,
     denn manchmal wird die Liste von hinten nach vorn durchgegangen und dabei
     werden einige Elemente entfernt, so dass vorne leere Plaetze entstehen.
     Um Zeit zu sparen, werden die verbliebenen Elemente nicht an den Anfang
     der Liste kopiert. */
unsigned short testgraphenzahl[MAXN2+2];  /* Anzahl der Testgraphen, die den
  jeweiligen Ramseyzahlen zugeordnet werden (wird bei Auswertung ermittelt)
  (das Element (nn+1) ist fuer Testgraphen, die eine Ramseyzahl > nn haben) */ 
GRAPHLISTE testgraphkanten[LISTENGROESSE];  /* Die Testgraphen mit den Nummern
    "testgraphkanten[i].anf" bis "testgraphkanten[i].end" haben i Kanten */
BOOL *geprueft;   /* Zeiger auf das Array, in dem fuer jeden Graphen
     steht, ob er bereits auf die Subgrapheneigenschaft geprueft worden ist.
     Dieses Array wird in der Funktion "wende_testgraphen_an" benoetigt. */
unsigned char in_anteil[MAXN2+1];    /* Diese Variable gibt (in Prozent) den
     Anteil der Testgraphen, die in g^c enthalten sind, an den Testgraphen
     insgesamt an. Fuer jede Knotenzahl gibt es ein Element. */
     /* das Array wird zur Zeit nicht genutzt */
BOOL ramseygraph_ausgabe = False; 
     /* True => Option "write_ramseygraph" benutzt */
BOOL ramseygraph_ausgabe_all = False;
     /* True => Option "write_ramseygraph_all" benutzt */
KNOTEN ramseylevel_start = MAXN2+1;    /* dann kleinste Knotenzahl, fuer
          die die Ramseygraphen ausgegeben werden sollen */
BOOL ramseyfilefilled = False;  /* True => Option "write_ramseygraph" benutzt
                                   und einen Ramseygraphen geschrieben */ 


/*   Fuer die Zeitnahme:   */
/*   ===================   */
clock_t savetime=0;       /* verbrauchte Zeit seit Programmbeginn */
clock_t prevtime=0;       /* verbrauchte Zeit vorm Recovern       */ 
struct tms TMS;


/*   Fuer die Speicherung der Automorphismengroessen:   */
/*   ================================================   */

AUTOLISTE *a_first[MAXN2+1];    /* Liste mit Automorphismengroessen */
unsigned long long a_listlen[MAXN2+1];      /* Laenge der Liste */
BOOL grpsizes = False;          /* True  = Statistik erwuenscht */
unsigned long long grpsize;     /* fuer eine konkrete Automorphismengroesse */


/*   Fuer den Recover-Modus:   */
/*   =======================   */
int S_intervall;      /* Anzahl der Sekunden, die zwischen zwei Sicherungen
                         maximal verstreichen (sofern nicht zuvor schon so
                         viele Graphen erzeugt wurden, dass eine Sicherung
                         notwendig ist) */
unsigned long long zweig[MAXN2+2];  /* gibt fuer jede Rekursionsebene die Nummer
                     des Zweigs im Konstruktionsbaum an (beginnend mit 1) */
unsigned long long recoverzweig[MAXN2+2];  /* gibt fuer jede Rekursionsebene die 
                             Nummer des beim Recovern geforderten Zweigs an */
  /* Falls in einer Ebene eine Ausgabe erfolgt, obwohl die Graphen noch nicht
     die maximal erwuenschte Knotenzahl haben, so werden die Arrayelemente
     zweimal pro Graph erhoeht (einmal bei der Ausgabe und einmal bei der
     Konstruktion). Da dies sowohl bei der Konstruktion als auch beim Recovern
     geschieht, verfaelscht es das Ergebnis nicht. */
BOOL recover = True;  /* False => es wird kein dump-File eingelesen oder
                         Recover-Modus ist abgeschlossen */
BOOL save = True;     /* False => es werden keine dump-Files erzeugt */
char recoverfilename[filenamenlaenge];  /* fuer den Namen des gesuchten bzw.
                                           zu oeffnenden Files */
     /* "save_flag" ist unten definiert und steht auf 1, wenn wieder eine 
        Speicherung erfolgen muss */


/* Fuer die Groetzsch-Option */
/* ========================= */

GRAPH_STRUCT groetzsch_g; /* der Groetzsch-Graph */
BOOL groetzsch = False;   /* True => nur diejenigen Graphen werden gezaehlt
        und ausgegeben, in denen der Groetzsch-Graph als Subgraph enthalten
        ist. */ 


/*******************/
/*   Prototypen:   */
/*******************/

void schluss(void);          /* Programmende */
void akzeptiere(KNOTEN n,BOOL callnauty); 
     /* wird von "konstruiere" aus aufgerufen */
void *hole_permanenten_speicher(size_t len);  /* fuer lies_dumpfile */    


/****************************************************************************/
/************** es folgen Funktionen zur Fehlerbehandlung *******************/
/****************************************************************************/

/***************************REPEATED_FOPEN***********************************/

FILE *repeated_fopen(char *filename,char *mode,int tries) {
  int i=0;
  FILE *file = nil;
  do {
    file = fopen(filename,mode);
    i++;
    if (file==nil && i<tries) {sleep(5);}
  } while(file==nil && i<tries);
  return(file);     /* es kann "nil" zurueckgegeben werden */
}

/***************************SCHREIBFEHLER************************************/

void schreibfehler(char *filename,size_t soll,size_t ist,BOOL cont) {
  static FILE *logfile;
  fprintf(stderr,"Error while writing to file %s!\n",filename);
  fprintf(stderr,"Tried to write %ld entries, wrote %ld.\n",soll,ist);
  perror((char *)"Error description");
  if (logfile = repeated_fopen(logfilename,"a",5)) {
    fprintf(logfile,"Error while writing to file %s!\n",filename);
    fprintf(logfile,"Tried to write %ld entries, wrote %ld.\n",soll,ist);
    if (cont) fprintf(logfile,"Process continues...\n");
    fclose(logfile);
  }
  if (cont) fprintf(stderr,"Process continues...\n");
  else {schluss();}
}

/******************************LESEFEHLER************************************/

void lesefehler(char *filename,size_t soll,size_t ist,BOOL cont) {
  static FILE *logfile;
  fprintf(stderr,"Error while reading from file %s!\n",filename);
  fprintf(stderr,"Tried to read %ld entries, read %ld.\n",soll,ist);
  perror((char *)"Error description");
  if (logfile = repeated_fopen(logfilename,"a",5)) {
    fprintf(logfile,"Error while reading from file %s!\n",filename);
    fprintf(logfile,"Tried to read %ld entries, read %ld.\n",soll,ist);
    if (cont) fprintf(logfile,"Process continues...\n");
    fclose(logfile);
  }
  if (cont) fprintf(stderr,"Process continues...\n");
  else {schluss();}
}


/****************************************************************************/
/****** es folgen Funktionen fuer den Recover-Modus: ************************/
/****************************************************************************/

/*******************SIGNALHANDLER****************************/

/* SA_OLDSTYLE */

#ifndef	SA_RESTART
#define	SA_RESTART	0
#endif

#ifdef __STDC__
volatile	int	save_flag = 0;

void	sigcatch( int sig )
{
	save_flag = 1;
}

int		setup( void )

#else

int save_flag = 0;

void sigcatch (int sig)
{
	save_flag = 1;
}

int		setup()
#endif

{
	struct	sigaction	action, old;
	struct	itimerval	timer;

	action.sa_handler = sigcatch;
	action.sa_flags   = SA_RESTART;
	sigemptyset( &action.sa_mask );

	if( sigaction( SIGALRM, &action, &old ) == -1 )
	{
		perror( "sigaction" );
		return( -1 );
	}

	timer.it_interval.tv_sec  = S_intervall;
	timer.it_interval.tv_usec = 0;
	timer.it_value.tv_sec     = timer.it_interval.tv_sec;
	timer.it_value.tv_usec    = timer.it_interval.tv_usec;

	if( setitimer( ITIMER_REAL, &timer, NULL ) == -1 )
	{
		perror( "setitimer" );
		sigaction( SIGALRM, &old, NULL );
		return( -1 );
	}

	return( 0 );
}


/*******************SCHREIBE_DUMPFILE***************************************/
/*  Diese Funktion schreibt alle Daten, die zum Recovern des Prozesses
    benoetigt werden, in die Datei mit dem Namen "recoverfilename"         */
/*  Es wird davon ausgegangen, dass kein zu speichernder Wert groesser
    als 2^64-1 ist.                                                        */
/*  Die Filenamen der Inputfiles brauchen nicht gespeichert zu werden,
    denn wenn dieses Dumpfile zum Lesen geoeffnet werden soll, muessen
    die Filenamen sowieso als Parameter zum Programmaufruf eingegeben
    werden. Genauso verhaelt es sich mit der Knotenzahl (die wird jedoch
    trotzdem abgespeichert, um das Dumpfile ggf. auch abseits des
    Programms lesen zu koennen) und den Optionen "stdout", "brandt",
    "mod", "class" und "level". "outputlevel" wird gespeichert, kommt aber
    nicht im Filenamen vor, so dass beim Restart der alte Outputlevel
    wiederhergestellt wird, auch wenn beim Aufruf ein neuer Wert angegeben
    wurde. */ 
/*  Falls "finished" den Wert "True" besitzt, so ist der Prozess zu Ende.  */
/*  Die output_graphenzahlen werden nicht gespeichert, sondern nach dem
    Restart wieder auf 0 gesetzt, so dass, wenn die naechsten Graphen
    ausgegeben werden, beim ersten Mal kein Gebrauch von der Short-Code-
    Eigenschaft gemacht wird. Es wird der komplette Graph ausgegeben.
    Auf diese Weise erspart man sich das Zwischenspeichern der zuletzt
    erzeugten Graphen in jeder Ebene. Dafuer werden die Outputfiles etwas
    laenger, denn es wird nicht optimal komprimiert. */

void schreibe_dumpfile(BOOL finished) {
  static FILE *recoverfile, *logfile;
  static KNOTEN data[MAXN2*16+29+DATAOUTPUTLEN];
      /* das Maximum aus MAXN2*16+29 und DATAOUTPUTLEN wuerde auch reichen */
      /* damit die ersten Daten ohne overflow angesammelt werden koennen */
  static KNOTEN i;
  static int pos;
  static TESTGRAPH *t;
  static AUTOLISTE *a;
  static unsigned long long l;
  static size_t written;

  /* dump-File oeffnen: */
  if ((recoverfile = repeated_fopen(recoverfilename,"w",1))==nil) {
    fprintf(stderr,"Could not open dump file %s!\n",recoverfilename);
    fprintf(stderr,"Process continues without saving...\n");
    perror((char *)"Error description");
    if (logfile = repeated_fopen(logfilename,"a",5)) {
      fprintf(logfile,"Could not open dump file %s!\n",recoverfilename);
      fprintf(logfile,"Process continues without saving...\n");
      fclose(logfile);
    }
  }   

  pos = 0;                             /* Position im Array "data" */
  data[pos++] = (KNOTEN)finished;                    /* Finished   */

  if (!finished) {
    data[pos++] = nn;                                  /* Knotenzahl */
    data[pos++] = outputlevel;                         /* Outputlevel */
    for (i=3; i<=nn; i++) {                   
      data[pos++] = (KNOTEN)(zweig[i]>>56);            /* Zweige */
      data[pos++] = (KNOTEN)((zweig[i]>>48)&255);
      data[pos++] = (KNOTEN)((zweig[i]>>40)&255);
      data[pos++] = (KNOTEN)((zweig[i]>>32)&255);
      data[pos++] = (KNOTEN)((zweig[i]>>24)&255);
      data[pos++] = (KNOTEN)((zweig[i]>>16)&255);
      data[pos++] = (KNOTEN)((zweig[i]>>8)&255);
      data[pos++] = (KNOTEN)(zweig[i]&255);
    }

    for (i=3; i<=nn; i++) {                            /* Graphenzahlen */
      data[pos++] = (KNOTEN)(graphenzahl[i]>>56);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>48)&255);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>40)&255);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>32)&255);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>24)&255);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>16)&255);
      data[pos++] = (KNOTEN)((graphenzahl[i]>>8)&255);
      data[pos++] = (KNOTEN)(graphenzahl[i]&255);
    }

    data[pos++] = (KNOTEN)(acceptcount>>56);          /* Acceptcount */
    data[pos++] = (KNOTEN)((acceptcount>>48)&255);
    data[pos++] = (KNOTEN)((acceptcount>>40)&255);
    data[pos++] = (KNOTEN)((acceptcount>>32)&255);
    data[pos++] = (KNOTEN)((acceptcount>>24)&255);
    data[pos++] = (KNOTEN)((acceptcount>>16)&255);
    data[pos++] = (KNOTEN)((acceptcount>>8)&255);
    data[pos++] = (KNOTEN)(acceptcount&255);

    data[pos++] = (KNOTEN)(eindeutig>>56);            /* Eindeutig */
    data[pos++] = (KNOTEN)((eindeutig>>48)&255);
    data[pos++] = (KNOTEN)((eindeutig>>40)&255);
    data[pos++] = (KNOTEN)((eindeutig>>32)&255);
    data[pos++] = (KNOTEN)((eindeutig>>24)&255);
    data[pos++] = (KNOTEN)((eindeutig>>16)&255);
    data[pos++] = (KNOTEN)((eindeutig>>8)&255);
    data[pos++] = (KNOTEN)(eindeutig&255);

    data[pos++] = (KNOTEN)(nicht_eindeutig>>56);       /* Nicht_eindeutig */
    data[pos++] = (KNOTEN)((nicht_eindeutig>>48)&255);
    data[pos++] = (KNOTEN)((nicht_eindeutig>>40)&255);
    data[pos++] = (KNOTEN)((nicht_eindeutig>>32)&255);
    data[pos++] = (KNOTEN)((nicht_eindeutig>>24)&255);
    data[pos++] = (KNOTEN)((nicht_eindeutig>>16)&255);
    data[pos++] = (KNOTEN)((nicht_eindeutig>>8)&255);
    data[pos++] = (KNOTEN)(nicht_eindeutig&255);

    data[pos++] = (KNOTEN)(nautys>>56);               /* Nautys */
    data[pos++] = (KNOTEN)((nautys>>48)&255);
    data[pos++] = (KNOTEN)((nautys>>40)&255);
    data[pos++] = (KNOTEN)((nautys>>32)&255);
    data[pos++] = (KNOTEN)((nautys>>24)&255);
    data[pos++] = (KNOTEN)((nautys>>16)&255);
    data[pos++] = (KNOTEN)((nautys>>8)&255);
    data[pos++] = (KNOTEN)(nautys&255);

    data[pos++] = (KNOTEN)(spannende>>56);            /* Spannende */
    data[pos++] = (KNOTEN)((spannende>>48)&255);
    data[pos++] = (KNOTEN)((spannende>>40)&255);
    data[pos++] = (KNOTEN)((spannende>>32)&255);
    data[pos++] = (KNOTEN)((spannende>>24)&255);
    data[pos++] = (KNOTEN)((spannende>>16)&255);
    data[pos++] = (KNOTEN)((spannende>>8)&255);
    data[pos++] = (KNOTEN)(spannende&255);

    data[pos++] = (KNOTEN)(gute>>56);                 /* Gute */
    data[pos++] = (KNOTEN)((gute>>48)&255);
    data[pos++] = (KNOTEN)((gute>>40)&255);
    data[pos++] = (KNOTEN)((gute>>32)&255);
    data[pos++] = (KNOTEN)((gute>>24)&255);
    data[pos++] = (KNOTEN)((gute>>16)&255);
    data[pos++] = (KNOTEN)((gute>>8)&255);
    data[pos++] = (KNOTEN)(gute&255);

    times(&TMS);
    savetime = TMS.tms_utime + prevtime;
    data[pos++] = (KNOTEN)(savetime>>24);              /* Zeit */
    data[pos++] = (KNOTEN)((savetime>>16)&255);
    data[pos++] = (KNOTEN)((savetime>>8)&255);
    data[pos++] = (KNOTEN)(savetime&255);

    data[pos++] = (KNOTEN)(count>>24);                 /* Count */
    data[pos++] = (KNOTEN)((count>>16)&255);
    data[pos++] = (KNOTEN)((count>>8)&255);
    data[pos++] = (KNOTEN)(count&255);

    data[pos++] = (KNOTEN)(count2>>24);                /* Count2 */
    data[pos++] = (KNOTEN)((count2>>16)&255);
    data[pos++] = (KNOTEN)((count2>>8)&255);
    data[pos++] = (KNOTEN)(count2&255);

    if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile)) 
        < (size_t)pos) 
       {schreibfehler(recoverfilename,(size_t)pos,written,True);}
    pos = 0;
                      
    t = firsttestgraph;             /* Testgraphen durchgehen */
    while (t) {
      data[pos++] = t->ramsey;                         /* Mindestramseyzahl */
      t = t->next;
      if (pos == DATAOUTPUTLEN) {
        if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile)) 
            < (size_t)pos) 
           {schreibfehler(recoverfilename,(size_t)pos,written,True);}
        pos = 0;
      }
    }

    if (grpsizes) {
      if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile)) 
          < (size_t)pos) 
         {schreibfehler(recoverfilename,(size_t)pos,written,True);}
      pos = 0;
      for (i=4; i<=nn; i++) {         /* groupsize-Listen durchgehen */
        a = a_first[i];
        while (a) {
          l = a->a_size;
          data[pos++] = (KNOTEN)(l>>56);
          data[pos++] = (KNOTEN)(l>>48)&255;
          data[pos++] = (KNOTEN)(l>>40)&255;
          data[pos++] = (KNOTEN)(l>>32)&255;
          data[pos++] = (KNOTEN)(l>>24)&255;
          data[pos++] = (KNOTEN)(l>>16)&255;
          data[pos++] = (KNOTEN)(l>>8)&255;
          data[pos++] = (KNOTEN)(l&255);
          if (pos+8 > DATAOUTPUTLEN) {
            if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile))
                < (size_t)pos) 
               {schreibfehler(recoverfilename,(size_t)pos,written,True);}
            pos = 0;
          }
          l = a->a_anz;
          data[pos++] = (KNOTEN)(l>>56);
          data[pos++] = (KNOTEN)(l>>48)&255;
          data[pos++] = (KNOTEN)(l>>40)&255;
          data[pos++] = (KNOTEN)(l>>32)&255;
          data[pos++] = (KNOTEN)(l>>24)&255;
          data[pos++] = (KNOTEN)(l>>16)&255;
          data[pos++] = (KNOTEN)(l>>8)&255;
          data[pos++] = (KNOTEN)(l&255);
          if (pos+8 > DATAOUTPUTLEN) {
            if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile))
                < (size_t)pos) 
               {schreibfehler(recoverfilename,(size_t)pos,written,True);}
            pos = 0;
          }
          a = a->next;
        }     
        data[pos++] = 0;  data[pos++] = 0;  data[pos++] = 0;  data[pos++] = 0;
        data[pos++] = 0;  data[pos++] = 0;  data[pos++] = 0;  data[pos++] = 0;
        /* Ende-Kennzeichen */
      }
    }
  }

  if ((written = fwrite(data,sizeof(KNOTEN),(size_t)pos,recoverfile)) 
      < (size_t)pos  ||  ferror(recoverfile)) 
     {schreibfehler(recoverfilename,(size_t)pos,written,True);}
  fclose(recoverfile);
  save_flag = 0;                                 /* alles geschrieben */
}
       

/**************LIES_DUMPFILE*************************************************/
/*  Liest zu Programmbeginn die erforderlichen Daten fuer einen Restart ein.
    Alle uebrigen Daten sind bereits unabhaengig vom Dumpfile wiederher-
    gestellt worden. */

void lies_dumpfile(void) {
  static FILE *recoverfile;                 /* dump-File */
  static KNOTEN k[8];         /* Platz fuer acht gelesene Eintraege */
  static KNOTEN i;
  static TESTGRAPH *t;
  static AUTOLISTE *a,*curr;
  static unsigned long long l;
  static size_t gelesen;

  if ((recoverfile = repeated_fopen(recoverfilename,"r",1))==nil) {
    fprintf(stderr,"Could not find dump file %s.\n",recoverfilename);
    fprintf(stderr,"I will proceed without.\n");
    recover = False;  return;
  }

  if ((gelesen = fread(k,sizeof(KNOTEN),1L,recoverfile))<1L)
     {lesefehler(recoverfilename,1L,gelesen,False);}
  if (k[0]==1) {                 /* finished-Flag ist gesetzt */
    fprintf(stderr,"Opened dump file %s:\n",recoverfilename);
    fprintf(stderr,"Process has already finished!\n");
    fclose(recoverfile);
    schluss();
  }

  /* alles ok: einlesen */
  if ((gelesen = fread(k,sizeof(KNOTEN),1L,recoverfile))<1L)
     {lesefehler(recoverfilename,1L,gelesen,False);}     /* Knotenzahl */
  if (k[0]!=nn) {fprintf(stderr,"Logischer Fehler in lies_dumpfile!\n");
                 schluss();}
  if ((gelesen = fread(k,sizeof(KNOTEN),1L,recoverfile))<1L)
     {lesefehler(recoverfilename,1L,gelesen,False);}     /* Outputlevel */
  outputlevel = k[0];

  for (i=3; i<=nn; i++) {                   
    if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
       {lesefehler(recoverfilename,8L,gelesen,False);}     /* Zweige */
    recoverzweig[i] = (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);
  }

  for (i=3; i<=nn; i++) {                   
    if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
       {lesefehler(recoverfilename,8L,gelesen,False);}    /* Graphenzahlen */
    graphenzahl[i] =  (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);
  }

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Acceptcount */
  acceptcount =       (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Eindeutig */
  eindeutig =         (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Nicht_eindeutig */
  nicht_eindeutig =   (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Nautys */
  nautys =            (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Spannende */
  spannende =         (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
     {lesefehler(recoverfilename,8L,gelesen,False);}     /* Gute */
  gute =              (unsigned long long)(k[0])<<56 |
                      (unsigned long long)(k[1])<<48 |
                      (unsigned long long)(k[2])<<40 |
                      (unsigned long long)(k[3])<<32 |
                      (unsigned long long)(k[4])<<24 |
                      (unsigned long long)(k[5])<<16 |
                      (unsigned long long)(k[6])<<8  |
                      (unsigned long long)(k[7]);

  if ((gelesen = fread(k,sizeof(KNOTEN),4L,recoverfile))<4L)
     {lesefehler(recoverfilename,4L,gelesen,False);}     /* Zeit */
  prevtime = (clock_t)(k[0])<<24 |
             (clock_t)(k[1])<<16 |
             (clock_t)(k[2])<<8  |
             (clock_t)(k[3]);

  if ((gelesen = fread(k,sizeof(KNOTEN),4L,recoverfile))<4L)
     {lesefehler(recoverfilename,4L,gelesen,False);}    /* Count */
  recovercount = (int)(k[0])<<24 |
                 (int)(k[1])<<16 |
                 (int)(k[2])<<8  |
                 (int)(k[3]);

  if ((gelesen = fread(k,sizeof(KNOTEN),4L,recoverfile))<4L)
     {lesefehler(recoverfilename,4L,gelesen,False);}    /* Count2 */
  recovercount2 = (int)(k[0])<<24 |
                  (int)(k[1])<<16 |
                  (int)(k[2])<<8  |
                  (int)(k[3]);

  t = firsttestgraph;                           /* Testgraphen durchgehen */
  while (t) {
    if ((gelesen = fread(k,sizeof(KNOTEN),1L,recoverfile))<1L)
       {lesefehler(recoverfilename,1L,gelesen,False);}  /* Mindestramseyzahl */
    t->ramsey = k[0]; 
    t = t->next;
  }
      
  if (grpsizes) {
    for (i=4; i<=nn; i++) {                /* groupsize-Listen durchgehen */
      a_listlen[i] = 0;
      curr = a_first[i];     /* = nil */
      do {
        if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
           {lesefehler(recoverfilename,8L,gelesen,False);}     /* Groesse */
        l = (unsigned long long)(k[0])<<56 |
            (unsigned long long)(k[1])<<48 |
            (unsigned long long)(k[2])<<40 |
            (unsigned long long)(k[3])<<32 |
            (unsigned long long)(k[4])<<24 |
            (unsigned long long)(k[5])<<16 |
            (unsigned long long)(k[6])<<8  |
            (unsigned long long)(k[7]);
        if (l) {
          a = (AUTOLISTE *)hole_permanenten_speicher(sizeof(AUTOLISTE));
          a->a_size = l;
          if ((gelesen = fread(k,sizeof(KNOTEN),8L,recoverfile))<8L)
             {lesefehler(recoverfilename,8L,gelesen,False);}   /* Anzahl */
          a->a_anz = (unsigned long long)(k[0])<<56 |
                     (unsigned long long)(k[1])<<48 |
                     (unsigned long long)(k[2])<<40 |
                     (unsigned long long)(k[3])<<32 |
                     (unsigned long long)(k[4])<<24 |
                     (unsigned long long)(k[5])<<16 |
                     (unsigned long long)(k[6])<<8  |
                     (unsigned long long)(k[7]);
          a->next = nil;
          if (curr) {curr->next = a;} else {a_first[i] = a;}
          curr = a;
          a_listlen[i]++;
        }
      } while (l);
    }
  }
 
  if (ferror(recoverfile)) {
    fprintf(stderr,"Error while reading dump file %s!\n",recoverfilename);
    fclose(recoverfile);
    schluss();
  }
  fclose(recoverfile);
}

/****************************************************************************/
/****** es folgen Funktionen fuer den Subgraphentest: ***********************/
/****************************************************************************/

/****************************EINFUGEN****************************************/

void einfugen (GRAPH gr,unsigned char adj[knoten+1],unsigned char v,
               unsigned char w)
/* Fuegt die Kante (v,w) in den Graphen graph ein. Dabei wird aber davon */
/* ausgegangen, dass in adj die wirklich aktuellen werte fuer die */
/* Adjazenzen stehen. Die adjazenzen werden dann aktualisiert. */

{
gr[v][adj[v]]=w;
gr[w][adj[w]]=v;
adj[v]++;
adj[w]++;
}


/******************************WEITERBETTEN**********************/

void weiterbetten(GRAPH testg,ADJAZENZ testadj,GRAPH g,ADJAZENZ adj,ADJAZENZ bild,
                  int *eingebettet, ADJAZENZ reihenfolge, graph frei, int stelle,
		  graph *nautyg)


{
signed int i, bestvertex, ziel;
graph moegliche_ziele;

if (stelle > testg[0][0]) { *eingebettet=1; return; } /* alle eingebettet */

bestvertex=reihenfolge[stelle];
if (testadj[bestvertex]==0) { *eingebettet=1; return; } /* der Rest sind nur noch isolierte Knoten */
moegliche_ziele = frei;
for (i=0; i<testadj[bestvertex]; i++)
  { ziel=bild[testg[bestvertex][i]];
    if (ziel != unbelegt) moegliche_ziele &= nautyg[ziel]; 
    /* er muss auf einen Nachbarn des Bildes abgebildet werden */
  }

for (i= -1; ((i = nextelement(&moegliche_ziele,1,i))>=0) && !(*eingebettet);  )
  { 
  if (adj[i]>=testadj[bestvertex])
    { bild[bestvertex]=i; 
      DELELEM1(&frei,i);
      weiterbetten(testg,testadj,g,adj,bild,eingebettet,reihenfolge,frei,stelle+1,nautyg);
      ADDELEM1(&frei,i);
      bild[bestvertex]=unbelegt;
    }
}
}



/******************************SUBG_ROUTINE**************************/

/* ueberprueft, ob testg <= g */

int subg_routine(GRAPH testg, ADJAZENZ testadj,GRAPH g, ADJAZENZ adj, graph *nautyg)
     /* nautyg ist gleich g, aber nochmal in nauty-repraesentierung */
{

short priority[knoten+1];
ADJAZENZ bild, reihenfolge; /* priority zaehlt die anzahl der belegten nachbarn */
ADJAZENZ grhf; /* in welcher Reihenfolge sollen die Knoten von g abgearbeitet werden */
int i, j, eingebettet=0, bestvertex=1, bestpriority, stelle;
graph frei= 0; /* welche Knoten sind noch moegliche Ziele */
unsigned char puffer;
int getauscht;

if (testg[0][0]>g[0][0]) return(0); /* kann nicht klappen -- ermoeglicht die
				       Vernachlaessigung von isolierten Knoten */

for (i=1; i<=testg[0][0]; i++) { bild[i]=unbelegt; priority[i]=0;
			         if (testadj[i]>testadj[bestvertex]) bestvertex=i;}

for (i=1; i<=g[0][0]; i++) frei |= nautyg[i]; /* es reichen die Knoten, die zu irgendeinem anderen
						 adjazent sind */

if (testadj[bestvertex]==0) return(1); /* alles isolierte Knoten -- das geht immer, da |g|>=|testg|*/

reihenfolge[1]=bestvertex; /* reihenfolge[1] ist am Anfang ein Knoten groesster Valenz */
priority[bestvertex]=SHRT_MIN; /* so dass er auch mit etlichen ++ nicht wieder auf 0 kommt (zumindest nicht
				  bei Graphengroessen, die bearbeitbar sind (bis zu -SHRT_MIN+1 funktionierts)) */
for (j=0; j<testadj[bestvertex]; j++) priority[testg[bestvertex][j]]++;
/* Die Reihenfolge geht danach, wieviele nachbarn schon vorher sind und unter gleichen danach, wer
   groessere Valenz hat */
for (stelle=2; stelle<=testg[0][0]; stelle++)
  { for (i=1; (i<= testg[0][0]) && (priority[i]<0); i++); /* suche ersten mit positiver Prioritaet */
  /* jetzt: suche besten */
    bestvertex=i;
    bestpriority=priority[bestvertex];
    for ( ; i<= testg[0][0]; i++)
      if (((priority[i]>bestpriority) || ((priority[i]==bestpriority) && (testadj[i]>testadj[bestvertex]))))
	{ bestpriority=priority[i]; bestvertex=i; }
    /* jetzt ist der beste gefunden: */
    reihenfolge[stelle]=bestvertex;
    priority[bestvertex]=SHRT_MIN;
    /* wenn testadj[bestvertex]==0 kann abgebrochen werden, da dann nur noch welche mit valenz 0 kommen,
       die eh nicht eingebettet werden muessen */
    if (testadj[bestvertex]==0) break;
    for (j=0; j<testadj[bestvertex]; j++) priority[testg[bestvertex][j]]++;
  }


/* jetzt ist die Reihenfolge bei testg festgelegt festgelegt */

for (i=1; i<= g[0][0]; i++) grhf[i]=i;
getauscht=1;
for (i=g[0][0]; (i>1) && getauscht ; i--)
  { getauscht=0;
    for (j=1; j<i; j++) if (adj[grhf[j+1]]>adj[grhf[j]]) { getauscht=1;
                                             puffer=grhf[j];
                                             grhf[j]=grhf[j+1];
                                             grhf[j+1]=puffer;
					   }
  }




bestvertex=reihenfolge[1];
for (i=1; (i<=g[0][0]) && !eingebettet && (adj[grhf[i]]>=testadj[bestvertex]); i++)
  {   puffer=grhf[i];
      bild[bestvertex]=puffer; 
      DELELEM1(&frei,puffer);
      weiterbetten(testg,testadj,g,adj,bild,&eingebettet,reihenfolge,frei,2,nautyg);
      ADDELEM1(&frei,puffer);
      bild[bestvertex]=unbelegt;
    }

return(eingebettet);
}

/*********************************ORDNEN******************************/

void ordnen(ADJAZENZ tr,int anzahl)
/* ordnet einfach nur die naechsten anzahl Zeichen nach dem Zeiger tr. */
/* der groesste kommt zuerst */
/* Bubblesort */

{

unsigned char puffer, getauscht;
int i,j;

getauscht=1;
for (i=anzahl-1; (i>0) && getauscht ; i--)
  { getauscht=0;
    for (j=0; j<i; j++) if (tr[j+1]>tr[j]) { getauscht=1;
                                             puffer=tr[j];
                                             tr[j]=tr[j+1];
                                             tr[j+1]=puffer;
					   }
  }


}


/*********************************ORDNEN2******************************/

void ordnen2(ADJAZENZ tr,int anzahl, int wieviel)
/* ordnet einfach nur die naechsten anzahl Zeichen nach dem Zeiger tr. */
/* sorgt aber nur dafuer, dass die ersten "wieviel" groessten wirklich */
/* passend stehen. Der groesste kommt zuerst */
/* Bubblesort */

{

unsigned char puffer, getauscht;
int i,j;

getauscht=1;
for (i=0; (i<wieviel) && getauscht ; i++)
  { getauscht=0;
    for (j=anzahl-1; j>i; j--) if (tr[j-1]<tr[j]) { getauscht=1;
                                             puffer=tr[j];
                                             tr[j]=tr[j-1];
                                             tr[j-1]=puffer;
					   }
  }


}



/************************SUBGRAPH***************************/

/* ueberprueft, ob testg in g enthalten ist, indem wirklich versucht wird,
   testg einzubetten. */

int subgraph(GRAPH_STRUCT *testg, GRAPH_STRUCT *g)

{
ADJAZENZ ordertestadj, orderadj; /* um zu ueberpruefen, ob das mit den Valenzen hinkommt */
int i;

if (testg->g[0][0]>g->g[0][0]) return(0);

memcpy(ordertestadj,testg->adj+1,(int)testg->g[0][0]);
memcpy(orderadj,g->adj+1,(int)g->g[0][0]);
ordnen(ordertestadj,(int)testg->g[0][0]);
ordnen2(orderadj,(int)g->g[0][0],(int)testg->g[0][0]);

for (i=testg->g[0][0]-1; i>=0; i--) if (ordertestadj[i]>orderadj[i]) return(0); 

/* Der Valenzenvektor von orderadj muss den von ordertestadj "dominieren" */

if ((testg->g[0][0]==g->g[0][0]) && (testg->more_than_half))
               return(subg_routine(g->inv_g, g->inv_adj, testg->inv_g, testg->inv_adj, testg->inv_nautyg));
else
return(subg_routine(testg->g, testg->adj, g->g, g->adj, g->nautyg));
}

/***************************TRYSUBGRAPH**********************************/

/* Baut den durch die Menge induzierten Untergraphen und ueberprueft, ob er
   in sg->inv_g enthalten ist */

int trysubgraph(graph menge, GRAPH_STRUCT *lg, GRAPH_STRUCT *sg)

{
GRAPH induzg;
ADJAZENZ induzadj;
ADJAZENZ bild, urbild;
graph nautyg[knoten+1];
signed int i,j;
int knotenzahl, vertex;
ADJAZENZ ordertestadj, orderadj; /* um zu ueberpruefen, ob das mit den Valenzen hinkommt */


/* hierbei wird sg->inv_g immer duenn sein */

knotenzahl=lg->g[0][0];
memcpy(nautyg,lg->nautyg,sizeof(graph)*(knoten+1));

for (i=1; i<=knotenzahl; i++) nautyg[i] &= menge;


i=-1; for (j=1; (i=nextelement(&menge,1,i)) >=0; j++) { bild[i]=j; urbild[j]=i; }
knotenzahl=j-1;


/* bei der Nummerierung gilt: a<b <==> bild(a) < bild(b) */

for (i=1; i<=knotenzahl; i++)
  { induzadj[i]=0;
  /* for (j=0; j<knoten; j++) induzg[i][j]=leer; */ }

induzg[0][0]=knotenzahl;
for (j=1; j<knotenzahl; j++) /* < reicht, da fuer == eh alle Nachbarn kleiner sind */
  { vertex=urbild[j]; /* i entspricht altem Namen im Graphen, j neuem Namen .... */
    i=-1; while ((i=nextelement(nautyg+vertex,1,i)) >=0) 
                          if (i>vertex)einfugen(induzg, induzadj, bild[i], j);
  }


memcpy(ordertestadj,induzadj+1,knotenzahl);
memcpy(orderadj,sg->inv_adj+1,knotenzahl); /* die knotenzahl muss ja gleich sein */
ordnen(ordertestadj,knotenzahl);
ordnen(orderadj,knotenzahl);

for (i=0; i<knotenzahl; i++) if (ordertestadj[i]>orderadj[i]) return(0); 

/* Der Valenzenvektor von orderadj muss den von ordertestadj "dominieren" */

return(subg_routine(induzg, induzadj, sg->inv_g, sg->inv_adj, sg->inv_nautyg));
}

/*****************************MENGENKONSTRUKTION*************************/

void mengenkonstruktion(int rest_mengengroesse, GRAPH_STRUCT *smallg, 
			GRAPH_STRUCT *largeg, int rest_kantenzahl, 
			graph moegliche_knoten, graph menge, int *erfolg, int anzahl_moeglich,
			ADJAZENZ rhf, int naechster)

{
int knotenzahl, neue_kanten, puffer;
signed int i,run, merke_anz_moeglich;
graph merke_moegliche_knoten;
graph dummy;

if (rest_mengengroesse==0)
  { *erfolg= trysubgraph(menge, largeg, smallg);
    return; }

knotenzahl=largeg->g[0][0];

if (rest_kantenzahl>0) /* noch keine verbotenen knoten */
{ 
  for ( ; (anzahl_moeglich >= rest_mengengroesse) && !(*erfolg); naechster++)
    {
    i=rhf[naechster];
    neue_kanten = P_POPCOUNT(menge & largeg->nautyg[i]);
    DELELEM1(&moegliche_knoten,i); 
    anzahl_moeglich--;
    if (neue_kanten <= rest_kantenzahl)
      { 
	ADDELEM1(&menge,i);
	merke_anz_moeglich=anzahl_moeglich;
	merke_moegliche_knoten = moegliche_knoten; /* nur, fuer den Fall, dass die naechste Zeile zutrifft */
	if (neue_kanten == rest_kantenzahl) /* dann muss moegliche_knoten neu belegt werden */
	  { run=-1; while ((run=nextelement(&menge,1,run)) >=0) moegliche_knoten &= ~(largeg->nautyg[run]); 
	    anzahl_moeglich = POPCOUNT(moegliche_knoten); }
	  /* alle Nachbarn von Knoten in der Menge sind nicht mehr moeglich */
	mengenkonstruktion(rest_mengengroesse-1, smallg, largeg, rest_kantenzahl-neue_kanten, 
			   moegliche_knoten, menge, erfolg,anzahl_moeglich,rhf,naechster+1);
	anzahl_moeglich = merke_anz_moeglich;
	DELELEM1(&menge,i);
	moegliche_knoten = merke_moegliche_knoten;
      }
    }
}
else /* d.h. es duerfen keine Kanten mehr eingefuegt werden */
{ 
  for ( ; (anzahl_moeglich >= rest_mengengroesse) && !(*erfolg); naechster++)
    { i=rhf[naechster];
    if (ISELEM1(&moegliche_knoten,i))
      {
	DELELEM1(&moegliche_knoten,i); 
	anzahl_moeglich--;
	ADDELEM1(&menge,i);
	merke_anz_moeglich=anzahl_moeglich;
	anzahl_moeglich -= P_POPCOUNT(((largeg->nautyg)[i]) & moegliche_knoten);
	merke_moegliche_knoten = moegliche_knoten;
	moegliche_knoten &= ~(largeg->nautyg[i]); /* es duerfen ja keine kanten mehr reinkommen */
	mengenkonstruktion(rest_mengengroesse-1, smallg, largeg, 0, moegliche_knoten, menge, erfolg, 
			   anzahl_moeglich,rhf,naechster+1);
	moegliche_knoten = merke_moegliche_knoten;
	anzahl_moeglich = merke_anz_moeglich;
	DELELEM1(&menge,i); }
    }
}

}


/*************************SUPERGRAPHSET********************************/

int supergraphset(GRAPH_STRUCT *smallg, int kantenzahl, GRAPH_STRUCT *largeg)

/* Ueberprueft, ob largegraph eine Menge der Groesse |smallg->g| enthaelt, so dass
   smallg->inv_g Obergraph des durch diese Menge induzierten Graphen ist. Dabei brauchen
   natuerlich nur Mengen mit maximal kantenzahl (=kantenzahl von smallg->inv_g) Kanten
   betrachtet zu werden, da es ansonsten eh nicht moeglich ist.

   Vorgehensweise: Zuerst werden alle Mengen konstruiert, die in Frage kommen.
   Jedes Mal wenn eine solche Menge gefunden wurde, wird getestet, ob smallg
   tatsaechlich Obergraph ist. */

/* Vorgehensweise: Mengen -- fast -- in lexikographischer Ordnung erzeugen, sobald die
   zulaessige Kantenzahl erreicht ist, aber Nachbarn in moegliche_knoten
   verbieten. Das erste in die Menge getane Element ist aber immer ein Element mit minimaler
   Valenz in dieser Menge. Ganz mit einem sortierten Graphen zu arbeiten waere besser, aber
   das Sortieren wuerde sich nicht auszahlen. */

{

int erfolg=0; /* wurde eine solche Menge gefunden ? */
int aktkantenzahl=0; /* wieviele Kanten enthaelt die bisher konstruierte menge schon */
graph moegliche_knoten; /* welche Knoten koennen in der Menge sein ? */
graph mengenknoten=0; /* welche sind drin */
int knotenzahl, mengengroesse;
graph merke_moegliche_knoten;
int i,j;
ADJAZENZ rhf;
int getauscht;
unsigned char puffer;
unsigned char *adj;
int anzahl_moeglich, mazm;

if (smallg->g[0][0]>largeg->g[0][0]) return(0);

anzahl_moeglich=knotenzahl=largeg->g[0][0];
mengengroesse=smallg->g[0][0];
moegliche_knoten=ALL_MASK[knotenzahl];
adj=largeg->adj;

for (i=1; i<= knotenzahl; i++) rhf[i]=i;
getauscht=1;
for (i=knotenzahl; (i>1) && getauscht ; i--)
  { getauscht=0;
    for (j=1; j<i; j++) if (adj[rhf[j+1]]<adj[rhf[j]]) { getauscht=1;
                                             puffer=rhf[j];
                                             rhf[j]=rhf[j+1];
                                             rhf[j+1]=puffer;
					   }
  }

if (kantenzahl>0)
{
  for (i=1; (anzahl_moeglich >= mengengroesse) && !erfolg; i++) 
         /* Erst werden die Mengen getestet, die rhf[1] enthalten, dann die die rhf[1] NICHT enthalten...
            Da einmal verbotene Knoten nie wieder in der Menge erlaubt werden, kann so abgebrochen werden */
    { 
      puffer=rhf[i];
      ADDELEM1(&mengenknoten,puffer);
      DELELEM1(&moegliche_knoten,puffer); anzahl_moeglich--;
      mengenkonstruktion(mengengroesse-1, smallg, largeg, kantenzahl, moegliche_knoten, mengenknoten, 
			 &erfolg, anzahl_moeglich, rhf, i+1);
      DELELEM1(&mengenknoten,puffer); }
}
else
{
  for (i=1; (anzahl_moeglich >= mengengroesse) && !erfolg; i++)
    { 
      puffer=rhf[i];
      ADDELEM1(&mengenknoten,puffer); 
      DELELEM1(&moegliche_knoten,puffer); anzahl_moeglich--;
      mazm=anzahl_moeglich;
      merke_moegliche_knoten = moegliche_knoten;
      moegliche_knoten &= ~(largeg->nautyg[puffer]); /* es duerfen ja keine kanten mehr reinkommen */
      anzahl_moeglich = POPCOUNT(moegliche_knoten);
      mengenkonstruktion(mengengroesse-1, smallg, largeg, 0, moegliche_knoten, mengenknoten, 
			 &erfolg, anzahl_moeglich, rhf, i+1);
      moegliche_knoten = merke_moegliche_knoten;
      anzahl_moeglich=mazm;
      DELELEM1(&mengenknoten,puffer); }
}

return(erfolg);
}

/***************************CONTAINED_IN_COMPL***************************/

int contained_in_compl(GRAPH_STRUCT *testg, GRAPH_STRUCT *g)
/* testet, ob testg <= g^c -- gibt 1 zurueck, wenn ja, 0 sonst */

{

if (!(testg->dense)) /* dann wird der normale Test gemacht */
   return(subg_routine(testg->g,testg->adj, g->inv_g, g->inv_adj, g->inv_nautyg));

/* sonst wird versucht, in g eine menge der Groesse |testg| zu finden, so dass
   testg^c Obergraph des durch diese Menge induzierten Graphen ist */
else
  { 
    /*fprintf(stderr,"Alternativ getestet wird: \n"); schreibegraph(testg->g);*/
    return(supergraphset(testg, testg->inv_kantenzahl, g));
  }

       } 


/***********************BELEGESTRUCT****************************/

void belegestruct(GRAPH_STRUCT *g)

/* geht davon aus, dass g->g und g->adj schon belegt sind und belegt 
   den Rest */

{
int i,j, summe, knotenzahl, run, testzahl;

knotenzahl=(g->g)[0][0];
for (i=1, summe=0; i<=knotenzahl; i++) summe+= (g->adj)[i]; /* 2fache Kantenzahl */
/* der vollstaendige graph hat n*(n-1)/2 Kanten -- Vereinbarung: Dense, wenn
   mindestens DENSE_SATZ Prozent der Moeglichen kanten da sind -- da ja dann 
   ueber die Vorauswahl der mengen gegangen 
   wird, was zusaetzliche Zeit kostet sollte es nicht zu niedrig angesetzt werden */

testzahl= knotenzahl*(knotenzahl-1); /* 2 Mal der Zahl der Kanten im vollstaendigen Graphen */
if (2*summe > testzahl) g->more_than_half=1; else g->more_than_half=0;

testzahl *= DENSE_SATZ;
testzahl /= 100;
if (summe >= testzahl) g->dense=1; else g->dense=0;

g->inv_kantenzahl = (knotenzahl*(knotenzahl-1) - summe)/2;

for (i=0; i<=knotenzahl; i++)
  { g->nautyg[i]=g->inv_nautyg[i]=0;
    g->inv_adj[i]=0;
    /*for (j=0; j<knoten; j++) g->inv_g[i][j]=leer;*/ }


g->inv_g[0][0]=knotenzahl;
 
for (i=1; i<=knotenzahl; i++)
  { for (j=0; j<((int)(g->adj)[i])-1; j++) 
       if ((g->g)[i][j]>= (g->g)[i][j+1])
	 { fprintf(stderr,"Die Adjazenzlisten muessen geordnet sein\n"); 
	   exit(1); }
    for (j=1, run=0; j<=knotenzahl; j++)
      { if ((run < (g->adj[i])) && ((g->g)[i][run]==j)) { ADDELEM1((g->nautyg)+i,j); run++; }
      else { ADDELEM1((g->inv_nautyg)+i,j);  
             if (j>i) einfugen((g->inv_g), (g->inv_adj), i, j); }
    }
  }

}

/***********************NAUTY_TO_GRAPH**********gb***************/

void nauty_to_graph(graph *nautyg, GRAPH gr, ADJAZENZ adj, graph knotenzahl)
{
signed int i,j,m;


if ((knotenzahl%(8*sizeof(graph)))==0) m=knotenzahl/(8*sizeof(graph)); else m=knotenzahl/(8*sizeof(graph))+1;


for (i=0; i<=knotenzahl; i++) 
   { /*for (j=0; j<MAXN2; j++) gr[i][j]=leer;*/
     adj[i]=0; }

gr[0][0]=knotenzahl;

for (i=0; i<knotenzahl; i++)
    { j = -1;
      while ((j = nextelement((set *)nautyg+m*i,m,j)) >= 0)
	  if (j>i) { einfugen(gr,adj,i+1,j+1); }
    }

}

/***********************GRAPH_TO_NAUTY**************************************/

void graph_to_nauty(GRAPH gr,ADJAZENZ adj,graph *g,KNOTEN *n) {
  static KNOTEN i,j;
  *n = gr[0][0];
  for (i=0; i<*n; i++)  {g[i]=0;}
  for (i=1; i<=*n; i++) {
    for (j=0; j<adj[i]; j++) {ADDELEM1(&g[i-1],gr[i][j]-1);}
  }
}


/****************************************************************************/
/***** es folgen Funktionen fuer die Ermittlung der lokalen Dichte: *********/
/****************************************************************************/

/*****************************SIMPEL_MENGENKONSTRUKTION*************************/

/* Im Fall von vielen erlaubten Kanten ist es der schnellste Weg, einfach ohne
   Nachzudenken brute force tests zu machen */

void simpel_mengenkonstruktion(int rest_mengengroesse, graph largenaut[], int rest_kantenzahl, 
			graph menge, int *erfolg, 
			int anzahl_moeglich, KNOTEN *rhf, int naechster)

{
int neue_kanten;
int i;
graph mm;

for ( ; (anzahl_moeglich >= rest_mengengroesse) && !(*erfolg); naechster++)
  {
    i=rhf[naechster];
    neue_kanten = P_POPCOUNT(menge & largenaut[i]);
    anzahl_moeglich--;
    if (neue_kanten <= rest_kantenzahl)
      { mm = menge;
	ADDELEM1(&mm,i);
	if (rest_mengengroesse-1)
	  { if (anzahl_moeglich >= rest_mengengroesse-1)
	simpel_mengenkonstruktion(rest_mengengroesse-1, largenaut, rest_kantenzahl-neue_kanten, 
			   mm, erfolg, anzahl_moeglich,rhf,naechster+1); }
	else { *erfolg =1; return; }

      }
    }
}



/*****************************MENGENKONSTRUKTION_LD*************************/

void mengenkonstruktion_ld(int rest_mengengroesse, graph largenaut[], int rest_kantenzahl, 
		    	   graph moegliche_knoten, graph menge, int *erfolg, 
			   int anzahl_moeglich, KNOTEN *rhf, int naechster)

{
int neue_kanten, merke_anz_moeglich;
int i,j,run;
graph merke_moegliche_knoten, mm;


if (rest_kantenzahl>0) /* noch keine verbotenen knoten */
{ 
  for ( ; (anzahl_moeglich >= rest_mengengroesse) && !(*erfolg); naechster++)
    {
    i=rhf[naechster];
    neue_kanten = P_POPCOUNT(menge & largenaut[i]);
    DELELEM1(&moegliche_knoten,i); 
    anzahl_moeglich--;
    if (neue_kanten <= rest_kantenzahl)
      { mm=menge;
	ADDELEM1(&mm,i);
	merke_anz_moeglich=anzahl_moeglich;
	merke_moegliche_knoten = moegliche_knoten; /* nur, fuer den Fall, dass die naechste Zeile zutrifft */
	if (neue_kanten == rest_kantenzahl) /* dann muss moegliche_knoten neu belegt werden */
	  { run=-1; while ((run=nextelement(&mm,1,run)) >=0) moegliche_knoten &= ~(largenaut[run]); 
	    anzahl_moeglich = POPCOUNT(moegliche_knoten); }
	  /* alle Nachbarn von Knoten in der Menge sind nicht mehr moeglich */
if (rest_mengengroesse-1)
  { if (anzahl_moeglich >= rest_mengengroesse-1)
	mengenkonstruktion_ld(rest_mengengroesse-1, largenaut, rest_kantenzahl-neue_kanten, 
			      moegliche_knoten, mm, erfolg, anzahl_moeglich,rhf,naechster+1); }
else { *erfolg =1; return; }
	anzahl_moeglich = merke_anz_moeglich;
	moegliche_knoten = merke_moegliche_knoten;
      }
    }
}
else /* d.h. es duerfen keine Kanten mehr eingefuegt werden */
{ 
  for ( ; (anzahl_moeglich >= rest_mengengroesse) && !(*erfolg); naechster++)
    { i=rhf[naechster];
    if (ISELEM1(&moegliche_knoten,i))
      {
	DELELEM1(&moegliche_knoten,i); 
	anzahl_moeglich--;
	mm=menge; ADDELEM1(&mm,i);
	merke_anz_moeglich=anzahl_moeglich;
	anzahl_moeglich -= P_POPCOUNT((largenaut[i]) & moegliche_knoten);
	merke_moegliche_knoten = moegliche_knoten;
	moegliche_knoten &= ~(largenaut[i]); /* es duerfen ja keine kanten mehr reinkommen */
if (rest_mengengroesse-1)
	mengenkonstruktion_ld(rest_mengengroesse-1, largenaut, 0, moegliche_knoten, mm, erfolg, 
			      anzahl_moeglich,rhf,naechster+1);
else { *erfolg =1; return; }
	moegliche_knoten = merke_moegliche_knoten;
	anzahl_moeglich = merke_anz_moeglich;
	}
    }
}

}


/*************************EXIST_SMALL_SET*********gb***********************/

int exist_small_set(graph largenaut[],int knotenzahl, int mengengroesse, int kantenzahl, KNOTEN *rhf)

/* Ueberprueft, ob largegraph eine Menge der Groesse "mengengroesse" enthaelt, die nur
   kantenzahl Kanten aufspannt. Gibt 1 zurueck, wenn ja, 0 sonst */

{

int erfolg=0; /* wurde eine solche Menge gefunden ? */
int aktkantenzahl=0; /* wieviele Kanten enthaelt die bisher konstruierte menge schon */
graph moegliche_knoten; /* welche Knoten koennen in der Menge sein ? */
graph mengenknoten=0; /* welche sind drin */
graph merke_moegliche_knoten;
int i,j, getauscht, puffer, anzahl_moeglich, mazm;

anzahl_moeglich = knotenzahl;

if (nosimpeltest_gb)
  /* +1, da "small set" ja fuer kantenzahl -1 gesucht wird */
  { 
    moegliche_knoten=ALL_MASK2[knotenzahl];
    if (kantenzahl>0)
      {
	for (i=0; (anzahl_moeglich >= mengengroesse) && !erfolg; i++) 
	  /* Erst werden die Mengen getestet, die rhf[0] enthalten, dann die die rhf[0] NICHT enthalten...
	     Da einmal verbotene Knoten nie wieder in der Menge erlaubt werden, kann so abgebrochen werden */
	  { 
	    puffer=rhf[i];
	    mengenknoten=0;
	    ADDELEM1(&mengenknoten,puffer);
	    DELELEM1(&moegliche_knoten,puffer); anzahl_moeglich--;
	    mengenkonstruktion_ld(mengengroesse-1, largenaut, kantenzahl, moegliche_knoten, mengenknoten, 
			       &erfolg, anzahl_moeglich, rhf, i+1); }
      }
    else
      {
	for (i=0; (anzahl_moeglich >= mengengroesse) && !erfolg; i++)
	  { 
	    puffer=rhf[i];
	    mengenknoten=0;
	    ADDELEM1(&mengenknoten,puffer); 
	    DELELEM1(&moegliche_knoten,puffer); anzahl_moeglich--;
	    mazm=anzahl_moeglich;
	    merke_moegliche_knoten = moegliche_knoten;
	    moegliche_knoten &= ~(largenaut[puffer]); /* es duerfen ja keine kanten mehr reinkommen */
	    anzahl_moeglich = POPCOUNT(moegliche_knoten);
	    mengenkonstruktion_ld(mengengroesse-1, largenaut, 0, moegliche_knoten, mengenknoten, 
			          &erfolg, anzahl_moeglich, rhf, i+1);
	    moegliche_knoten = merke_moegliche_knoten;
	    anzahl_moeglich=mazm; }
      }
  }

else /* der Simpeltest ist schneller */

  { for (i=0; (anzahl_moeglich >= mengengroesse) && !erfolg; i++) 
	  { 
	    puffer=rhf[i];
	    mengenknoten=0;
	    ADDELEM1(&mengenknoten,puffer);
	    anzahl_moeglich--;
	    simpel_mengenkonstruktion(mengengroesse-1, largenaut, kantenzahl, mengenknoten, 
			       &erfolg, anzahl_moeglich, rhf, i+1);
	  }
  }

return(erfolg);
}


/***************************LOKALE_DICHTE_GB***************gb***************/

BOOL lokale_dichte_gb(graph *g,KNOTEN n, KNOTEN *rhf) 
{

if (l_k==0) return(TRUE);
if (exist_small_set(g,(int) n, (int)l_n, (int)l_k-1, rhf)) return(FALSE);
return(TRUE);
}


/****************************************************************************/
/******** es folgen die "klassischen" Funktionen des Programms: *************/
/****************************************************************************/

/********************GIB_SPEICHER_FREI***************************************/
/*  diese Funktion bezieht sich nur auf den dynamischen Speicher, nicht aber
    auf den halbdynamischen                                                 */
/*  der erste Block wird nicht geloescht, sondern nur geleert, da kurz darauf
    sowieso wieder ein Speicherblock benoetigt wird. Der muss dann nicht 
    wieder alloziert werden.                                                */  
void gib_speicher_frei(void) {
  MEMORY *mem;
  if (firstmem) {
    currmem = firstmem->next;
    while (currmem) {
      mem = currmem->next;
      if (currmem->memory) {free(currmem->memory);}
      free(currmem);
      currmem = mem;
    }
    firstmem->next = nil;
    firstmem->used = 0;
    currmem = firstmem;
  }
}
 
/***********************GIB_FESTSPEICHER_FREI********************************/
/*  Diese Funktion bezieht sich nur auf den halbdynamischen Speicher.       */ 
/*  Diese Funktion gibt alle an "first" anschliessenden Speicherbloecke frei.
    In "first" selbst wird die Zahl der belegten Bytes auf "used_in_first"
    (herunter-)gesetzt. Ist first==nil, so wird die gesamte Liste 
    firstfestmem geloescht.                                                 */
    
void gib_festspeicher_frei(MEMORY *first,size_t used_in_first) {
  MEMORY *mem;
  if (first==nil) {               /* alles loeschen */
    while (firstfestmem) {
      mem = firstfestmem->next;
      if (firstfestmem->memory) {free(firstfestmem->memory);}
      free(firstfestmem);
      firstfestmem = mem;
    } 
    firstfestmem = currfestmem = nil;
  }
  else {
    currfestmem = first;
    first->used = used_in_first;
    first = first->next;
    while (first) {
      mem = first->next;
      if (first->memory) {free(first->memory);}
      free(first);
      first = mem;
    }
    currfestmem->next = nil;
  }
}

/***********************SCHLUSS**********************************************/

void schluss(void) {
  gib_speicher_frei();
  if (firstmem) {          /* ein leerer Speicherblock ist noch im Speicher */
    if (firstmem->memory) {free(firstmem->memory);}
    free(firstmem);
  }
  gib_festspeicher_frei(nil,0L);
  if (inputfile) {fclose(inputfile);}
  exit(0);
}

/**********SPEICHERE_GRAPHEN_AUS_OUTPUTLISTE_IN_FILES************************/
/*  Diese Funktion schreibt alle Daten aus der Outputliste in die
    zugehoerigen Files, wobei diese zuvor geoeffnet und anschliessend
    geschlossen werden. */

void speichere_graphen_aus_outputliste_in_files(void) {
  static unsigned short j;
  static FILE *logfile;
  static size_t to_write,written,written_gesamt;
  for (j = ramseygraph_ausgabe_all ? 0 : (unsigned short)outputlevel; 
       j<=(ramseygraph_ausgabe_all ? anz_testgraphen-1 : 
          (unsigned short)(nn+(ramsey))); j++) {
    if (outputbufferused[j]) {     /* gibt's was zu schreiben? */

      /* Datei oeffnen */
      if (standardout && (ramseygraph_ausgabe_all || j==nn+(ramsey))) 
         {outputfile[j] = stdout;}
      else {
        outputfile[j] = repeated_fopen(outputfilename[j],"a",5);
        if (outputfile[j]==nil) 
           {fprintf(stderr,"Can't open output file %s!\n",outputfilename[j]);
            perror((char *)"Error description");
            schluss();}
      }

      /* schreiben (haeppchenweise wegen "interrupted system call") */
      written_gesamt = 0L;     /* geschriebene Bytes in diesem Durchgang */
      while (written_gesamt < outputbufferused[j]) {
        to_write = MIN(output_one_row,outputbufferused[j]-written_gesamt);
        if ((written = fwrite(outputbufferstart[j]+written_gesamt,
            sizeof(KNOTEN),to_write,outputfile[j])) < to_write)  /* Fehler */
	   {schreibfehler(outputfilename[j],to_write,written,False);}
        written_gesamt += to_write;
      }
      outputbufferused[j] = 0L;

      /* Datei schliessen */
      if (!standardout || (!ramseygraph_ausgabe_all && j!=nn+(ramsey))) { 
        /* keine stdout-Ausgabe */
        if (ferror(outputfile[j])) { 
          fprintf(stderr,"Error while writing into file %s!\n",
                  outputfilename[j]);
          perror((char *)"Error description");
          if (logfile = repeated_fopen(logfilename,"a",5)) {
            fprintf(logfile,"Error while writing into file %s!\n",
                    outputfilename[j]);
            fclose(logfile);
          }
          schluss();
        }
        if (fclose(outputfile[j])) {       /* Fehler */
          fprintf(stderr,"Error while closing file %s!\n",
                  outputfilename[j]);
          perror((char *)"Error description");
          if (logfile = repeated_fopen(logfilename,"a",5)) {
            fprintf(logfile,"Error while closing file %s!\n",
                    outputfilename[j]);
            fclose(logfile);
          }
          schluss();
        }
      }
    }
  }
 
  if (save) {schreibe_dumpfile(False);}   /* weil die Graphen beim
                             Restart nicht mehr produziert werden */
}   

/*********************GIB_GRAPHEN_AUS**********************************/
/*   Format:     multi_code2_s_old   (wie multi_code_s_old, aber mit
                 1-Byte-Ausgabe der uebereinstimmenden Eintraege)     */
/*               oder  Brandt-Format                                  */
/*   Dient nur zur Zwischenspeicherung der Graphen, die spaeter 
     in einem Rutsch ausgegeben werden.                               */
/*   "file" ist die Nummer des Outputbuffers, in dem der Graph 
     zwischengespeichert werden soll                                  */

void gib_graphen_aus(graph *g, KNOTEN n, unsigned short file) {
  KNOTEN d=0;
  int dummy,anzahl;
  KNOTEN i,j;

  if (brandt) {
    anz_b = 0;
    for (i=0; i<n; i++) {
      for (j=0; j<n; j++) 
        {ausgabe_b[anz_b++] = (i!=j && ISELEM1(&g[i],j))+'0';}
    }
    ausgabe_b[anz_b++] = '\n'; 
    memcpy(outputbufferstart[file]+outputbufferused[file],ausgabe_b,
           sizeof(KNOTEN)*(size_t)anz_b);
    outputbufferused[file] += (size_t)anz_b;
  }
  else {
    dummy = (int)(output_graphenzahl[file]++)&1;
    anzahl = 0; 
    ausgabe[file][dummy][anzahl++] = n;
    for (i=0; i<n-1; i++) {
      for (j=i+1; j<n; j++) {
	if (ISELEM1(&g[i],j))
	  {ausgabe[file][dummy][anzahl++] = j+1;}
      }
      ausgabe[file][dummy][anzahl++] = 0;
    }
    anz[file][dummy] = anzahl;
    if (output_graphenzahl[file]>1) {       /* Uebereinstimmungen bestimmen */
      while (ausgabe[file][0][d]==ausgabe[file][1][d] && d<255 && 
             d<anz[file][0] && d<anz[file][1]) {d++;}
    }

    *(outputbufferstart[file]+(outputbufferused[file]++)) = d;
    memcpy(outputbufferstart[file]+outputbufferused[file],&ausgabe[file][dummy]
      [d],sizeof(KNOTEN)*(size_t)(anz[file][dummy]-(int)d));
    outputbufferused[file] += (size_t)(anz[file][dummy]-(int)d);
  }
  if (outputbufferused[file] > outputbufferlen[file])    
      /* Zwischenspeicher ist voll - alles ausgeben */
      {speichere_graphen_aus_outputliste_in_files();}
}

/***********************HOLE_SPEICHER****************************************/
/*  Stellt len Bytes dynamischen Speicher zur Verfuegung                    */
/*  len wird aufgerundet, so dass die Zahl durch sizeof(void *) teilbar ist */

void *hole_speicher(size_t len) {    
  static  void *ptr;
  static FILE *logfile;
  if (len > MEMBLOCKSIZE) { 
     fprintf(stderr,"Required memblock too big (change MEMBLOCKSIZE)!\n");
     if (logfile = repeated_fopen(logfilename,"a",5)) {
       fprintf(logfile,"Required memblock too big (change MEMBLOCKSIZE)!\n");
       fclose(logfile);
     }
     schluss();
  }
  if (currmem==nil) {            /* noch kein dynamischer Speicherbereich */
    if ((firstmem = currmem = (MEMORY *)malloc(sizeof(MEMORY)))==nil) {
       fprintf(stderr,"No memory for memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currmem->next = nil;
    currmem->used = 0;
    if ((currmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memblock!\n");
         fclose(logfile);
       }
       schluss();
    }
  }
  if (MEMBLOCKSIZE - currmem->used < len) {
      /* Speicherbereich zu klein => naechsten Block bereitstellen */  
    if ((currmem->next = (MEMORY *)malloc(sizeof(MEMORY)))==nil) { 
       fprintf(stderr,"No memory for new memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currmem = currmem->next;
    currmem->next = nil;
    currmem->used = 0;
    if ((currmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for new memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memblock!\n");
         fclose(logfile);
       }
       schluss();
     }
  }
  ptr = (char *)currmem->memory + currmem->used;
  currmem->used += (len-1)/sizeof(void *)*sizeof(void *)+sizeof(void *);
  return(ptr);
}   

/***********************HOLE_FESTSPEICHER************************************/
/*  Wie hole_speicher, aber in einer anderen Liste, die nur nach Rueckschritt
    in der Rekursion (teilweise) wieder geloescht wird                      */
/*  len wird aufgerundet, so dass die Zahl durch sizeof(void *) teilbar ist */

void *hole_festspeicher(size_t len) {    
  static void *ptr;
  static FILE *logfile;
  if (len > MEMBLOCKSIZE) {  
     fprintf(stderr,"Required memblock too big (change MEMBLOCKSIZE)!\n");
     if (logfile = repeated_fopen(logfilename,"a",10)) {
       fprintf(logfile,"Required memblock too big (change MEMBLOCKSIZE)!\n");
       fclose(logfile);
     }
     schluss();
  }
  if (currfestmem==nil) {       /* noch kein dynamischer Speicherbereich */
    if ((firstfestmem = currfestmem = (MEMORY *)malloc(sizeof(MEMORY)))==nil) {
       fprintf(stderr,"No memory for memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currfestmem->next = nil;
    currfestmem->used = 0;
    if ((currfestmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memblock!\n");
         fclose(logfile);
       }
       schluss();
    }
  }
  if (MEMBLOCKSIZE - currfestmem->used < len) {
      /* Speicherbereich zu klein => naechsten Block bereitstellen */  
    if ((currfestmem->next = (MEMORY *)malloc(sizeof(MEMORY)))==nil) { 
       fprintf(stderr,"No memory for new memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currfestmem = currfestmem->next;
    currfestmem->next = nil;
    currfestmem->used = 0;
    if ((currfestmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for new memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memblock!\n");
         fclose(logfile);
       }
       schluss();
    }
  }
  ptr = (char *)currfestmem->memory + currfestmem->used;
  currfestmem->used += (len-1)/sizeof(void *)*sizeof(void *)+sizeof(void *);
  return(ptr);
}   

/***********************HOLE_PERMANENTEN_SPEICHER****************************/
/*  Wie hole_speicher, aber in einer anderen Liste, die nie wieder geloescht
    wird                                                                    */
/*  len wird aufgerundet, so dass die Zahl durch sizeof(void *) teilbar ist */

void *hole_permanenten_speicher(size_t len) {    
  static void *ptr;
  static FILE *logfile;
  if (len > MEMBLOCKSIZE) {  
     fprintf(stderr,"Required memblock too big (change MEMBLOCKSIZE)!\n");
     if (logfile = repeated_fopen(logfilename,"a",10)) {
       fprintf(logfile,"Required memblock too big (change MEMBLOCKSIZE)!\n");
       fclose(logfile);
     }
     schluss();
  }
  if (currpermmem==nil) {       /* noch kein dynamischer Speicherbereich */
    if ((firstpermmem = currpermmem = (MEMORY *)malloc(sizeof(MEMORY)))==nil) {
       fprintf(stderr,"No memory for memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currpermmem->next = nil;
    currpermmem->used = 0;
    if ((currpermmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for memblock!\n");
         fclose(logfile);
       }
       schluss();
    }
  }
  if (MEMBLOCKSIZE - currpermmem->used < len) {
      /* Speicherbereich zu klein => naechsten Block bereitstellen */  
    if ((currpermmem->next = (MEMORY *)malloc(sizeof(MEMORY)))==nil) { 
       fprintf(stderr,"No memory for new memorynode!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memorynode!\n");
         fclose(logfile);
       }
       schluss();
    }
    currpermmem = currpermmem->next;
    currpermmem->next = nil;
    currpermmem->used = 0;
    if ((currpermmem->memory = (void *)malloc(MEMBLOCKSIZE))==nil) {
       fprintf(stderr,"No memory for new memblock!\n");
       if (logfile = repeated_fopen(logfilename,"a",10)) {
         fprintf(logfile,"No memory for new memblock!\n");
         fclose(logfile);
       }
       schluss();
    }
  }
  ptr = (char *)currpermmem->memory + currpermmem->used;
  currpermmem->used += (len-1)/sizeof(void *)*sizeof(void *)+sizeof(void *);
  return(ptr);
}   


/***********************HOLE_GROUPSIZE*****************************/
/*  Diese Funktion wird als "userlevelproc" aufgerufen. Mit ihrer
    Hilfe wird die Automorphismengroesse ermittelt.               */

void hole_groupsize(nvector *lab,nvector *ptn,int level,nvector *orbits,
     statsblk *stats,int tv,int index,int tcellsize,int numcells,
     int childcount,int n) {
  grpsize *= (unsigned long long)index;
}


/***********************SAVE_GROUPSIZE*****************************/
/* Diese Funktion speichert die im nauty-Format uebergebene Auto-
   morphismengroesse in der Liste fuer Knotenzahl n ab.           */

void save_groupsize(unsigned long long grpsize,KNOTEN n) {
  AUTOLISTE *curr,*curr_prev,*neu;

  /* Liste durchsuchen */
  curr = a_first[n];  curr_prev = nil;
  while (curr && curr->a_size < grpsize) 
        {curr_prev = curr;  curr = curr->next;}  
  if (curr) {
    if (curr->a_size == grpsize) {(curr->a_anz)++;}
    else {     /* curr->a_size > grpsize */
      neu = (AUTOLISTE*)hole_permanenten_speicher(sizeof(AUTOLISTE));
      neu->a_size = grpsize;
      neu->a_anz = 1;
      a_listlen[n]++;
      neu->next = curr;
      if (curr_prev) {curr_prev->next = neu;} else {a_first[n] = neu;}
    }
  }
  else {     /* neues Element wird letztes Listenelement */
    neu = (AUTOLISTE*)hole_permanenten_speicher(sizeof(AUTOLISTE));
    neu->a_size = grpsize;
    neu->a_anz = 1;
    a_listlen[n]++;
    neu->next = curr;    /* = nil */
    if (curr_prev) {curr_prev->next = neu;} else {a_first[n] = neu;}
  }    
}

/**********************GIB_GROUPSIZES_AUS************************************/
/*  Diese Funktion gibt die Liste der Groupsizes zusammen mit den Anzahlen
    der zugehoerigen Graphen aus, und zwar nach f. Es werden maximal max2
    Eintraege ausgegeben (max2==0 => alles ausgeben).                       */

void gib_groupsizes_aus(FILE *f,unsigned long long max2,KNOTEN von,KNOTEN bis) {
  AUTOLISTE *curr;
  KNOTEN i;
  unsigned long long max;
  for (i=von; i<=bis; i++) {
    max = max2;
    curr = a_first[i];
    fprintf(f,"\nAutomorphism groups for MTF graphs with %d vertices:\n",i);
    if (sizeof(unsigned long)==sizeof(unsigned long long)) {
      fprintf(f,"Number of different automorphism group sizes: %ld\n",
              a_listlen[i]);
      if (max2 && (max2<a_listlen[i]))
           {fprintf(f,"Some statistics (first %ld entries):\n",max2);}
      else {fprintf(f,"Statistics:\n");}
      fprintf(f,"  Groupsize   Number of graphs\n");
      while (curr && (max>0 || max2==0)) {
        fprintf(f,"%10ld    %10ld\n",curr->a_size,curr->a_anz);
        if (max>0) {max--;}            /* "if" eigentlich nicht noetig */
        curr = curr->next;
      }
    } 
    else {
      fprintf(f,"Number of different automorphism group sizes: %lld\n",
              a_listlen[i]);
      if (max2 && (max2<a_listlen[i]))
           {fprintf(f,"Some statistics (first %lld entries):\n",max2);}
      else {fprintf(f,"Statistics:\n");}
      fprintf(f,"  Groupsize   Number of graphs\n");
      while (curr && (max>0 || max2==0)) {
        fprintf(f,"%10lld    %10lld\n",curr->a_size,curr->a_anz);
        if (max>0) {max--;}            /* "if" eigentlich nicht noetig */
        curr = curr->next;
      }
    }
  }
}

/***********************HOLE_ZEIGER_AUS_BAUM*********************************/
/*   len = Anzahl Elemente in Menge                                         */
/*   menge = Array mit geordneter Menge                                     */
/*   der Zeiger auf die zugehoerige Menge wird aus dem Baum geholt          */
/*   Voraussetzung:  Es gibt das zugehoerige Blatt im Baum                  */

ELEM *hole_zeiger_aus_baum(KNOTEN *menge,KNOTEN len) {
  KNOTEN i=0;      /* Zeiger auf aktuelle Stelle in Menge */
  TREENODE *t;
  while (i<len) {         /* noch nicht am Ende des Codes */
    if (i==0) {t=tree[menge[0]];} 
    else {
      /* if (t->more==nil) {
           fprintf(stderr,"Logischer Fehler in hole_zeiger_aus_baum %d!\n",i);
           schluss();
         } */
      t = (t->more)[menge[i]-menge[i-1]-1];
    }
    /* if (t==nil) {
         fprintf(stderr,"Logischer Fehler 2 in hole_zeiger_aus_baum %d!\n",i);
       } */
    i++;
  }        /* while */
  return(t->ptr);
}   

/***********************LOESCHE_FORTSETZUNG_AUS_BAUM*************************/
/*   menge = Array mit geordneter Menge (mindestens STATICLEN+1 Elemente)   */
/*   Das Blatt, in dem der Verweis auf diese Menge steht, befindet (oder
     besser: befand) sich im dynamischen Speicher. Somit auch ein Array,
     auf das ein more-Zeiger zeigt. Dieser more-Zeiger wird geloescht, so
     dass alle nachfolgenden Blaetter im dynamischen Speicher nicht mehr
     angesprochen werden koennen                                            */

void loesche_fortsetzung_aus_baum(KNOTEN *menge) {
  KNOTEN i=0;        /* Zeiger auf aktuelle Stelle in Menge */
  TREENODE *t;
  while (i<STATICLEN) {  /* noch nicht am Ende des halbdynamischen Baumteils */
    if (i==0) {t=tree[menge[0]];} 
    else {
      /* if (t->more==nil) {
           fprintf(stderr,"Logischer Fehler in loesche_fortsetzung...!\n");
           schluss();
         } */ 
      t = (t->more)[menge[i]-menge[i-1]-1];
    }
    /* if (t==nil) {
         fprintf(stderr,"Logischer Fehler 2 in loesche_fortsetzung...!\n");
         schluss();
       } */
    i++;
  }        /* while */
  t->more = nil;         /* Fortsetzung loeschen */
}   

/***********************ORDNE_ZEIGER_IN_BAUM*********************************/
/*   len = Anzahl Elemente in Menge                                         */
/*   menge = Array mit geordneter Menge                                     */
/*   Sobald der feste Teil des Baums steht, kann immer diese Funktion
     benutzt werden, da in der Tiefe <= STATICLEN keine neuen Blaetter
     mehr erzeugt werden.                                                   */ 

void ordne_zeiger_in_baum(ELEM *ptr, KNOTEN *menge,KNOTEN len) {
  KNOTEN i=0;     /* Zeiger auf aktuelle Stelle in Menge */
  signed int j;
  TREENODE *t;
  TREENODE **tt;
  while (i<len) {         /* noch nicht am Ende des Codes */
    if (i==0) {t=tree[menge[0]];} 
    else {
      if (t->more==nil) {
        t->more = (TREENODE **)
                  hole_speicher(sizeof(TREENODE *)*(nn-menge[i-1]-1));
        for (j=(int)nn-(int)menge[i-1]-2; j>=0; j--) {(t->more)[j]=nil;}
      } 
      tt = t->more;
      t = tt[menge[i]-menge[i-1]-1];
    }
    if (t==nil) {    /* neuen Baumknoten einrichten */
      t = (TREENODE *)hole_speicher(sizeof(TREENODE));
      t->ptr = nil;
      t->more = nil;
      if (i==0) {tree[menge[0]]=t;} else {tt[menge[i]-menge[i-1]-1]=t;}
    }
    i++;
  }        /* while */
  t->ptr = ptr;
}   

/***********************ORDNE_FESTZEIGER_IN_BAUM*****************************/
/*   len = Anzahl Elemente in Menge                                         */
/*   menge = Array mit geordneter Menge                                     */
/*   diese Funktion ist fuer den Teil des Baums, der immer im Speicher
     erhalten bleibt (Tiefe <= STATICLEN)                                   */

void ordne_festzeiger_in_baum(ELEM *ptr, KNOTEN *menge,KNOTEN len) {
  KNOTEN i=0,j;     /* Zeiger auf aktuelle Stelle in Menge */
  TREENODE *t;
  TREENODE **tt;
  while (i<len) {         /* noch nicht am Ende des Codes */
    if (i==0) {t=tree[menge[0]];} 
    else {
      if (t->more==nil) {
        t->more = (TREENODE **)
                  hole_festspeicher(sizeof(TREENODE *)*(nn-menge[i-1]-1));
        for (j=0; j<nn-menge[i-1]-1; j++) {(t->more)[j]=nil;}
      } 
      tt = t->more;
      t = tt[menge[i]-menge[i-1]-1];
    }
    if (t==nil) {    /* neuen Baumknoten einrichten */
      t = (TREENODE *)hole_festspeicher(sizeof(TREENODE));
      t->ptr = nil;
      t->more = nil;
      if (i==0) {tree[menge[0]]=t;} else {tt[menge[i]-menge[i-1]-1]=t;}
    }
    i++;
  }        /* while */
  t->ptr = ptr;
}   

/***********************BILDE_UMKEHRFUNKTION*********************************/
/*  Diese Funktion bildet die Umkehrfunktion von lab und uebergibt das
    Ergebnis an labrev.  Die Funktion bildet die Werte 0,...,n-1 ab         */

void bilde_umkehrfunktion(nvector *lab,nvector *labrev,int n) {
  int i;
  for (i=0; i<n; i++) {labrev[lab[i]] = i;}
}

/************************ERKENNE_PERMUTATION******************************/
/*  wird von nauty aufgerufen, um einen Automorphismus zu registrieren.  */

void erkenne_permutation(int count, permutation *perm, nvector *orbits,
     int numorbits, int stabvertex, int n) {
  autom = True;
}   

/***********************FINDE_ORBITZAHLEN************************************/
/* Gegeben sind die Orbitzahlen eines Originalgraphen und der Automorphismus
   labrev, der den Originalgraphen in den kanonischen Graphen ueberfuehrt.
   Die vorliegende Funktion bestimmt die Orbitzahlen im kanonischen Graphen,
   wobei wie immer die Zahlen moeglichst klein sein sollen.                 */
/* "origorbits" und "nautorbits" duerfen auf dasselbe Array zeigen, da das
   Array erst vollstaendig ausgelesen und dann beschrieben wird.            */

void finde_orbitzahlen(int n,nvector *labrev,nvector *origorbits,
                       nvector *nautorbits) {
  static int eqv[MAXN][MAXN],anz[MAXN];
  int i,j,k;
  for (i=0; i<n; i++) {anz[i]=0;}      /* Aequivalenzlisten loeschen */
  /* Zunaechst werden alle Knoten, die im selben Orbit x liegen, in der
     Aequivalenzliste des Knotens x festgehalten: */
  for (i=0; i<n; i++) {eqv[origorbits[i]][anz[origorbits[i]]++] = i;}
  /* Nun werden diese Adjazenzlisten konvertiert, so dass sie fuer den
     kanonischen Graphen gueltig sind, und die kleinsten Nummern gesucht: */
  for (i=0; i<n; i++) {
    if (anz[i]>0) {
      k = n;          /* Nummer des kleinsten Knotens in der Adjazenzliste */
      for (j=0; j<anz[i]; j++)
        {if ((eqv[i][j] = labrev[eqv[i][j]]) < k) {k=eqv[i][j];} }
      for (j=0; j<anz[i]; j++) 
        {nautorbits[eqv[i][j]] = k;}
    }
  }  
}
  
/********************SPEICHERE_INDEXMENGE************************************/
/*  speichert die uebergebene Indexmenge in ein Listenelement               */

void speichere_indexmenge(int *index, int len, INDEXELEM **first) {
  INDEXELEM *ptr;
  ptr = (INDEXELEM *)hole_speicher(sizeof(INDEXELEM));
  ptr->next = *first;    /* first zeigt auf das erste Listenelement */
  *first = ptr;
  ptr->len = len;
  ptr->index = (int *)hole_speicher(sizeof(int)*(size_t)len);
  memcpy(ptr->index,index,sizeof(int)*(size_t)len);
}
           
/********************SORTIERE_PAARVEKTOR*************************************/
/*   Sortiert Menge von Paaren absteigend                                   */
/*   Bubblesort, da len selten groesser als 4 werden duerfte                */

void sortiere_paarvektor(PAAR *menge,int len) {
  static KNOTEN i;
  static PAAR h;
  while (len>1) {
    for (i=0; i<len-1; i++) {
      if (memcmp((char *)&menge[i],(char *)&menge[i+1],sizeof(PAAR))<0) 
        {h=menge[i]; menge[i]=menge[i+1]; menge[i+1]=h;}
    }    /* danach ist das kleinste Element ganz hinten */
    len--;
  }
}

/********************SORTIERE_MENGE******************************************/
/*   Sortiert Menge aufsteigend                                             */
/*   Bubblesort, da len selten groesser als 4 werden duerfte                */

void sortiere_menge(KNOTEN *menge,int len) {
  static KNOTEN i,h;
  static BOOL tausch;
  tausch = True;
  while (len>1 && tausch) {
    tausch = False;
    for (i=0; i<len-1; i++) {
      if (menge[i]>menge[i+1])
         {h=menge[i]; menge[i]=menge[i+1]; menge[i+1]=h; tausch=True;}
    }    /* danach ist das groesste Element ganz hinten */
    len--;
  }
}

/********************SORTIERE_MENGE2*****************************************/
/*   Bubblesort, da len selten groesser als 4 werden duerfte                */
/*   Arbeitet wie "sortiere_menge", aber die Quellmenge "menge" bleibt
     unveraendert. Stattdessen wird die sortierte Menge in das Array
     "zielmenge" geschrieben.                                               */

void sortiere_menge2(KNOTEN *menge,KNOTEN len,KNOTEN *zielmenge) {
  static KNOTEN i,h;
  static BOOL tausch;   /* zeigt an, ob in einem Durchlauf vertauscht wurde */
  memcpy(zielmenge,menge,sizeof(KNOTEN)*(size_t)len);
  tausch = True;        /* damit Schleife durchlaufen wird */
  while (len>1 && tausch) {
    tausch = False;
    for (i=0; i<len-1; i++) {
      if (zielmenge[i]>zielmenge[i+1]) {h=zielmenge[i]; 
        zielmenge[i]=zielmenge[i+1]; zielmenge[i+1]=h;  tausch=True;}
    }    /* danach ist das groesste Element ganz hinten */
    len--;
  }
}

/********************SPEICHERE_MENGE*****************************************/
/*  speichert die uebergebene Menge mitsamt Parameter "nautykandidaten" in ein 
    Listenelement und erzeugt einen Verweis im Baum auf das Listenelement   */

void speichere_menge(KNOTEN *menge, KNOTEN len, KNOTEN *kanon,
     KNOTEN nautykandidaten) {
  ELEM *ptr;  
  
  ptr = (ELEM *)hole_festspeicher(sizeof(ELEM));
  ptr->next = firstmenge;   /* zunaechst sind alle Elemente in einer Liste */
  firstmenge = ptr;
  if (ptr->next) {ptr->next->prev = ptr;}
  ptr->prev = nil;
  ptr->base = nil;   /* Zeiger auf zugehoerige Liste mit
                        aequivalenten Mengen (zu Beginn unbestimmt) */
  ptr->len = len;
  ptr->nautykandidaten = nautykandidaten;
  memcpy(ptr->menge,menge,sizeof(KNOTEN)*(size_t)len);
  if (nautykandidaten) {
    ptr->kanon = (KNOTEN *)
                 hole_festspeicher(sizeof(KNOTEN)*(size_t)nautykandidaten);
    memcpy(ptr->kanon,kanon,sizeof(KNOTEN)*(size_t)nautykandidaten);
  }
  else {ptr->kanon = nil;}
  ordne_zeiger_in_baum(ptr,menge,len);
  gute++;
}
           
/*************************KOMPLEMENTMENGE**********************************/
/*  bildet und uebergibt die (sortierte) Komplementmenge von "menge",
    die ebenfalls sortiert sein muss. n ist die Anzahl der Elemente in beiden
    Mengen zusammen                                                       */

void komplementmenge(KNOTEN *menge,KNOTEN len,KNOTEN *komp,KNOTEN *komplen,
     KNOTEN n) {
  KNOTEN i=0,pos=0;
  *komplen = 0;
  while (pos<len) {
    while (i<menge[pos]) {komp[(*komplen)++] = i; i++;}
    pos++; i++;
  }
  while (i<n) {komp[(*komplen)++] = i; i++;}
}  

/*************************SUCHE_ORIGINAL************************************/
/*   Findet das Original unter den gleichwertigen Kantenkombinationen.     */
/*   Zurueckgegeben wird die laufende Nummer des Originals innerhalb der
     Liste der Indexmengen.                                                */

int suche_original(KANTE *liste,int listlen,INDEXELEM *firstindexmenge,
                   KANTE *l2, int n) { 
  static graph g[MATRIXGROESSE2];
  signed int i, j, *k, erg=-1;
  for (i=0; i<n; i++) {g[i]=0;}             /* Graph loeschen */
  for (i=0; i<listlen; i++) {               /* Originalkanten setzen */
    ADDELEM1(&g[liste[i].k1],liste[i].k2);
    ADDELEM1(&g[liste[i].k2],liste[i].k1);
  }
  i = 0;
  while (firstindexmenge && erg==-1) {
    /* if (firstindexmenge->len != listlen) 
       {fprintf(stderr,"Logischer Fehler in suche_original!\n"); schluss();} */
    k = firstindexmenge->index;
    for (j=0; j<listlen; j++)
      {if (!ISELEM1(&g[l2[k[j]].k1],l2[k[j]].k2)) {j=listlen+1;} }
    if (j==listlen) {erg=i;}            /* alle Kanten stimmten ueberein */
    i++;  firstindexmenge = firstindexmenge->next;
  }   /* while */
  /* if (erg==-1) {fprintf(stderr,"Logischer Fehler 2 in suche_original!\n");
                   schluss();} */
  /* die folgenden Zeilen sind unnoetig (g ist ja nur lokal) */
  /* for (i=0; i<listlen; i++) {
       DELELEM1(&g[liste[i].k1],liste[i].k2);
       DELELEM1(&g[liste[i].k2],liste[i].k1);
     } */
  return(erg);
}

/***********************VORTEST_M******************************************/
/*  Bevor fuer gleichwertige Kantenmengen die Funktion "mache_nautytest"
    aufgerufen wird, wird dieser Vortest aufgerufen. Mit seiner Hilfe wird
    versucht, die Mengen auch ohne "nauty" zu klassifizieren.
    Jede Kante in der Menge besitzt 2 adjazente Knoten (wobei gemeinsame
    Knoten zweier Kanten wie getrennte Knoten behandelt werden). Es wird
    nun zu jeder Kantenkombination der Gradvektor der adjazenten Knoten 
    ausgerechnet, wobei die Grade in PAAREN (a,b) gespeichert werden.
    "a" gibt die Anzahl der benachbarten Knoten innerhalb der spannenden
    Menge an und "b" die Anzahl der benachbarten Knoten im Komplement.
    Auf diese Weise koennen z.B. folgende Kantenmengen unterschieden werden:
                   O   O---O---O     und    O---O   O---O
              (hierbei ist "O" ein Knoten und "---" eine Kante)
    Die a-Werte des (geordneten) Gradvektors der ersten Menge lauten 2,1,1,0.
    Die a-Werte des Gradvektors der zweiten Menge lauten 1,1,1,1.         */
/*  Es wird "True" zurueckgegeben, wenn der Test erfolgreich bestanden 
    wurde. */
/*  Bei dem Test werden eventuell einige Indexmengen aus der Liste geworfen
    und dabei vielleicht auch der Wert von "firstindexmenge" veraendert.
    Der neue Wert wird an die aufrufende Funktion zurueckgegeben. Dasselbe
    passiert mit "anz_mgl" und mit "orig". */

BOOL vortest_m(graph *g, int n, KANTE *liste, int listlen, KANTE *l2,
     INDEXELEM **firstindexmenge, int *anz_mgl, KNOTEN *menge, int len,
     KNOTEN *komp, int *orig) {
  static int i,j;
  static int *k;
  static PAAR dgv[MAXN2];        /* Doppelgradvektor */
  static PAAR dgv_orig[MAXN2];   /* Doppelgradvektor des Originals */
  static graph menge_mask;       /* die spannende Menge als Bitmaske */
  static graph komp_mask;        /* die Komplementmenge als Bitmaske */
  static int erg;        /* Vergleichsergebnis */
  static INDEXELEM *e;   /* Zeiger auf eine Indexmenge */
  static INDEXELEM *e2;  /* Zeiger auf den Vorgaenger von "e" */
  
  /* Spannende Menge und Komplementmenge als Bitmaske: */
  menge_mask = komp_mask = 0;
  for (i=0; i<len; i++)   {ADDELEM1(&menge_mask,menge[i]);}
  for (i=0; i<n-len; i++) {ADDELEM1(&komp_mask,komp[i]);} 

  /* Zunaechst Doppelgradvektor des Originals ausrechnen: */
  for (i=0; i<listlen; i++) {               /* Originalkanten setzen */
    ADDELEM1(&g[liste[i].k1],liste[i].k2);
    ADDELEM1(&g[liste[i].k2],liste[i].k1);
  }
  for (i=0; i<len; i++) {                   /* Adjazenzen zaehlen */
    dgv_orig[i].k1 = POPCOUNT(g[menge[i]]&menge_mask);
    dgv_orig[i].k2 = POPCOUNT(g[menge[i]]&komp_mask);
  }        
  sortiere_paarvektor(dgv_orig,len);        /* Vektor sortieren */
  for (i=0; i<listlen; i++) {               /* Originalkanten loeschen */
    DELELEM1(&g[liste[i].k1],liste[i].k2);
    DELELEM1(&g[liste[i].k2],liste[i].k1);
  }

  /* Nun die Liste durchgehen: */
  i = 0;
  e = *firstindexmenge;  e2 = nil;
  while (e) {
    if (i != *orig) {       /* Original nicht mit sich selbst vergleichen */
      k = e->index;

      /* Doppelgradvektor ausrechnen: */
      if (e->len!=listlen)
         {fprintf(stderr,"Logischer Fehler in vortest_m\n"); schluss();}
      for (j=0; j<e->len; j++)                     /* Kanten setzen */
        {ADDELEM1(&g[l2[k[j]].k1],l2[k[j]].k2);
         ADDELEM1(&g[l2[k[j]].k2],l2[k[j]].k1);}
      for (j=0; j<len; j++) {                      /* Adjazenzen zaehlen */
        dgv[j].k1 = POPCOUNT(g[menge[j]]&menge_mask);
        dgv[j].k2 = POPCOUNT(g[menge[j]]&komp_mask);
      }        
      sortiere_paarvektor(dgv,len);                /* Vektor sortieren */
      for (j=0; j<e->len; j++)       /* Kanten loeschen */
        {DELELEM1(&g[l2[k[j]].k1],l2[k[j]].k2);
         DELELEM1(&g[l2[k[j]].k2],l2[k[j]].k1);}

      /* Vektoren vergleichen: */
      erg = (int)memcmp((char *)dgv_orig,(char *)dgv,sizeof(PAAR)*(size_t)len);
      if (erg > 0) {                         /* e aus der Liste nehmen */
        (*anz_mgl)--;                        /* e2 nicht weitersetzen */
        if (i < *orig) {(*orig)--;}          /* "orig" verschiebt sich */
        if (e2) {e2->next = e->next;} else {*firstindexmenge = e->next;}
      }
      else if (erg < 0) {return(False);}  
                                             /* orig ist nicht kanonisch */
      else {e2 = e;   i++;}                  /* e bleibt in der Liste */
    }  /* if i!=*orig */
    else {e2 = e;   i++;}                    /* e bleibt in der Liste */
    e = e->next;
  }
  return(True);
}
        
/***********************MACHE_NAUTYTEST************************************/
/*  fuer eine Kantenmenge, die entfernt worden ist, wird geprueft, ob sie
    die kanonische Kantenmenge innerhalb der spannenden Menge ist         */
/*  Parameter:  siehe "gute_menge"                                        */
/*  Erlaeuterungen siehe Anfang des Programms                             */

BOOL mache_nautytest(graph *g, int n, KANTE *l2, int anz, INDEXELEM 
     *firstindexmenge, int anz_mgl, KNOTEN *menge, int len, KNOTEN *komp,
     int orig) {
  static FILE *logfile;
  static nvector lab[MAXN],labrev[MAXN],ptn[MAXN],orbits[MAXN];
  static graph g2[MATRIXGROESSE],canong[MATRIXGROESSE];  /* canong ist dummy */
  int i,j,n2,m2;      /* n2 = Groesse des konstruierten Graphen */ 
                      /* m2 = Anzahl setwords pro Knoten */
  int *k;
  BOOL erg = True;
  if ((n2=anz+n+anz_mgl)>MAXN)
    {fprintf(stderr,"Graph too big in function 'mache_nautytest'!\n");
     if (logfile = repeated_fopen(logfilename,"a",10)) {
       fprintf(logfile,"Graph too big in function 'mache_nautytest'!\n");
       fclose(logfile);
     }
     schluss();}
  m2 = (n2+WORDSIZE-1)/WORDSIZE;
  /* neue Knoten einfuehren und Farbklassen festlegen: */
  /* wegen der sowieso unterschiedlichen Klassen stoeren die Loops
     an den alten Knoten nicht */
  i=0;
  while (i<n-len) {lab[i] = komp[i];  ptn[i] = 1;  i++;}
  if (n-len>0) {ptn[i-1] = 0;}     /* Farbklasse 1: Restgraph */ 
  while (i<n) {lab[i]=menge[i-(n-len)];  ptn[i] = 1;  i++;}
  ptn[i-1] = 0;              /* Farbklasse 2: spannende Menge */
  while (i<n+anz) {lab[i] = i;  ptn[i] = 1;  i++;}
  ptn[i-1] = 0;              /* Farbklasse 3: Knoten, die die Kanten teilen */
  while (i<n+anz+anz_mgl) {lab[i] = i;  ptn[i] = 1;  i++;}
  ptn[i-1] = 0;              /* Farbklasse 4: Knoten fuer die Moeglichkeiten */
  /* Graph g nach g2 kopieren (unsaubere Version): */ 
  for (i=0; i<n2; i++) {
    EMPTYSET(&g2[m2*i],m2);
    if (i<n) {g2[m2*i] = g[i];}
  }
  /* Neue Knoten, die die Kanten aus "l2" teilen, einfuegen: */
  /* (Knoten n+i unterteilt die Kante l2[i]) */
  for (i=0; i<anz; i++) {
    ADDELEMENT(&g2[m2*l2[i].k1],n+i);  ADDELEMENT(&g2[m2*(n+i)],l2[i].k1);
    ADDELEMENT(&g2[m2*l2[i].k2],n+i);  ADDELEMENT(&g2[m2*(n+i)],l2[i].k2);
  }
  /* Neue Knoten, die die Moeglichkeiten repraesentieren, einfuegen: */
  i = n+anz;
  while (firstindexmenge) {
    k = firstindexmenge->index;
    for (j=0; j<firstindexmenge->len; j++)
      {ADDELEMENT(&g2[m2*(n+k[j])],i);  ADDELEMENT(&g2[m2*i],n+k[j]);}
    firstindexmenge = firstindexmenge->next;
    i++;
  }
  /* if (i!=n2) {fprintf(stderr,"Logischer Fehler in mache_nautytest!\n");
                 schluss();} */
  options.defaultptn = FALSE;
  options.getcanon = TRUE;
  options.userautomproc = erkenne_permutation;
  options.userlevelproc = NILFUNCTION;
  autom = False;
  for (i=0; i<n2; i++) {DELELEMENT(&g2[m2*i],i);}
  nauty(g2,lab,ptn,NILSET,orbits,&options,&stats,workspace,
        (setword)WORKSIZE,m2,n2,canong);   /* Automorphismen */
  /* Loops brauchen nicht wieder gesetzt zu werden, da g2 verfaellt */
  /* "canong" wird nicht gebraucht, nur "orbits" und "lab" */
  bilde_umkehrfunktion(lab,labrev,n2);      
  if (autom) {finde_orbitzahlen(n2,labrev,orbits,orbits);}
  /* ansonsten ist nach wie vor orbits[i]==i */
  for (i=0; i<anz_mgl && erg==True; i++) {
    if (i!=orig && orbits[labrev[n+anz+i]]<orbits[labrev[n+anz+orig]])
       {erg = False;}
  }   
  return(erg);
}
  
/***********************GUTE_MENGE*****************************************/
/*   Die Funktion testet:    
     Ist "menge" eine gute Menge in g in dem Sinn, dass durch Entfernung von
     Kanten innerhalb der Menge keine neuen Kanten gesetzt werden koennen?
     Einzige Moeglichkeit:  Eine Kante zwischen einem Element in der Menge
     und einem Element ausserhalb der Menge                               */
/*   Weiterer Test:  Durch das Loeschen von Kanten verringert sich die
     Valenz der beteiligten Knoten. Wird dadurch die Valenz bei einem 
     Knoten kleiner als diejenige des neuen Knotens (len)?  Falls dies der
     Fall ist und auch dann so bleibt, wenn der Knoten mit dem neuen Knoten
     verbunden wird, so ist die Menge unbrauchbar.  Das Array "val" enthaelt
     die Valenzen der Knoten.                                             */
/*   Ein neuer Vortest ist eingefuehrt: "Vortest v" (siehe Kommentar inner-
     halb der Funktion). Knoten, die denselben Gradvektor haben wie der
     potentielle neue Knoten, sind gleichwertig bezueglich ihrer Kanonizitaet
     und nauty wird spaeter darueber entscheiden, welcher Knoten kanonisch
     ist. Die gleichwertigen Kandidaten werden im Array "kanon" gespeichert,
     wobei "nautykandidaten" die Anzahl dieser Kanidaten angibt. Falls
     "nautykandidaten==0", so ist der neue Knoten automatisch kanonisch.
     "nautykandidaten" ist nur dann von Bedeutung, wenn "erg==True" ist. */
/*   Falls die spannende Menge am Ende als gut erkannt wird, so wird sie
     abgespeichert. */

void gute_menge(graph *g,KNOTEN n,KNOTEN *menge,KNOTEN len,KNOTEN *val) {
  static KANTE liste[LISTENGROESSE];   /* fuer entfernte Kanten */
  static KNOTEN komp[MAXN2];           /* Komplement von "menge" */
  static KNOTEN valenzvektor[MAXN2];        /* fuer "Vortest v": Referenz */
  static KNOTEN valenzvektor2[MAXN2];       /* fuer "Vortest v": Vergleich */
  static KNOTEN zykelvektor[LISTENGROESSE]; /* fuer "Vortest v": Referenz */
  static KNOTEN zykelvektor2[LISTENGROESSE]; /* fuer "Vortest v": Vergleich */
  static int len3;                           /* Laenge der beiden Vektoren */
  static int summe, summe2;        /* zur Summenbildung fuer "Vortest v" */
  static signed int vektorvgl;     /* Ergebnis des Vektorvergleichs */
  static BOOL sortiert;            /* True => Vektor fuer v wurde sortiert */
  static KNOTEN nautykandidaten;
  static KNOTEN kanon[MAXN2];
  static KNOTEN menge2[MAXN2];     /* zur Speicherung einer Knotenmenge */
  static KNOTEN len2;              /* Groesse dieser Menge */
  KNOTEN x,y,komplen;
  BOOL erg=True;
  int i,j,listlen = 0;

  spannende++;
  nautykandidaten = 0;
  /* Kanten aus der spannenden Menge entfernen: */
  for (x=0; x<len-1; x++) {
    for (y=x+1; y<len; y++) {
      if (ISELEM1(&g[menge[x]],menge[y])) {
        liste[listlen].k1 = menge[x];   liste[listlen].k2 = menge[y]; 
        listlen++;
        DELELEM1(&g[menge[x]],menge[y]);
        DELELEM1(&g[menge[y]],menge[x]);
        val[menge[x]]--;  val[menge[y]]--;
        if (val[menge[x]]<len-1 || val[menge[y]]<len-1) {erg=False; x=y=len;}
        /* pruefen, ob Valenz durch Entfernen der Kante zu klein wird */
      }
    }  /* for y */
  }    /* for x */
  
  /* "Vortest v":  Ist geordneter Gradvektor der Nachbarn des neuen Knoten v
     groesstmoeglich unter allen Knoten mit gleicher Valenz? */
  /* Dieser Test gehoert von der Ordnung her eigentlich an die Stelle, wo
     der neue Graph tatsaechlich erzeugt wird. Aus Geschwindigkeitsgruenden
     ist er aber bereits hier eingebaut, denn beim Fehlschlagen kann man sich
     die Konstruktion des neuen Graphen sparen. Zudem ist der Test auf zu
     kleine Valenz eigentlich auch ein "Vortest v". */
  /* Wenn kein anderer Knoten die Valenz des neuen Knotens hat (sondern alle
     Knoten eine hoehere Valenz haben), dann muessen keine Vergleiche durch-
     gefuehrt werden, die spannende Menge gilt weiterhin als gut und "call-
     nauty" behaelt den Wert 0.  Auf diese Weise muss in der Funktion
     "konstruiere" gar nicht mehr geprueft werden, ob der neue Knoten als
     einziger die kleinste Valenz hat. */
  /* WICHTIG: Wenn v nicht den groessten, sondern den kleinsten Gradvektor
     zugeordnet bekommen soll, so wird die Laufzeit massiv schlechter! */ 
  if (erg==True) {        /* bis jetzt ist die Menge noch gut */
    /* Valenzen wie nach dem Einfuegen des neuen Knotens herstellen: */
    for (x=0; x<len; x++) {val[menge[x]]++;}  
    /* Referenzgradvektor bestimmen: */
    summe = 0;   sortiert = False;
    for (x=0; x<len; x++) 
      {summe += (int)(valenzvektor[x] = val[menge[x]]);}
    /* Andere Gradvektoren bestimmen und vergleichen: */
    for (y=0; y<n; y++) {
      if (val[y]==len) {   /* y hat gleiche Valenz wie der neue Knoten */
        i = summe2 = 0;             /* i = Position im Gradvektor */
        for (x=0; x<n; x++) {
          if (x!=y && ISELEM1(&g[y],x)) 
            {summe2 += (int)(valenzvektor2[i++] = val[x]);}
        }
        if (i<len)      /* es fehlt noch die Verbindung zum neuen Knoten */ 
          {summe2 += (int)(valenzvektor2[i++] = len);} 
          /* Valenz des neuen Knotens */
        if (summe<summe2) {erg = False;  y = n;} 
        else if (summe2==summe) {
          sortiere_menge(valenzvektor2,(int)len);
          if (!sortiert) {sortiere_menge(valenzvektor,(int)len);
                          sortiert=True;}
          if ((vektorvgl = (signed int)memcmp(valenzvektor,valenzvektor2,
                           sizeof(KNOTEN)*len)) < 0) {erg=False; y=n;}
          else if (vektorvgl==0) {kanon[nautykandidaten++] = y;}
          /* gleichwertige Moeglichkeit => nauty wird spaeter entscheiden */
        }
      }   /* if val[y]==len */
    }
    /* Alte Valenzen wiederherstellen: */
    for (x=0; x<len; x++) {val[menge[x]]--;}
  }

  if (erg==True && nautykandidaten) { 
    /* weiterer "Vortest v":  Anzahl der 4-Zykel, die durch
    den neuen Knoten und die anderen Kandidaten laufen, vergleichen. */

    /* Adjazenzen wie nach dem Einfuegen des neuen Knotens herstellen: */
    g[n] = 0;  
    for (x=0; x<len; x++) {ADDELEM1(&g[menge[x]],n); ADDELEM1(&g[n],menge[x]);}
      
    summe = j = 0;               /* Zykelvektor vom neuen Knoten erstellen */
    for (x=0; x<len-1; x++) {
      for (y=x+1; y<len; y++) {
        summe += (int)(zykelvektor[j++] = POPCOUNT(g[menge[x]]&g[menge[y]]));
        /* Die Anzahl der 4-Zykel durch n, menge[x] und menge[y] ist eigentlich
           POPCOUNT minus 1, aber da bei "summe2" auch nicht subtrahiert wird,
           bedeutet dies keinen Einfluss auf den Vergleich */ 
      }
    }
    len3 = j;
    sortiert = False;         /* Zykelvektor ist noch nicht sortiert */

    i = 0;
    while (i<nautykandidaten) {    /* interessante Knoten durchgehen */
      len2 = 0;
      for (x=0; x<=n; x++)     /* Nachbarmenge des Knotens ermitteln */
        {if (kanon[i]!=x && ISELEM1(&g[kanon[i]],x)) {menge2[len2++] = x;} }

      summe2 = j = 0;         /* Zykelvektor vom Knotenkandidaten erstellen */
      for (x=0; x<len2-1; x++) {
        for (y=x+1; y<len2; y++) {
          summe2 += 
            (int)(zykelvektor2[j++] = POPCOUNT(g[menge2[x]]&g[menge2[y]]));
        }
      }
      if (summe < summe2) {erg = False;  i = nautykandidaten;}
      else if (summe > summe2) {kanon[i] = kanon[--nautykandidaten];}
      else {     /* Summe ist gleich => Vektoren ueberpruefen */
        sortiere_menge(zykelvektor2,len3);
        if (!sortiert) {sortiere_menge(zykelvektor,len3);  sortiert=True;}
        if ((vektorvgl = (signed int)memcmp(zykelvektor,zykelvektor2,
                          sizeof(KNOTEN)*len3)) < 0)
           {erg = False;  i = nautykandidaten;}
        else if (vektorvgl > 0) {kanon[i] = kanon[--nautykandidaten];}
        else {i++;}
          /* gleichwertige Moeglichkeit => nauty wird spaeter entscheiden */
      }
    }
 
    /* neue Adjazenzen wieder loeschen (g[n] ist jetzt unwichtig): */ 
    for (x=0; x<len; x++) {DELELEM1(&g[menge[x]],n);}
  }
  /* Ende der Vortests v */     
  
  if (erg==True && listlen>0) {      /* es wurden Kanten geloescht */
    /* Kann neue Kante zwischen der spannenden Menge und dem Komplement
       eingefuegt werden? */ 
    komplementmenge(menge,len,komp,&komplen,n);
    x=0;
    while (x<len) { 
      y=0;
      while (y<komplen) {
        /* die folgende if-Anweisung funktioniert auch dann, wenn (i,i)
           als Loop gesetzt ist, und zwar wegen der ersten Bedingung */
        if ((g[menge[x]]&g[komp[y]])==0 && !ISELEM1(&g[menge[x]],komp[y]))
          {erg=False;  y=komplen; x=len;}
        y++;
      }
      x++;
    }
  }

  /* geloeschte Kanten wieder einfuegen */
  while (listlen>0) {
    listlen--;
    ADDELEM1(&g[liste[listlen].k1],liste[listlen].k2);
    ADDELEM1(&g[liste[listlen].k2],liste[listlen].k1);
    val[liste[listlen].k1]++;
    val[liste[listlen].k2]++;
  }
  
  if (erg==True) {speichere_menge(menge,len,kanon,nautykandidaten);}
}  

/***********************GUTE_MENGE_TEIL2************************************/
/*   Der in diesem Teil aufgefuehrte Test koennte auch direkt in die
     Funktion "gute_menge" eingebaut werden. Jedoch soll er so oft wie 
     moeglich vermieden werden und wird deshalb erst aufgerufen, wenn fuer
     die spannenden Mengen die Aequivalenzklassen festgelegt sind, so dass
     der Test fuer jede Aequivalenzklasse nur einmal aufgerufen werden muss.
     Der Test:  Welche verschiedenen Moeglichkeiten gibt es, Kanten
     in die Menge einzusetzen?  Ist eine davon von groesserer Kardinalitaet
     als die entfernte Menge?  Falls ja, so ist die Menge nicht gut, denn
     der zugehoerige Graph ist nicht der kanonische Vorgaenger des erweiter-
     ten Graphen, der entsteht, indem man einen neuen Knoten mit der Menge
     verknuepft. Bei gleicher Kardinalitaet entscheidet nauty ueber die
     Kanonizitaet. Der Rueckgabewert "True" bedeutet, dass die Menge 
     kanonisch ist.                                                        */
/*   Um den Test durchfuehren zu koennen, ist es zunaechst notwendig, alle
     Kanten aus der spannenden Menge zu entfernen. Nachher muessen sie dann
     wieder in den Graphen eingefuegt werden. Beides wird jedoch in der 
     aufrufenden Funktion "konstruiere" gemacht. "liste" enthaelt die ent-
     fernten Kanten. Es sind "listlen" Stueck.                             */

BOOL gute_menge_teil2(graph *g,KNOTEN n,KNOTEN *menge,KNOTEN len,KANTE *liste,
     int listlen) {
  static KANTE l2[LISTENGROESSE];      /* fuer einsetzbare Kanten */
  static int l3[LISTENGROESSE];        /* Nummern der eingesetzten Kanten */
  static KNOTEN komp[MAXN2];           /* Komplement von "menge" */
  static graph einsetzbar[MATRIXGROESSE2];  /* (i,j) markiert <=> einsetzbar */
  static graph setze_ein[MATRIXGROESSE2];   /* (i,j) markiert <=> eingesetzt */
  int anz = 0;              /* Anzahl der (prinzipiell) einsetzbaren Kanten */
  int anz_muss = 0;     /* Anzahl der Kanten, die eingesetzt werden MUESSEN */
  int anz_mgl = 0;      /* Anzahl der gleichwertigen Moeglichkeiten */
  int orig;             /* Nummer des Originals unter diesen Moeglichkeiten */
  KANTE h;              /* Hilfsvariable */
  KNOTEN x,y,komplen;
  BOOL erg=True;
  int i,j;
  INDEXELEM *firstindexmenge = nil;      /* fuer gleichwertige Kantenmengen */

  if (listlen>0 && len>2) { 
    /* versuchen, verschiedene Kombinationen von Kanten einzusetzen
       (mindestens listlen Kanten muessen es sein) */
    /* listlen>0:  Falls keine Kante entfernt wurde, so kann in den Graphen
       (s.o.)      auch keine Kante eingesetzt werden, denn sonst haette
                   diese wegen der Maximalitaet bereits im Originalgraphen
                   (dem Graphen ohne die entfernten Kanten) eingesetzt
                   sein muessen.
       len>2:  falls len==1  =>  keine Kante einsetzbar
               falls len==2  =>  nur eine Kante einsetzbar, und falls sie
               einsetzbar ist, MUSS sie im Originalgraphen auch tatsaechlich
               vorhanden sein, da er sonst nicht maximal waere.
       generell:  Es muessen keine Kantenmengen getestet werden, von denen
          die geloeschten Kanten eine Teilmenge sind, denn sonst muesste
          wegen der Maximalitaet die Obermenge im Originalgraphen enthalten 
          sein. */
    /* Kanten finden, die prinzipiell im Originalgraphen sein koennen, d.h.
       die kein Dreieck mit dem Komplement der spannenden Menge bilden */
    for (x=0; x<n; x++) {einsetzbar[x]=0;}    /* Graph loeschen */
    for (x=0; x<len-1; x++) {
      for (y=x+1; y<len; y++) { 
        /* da menge[x] und menge[y] nicht miteinander verbunden sind,
           funktioniert dieser Test trotz der Loops */
        if ((g[menge[x]]&g[menge[y]])==0) {
          ADDELEM1(&einsetzbar[menge[x]],menge[y]);
          ADDELEM1(&einsetzbar[menge[y]],menge[x]);
          l2[anz].k1 = menge[x];   l2[anz].k2 = menge[y];   anz++;
        }
      }
    }
    /* if (anz<listlen)
        {fprintf(stderr,"Logischer Fehler 1 in gute_menge!\n"); schluss();}*/
    if (anz>listlen) {
      /* Die entfernten Kanten sind nicht die einzigen, die einsetzbar sind. 
         Nun Kanten herausfinden, die im Originalgraphen sein MUESSEN.
         Diese Kanten werden an den Anfang der Liste gestellt */
      nicht_eindeutig++;
      for (i=0; i<anz; i++) {
        if ((einsetzbar[l2[i].k1]&einsetzbar[l2[i].k2])==0) {
          /* Kante l2[i] muss im Originalgraph enthalten sein */
          h = l2[anz_muss];  l2[anz_muss] = l2[i];  l2[i] = h;  anz_muss++;
          /* sowohl die Kante i als auch die Kante anz_muss sind bereits
             geprueft worden, denn i>=anz_muss => durch Vertauschung wird
             nichts uebergangen */
        }
      }
      /* if (anz_muss>=listlen)
            {fprintf(stderr,"Logischer Fehler 2 in gute_menge!\n");
             schluss();} */
        
      /* nun alle maximalen Kombinationen finden */
      for (x=0; x<n; x++) {setze_ein[x]=0;}     /* Graph loeschen */
      for (i=0; i<anz_muss; i++) {    /* erzwungene Kanten setzen */
        ADDELEM1(&setze_ein[l2[i].k1],l2[i].k2);
        ADDELEM1(&setze_ein[l2[i].k2],l2[i].k1);
        l3[i] = i;
      }
      i = anz_muss;
      l3[i] = i;
      while (i>=anz_muss && erg==True) {
        while (l3[i]<anz && erg==True) {
          if ((setze_ein[l2[l3[i]].k1]&setze_ein[l2[l3[i]].k2])==0) {
            if (i+1>listlen) {erg=False;}    /* groessere Menge */
            else if (i+1==listlen) 
              {speichere_indexmenge(l3,i+1,&firstindexmenge);
               anz_mgl++;}
              /* falls die gespeicherte Menge nicht maximal ist, so wird
                 noch eine groessere Menge gefunden werden => gespeicherte
                 Menge uninteressant */
            ADDELEM1(&setze_ein[l2[l3[i]].k1],l2[l3[i]].k2);
            ADDELEM1(&setze_ein[l2[l3[i]].k2],l2[l3[i]].k1);
            i++;  l3[i]=l3[i-1]+1;
          }
          else {l3[i]++;}
        }  /* while */    
        i--;
        if (i>=anz_muss) {
          DELELEM1(&setze_ein[l2[l3[i]].k1],l2[l3[i]].k2);
          DELELEM1(&setze_ein[l2[l3[i]].k2],l2[l3[i]].k1); 
          l3[i]++;
        }
      }    /* while */
      /* die gespeicherten Mengen werden spaeter zusammen mit dem gesamten
         dynamischen Speicher geloescht */     
        
      /* if (erg==True && anz_mgl==0)
            {fprintf(stderr,"Logischer Fehler 3 in gute_menge!\n");
             schluss();} */
          
      if (erg==True && anz_mgl>1) {     /* nauty muss angewendet werden */
        komplementmenge(menge,len,komp,&komplen,n);
        orig = suche_original(liste,listlen,firstindexmenge,l2,(int)n);
        if ((erg = vortest_m(g,(int)n,liste,listlen,l2,&firstindexmenge,
            &anz_mgl,menge,(int)len,komp,&orig))) {
          if (anz_mgl>1) {
            nautys++;
            erg = mache_nautytest(g,(int)n,l2,anz,firstindexmenge,
                            anz_mgl,menge,(int)len,komp,orig);
          }
        }
      }   /* if */
    }   /* if anz>listlen */
    else {eindeutig++;}        /* anz==listlen */     

    /* for (i=0; i<listlen; i++) {     -- Test --
         int j;
         for (j=0; j<anz; j++) 
           {if (liste[i].k1==l2[j].k1 && liste[i].k2==l2[j].k2) {j=anz+2;} }
         if (j!=anz+3)
           {fprintf(stderr,"Logischer Fehler 4 in gute_menge!\n");
                    schluss();}
       } */      
  }    /* if listlen>0 && len>2 */
  
  return(erg);
}

/***********************BILDE_STARTGRAPH*************************************/
/*   Der Graph g wird geloescht, nur der Loop (i,i) wird gesetzt, und zwar
     aus technischen Gruenden. Es bedeutet NICHT, dass der Loop tatsaechlich
     vorhanden sein soll.                                                   */

void bilde_startgraph(graph *g) {
  int k;
  /* Inzidenzliste g erstellen: */
  for (k=0; k<MATRIXGROESSE2; k++)   {g[k]=0;  ADDELEM1(&g[k],k);}
}

/**********************RICHTE_STATIC_BAUM_EIN*******************************/
/* diese Funktion richtet denjenigen Teil des Mengenbaums ein, der nie
   geloescht wird                                                          */

void richte_static_baum_ein(void) {
  KNOTEN menge[STATICLEN];
  int i,j;
  for (i=0; i<STATICLEN; i++) {menge[i]=i;}
  while (True) {
    ordne_festzeiger_in_baum(nil,menge,STATICLEN);
    i = STATICLEN-1;
    while (menge[i]==nn-STATICLEN+i)  {i--;  if (i<0) {return;}  }
    menge[i]++; 
    for (j=i+1; j<STATICLEN; j++) {menge[j]=menge[i]+(j-i);}
  }
}

/************************SPEICHERE_PERMUTATION******************************/
/*  Wird von nauty aufgerufen, um einen Automorphismus zu speichern.       */
/*  Der Automorphismus wird noch im selben Rekursionsschritt ausgewertet,
    deshalb genuegt dynamischer Speicher.                                  */
/*  firstautom ist ein globaler Zeiger auf den ersten Automorphismus.      */

void speichere_permutation(int count, permutation *perm, nvector *orbits,
     int numorbits, int stabvertex, int n) {
  PERMUTATION *p;
  p = (PERMUTATION *)hole_speicher(sizeof(PERMUTATION));
  p->next = firstautom[n];
  firstautom[n] = p;
  memcpy(p->perm,perm,sizeof(permutation)*n);
}   

/********************SPEICHERE_PERMUTATION_FEST*****************************/
/*  Wird von nauty aufgerufen, um einen Automorphismus zu speichern.       */
/*  Der Automorphismus wird im naechsttieferen Rekursionsschritt ge-
    braucht, deshalb wird er im halbdynamischen Speicher gespeichert.      */ 
/*  firstautom ist ein globaler Zeiger auf den ersten Automorphismus.      */

void speichere_permutation_fest(int count, permutation *perm, nvector *orbits,
     int numorbits, int stabvertex, int n) {
  PERMUTATION *p;
  p = (PERMUTATION *)hole_festspeicher(sizeof(PERMUTATION));
  p->next = firstautom[n];
  firstautom[n] = p;
  memcpy(p->perm,perm,sizeof(permutation)*n);
}   

/***************************INIT_GROETZSCH**************gb*****************/

void init_groetzsch()

/* initialisiert die globale Variable groetzsch_g mit dem Groetzsch Graphen */

{

int i,j;

for (i=0; i<= MAXN2; i++) 
  { groetzsch_g.adj[i]=0;
    for (j=0; j<=MAXN2; j++) groetzsch_g.g[i][j]=0;
  }

groetzsch_g.g[0][0]=11;

einfugen(groetzsch_g.g,groetzsch_g.adj,1,2);
einfugen(groetzsch_g.g,groetzsch_g.adj,1,5);
einfugen(groetzsch_g.g,groetzsch_g.adj,1,7);
einfugen(groetzsch_g.g,groetzsch_g.adj,1,10);
einfugen(groetzsch_g.g,groetzsch_g.adj,2,3);
einfugen(groetzsch_g.g,groetzsch_g.adj,2,6);
einfugen(groetzsch_g.g,groetzsch_g.adj,2,8);
einfugen(groetzsch_g.g,groetzsch_g.adj,3,4);
einfugen(groetzsch_g.g,groetzsch_g.adj,3,7);
einfugen(groetzsch_g.g,groetzsch_g.adj,3,9);
einfugen(groetzsch_g.g,groetzsch_g.adj,4,5);
einfugen(groetzsch_g.g,groetzsch_g.adj,4,8);
einfugen(groetzsch_g.g,groetzsch_g.adj,4,10);
einfugen(groetzsch_g.g,groetzsch_g.adj,5,6);
einfugen(groetzsch_g.g,groetzsch_g.adj,5,9);
einfugen(groetzsch_g.g,groetzsch_g.adj,6,11);
einfugen(groetzsch_g.g,groetzsch_g.adj,7,11);
einfugen(groetzsch_g.g,groetzsch_g.adj,8,11);
einfugen(groetzsch_g.g,groetzsch_g.adj,9,11);
einfugen(groetzsch_g.g,groetzsch_g.adj,10,11);

belegestruct(&groetzsch_g);
}


/***************************INITIALISIERE***********************************/

void initialisiere(void) {
  KNOTEN i,k;

  /* "full", "ALL_MASK" und "ALL_MASK2" initialisieren */
  for (k=0; k<MAXN2; k++) {
    full[k]=0;
    for (i=0; i<=k; i++) {ADDELEM1(&full[k],i);}
  } 
  for (i=0; i<=knoten; i++) {
    ALL_MASK[i]=0;
    for (k=1; k<=i; k++) {ADDELEM1(&ALL_MASK[i],k);}
  }
  for (i=0; i<=MAXN2; i++) {
    ALL_MASK2[i]=0;
    for (k=0; k<i; k++) {ADDELEM1(&ALL_MASK2[i],k);}
  }

  /* "zweig" und "recoverzweig" initialisieren: */
  for (i=0; i<=MAXN2+1; i++) {zweig[i] = recoverzweig[i] = 0;}

  /* Ausgabestrings initialisieren: */
  for (i=0; i<=MAXN2; i++) {anz[i][0] = anz[i][1] = 0;}

  /* Graphenzahlen initialisieren: */
  for (i=0; i<=MAXN2; i++) {output_graphenzahl[i] = graphenzahl[i] = 0L;}

  /* Groupsize-Listen initialisieren: */
  for (i=0; i<=MAXN2; i++) {a_first[i] = nil;  a_listlen[i] = 0L;}

  /* Baeume initialisieren */
  richte_static_baum_ein();      /* dabei bekommt "currfestmem" einen Wert */

  /* startgraph (maximaler dreiecksfreier Graph mit 3 Knoten) bilden: */  
  bilde_startgraph(g);
  ADDELEM1(&g[0],1);    ADDELEM1(&g[1],0);
  ADDELEM1(&g[0],2);    ADDELEM1(&g[2],0);

  /* nauty-Aufruf vorbereiten: */
  options.getcanon = TRUE;
  options.digraph = FALSE;
  options.writeautoms = FALSE;
  options.writemarkers = FALSE;
  options.defaultptn = TRUE;
  options.cartesian = FALSE;
  options.linelength = 65;
  options.outfile = (FILE*)NULL;
  options.userrefproc = NILFUNCTION;
  options.userautomproc = NILFUNCTION;
  options.userlevelproc = NILFUNCTION;
  options.usernodeproc = NILFUNCTION;
  //options.usertcellproc = NILFUNCTION; //No longer used in nauty 2.4
  options.invarproc = NILFUNCTION;
  options.tc_level = 0;
  options.mininvarlevel = 0;
  options.maxinvarlevel = 0;
  options.invararg = 0;
  //options.groupopts = (groupblk *)NULL; //No longer used in nauty 2.4
}

/***************************INITIALISIERE2**********************************/
/*  Diese Initialisierung darf erst nach dem Laden des dump-Files erfolgen,
    da "outputlevel" veraendert werden koennte.                            */

void initialisiere2(void) {
  KNOTEN i;
  unsigned short j;
  KNOTEN *ptr;   /* fuer die Initialisierung des Outputbuffers */
  size_t used;   /* fuer die Initialisierung des Outputbuffers */

  /* "outputbufferstart", "outputbufferlen" und "outputbufferused": */
  ptr = outputbuffer;   used = 0L;
  
  if (ramseygraph_ausgabe_all) {    /* gleichmaessige Aufteilung */
    for (j=0; j<anz_testgraphen; j++) {
      outputbufferstart[j] = ptr;
      if (j==anz_testgraphen-1)       /* gesamten restlichen Buffer zuweisen */
         {outputbufferlen[j] = outputbuffergroesse - used - 
          (size_t)OUTPUTCODESIZE;}
      else {outputbufferlen[j] = (size_t)(outputbuffergroesse/anz_testgraphen)
            - (size_t)OUTPUTCODESIZE;}
      /*fprintf(stderr,"Buffer for test graph %ld: Address %ld, "
              "length %ld, extra space %ld\n",(unsigned long)j+ramsey_min,ptr,
              outputbufferlen[j],(size_t)OUTPUTCODESIZE);*/ 
      ptr += outputbufferlen[j]+(size_t)OUTPUTCODESIZE;
      used += outputbufferlen[j]+(size_t)OUTPUTCODESIZE;
      /* OUTPUTCODESIZE als Ueberhang, so dass der Buffer voll ist, wenn
         "outputbufferused > outputbufferlen" gilt */
      if (used>outputbuffergroesse) {
          fprintf(stderr,"Internal error: outputbuffer is initialized "
          "incorrectly: Used %ld of %ld!\n",used,outputbuffergroesse);
        schluss();
      } 
      outputbufferused[j] = 0L;
    } 
  }
  else {       /* fuer groessere Knotenzahlen mehr Speicherplatz */
    for (i=outputlevel; i<=nn+(ramsey); i++) {
      outputbufferstart[i] = ptr;
      if (i==nn+(ramsey))       /* gesamten restlichen Buffer zuweisen */
         {outputbufferlen[i] = outputbuffergroesse - used - 
          (size_t)OUTPUTCODESIZE;}
      else {outputbufferlen[i] = MAX((size_t)OUTPUTCODESIZE*3L,
            (size_t)(outputbuffergroesse)>>((nn+(ramsey)-i)*2L));}
      /*fprintf(stderr,"Buffer for %d vertex graphs: Address %ld, length %ld, "
                "extra space %ld\n",i,ptr,outputbufferlen[i],
                (size_t)OUTPUTCODESIZE);*/ 
      ptr += outputbufferlen[i]+(size_t)OUTPUTCODESIZE;
      used += outputbufferlen[i]+(size_t)OUTPUTCODESIZE;
      /* OUTPUTCODESIZE als Ueberhang, so dass der Buffer voll ist, wenn
         "outputbufferused > outputbufferlen" gilt */
      if (used>outputbuffergroesse) {
          fprintf(stderr,"Internal error: outputbuffer is initialized "
          "incorrectly: Used %ld of %ld!\n",used,outputbuffergroesse);
        schluss();
      } 
      outputbufferused[i] = 0L;
    }
  }
}

/***********************L_CONCAT********************************************/
/*  haengt die Elemente der Liste, in der sich elem2 befindet, an das Ende
    der Liste, in der sich elem1 befindet, an, und loescht die zweite Liste
    aus der Liste der Listen.
    Voraussetzung: Beide Elemente muessen sich bereits in Listen abseits
    der grossen Liste (mit allen spannenden Mengen) befinden.
    firstlist zeigt auf den Beginn und lastlist auf das Ende der Liste der 
    Listen.                                                                */
/*  die Parameter elem->prev werden nicht aktualisiert, da sie in den
    Listen mit aequivalenten Mengen nicht wichtig sind                     */

void l_concat(ELEM *elem1, ELEM *elem2, LISTE **firstlist,LISTE **lastlist) {
  LISTE *basis1, *basis2;
  ELEM *cell;
  basis1 = elem1->base;
  basis2 = elem2->base;
  if (basis1 != basis2) {        /* gleiche Liste => nichts passiert */
    basis1->last->next = basis2->first;
    cell = basis2->first;
    while (cell) {
      cell->base = basis1;
      cell = cell->next;
    }
    basis1->last = basis2->last;
    if (basis2->prevlist) {basis2->prevlist->nextlist = basis2->nextlist;}
    else                  {*firstlist = basis2->nextlist;}
    if (basis2->nextlist) {basis2->nextlist->prevlist = basis2->prevlist;}
    else                  {if (lastlist) {*lastlist = basis2->prevlist;}}
  }
}

/************************BILDE_AB*******************************************/
/*  Mit Hilfe von "perm" wird aus der Quellmenge die Zielmenge             */

void bilde_ab(KNOTEN *quellmenge,KNOTEN len,KNOTEN *zielmenge,
              permutation *perm) {
  static KNOTEN i;
  for (i=0; i<len; i++) {zielmenge[i] = perm[quellmenge[i]];}
  sortiere_menge(zielmenge,(int)len);
}

/******************WENDE_TESTGRAPHEN_AN*************************************/
/* Diese Funktion nimmt alle Testgraphen aus der Liste testgraphenliste[n-1]
   und prueft, ob diese in g enthalten sind. g besitzt n Knoten. Diejenigen
   Testgraphen, die nicht in g enthalten sind, werden in die Liste
   testgraphenliste[n] aufgenommen. */
/* Beim Pruefen werden die Obergraph- und Untergrapheigenschaften beruecksich-
   tigt, und zwar in folgender Weise:
   von_oben==True:  Die Testgraphen werden nach absteigender Kantenzahl durch-
   laufen (die Testgraphenliste also von vorn nach hinten).
   Falls Testgraph h in g^c enthalten ist, dann auch alle Untergraphen.
   von_oben==False: Die Testgraphen werden nach aufsteigender Kantenzahl durch-
   laufen (die Testgraphenliste also von hinten nach vorn). Falls Testgraph h 
   in g^c nicht enthalten ist, dann auch alle Obergraphen nicht.
   Die Graphen, auf deren Eigenschaft mittels anderer Graphen geschlossen
   werden kann, werden im Array "geprueft" markiert. */
/* Der Rueckgabewert ist dann "True", wenn jeder der Testgraphen in g^c
   enthalten ist. */
 
BOOL wende_testgraphen_an(graph *g,KNOTEN n,BOOL von_oben) {
  static GRAPH_STRUCT gg;             /* alle zu g gehoerigen Informationen */
  static unsigned short i;
  static unsigned short *k;           /* fuer Liste, die durchlaufen wird */
  static unsigned short *anf,*end;    /* Zeiger auf Grenzen dieser Liste */
  static unsigned short *l;           /* fuer Liste, die gefuellt wird */
  static signed int anf2,end2;        /* Grenzen dieser Liste */
  static TESTGRAPH *tg;               /* Zeiger auf getesteten Graphen */
  static unsigned short *j;           /* Laufvariable */
                          
  for (i=1; i<=anz_testgraphen; i++) {geprueft[i] = False;}
  nauty_to_graph(g,gg.g,gg.adj,n);
  belegestruct(&gg);
  anf = &testgraphenliste[n-1][testgraphenliste_anf[n-1]];
  end = &testgraphenliste[n-1][testgraphenliste_end[n-1]];
  l = testgraphenliste[n];    /* einmalige Zuweisung => Zeitersparnis */

  if (von_oben) {
    anf2 = testgraphenliste_anf[n-1];   end2 = anf2-1;
    for (k=anf; k<=end; k++) {  
      tg = testgraphen[*k];
      if (geprueft[*k]) {    /* der Graph ist markiert worden => er ist Unter-
        graph eines Graphen, der Subgraph von g ist => er ist selbst Subgraph
        von g => seine Untergraphen sind ebenfalls Subgraphen von g und koen-
        nen deshalb markiert werden */
        j = tg->untergraphen;
        while (*j) {geprueft[*j] = True;  j++;}
      }
      else {                        /* noch nicht geprueft */
        if (!(tg->maximalzahl_erreicht) || ramseygraph_ausgabe_all) {
          /* Test lohnt sich */
          if (contained_in_compl(&(tg->g),&gg)) {  /* Untergraphen markieren */
            j = tg->untergraphen;
            while (*j) {geprueft[*j] = True;  j++;}
          }
          else {                             /* Graph in die Liste aufnehmen */
            l[++end2] = *k;
            if (ramseygraph_ausgabe_all && n>=ramseylevel_start && !recover)
	       {gib_graphen_aus(g,n,tg->nummer - ramsey_min);}
            if (tg->ramsey < n)  {
              tg->ramsey = n;
              if (n==nn) {
                tg->maximalzahl_erreicht = True;
                if (ramseygraph_ausgabe)
                   {gib_graphen_aus(g,n,n+1);  ramseyfilefilled = True;}
              }
            }
          }
        } 
      }
    }
  }

  else {     /* von unten */
    end2 = testgraphenliste_end[n-1];   anf2 = end2+1;
    for (k=end; k>=anf; k--) {  
      tg = testgraphen[*k];
      if (geprueft[*k]) {  /* der Graph tg ist markiert worden => er ist Ober-
        graph eines Graphen, der nicht Subgraph von g ist => er ist selbst
        auch kein Subgraph von g => seine Obergraphen sind ebenfalls keine
        Subgraphen von g und koennen deshalb markiert werden. Desweiteren
        kann tg in die Liste l aufgenommen werden, jedoch noch nicht die
        Obergraphen von tg, da sonst die Reihenfolge in der Liste l durch-
        einandergeraten wuerde. */
        if (ramseygraph_ausgabe_all && n>=ramseylevel_start && !recover)
   	   {gib_graphen_aus(g,n,tg->nummer - ramsey_min);}
        if (tg->ramsey < n)  {
          tg->ramsey = n;
          if (n==nn) {
            tg->maximalzahl_erreicht = True;
            /* if (ramseygraph_ausgabe)
                  {gib_graphen_aus(g,n,n+1);  ramseyfilefilled = True;} */
            /* Diese Ausgabe entfaellt, denn es ist bereits ein Ramseygraph
               gespeichert worden, der auch fuer den vorliegenden Testgraphen
               gueltig ist. Denn "geprueft==True", also ist tg ein Obergraph
               eines Graphen, fuer den gerade festgestellt wurde, dass die
               Maximalzahl erreicht wurde, fuer den also ein Ramseygraph
               gespeichert wurde. */
          }
        }
        if (!(tg->maximalzahl_erreicht) || ramseygraph_ausgabe_all) 
           {l[--anf2] = *k;}   /* Graph tg in die Liste aufnehmen */
        j = tg->obergraphen;
        while (*j) {geprueft[*j] = True;  j++;}
      }
      else if (!(tg->maximalzahl_erreicht) || ramseygraph_ausgabe_all) {   
        /* noch nicht geprueft und Test lohnt sich */ 
        if (!contained_in_compl(&(tg->g),&gg)) {
          if (ramseygraph_ausgabe_all && n>=ramseylevel_start && !recover)
      	     {gib_graphen_aus(g,n,tg->nummer - ramsey_min);}
          if (tg->ramsey < n)  {
            tg->ramsey = n;
            if (n==nn) {
              tg->maximalzahl_erreicht = True;
              if (ramseygraph_ausgabe)
                 {gib_graphen_aus(g,n,n+1);  ramseyfilefilled = True;}
            }
          }
          if (!(tg->maximalzahl_erreicht) || ramseygraph_ausgabe_all) 
             {l[--anf2] = *k;}  /* Graph tg in die Liste aufnehmen */ 
          j = tg->obergraphen;
          while (*j) {geprueft[*j] = True;  j++;}
          /* die Obergraphen duerfen noch nicht in die Liste l aufgenommen
             werden (s.o.) */
        }
      } 
    }
  }     

  testgraphenliste_anf[n] = (unsigned short)anf2; 
  testgraphenliste_end[n] = (unsigned short)end2;
  if (anf2<=end2)
    {in_anteil[n] = 100-100*(end2-anf2+1)/((unsigned short)(end-anf)+1);}
  return(anf2>end2);
}

/**********************SORTIERE_KNOTEN**************************************/
/*  Sortiert die n Knoten im Graphen g nach aufsteigender Valenz.          */
/*  Die Valenzen sind im Array "val" bereits gegeben.                      */
/*  liste[i] enthaelt die Nummer des Nachfolgers von i in der Liste        */
/*  In der Liste stehen die Elemente 1,...,n statt 0,...,n-1.              */
/*  Am Ende erhaelt f die Abbildung.                                       */

void sortiere_knoten(KNOTEN n,KNOTEN *val,KNOTEN *f) {
  static KNOTEN liste[MAXN2+1];
  KNOTEN pos,i;
  liste[0] = 0;           /* Kennzeichnung fuer Ende der Liste */
  for (i=0; i<n; i++) {     /* Knoten i einsortieren */
    pos = 0;                /* Position in der Liste */
    while (liste[pos] && val[i]>val[liste[pos]-1]) {pos = liste[pos];}
    liste[i+1] = liste[pos];  liste[pos] = i+1;
  }
  pos = 0;   i = 0;
  while (liste[pos]) {f[i++] = liste[pos]-1;  pos = liste[pos];}
}

/**********************ERGAENZE_MENGE***************************************/
/*  Die Menge "menge[0]" bis "menge[anz-1]" bildet bereits eine spannende
    Menge. Insgesamt duerfen die spannenden Mengen aber "max" Elemente
    besitzen. Diese Funktion bildet alle Obermengen (einschliesslich der
    Menge selbst), die ebenfalls spannende Mengen sind. Dabei gilt, dass
    nur Elemente groesser "index[anz-1]" hinzukommen koennen, da alle
    anderen Elemente entweder bereits in der Menge oder verboten sind.
    "val" enthaelt die Valenzen der Menge. "f" ist die Abbildung von
    "index" nach "menge".                                                  */
/*  Die Funktion ruft den Test "gute_menge" auf.                           */
/*  In "verboten" stehen die Indices der Knoten, die die Menge nicht ergaen-
    zen duerfen. Dazu zaehlen auch die Knoten, die bereits in der Menge 
    sind.                                                                  */

void ergaenze_menge(KNOTEN n, graph *g, graph verboten,
     KNOTEN *menge, KNOTEN anz, KNOTEN min, KNOTEN max, 
     KNOTEN *val,graph men) {
  static KNOTEN j,zielmenge[MAXN2]; 
  KNOTEN i;
  graph verboten_S_c;   /* Knoten, die verboten, aber nicht in S sind */
  graph men_neu;

  if (anz>=min) {
    sortiere_menge2(menge,anz,zielmenge);
    gute_menge(g,n,zielmenge,anz,val);
  }
  if (anz<max) {          /* Es koennen noch Obermengen gebildet werden */
    /* Vortest siehe "spannende_menge" */ 
    if (verboten_S_c = verboten^men) {
      for (i=0; i<n; i++) {
        if (ISELEM1(&verboten_S_c,i)) {    /* i ist verboten und in S^c */
          for (j=0; j<anz; j++) {
            if (!ISELEM1(&g[i],menge[j]) && (g[i]&g[menge[j]]&~men)==0)
               {return;}
          }
        }
      }
    } 

    for (i=0; i<n; i++) {
      if (!ISELEM1(&verboten,i)) {
        menge[anz] = i;
        ADDELEM1(&verboten,i);
        men_neu = men;
        ADDELEM1(&men_neu,i);
        ergaenze_menge(n,g,verboten,menge,anz+1,min,max,val,men_neu);
        /* DELELEM1(&men,i); */
      }
    }
  }
}
   
/**********************SPANNENDE_MENGE**************************************/
/*  Diese Funktion ermittelt rekursiv spannende Mengen S.                  */
/*  "n" ist die Anzahl der Knoten im Graph "g", "verboten" markiert die
    verbotenen und "erreicht" die erreichten Knoten.                       */
/*  Wichtig:  Im Graphen muessen alle Loops gesetzt sein, damit nicht
    nur die Nachbarn eines Knotens, sondern auch der Knoten selbst als
    erreicht gekennzeichnet wird.                                          */
/*  "menge[0]" bis "menge[anz-1]" ist die ermittelte Menge S,
    "men" ist dasselbe als Bitmaske. "men" wird nicht veraendert, sondern
    bei Bedarf nach "men_neu" kopiert. In "men_neu" wird dann ein zusaetz-
    liches Bit gesetzt. Um den Vorgang rueckgaengig zu machen, muss das
    Bit nicht wieder geloescht werden, da "men" den dann wieder gueltigen
    Wert enthaelt. Auf diese Weise wird etwas Zeit gespart.  
    "max" gibt an, wie gross die spannende Menge hoechstens werden darf,
    "min" gibt an, wie gross sie mindestens sein muss. Der letztere 
    Parameter ist notwendig, weil in zwei Durchlaeufen nach spannenden
    Mengen gesucht wird. Beim zweiten Aufruf werden u.U. Mengen gefunden,
    die schon im ersten Durchlauf gefunden worden waren. Dies kann man ver-
    hindern, indem man eine Mindestgroesse voraussetzt, die die im ersten
    Durchlauf gefundenen Mengen allesamt nicht besitzen.                   */

void spannende_menge(KNOTEN n, graph *g, graph verboten, graph erreicht,
                     KNOTEN *menge, KNOTEN anz, KNOTEN min,
                     KNOTEN max, KNOTEN *val, graph men) {
  KNOTEN i,j;
  graph erreicht_neu;
  graph men_neu;
  graph verboten_S_c;    /* Knoten, die verboten, aber nicht in S sind */

  /* Vortest: Kann eine Kante zwischen einem Knoten aus S und einem
     verbotenen Knoten, der nicht in S ist, gezogen werden? Wenn ja,
     dann kann diese Kante auch dann gezogen werden, wenn S komplett
     erzeugt ist (da der verbotene Knoten nicht in S aufgenommen wird),
     also ist die spannende Menge nicht gut => es erfolgt Rueckschritt */
  /* In der if-Abfrage wird durch die "&~men"-Verknuepfung sichergestellt,
     dass gemeinsame Nachbarn hoechstens in S vorkommen koennen. Das ist
     erlaubt, da die Kanten in S spaeter ja allesamt entfernt werden und
     dadurch die entstandenen Dreiecke zerstoert werden. */ 
  if (verboten_S_c = verboten^men) {
    for (i=0; i<n; i++) {
      if (ISELEM1(&verboten_S_c,i)) {    /* i ist verboten und in S^c */
        for (j=0; j<anz; j++) {
          if ((g[i]&g[menge[j]]&~men)==0 && !ISELEM1(&g[i],menge[j]))
             {return;}
        }
      }
    }
  } 

  /* Knoten j, der noch nicht erreicht ist, finden */
  j=0;  while (ISELEM1(&erreicht,j)) {j++;}  
  /* j>=n kann nicht passieren, da mindestens ein Knoten fehlt */

  /* Schritt 1:  Knoten j wird in die Menge aufgenommen */
  if (!ISELEM1(&verboten,j)) {
    ADDELEM1(&verboten, j);      /* Knoten wird nicht nochmal aufgenommen */
    men_neu = men;
    ADDELEM1(&men_neu, j);
    menge[anz] = j;           /* Mengenelemente */
    if ((erreicht_neu = erreicht | g[j]) == full[n-1])
      {ergaenze_menge(n,g,verboten,menge,anz+1,min,max,val,men_neu);}  
       /* spannende Menge gefunden - Test auf gute Menge inklusive */
    else if (anz+1<max)
      {spannende_menge(n,g,verboten,erreicht_neu,
                       menge,anz+1,min,max,val,men_neu);}
    /* DELELEM1(&men, j); */
  }

  /* Schritt 2:  Knoten j ist verboten => einer der Nachbarn i muss in die
     Menge. */
  /* ADDELEM1(&verboten,j); */   /* ist bereits geschehen */  
  if (g[j] & (~verboten)) {      /* nicht alle Nachbarn sind verboten */
    for (i=0; i<n; i++) {          /* alle Nachbarn durchgehen */
      if (!ISELEM1(&verboten,i) && ISELEM1(&g[j],i)) {
        ADDELEM1(&verboten, i);
        men_neu = men;
        ADDELEM1(&men_neu, i);
        menge[anz] = i;
        if ((erreicht_neu = erreicht | g[i]) == full[n-1]) 
          {ergaenze_menge(n,g,verboten,menge,anz+1,min,max,val,men_neu);}
        else if (anz+1<max)
          {spannende_menge(n,g,verboten,erreicht_neu,menge,
                           anz+1,min,max,val,men_neu);}
        /* DELELEM1(&men_neu, i); */
      }
    }
  }
}

/**********************KONSTRUIERE******************************************/
/*        Diese Funktion steuert die gesamte Konstruktion                  */
/*  n ist die waehrend der Konstruktion erreichte Knotenzahl               */
/*  "callnauty" ist False, wenn das Ergebnis des Aufrufs aus der naechst-
    hoeheren Rekursionsebene bereits feststeht. In dem Fall zeigt 
    firstautom[n] auf den ersten gesuchten Automorphismus.                 */

void konstruiere(KNOTEN n,BOOL callnauty) {
  static KNOTEN menge[MAXN2];  /* menge = aufspannende Menge */
  static KNOTEN sortiert[MAXN2]; 
      /* Knoten nach aufsteigender Valenz geordnet */
  static graph canong[MATRIXGROESSE2];       /* nur eine Dummyvariable */
  static nvector orbits[MAXN2], ptn[MAXN2];
  KANTE *kante;   /* Zeiger auf eine Liste von entfernten Kanten innerhalb
                     der spannenden Menge */
  int kantenlistlen;   /* Anzahl der entfernten Kanten in spannender Menge */
  static nvector lab[MAXN2], labrev[MAXN2];      /* Umkehrfunktion von lab */
  KNOTEN *val,i,j,minval=MAXN2,anzval=0;   /* val[i] = Valenz von Knoten i */
  ELEM *e, *e2;
  PERMUTATION *p;
  MEMORY *membeginn;    /* vor dem aktuellen Durchlauf */
  size_t used;          /* vor dem aktuellen Durchlauf */
  LISTE *l;
  LISTE *firstlist=nil;    /* halbdynamischer Speicherplatz:
                              Zeiger auf Liste von Listen */
  LISTE *lastlist=nil;     /* letzte Liste in der Liste von Listen */
  static graph erreicht;   /* fuer die Ermittlung spannender Mengen */
  static graph verboten;   /* fuer die Ermittlung spannender Mengen */
  static graph men;        /* fuer die Ermittlung spannender Mengen */
  static BOOL ende;        /* Abbruchflag in verschiedenen Schleifen */
  BOOL accept;             /* neu erzeugter Graph wird akzeptiert */

  if (!recover && (int)n==level) {          /* Arbeit aufteilen */    
    /* nur falls recover==False, da sonst "count"-Wert veraltet */
    /* n ist gleichzeitig die Rekursionstiefe */
    count++;
    count = count%mod;
    if (count!=rest) {return;}
  }

  /* Zeiger auf globale Arrays festlegen: */
  val = valarray[n];
  kante = kantenarray[n];

  membeginn = currfestmem;     /* festhalten: ab wo muss freigegeben werden */
  if (membeginn) {used = currfestmem->used;}    /* (nach der Rekursion) */  

  /* der folgende Zeiger wird nur bis zur Auswertung von nauty gebraucht: */
  firstmenge=nil;
  
  /* Valenzen, kleinste Valenz und Anzahl der Knoten mit kleinster 
     Valenz ermitteln (anzval) sowie Bits aus hoeheren Ebenen loeschen: */
  for (i=0; i<n; i++) {
    g[i] &= full[n-1];            /* unerwuenschte Bits ausschliessen */
    val[i] = POPCOUNT(g[i]) - 1;      /* -1 wegen des Loops */
    if (val[i]==minval) {anzval++;}
    else if (val[i]<minval) {anzval=1; minval=val[i];}
  }
 
  /* spannende Mengen ermitteln: */
  verboten = erreicht = men = 0;
  /* spannende Mengen der Ordnung <= minval ermitteln: */
  spannende_menge(n,g,verboten,erreicht,menge,0,1,minval,val,men);
  if (anzval<minval+1) {
    /* spannende Mengen der Ordnung minval+1 sind interessant. Alle Knoten
       mit minimaler Valenz muessen allerdings in diesen Mengen enthalten
       sein. */
    verboten = erreicht = 0;
    j=0;
    for (i=0; i<n; i++) {     /* Knoten mit kleinster Valenz in Menge */
      if (val[i]==minval) {
        menge[j++] = i;
        ADDELEM1(&verboten,i);
        erreicht |= g[i];
      }
    }
    men = verboten;
    if (erreicht==full[n-1]) 
      {ergaenze_menge(n,g,verboten,menge,anzval,minval+1,minval+1,val,men);}
    else {spannende_menge(n,g,verboten,erreicht,
                    menge,anzval,minval+1,minval+1,val,men);}
  }
  else if (anzval==minval+1) {
    /* es kommt noch genau eine weitere spannende Menge in Betracht */
    j = 0;  /* Menge mit minimaler Valenz bestimmen (automatisch sortiert) */
    erreicht = 0;
    for (i=0; i<n; i++)
      {if (val[i]==minval) {menge[j++] = i;  erreicht |= g[i];} }
    if (erreicht==full[n-1]) {gute_menge(g,n,menge,minval+1,val);} 
  }

  /* nun aequivalente Mengen ermitteln: */
  if (callnauty) {
    firstautom[n] = nil;
    options.defaultptn = TRUE;
    options.getcanon = FALSE;
    options.userautomproc = speichere_permutation;
    if (grpsizes && !recover) 
      {options.userlevelproc = hole_groupsize;  grpsize = 1L;}
    else          {options.userlevelproc = NILFUNCTION;}
    for (i=0; i<n; i++) {DELELEM1(&g[i],i);}
    nauty(g,lab,ptn,NILSET,orbits,&options,&stats,workspace,
          (setword)WORKSIZE,(int)MAXM2,(int)n,NILGRAPH);
    for (i=0; i<n; i++) {ADDELEM1(&g[i],i);}
    if (grpsizes && !recover) {save_groupsize(grpsize,n);}
  }
  /* "orbits" und "lab" werden nicht gebraucht, der Aufruf dient nur
     zum Erhalt der Automorphismen */

  /* zunaechst Listen erstellen anhand des ersten Automorphismus: */
  if (firstautom[n]) {
    while (firstmenge) {     /* Liste mit spannenden Mengen aufteilen */
      l = (LISTE *)hole_festspeicher(sizeof(LISTE));
      l->first = l->last = firstmenge;
      if (revert) {
        l->nextlist = firstlist;
        l->prevlist = nil;
        if (firstlist) {firstlist->prevlist = l;}
        firstlist = l;
        /* lastlist wird nicht gebraucht */
      }
      else {
        l->nextlist = nil;
        l->prevlist = lastlist;
        if (lastlist) {lastlist->nextlist = l;}
        lastlist = l;
        if (!firstlist) {firstlist = l;}
      }
      firstmenge = firstmenge->next;   /* erstes Element ist abgespalten 
                                          und in einer eigenen Liste */
      if (firstmenge) {firstmenge->prev = nil;}
      l->last->next = nil;
      l->last->base = l;
      ende=False;
      do {
        bilde_ab(l->last->menge,l->last->len,menge,firstautom[n]->perm);
        /* die erhaltene Menge "menge" muss sich entweder in der aktuellen
           Liste oder in der grossen Liste befinden */

        e = hole_zeiger_aus_baum(menge,l->last->len);
        /* if (e==nil) {
             fprintf(stderr,"Logischer Fehler 3 in konstruiere!\n");
             schluss();
	   }  
           if (e->base!=nil && e->base!=l)
             {fprintf(stderr,"Logischer Fehler in konstruiere!\n");
              schluss();} */
        if (e->base==nil) {       /* Element ist in der grossen Liste */
          if (e->next) {e->next->prev = e->prev;}
          if (e->prev) {e->prev->next = e->next;} 
          else         {firstmenge = e->next;}
          /* if (e->len != l->last->len) 
               {fprintf(stderr,"Logischer Fehler 4 in konstruiere\n");
                schluss();} */ 
          e->base = l;
          l->last->next = e;
          e->next = nil;
          l->last = e;
        }
        else {
          ende=True;
          /* if (e!=l->first) 
               {fprintf(stderr,"Logischer Fehler 2 in konstruiere!\n");
                schluss();} */
	}
      } while (ende==False);
    }   /* while (firstmenge) */

    /* nun gibt es die grosse Liste nicht mehr */
    /* restliche Automorphismen durchgehen und Listen zusammenfassen */ 
    p = firstautom[n]->next;
    while (p) {

      if (revert) {       /* von vorn nach hinten durchgehen */
        l = firstlist;
        while (l) {
          e = l->first;
          while (e) {
            bilde_ab(e->menge,e->len,menge,p->perm);
            /* die erhaltene Menge "menge" muss sich in einer Liste befinden */
            e2 = hole_zeiger_aus_baum(menge,e->len);
            l_concat(e,e2,&firstlist,nil);
            e = e->next;
          }
          l = l->nextlist;
        }   /* while l */
      }
      else {              /* von hinten nach vorn durchgehen */
        l = lastlist;
        while (l) {
          e = l->first;
          while (e) {
            bilde_ab(e->menge,e->len,menge,p->perm);
            /* die erhaltene Menge "menge" muss sich in einer Liste befinden */
            e2 = hole_zeiger_aus_baum(menge,e->len);
            l_concat(e,e2,&firstlist,&lastlist);
            e = e->next;
          }
          l = l->prevlist;
        }   /* while l */
      }

      p = p->next;
    }     /* while p */

  }       /* if (firstautom[n]) */

  else {  /* jede Menge in eigene Liste */
    /* Dieses Vorgehen koennte auch dann angewendet werden, wenn es Auto-
       morphismen gibt. Es kostet allerdings mehr Speicherplatz, da mehr
       Listenkoepfe eingerichtet werden. */
    while (firstmenge) {
      l = (LISTE *)hole_festspeicher(sizeof(LISTE));
      l->first = l->last = firstmenge;
      if (revert) {
        l->nextlist = firstlist;
        l->prevlist = nil;
        if (firstlist) {firstlist->prevlist = l;}
        firstlist = l;
        /* lastlist wird nicht gebraucht */
      }
      else {
        l->nextlist = nil;
        l->prevlist = lastlist;
        if (lastlist) {lastlist->nextlist = l;}
        lastlist = l;
        if (!firstlist) {firstlist = l;}
      }
      firstmenge = firstmenge->next;   /* erstes Element ist abgespalten 
                                          und in einer eigenen Liste */
      l->last->next = nil;
      l->last->base = l;
    }   /* while */   
  }     /* else */
    
  /* nun sind die Listen mit den aequivalenten Mengen angelegt */
  gib_speicher_frei();          /* Baum wird nicht mehr gebraucht */
  /* Zeiger im Baum auf Blaetter mit Tiefe > STATICLEN loeschen:
     (Zeiger auf Elemente brauchen nicht geloescht zu werden, da auf sie
      im naechsten Rekursionsschritt nicht zugegriffen wird, wenn sie nicht
      vorher initialisiert wurden). */
  l = firstlist;
  while (l) {
    /* l->first existiert (es gibt keine leeren Listen) */
    if (l->first->len > STATICLEN) {   /* alle Mengen haben gleiche Groesse */
      e = l->first;
      while (e) {
        loesche_fortsetzung_aus_baum(e->menge);
        e = e->next;
      }
    }

    /* der folgende Teil kann also entfallen:
       else {
         e = l->first;
         while (e) {
           ordne_zeiger_in_baum(nil,e->menge,e->len);
           e = e->next;
         }
       } */

    l = l->nextlist;
  }   /* while l */

  /* nun Listen auswerten: */
  l = firstlist;
  while (l) {
    accept = TRUE;          /* solange kein Widerspruch gefunden wird */
    e = l->first;           /* repraesentant aus der Liste */

    /* "val" muss aktualisiert werden wegen der Option "lokale_dichte" */
    kantenlistlen=0;
    for (i=0; i<e->len-1; i++) {   /* Kanten innerhalb der spannenden Menge
                                       entfernen */
      for (j=i+1; j<e->len; j++) {
        if (ISELEM1(&g[e->menge[i]],e->menge[j])) {
          DELELEM1(&g[e->menge[i]],e->menge[j]);
          DELELEM1(&g[e->menge[j]],e->menge[i]);
          kante[kantenlistlen].k1 = e->menge[i];
          kante[kantenlistlen].k2 = e->menge[j]; 
          val[e->menge[i]]--;   val[e->menge[j]]--;
          kantenlistlen++;
        }
      }
    }

    if (gute_menge_teil2(g,n,e->menge,e->len,kante,kantenlistlen)) {
      /* Menge "e" ist kanonisch */
      g[n] = 0;   ADDELEM1(&g[n],n);
      for (i=0; i<e->len; i++) { 
        /* spannende Menge mit neuem Knoten verbinden */
        ADDELEM1(&g[e->menge[i]],n);
        ADDELEM1(&g[n],e->menge[i]);
        val[e->menge[i]]++;
      }

      if (e->nautykandidaten) {     /* nauty muss ueber Kanonizitaet des neuen
                                       Knotens entscheiden */
        options.defaultptn = TRUE;
        options.getcanon = TRUE;
        options.userautomproc = speichere_permutation_fest;
        if (grpsizes) {options.userlevelproc = hole_groupsize;  grpsize = 1L;}
        else          {options.userlevelproc = NILFUNCTION;} 
        autom = False;
        firstautom[n+1] = nil;
        for (i=0; i<n+1; i++) {DELELEM1(&g[i],i);}
        nauty(g,lab,ptn,NILSET,orbits,&options,&stats,workspace,
              (setword)WORKSIZE,(int)MAXM2,(int)n+1,canong);
        for (i=0; i<n+1; i++) {ADDELEM1(&g[i],i);}
        /* "canong" wird nicht gebraucht, der Aufruf dient nur zum Erhalt
           von "lab" und "orbits" */
        bilde_umkehrfunktion(lab,labrev,(int)n+1);
        if (firstautom[n+1]) 
          {finde_orbitzahlen((int)n+1,labrev,orbits,orbits);}
        for (i=0; i<e->nautykandidaten; i++) {
          if (orbits[labrev[(e->kanon)[i]]]<orbits[labrev[n]]) 
            {accept=FALSE; i=e->nautykandidaten;}
        }
      }
      else {acceptcount++;}     /* accept ohne nauty-Aufruf */
      /* falls jetzt accept==TRUE, so ist der neue Knoten im kleinsten
         kanonischen Orbit unter allen Knoten mit gleicher Valenz
         und gleichem Gradvektor (also allen kanonischen Kandidaten) */
 
      /* Tests zu Ende */
      if (accept && local_density && n+1>=l_n) { 
        /* lokale Dichte ermitteln */
        val[n] = e->len;            /* Valenz des Knotens n+1 */
        sortiere_knoten(n+1,val,sortiert);
        if (!(lokale_dichte_gb(g,n+1,sortiert))) {accept = False;}
      }

      /* auch lokale Dichte - falls ermittelt - ist noch gross genug
         => Graph kann endgueltig genommen werden */
      if (accept) {
	if (grpsizes && !recover && e->nautykandidaten && (int)(n+1)!=level) {
          /* e->nautykandidaten>0  =>  "nauty" wurde ausgefuehrt  =>
          nauty wird auf naechster Stufe nicht ausgefuehrt  =>  Groupsize
          jetzt speichern, aber nur, wenn n+1!=level, denn vielleicht wird ja
          der Strang nicht weiter verfolgt. */
          save_groupsize(grpsize,n+1);
        }
        akzeptiere(n+1,e->nautykandidaten==0 ||
                   (grpsizes && (int)(n+1)==level));    /* Rekursion */
           /* Falls n+1==level, dann auf jeden Fall nauty aufrufen, sofern
              grpsizes erwuenscht sind, denn sie wurde eben noch nicht
              gespeichert */
      }
     
      /* zurueck aus Rekursion oder Graph nicht akzeptiert: 
         Ursprungsgraphen wiederherstellen -> naechste Menge nehmen */
      for (i=0; i<e->len; i++) 
        {DELELEM1(&g[e->menge[i]],n);  val[e->menge[i]]--;}
    }   /* if gute_menge_teil2 */

    while (kantenlistlen>0) {   /* geloeschte Kanten wieder einfuegen */
      kantenlistlen--;
      ADDELEM1(&g[kante[kantenlistlen].k1],kante[kantenlistlen].k2);
      ADDELEM1(&g[kante[kantenlistlen].k2],kante[kantenlistlen].k1);
      val[kante[kantenlistlen].k1]++;  val[kante[kantenlistlen].k2]++;
    }
    l = l->nextlist;   /* gleich naechste Liste - alle zu e aequivalenten
                          Mengen brauchen nicht betrachtet zu werden */
  }    /* while l */

  /* es folgt Rekursionsrueckschritt:  Festspeicher mit Listen loeschen */ 
  gib_festspeicher_frei(membeginn,used);
}

/**********************AKZEPTIERE*******************************************/
/*  In "g" steht ein Graph mit n (n>3) Knoten, der soeben akzeptiert wurde.
    Diese Funktion entscheidet, ob der Graph abgespeichert wird, auf die
    Ramsey-Eigenschaft geprueft wird oder ob auf ihm aufgebaut wird.       */

void akzeptiere(KNOTEN n,BOOL callnauty) {
  static GRAPH_STRUCT gg;
  static BOOL ende,count;
  static KNOTEN i,j;
  static nvector lab[MAXN2];   /* naechsten Graphen mit n Knoten erreicht */
  static nvector orbits[MAXN2], ptn[MAXN2];
  zweig[n]++;       /* naechsten Graphen mit n Knoten erreicht */
  zweig[n+1] = 0;   /* an DIESER Stelle muss dieser Befehl stehen, falls
        der soeben hinzugekommene Graph die Outputliste vollmacht und deshalb
        das Dumpfile erzeugt wird. Durch diesen Befehl wird das Ende der
        Verzweigung angezeigt. */
  if (!recover) {
    if (twin || groetzsch) {                 /* nur bedingt zaehlen */
      count = False;
      if (twin) {
        for (i=0; !count && i<n-1; i++) {
          for (j=i+1; !count && j<n; j++) {
            if ((g[i] & ~bit[i]) == (g[j] & ~bit[j])) {count = True;}
          }
        }
      }
      if (groetzsch && (!twin || count)) { 
        /* twin && count==False => nicht zaehlen */
        count = False;     /* wieder loeschen nach "twin"-Test */
        nauty_to_graph(g,gg.g,gg.adj,n);
        belegestruct(&gg);
        if (subgraph(&groetzsch_g,&gg)) {count = True;}
      }
      if (count) {graphenzahl[n]++;}
    }
    else {graphenzahl[n]++;}    /* auf jeden Fall zaehlen */
  }

  /* Graphen ausgeben oder als Referenzgraphen verwenden: */
  if (ramsey) {         /* auch im Recovermodus einsetzen */
    ende = wende_testgraphen_an(g,n,False /*in_anteil[n-1]>49*/);
           /* Testgraphen ausfiltern - "False" geht am schnellsten */
    if (ramseygraph_ausgabe_all) {ende = False;}  /* weitermachen */ 
  }
  else { 
    if (outputlevel<=n && !recover && ((!twin && !groetzsch) || count))
       {gib_graphen_aus(g,n,n);}
        /* Bei Gebrauch der Option "ramsey" sollen NICHT die mtf-Graphen
           ausgegeben werden, sondern die eingelesenen Graphen, gestaffelt
           nach Ramseyzahlen (darum nur hier speichern, nicht bei ramsey). */
    ende = False;
  }

  /* Weiterarbeiten: */
  if (!recover || (zweig[n] == recoverzweig[n])) {  
    /* aktueller Graph ist der, mit dem weitergearbeitet werden soll */
    if (save_flag && !recover) {    /* zwischenspeichern */
       /* !recover ist wichtig, falls das Recovern laenger als S_intervall
          Sekunden dauert (solange das Programm noch im Recover-Modus ist,
          wuerden nur nutzlose dump-Files abgespeichert werden). */
       /* In der Outputliste angesammelte Graphen ausgeben (denn die werden
          bei einem Restart nicht nochmal erzeugt) und Dumpfile speichern: */
      speichere_graphen_aus_outputliste_in_files();
    }
    if (n<nn && !ende)  {
      if (recover && recoverzweig[n+1]==0)  /* Recover-Modus abgeschlossen */
         {recover = False;   count = recovercount;   count2 = recovercount2;}
      konstruiere(n,callnauty);
    } /* ende=True => es sind keine Testgraphen mehr uebrig */
    else if (grpsizes && n==nn && !recover && callnauty) { 
      /* groupsize noch nicht errechnet und "konstruiere" wird nicht mehr 
         aufgerufen => hier aufrufen */
      options.defaultptn = TRUE;
      options.getcanon = FALSE;
      options.userautomproc = NILFUNCTION;
      options.userlevelproc = hole_groupsize;   grpsize = 1L;
      for (i=0; i<n; i++) {DELELEM1(&g[i],i);}
      nauty(g,lab,ptn,NILSET,orbits,&options,&stats,workspace,
         (setword)WORKSIZE,(int)MAXM2,(int)n,NILGRAPH);
      for (i=0; i<n; i++) {ADDELEM1(&g[i],i);}
      save_groupsize(grpsize,n);
    }
    else if (recover && recoverzweig[n+1]==0) /* Recover-Modus abgeschlossen */
      {recover = False;   count = recovercount;   count2 = recovercount2;}
  }   /* if (!recover) */
}
  
/**********************READ_OLD_OR_NEW***********************************/
/*  This function decides for a short-code  if a number is to be read   */
/*  from the last graph or from the file                                */

char read_old_or_new(FILE *f,unsigned char *lastinput,unsigned char s,
  unsigned char *z,unsigned char *num) {
  if (*z>=s) {        /* new number to be read from the file */
    if (fread(num,sizeof(unsigned char),1,f)==0) {return(2);}
    if (lastinput && *z<maxentries) {lastinput[*z] = *num; (*z)++;}
  }  /* if */
  else {*num = lastinput[*z]; (*z)++;}
  return(1);
}

/**********************READ_MULTI_CODE2_S_OLD*****************************/
/* nur fuer Graphen, die im unsigned-char-format gespeichert sind        */
/* Loops werden automatisch hinzugefuegt                                 */

char read_multi_code2_s_old(FILE *f,graph *g,KNOTEN *n) {
  static int i;
  static unsigned char s, z, num;    /* z = read numbers */
  static unsigned char lastinput[maxentries];
  if (fread(&s,sizeof(unsigned char),1,f)==0) {return(feof(f) ? 0 : 2);}
  if (s>maxentries) {return(3);}
  z=0;
  if (read_old_or_new(f,lastinput,s,&z,(unsigned char *)n)==2) {return(2);}
  if (*n >= nn) {return(4);}                   /* Graph zu gross */
  for (i=0; i<*n; i++) {g[i]=0;}
  i=0;
  while (i < (*n)-1) {
    if (read_old_or_new(f,lastinput,s,&z,&num)==2) {return(2);}
    if (num!=0) {ADDELEM1(&g[i],num-1); ADDELEM1(&g[num-1],i);}
    else {i++;}
  }  /* while */
  for (i=0; i<*n; i++) {ADDELEM1(&g[i],i);}
  return(1);
}

/*********************TESTGRAPH_IN_LISTE************************************/
/*  Speichert den Testgraphen g, der n Knoten besitzt, in die Liste aller
    Testgraphen, und zwar in absteigender Reihenfolge der Kantenzahlen.    */
/*  Die Testgraphen enthalten keine Loops.                                 */

void testgraph_in_liste(graph *g, KNOTEN n, unsigned long nummer) {
  static TESTGRAPH *t,*tl,*tl2;    /* tl2 = Vorgaenger von tl */
  static unsigned short i,kanten;
  t = (TESTGRAPH *)hole_festspeicher(sizeof(TESTGRAPH));
  t->n = n;
  t->nummer = nummer;
  t->ramsey = 0;
  t->maximalzahl_erreicht = False;
  nauty_to_graph(g,(t->g).g,(t->g).adj,n); 
  belegestruct(&(t->g));
  kanten = 0;
  for (i=1; i<=n; i++) {kanten += (t->g).adj[i];}
  t->kanten = kanten>>1;
  t->obergraphen = t->untergraphen = nil;
  anz_testgraphen++;
  if (anz_testgraphen>TESTGRAPHENLISTENGROESSE) {
    fprintf(stderr,"Error: Too many test graphs (maximum %d)!\n",
            TESTGRAPHENLISTENGROESSE);  exit(0);
  }

  /* Testgraph einsortieren: */
  tl = firsttestgraph;   tl2 = nil;
  while (tl && tl->kanten > t->kanten) {tl2 = tl;  tl = tl->next;}
  if (tl2) {tl2->next = t;} else {firsttestgraph = t;}
  t->next = tl;
}

/*******************ERSTELLE_TESTGRAPH_LISTENARRAY**************************/
/*  Diese Funktion erstellt das Array "testgraphen" und das Array
    "testgraphenliste" fuer die oberste Rekursionsebene, wobei diese Liste
    fuer alle Ebenen kopiert wird. Die Ebene, in der das Programm beginnt,
    kann naemlich variieren, z.B. bei Benutzung der Option "file".         */
/*  Ferner wird der Zeiger "geprueft" initialisiert.                       */
/*  Ausserdem wird das Array "in_anteil" initialisiert.                    */

void erstelle_testgraph_listenarray(void) {
  TESTGRAPH *t;
  unsigned short i;
  KNOTEN n;

  /* Arrays erstellen: */
  testgraphen = (TESTGRAPH **)
      hole_festspeicher(sizeof(TESTGRAPH *)*((size_t)anz_testgraphen+1));
      /* erstes Element dieses Arrays bleibt frei */
  testgraphenliste[3] = (unsigned short *)
      hole_festspeicher(sizeof(unsigned short)*((size_t)anz_testgraphen));
      /* erstes Element dieses Arrays bleibt NICHT frei */
  t = firsttestgraph;
  for (i=1; i<=anz_testgraphen; i++) {
    testgraphen[i] = t;
    t = t->next;
    testgraphenliste[3][i-1] = i;
  }
  testgraphenliste_anf[3] = 0;
  testgraphenliste_end[3] = anz_testgraphen-1;

  /* Array "testgraphenliste" kopieren: */
  for (n=4; n<=nn; n++) { 
    testgraphenliste[n] = (unsigned short *)
      hole_festspeicher(sizeof(unsigned short)*((size_t)anz_testgraphen+1));
    memcpy(testgraphenliste[n],testgraphenliste[3],sizeof(unsigned short)*
      ((size_t)anz_testgraphen));
    testgraphenliste_anf[n] = 0;
    testgraphenliste_end[n] = anz_testgraphen-1;
  }

  /* Array "geprueft" initialisieren: */
  geprueft = (BOOL *)
      hole_festspeicher(sizeof(BOOL)*((size_t)anz_testgraphen+1));

  /* Array "in_anteil" initalisieren: */
  for (n=3; n<=nn; n++) {in_anteil[n] = 0;}
}

/*****************ERSTELLE_TESTGRAPH_KANTENARRAY****************************/
/*  Diese Funktion erstellt das Array "testgraphkanten"                    */
/*  Die Testgraphen muessen nach der Kantenzahl sortiert sein.             */

void erstelle_testgraph_kantenarray(void) {
  static TESTGRAPH *t;
  static int i;
  for (i=0; i<LISTENGROESSE; i++)
    {testgraphkanten[i].end = 0;  testgraphkanten[i].anf = anz_testgraphen+1;}
  t = firsttestgraph;  i = 1;
  while (t) {
    if (i < testgraphkanten[t->kanten].anf)
       {testgraphkanten[t->kanten].anf = i;}
    if (i > testgraphkanten[t->kanten].end)
       {testgraphkanten[t->kanten].end = i;}
    t = t->next;  i++;
  } 
}

/****************ERMITTLE_OBERGRAPHEN***************************************/
/*  Zu jedem Testgraphen in der Testgraphenliste werden die minimalen
    Obergraphen ermittelt. Da die Testgraphen nach absteigender Kantenzahl
    sortiert sind, muessen jedem Graphen G nur diejenigen Graphen H zugeord-
    net werden, die weiter vorn in der Liste stehen. Diese Graphen werden
    wiederum von hinten nach vorn durchlaufen. Wenn ein Graph H als Obergraph
    ermittelt wird, so brauchen dessen Obergraphen nicht mehr geprueft zu
    werden, da sie keine minimalen Obergraphen mehr sein koennen. Das Array
    "pruefen" enthaelt fuer jeden Obergraphenkandidaten H die Information
    darueber, ob er noch zu pruefen ist. */

void ermittle_obergraphen(void) {
  static unsigned short *numlist;  /* Speicher fuer die Liste von Graphen */
  static BOOL *pruefen;
  static unsigned short pos;       /* Position in Liste */
  static unsigned short n;      /* gerade betrachteter Graph */
  static unsigned short i;      /* gerade betrachteter Obergraphenkandidat */
  static unsigned short j;
  numlist = (unsigned short *)
            hole_speicher(sizeof(unsigned short)*(size_t)anz_testgraphen+1);
  pruefen = (BOOL *)hole_speicher(sizeof(BOOL)*(size_t)anz_testgraphen+1);
    /* Speicher wird nur hier gebraucht => kein Festspeicher notwendig */
  n = 1;
  while (n<=anz_testgraphen) {     /* Graphen durchgehen */
    pos = 0;
    for (i=1; i<n; i++) {pruefen[i] = True;}
    /* Im folgenden beginnt die Schleife bei dem ersten Graphen, der MEHR
       Kanten hat als Graph n, denn alle Graphen mit gleicher Kantenzahl sind
       sowieso keine Obergraphen von Graph n (wenn man davon ausgeht, dass
       sich in der Liste keine isomorphen Graphen befinden) */
    for (i=testgraphkanten[testgraphen[n]->kanten].anf-1; i>=1; i--) {
      if (pruefen[i]) {
        if (subgraph(&(testgraphen[n]->g),&(testgraphen[i]->g))) {
          /* subgraph==True => i ist Obergraph von n,
             pruefen==True  => i ist minimaler Obergraph von n */
          numlist[pos++] = i;    /* Nummer des Obergraphen speichern */
          j=0;
          while ((testgraphen[i]->obergraphen)[j]) { 
            /* Obergraphen von i markieren -> nicht mehr pruefen */
            pruefen[(testgraphen[i]->obergraphen)[j]] = False;
            j++;
          }
        }
      }
      else {  /* pruefen[i]==False => i ist markiert worden => i ist Ober-
                 graph eines Obergraphen => Obergraphen von i auch markieren */
        j=0;
        while ((testgraphen[i]->obergraphen)[j]) { 
          /* Obergraphen von i markieren -> nicht mehr pruefen */
          pruefen[(testgraphen[i]->obergraphen)[j]] = False;
          j++;
        }
      }     
    }
    numlist[pos++] = 0;   /* Listenende */
    testgraphen[n]->obergraphen = (unsigned short *)
      hole_festspeicher(sizeof(unsigned short)*(size_t)pos);
    memcpy(testgraphen[n]->obergraphen,numlist,
      sizeof(unsigned short)*(size_t)pos);    /* Obergraphenliste kopieren */
    n++;   /* naechster Testgraph */
  }
}   

/****************ERMITTLE_UNTERGRAPHEN***************************************/
/*  Zu jedem Testgraphen in der Testgraphenliste werden die maximalen
    Untergraphen ermittelt. Die Funktion arbeitet analog zu der Funktion
    "ermittle_obergraphen". Sie setzt voraus, dass die Funktion "ermittle_
    obergraphen" bereits aufgerufen worden ist, denn um festzustellen, ob
    ein Graph H ein Untergraph von G ist, wird nicht die Funktion "subgraph"
    aufgerufen, sondern einfach in der Liste des Graphen H nachgeschaut, ob
    G ein minimaler Obergraph von H ist. Auf die Weise spart man sich das
    Array "pruefen", denn die Information ueber die Maximalitaet wird gleich
    mitgeliefert. */

void ermittle_untergraphen(void) {
  static unsigned short *numlist;  /* Speicher fuer die Liste von Graphen */
  static unsigned short pos;       /* Position in Liste */
  static unsigned short n;      /* gerade betrachteter Graph */
  static unsigned short i;      /* gerade betrachteter Untergraphenkandidat */
  static unsigned short j;
  static BOOL untergraph;
  numlist = (unsigned short *)
            hole_speicher(sizeof(unsigned short)*(size_t)anz_testgraphen+1);
    /* Speicher wird nur hier gebraucht => kein Festspeicher notwendig */
  n = anz_testgraphen;
  while (n>0) {        /* Graphen durchgehen */
    pos = 0;
    for (i=testgraphkanten[testgraphen[n]->kanten].end+1;
         i<=anz_testgraphen; i++) {
      /* Obergraphenliste von i durchlaufen */
      j=0;  untergraph=False;
      while (!untergraph && (testgraphen[i]->obergraphen)[j]) {
        if ((testgraphen[i]->obergraphen)[j]==n) {untergraph=True;}
        j++;
      }
      if (untergraph) {numlist[pos++] = i;}    /* Nummer speichern */
    }
    numlist[pos++] = 0;   /* Listenende */
    testgraphen[n]->untergraphen = (unsigned short *)
      hole_festspeicher(sizeof(unsigned short)*(size_t)pos);
    memcpy(testgraphen[n]->untergraphen,numlist,
      sizeof(unsigned short)*(size_t)pos);    /* Untergraphenliste kopieren */
    n--;   /* naechster Testgraph */
  }
}   

/****************WERTE_TESTGRAPHEN_AUS**************************************/
/*  Diese Funktion liest von jedem Testgraphen die Ramseyzahl aus, erstellt
    eine Statistik und schreibt den Testgraphen in die zugehoerige Datei,
    falls "ausgeben==True".                                                */
/*  g darf benutzt werden, weil der alte Inhalt nicht mehr gebraucht wird. */
/*  Auch der Wert von "firsttestgraph" wird anschliessend nicht mehr
    gebraucht (auch nicht beim Speichern des Dump-Files, das ja dann leer
    ist).                                                                  */
/*  Wenn diese Funktion aufgerufen wird, ist "ramsey==True". Die Ausdruecke
    innerhalb der Funktion koennten also noch vereinfacht werden.          */

void werte_testgraphen_aus(BOOL ausgeben) {
  KNOTEN i,n;
  
  for (i=0; i<=nn+1; i++) {testgraphenzahl[i]=0;}
  while (firsttestgraph) {          /* Liste durchgehen */
    i = firsttestgraph->ramsey+1;   /* Ramseyzahl bzw. Mindestramseyzahl */
    testgraphenzahl[i]++;
    if (outputlevel<=i && ausgeben)
      {graph_to_nauty((firsttestgraph->g).g,(firsttestgraph->g).adj,g,&n);
       gib_graphen_aus(g,n,i);}
    firsttestgraph = firsttestgraph->next;
  }
}    

/**************************MAIN*********************************************/

int main(int argc,char *argv[]) {

int i,k=0;                               /* k = gewuenschte Knotenzahl */
char buildstring[filenamenlaenge];       /* Platz fuer Teile des Filenamens */
char filenamenoptionen[filenamenlaenge]; /* zweiter Teil des Filenamens */
char *inputfilename = nil;               /* Zeiger auf Inputfilename */
char *ramseyfilename = nil;              /* Zeiger auf Ramseyfilename */
FILE *ramseyfile = nil;                  /* Ramseyfile, falls gewuenscht */
FILE *logfile;                           /* Logfile (wird immer erzeugt) */
char inputerg;
unsigned long inputnumber;     /* Gelesene Graphen aus dem Inputfile */
KNOTEN n;                     /* nur fuer Graphen aus dem Inputfile */
unsigned char ramsey_kn = 0; /* !=0 => Option mit diesem Parameter benutzen */
unsigned char ramsey_kn_e = 0; 
BOOL r_min = False, r_max = False;   /* True => Benutzer hat die zugehoerige
                                        Grenze selbst bestimmt */

if (MAXN2>WORDSIZE) {
  fprintf(stderr,"Error:  Value of MAXN2 (%d) is larger than WORDSIZE (%d)!\n",
          MAXN2,WORDSIZE);   exit(0);
}

if (argc<2) {
  fprintf(stderr,"%s usage:  %s [n x] [brandt] [mod x] [class x] [level x]\n"
          " [outputlevel x] [file filename] [local_density x y]\n" 
          " (x and y are numbers) [ramsey_kn x] [ramsey_kn-e x]\n" 
          " [ramsey filename [min [max]]] [write_ramseygraph]\n"
          " [write_ramseygraph_all [min]] [stdout] [no_recover]"
          " [no_save] [twin]\n",argv[0],argv[0]);
  exit(0);
}


/* Argumente auswerten: */
for (i=1; i<argc; i++) {
  switch (argv[i][0]) {
    case 'n':  {if (strcmp(argv[i],(char *)"n")==0) {
                  if (i+1<argc) {k=atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'n': missing parameter!\n"); 
                        exit(0);}
                }
                else if (strcmp(argv[i],(char *)"no_recover")==0) 
                   {recover = False;}
                else if (strcmp(argv[i],(char *)"no_save")==0)
		   {save = False;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'b':  {if (strcmp(argv[i],(char *)"brandt")==0) {brandt = True;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'e':  {if (strcmp(argv[i],(char *)"extinfo")==0) {extinfo = True;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'g':  {if (strcmp(argv[i],(char *)"grpsize")==0) {grpsizes = True;}
                else if (strcmp(argv[i],(char *)"groetzsch")==0)
		        {groetzsch = True;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'm':  {if (strcmp(argv[i],(char *)"mod")==0) {
                  if (i+1<argc) {mod = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'mod': missing parameter!\n");
                        exit(0);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'c':  {if (strcmp(argv[i],(char *)"class")==0) { 
                  if (i+1<argc) {rest = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'rest': missing parameter!\n");
                        exit(0);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'l':  {if (strcmp(argv[i],(char *)"level")==0) {
                  if (i+1<argc) {level = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'level': missing parameter!\n");
                        exit(0);}
                }
                else if (strcmp(argv[i],(char *)"local_density")==0) {
                  if (i+2<argc) {
		    local_density = True;   l_n = (KNOTEN)atoi(argv[++i]);
                    l_k = atoi(argv[++i]);
		  }
                  else {fprintf(stderr,"Option 'local_density': missing "
                        "parameter!\n");  exit(0);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'o':  {if (strcmp(argv[i],(char *)"outputlevel")==0) {
                  if (i+1<argc) {outputlevel = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'outputlevel': missing "
                        "parameter!\n");  exit(0);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'f':  {if (strcmp(argv[i],(char *)"file")==0) {
                  if (i+1<argc) {inputfilename = argv[++i];}
                  else {fprintf(stderr,"Option 'file': missing parameter!\n");
                        exit(0);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'r':  {if (strcmp(argv[i],(char *)"ramsey_kn")==0) {
                  if (i+1<argc) {ramsey_kn = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'ramsey_kn': missing "
                        "parameter!\n");  exit(0);}
                }
                else if (strcmp(argv[i],(char *)"ramsey_kn-e")==0) {
                  if (i+1<argc) {ramsey_kn_e = atoi(argv[++i]);}
                  else {fprintf(stderr,"Option 'ramsey_kn-e': missing "
                        "parameter!\n");  exit(0);}
                }
                else if (strcmp(argv[i],(char *)"ramsey")==0) {
                  if (i+1<argc) {ramseyfilename = argv[++i];}
                  else {fprintf(stderr,"Option 'ramsey': missing filename!\n");
                        exit(0);}
                  if (i+1<argc && argv[i+1][0]>='0' && argv[i+1][0]<='9') {
                    ramsey_min = atol(argv[++i]);   r_min = True;
                    if (i+1<argc && argv[i+1][0]>='0' && argv[i+1][0]<='9')
                      {ramsey_max = atol(argv[++i]);   r_max = True;}
                  }
                }
                else if (strcmp(argv[i],(char *)"reverse")==0) {revert=False;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 's':  {if (strcmp(argv[i],(char *)"stdout")==0) {standardout = True;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 'w':  {if (strcmp(argv[i],(char *)"write_ramseygraph")==0)
		   {ramseygraph_ausgabe = True;}
                else if (strcmp(argv[i],(char *)"write_ramseygraph_all")==0) {
                  ramseygraph_ausgabe_all = True;
                  if (i+1<argc && argv[i+1][0]>='0' && argv[i+1][0]<='9')
                     {ramseylevel_start = (KNOTEN)atol(argv[++i]);}
                }
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    case 't':  {if (strcmp(argv[i],(char *)"twin")==0) {twin = True;}
                else {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
                break;}
    default: {fprintf(stderr,"Unknown option %s!\n",argv[i]); exit(0);}
  }   /* switch */
}     /* for */

/* Knotenzahl ueberpruefen (ab hier nn==k): */
if (k<4) {fprintf(stderr,"Impossible or missing vertex number"
                         " (minimum 4). \n"); exit(0);}
if (k>MAXN2 || (ramsey && k==MAXN2)) 
    {fprintf(stderr,"Number of vertices too large for the"
             " data-types used. \n"); exit(0);}
nn=(KNOTEN)k;


/* Optionen "ramsey", "ramsey_kn" und "ramsey_kn-e" ueberpruefen: */
if (ramseyfilename) {         /* Option "ramsey" wird gewuenscht */
  if (ramsey_kn) {fprintf(stderr,"Error: Options 'ramsey' and 'ramsey_kn' "
                  "used simultaneously!\n"); exit(0);}
  if (ramsey_kn_e) {fprintf(stderr,"Error: Options 'ramsey' and 'ramsey_kn-e'"
                    " used simultaneously!\n"); exit(0);}
  if (strcmp(ramseyfilename,(char *)"stdin")==0) {ramseyfile = stdin;}
  else if ((ramseyfile = repeated_fopen(ramseyfilename,"r",1))==0) 
    {fprintf(stderr,"Error: Can't open file %s!\n",ramseyfilename); exit(0);}
  ramsey = True;
}
else {
  if (ramsey_kn && ramsey_kn_e) {fprintf(stderr,"Error: Options 'ramsey_kn'"
      " and 'ramsey_kn-e' used simultaneously!\n"); exit(0);}
  if (ramsey_kn && local_density) 
     {fprintf(stderr,"Error: Options 'ramsey_kn' and 'local_density' "
              "used simultaneously!\n"); exit(0);}
  if (ramsey_kn_e && local_density) 
     {fprintf(stderr,"Error: Options 'ramsey_kn-e' and 'local_density' "
              "used simultaneously!\n"); exit(0);}
  if (ramsey_kn) {local_density = True;  l_n = ramsey_kn;  l_k = 1;}
  if (ramsey_kn_e) {local_density = True;  l_n = ramsey_kn_e;  l_k = 2;}
}
if ((ramseygraph_ausgabe || ramseygraph_ausgabe_all) && !ramsey) 
   {fprintf(stderr,"Error: Using option 'write_ramseygraph' or "
            "'write_ramseygraph_all' without option 'ramsey'!\n");  exit(0);}
if (ramseygraph_ausgabe && ramseygraph_ausgabe_all) 
   {fprintf(stderr,"Error: Using options 'write_ramseygraph' and "
            "'write_ramseygraph_all' simultaneously!\n");  exit(0);}
if (ramseylevel_start>nn) {ramseylevel_start = nn;}


/* Optionen "local_density", "outputlevel", "mod", "rest" und "level" 
   ueberpruefen: */
if (outputlevel<4 || ramseygraph_ausgabe) {outputlevel=nn+(ramsey);}
/* bei Ramseyzahlen ist auch die naechsthoehere Knotenzahl interessant */

if (level!=-1) {
  if (mod==-1)  {fprintf(stderr,"Missing mod-parameter!\n"); exit(0);}
  if (rest==-1) {fprintf(stderr,"Missing class-parameter!\n"); exit(0);}
  if (level<4)  {fprintf(stderr,"Level parameter (%d) out of range!\n",level);
                 exit(0);}  /* falls level==3, so muesste schon innerhalb der
                               Funktion "main" entschieden werden, ob 
                               "konstruiere" aufgerufen werden darf */
}
if (rest!=-1) {
  if (rest<0)  {fprintf(stderr,"Invalid class-parameter!\n"); exit(0);}
  if (mod==-1) {fprintf(stderr,"Missing mod-parameter!\n"); exit(0);}
}
if (mod!=-1) {
  if (mod<1)    {fprintf(stderr,"Invalid mod-parameter!\n"); exit(0);}
  if (rest==-1) {fprintf(stderr,"Missing class-parameter!\n"); exit(0);}
  if (level==-1) {fprintf(stderr,"Missing level-parameter: "
                   "I will try level %d.\n",(level = k*2/3));}
}

if (l_k==0) {local_density = False;}     /* Test ueberflussig */
if (local_density) {
  if (l_n<2)
     {fprintf(stderr,"local_density x y: x too small!\n"); exit(0);}
  if ((double)l_k/(double)l_n <=  MENGENTEST_VERHAELTNIS)
       {nosimpeltest_gb = 1;}
  else {nosimpeltest_gb = 0;}
}


/* Filenamen-Kennung erstellen (Optionen beruecksichtigen): */
filenamenoptionen[0] = 0;     /* String loeschen */
if (inputfilename) {strcat(filenamenoptionen,(char *)"_IFILE_");
                    strcat(filenamenoptionen,inputfilename);}
if (twin) {strcat(filenamenoptionen,(char *)"_TWIN");}
if (groetzsch) {strcat(filenamenoptionen,(char *)"_GR");}
if (ramsey) {
  strcat(filenamenoptionen,(char *)"_RAMSEY_");
  strcat(filenamenoptionen,ramseyfilename);
  if (r_min) {sprintf(buildstring,"_F%u",ramsey_min);
              strcat(filenamenoptionen,buildstring);}
  if (r_max) {sprintf(buildstring,"_L%u",ramsey_max);
              strcat(filenamenoptionen,buildstring);}
  if (ramseygraph_ausgabe) {strcat(filenamenoptionen,(char *)"_WRG");}
  if (ramseygraph_ausgabe_all) {
    if (ramseylevel_start==nn) {sprintf(buildstring,(char *)"_WRGA");}
    else {sprintf(buildstring,(char *)"_WRGA%u",ramseylevel_start);}
    strcat(filenamenoptionen,buildstring);
  }
}
/* die drei folgenden Optionen schliessen sich gegenseitig aus: */
if (ramsey_kn)          {sprintf(buildstring,"_RAMSEY_K%d",ramsey_kn);
                         strcat(filenamenoptionen,buildstring);}
else if (ramsey_kn_e)   {sprintf(buildstring,"_RAMSEY_K%d-e",ramsey_kn_e);
                         strcat(filenamenoptionen,buildstring);}
else if (local_density) {sprintf(buildstring,"_LOCDEN_%d_%d",l_n,l_k);
                         strcat(filenamenoptionen,buildstring);}
if (mod!=-1)            {sprintf(buildstring,"_MOD%d.%d_L%d",rest,mod,level);
                         strcat(filenamenoptionen,buildstring);}
if (revert==False)      {strcat(filenamenoptionen,(char *)"_REV");}   
if (grpsizes)           {strcat(filenamenoptionen,(char *)"_GS");}


/* Logfilenamen erstellen: */
sprintf(logfilename,"MTF_N%d",nn);
strcat(logfilename,filenamenoptionen);
strcat(logfilename,brandt ? (char *)"_BR" : (char *)"_M2SO");
if (standardout) {strcat(logfilename,"_STDOUT");}
strcat(logfilename,(char *)".log");


/* Recoverfilenamen erstellen: */
sprintf(recoverfilename,"MTF_N%d",nn);
strcat(recoverfilename,filenamenoptionen);
strcat(recoverfilename,brandt ? (char *)"_BR" : (char *)"_M2SO");
if (standardout) {strcat(recoverfilename,"_STDOUT");}
strcat(recoverfilename,(char *)".dump");


/* Falls "ramsey"-Option gewaehlt: Testgraphen einlesen */
if (ramsey) {
  inputnumber = 1;   /* Nummer des zu lesenden Graphen */
  while (inputnumber<=ramsey_max && 
         (inputerg = read_multi_code2_s_old(ramseyfile,g,&n))) {
    /* g darf als Einlesevariable benutzt werden, da es noch nicht initiali-
       siert ist */
    switch (inputerg) {  
      case 1: {if (inputnumber>=ramsey_min) 
               {testgraph_in_liste(g,n,inputnumber);} 
               inputnumber++;
               break;}
      case 2: {fprintf(stderr,"Error while reading graph %ld!\n",inputnumber);
               schluss();}
      case 3: {fprintf(stderr,"Input graph %ld out of range!\n",inputnumber);
               schluss();}
      case 4: {fprintf(stderr,"Input graph %ld too big!\n",inputnumber);
               schluss();}
    }
  }  
  if (ramseyfile != stdin) {fclose(ramseyfile);}
  erstelle_testgraph_listenarray();
  erstelle_testgraph_kantenarray();
  ermittle_obergraphen();
  ermittle_untergraphen();
}
if (ramseygraph_ausgabe_all && anz_testgraphen>MAXFULLTEST) {
  fprintf(stderr,"Error: Too many test graphs (maximum %d)!\n",MAXFULLTEST);
  exit(0);
}
if (ramseygraph_ausgabe_all && anz_testgraphen>1 && standardout) {
  fprintf(stderr,"Error: Only one test graph allowed when option 'stdout' is "
                 "used!\n"); exit(0);
}
if (ramsey && anz_testgraphen==0) 
   {fprintf(stderr,"Error using 'ramsey': No test graphs!\n"); exit(0);}

init_groetzsch();
initialisiere();          /* alle globalen Variablen initialisieren */
                          /* VOR dem Oeffnen des Recover-Files, da dessen 
                             Inhalt einige Variablen veraendert */

/* Recoverfile (dump-File) oeffnen: */
if (recover) {lies_dumpfile();}

initialisiere2();       /* die globalen Variablen initialisieren, die vom
                           "outputlevel" oder der Testgraphenanzahl abhaengen
                           (der korrekte Outputlevel steht im Recover-File) */
  
/* Signal setzen, falls dump-Files erzeugt werden sollen: */
S_intervall = save ? Sicherungsintervall : 0;
if (save) setup();


/* Ausgabedateinamen bilden: */
if (ramseygraph_ausgabe_all) {     /* Bei Ausgabe aller Ramseygraphen:
                                      pro Testgraph ein File */
  for (i=0; i<(int)anz_testgraphen; i++) {    /* Testgraphen durchlaufen */
    sprintf(outputfilename[i],"MTF_N%d_T%d",nn,i+(int)ramsey_min);
    strcat(outputfilename[i],filenamenoptionen);
    strcat(outputfilename[i],brandt ? (char *)".br" : (char *)".m2so");
    if (!recover && !standardout) { 
      /* altes File loeschen */
      if ((outputfile[i] = repeated_fopen(outputfilename[i],"w",1))==nil) 
        {fprintf(stderr,"Can't open output file %s!\n",outputfilename[i]);
         exit(0);}
      fclose(outputfile[i]);
    }
    if (standardout) {sprintf(outputfilename[i],"stdout");}
       /* Name wird nur bei Fehlerbeschreibungen benutzt */
  }
}
else {     /* Bei Ausgabe der mtf-Graphen oder Testgraphen: pro Knotenzahl
              ein File */
  for (i=outputlevel; i<=nn+(ramsey); i++) {
    if (i==nn+1) {sprintf(outputfilename[i],"MTF_N+%d",i);}
    else         {sprintf(outputfilename[i],"MTF_N%d",i);} 
    strcat(outputfilename[i],filenamenoptionen);
    strcat(outputfilename[i],brandt ? (char *)".br" : (char *)".m2so");
    if (!recover && (!standardout || i!=nn+(ramsey))) { 
      /* altes File loeschen */
      if ((outputfile[i] = repeated_fopen(outputfilename[i],"w",1))==nil) 
        {fprintf(stderr,"Can't open output file %s!\n",outputfilename[i]);
         exit(0);}
      fclose(outputfile[i]);
    }
    if (standardout && i==nn+(ramsey)) {sprintf(outputfilename[i],"stdout");}
       /* Name wird nur bei Fehlerbeschreibungen benutzt */
  }
}   


/* Logfile oeffnen und Programmaufrufdaten ins Logfile schreiben: */
logfile = repeated_fopen(logfilename,recover ? "a" : "w",1);
if (logfile==nil) {fprintf(stderr,"Cannot open logfile %s!\n",logfilename); 
                   exit(0);}

fprintf(stderr,"\nProgram call:  ");
for (i=0; i<argc; i++) fprintf(stderr,"%s ",argv[i]);
fprintf(stderr,"\n\n");
fprintf(logfile,"Program call:  ");
for (i=0; i<argc; i++) {fprintf(logfile,"%s ",argv[i]);}
fprintf(logfile,"\n\n");


if (recover) {
  fprintf(logfile,"Recovered from file %s.\n\n",recoverfilename);
  fprintf(stderr,"Recovered from file %s.\n\n",recoverfilename);
}
else {
  fprintf(stderr,"Vertex number: %d\n",k);
  fprintf(logfile,"Vertex number: %d \n",k);
  if (level!=-1) {
    fprintf(stderr,"Breaking level: %d    Module: %d    "
                   "Class: %d\n",level,mod,rest);
    fprintf(logfile,"Breaking level: %d    Module: %d    "
                    "Class: %d\n",level,mod,rest);
  } 
}

if (ramsey) {
  fprintf(stderr,"Read %d test graphs from Ramsey file.\n",anz_testgraphen);
  fprintf(logfile,"Read %d test graphs from Ramsey file.\n",anz_testgraphen);
} 
fclose(logfile);


/* Konstruieren mit Einlesen der Daten: */
if (inputfilename) {
  if (strcmp(inputfilename,(char *)"stdin")==0) {inputfile = stdin;}
  else if ((inputfile = repeated_fopen(inputfilename,"r",1))==nil) 
    {fprintf(stderr,"Cannot open input file %s.\n",inputfilename); exit(0);}
  inputnumber = 0;
  while (inputerg = read_multi_code2_s_old(inputfile,g,&n)) {
    switch (inputerg) {  
      case 1: {inputnumber++;   
               if (n<=nn) {        /* Graph zu gross => nicht nehmen */
                 if (level>=0 && (int)n>level) {
                   count2++;
                   count2 = count2%mod;  
                   if (count2==rest) {akzeptiere(n,True);}
                 }
                 else {akzeptiere(n,True);}
               }
               break;}
      case 2: {fprintf(stderr,"Error while reading graph %ld!\n",
               inputnumber+1);  schluss();}
      case 3: {fprintf(stderr,"Input graph %ld out of range!\n",
               inputnumber+1);  schluss();}
      case 4: {fprintf(stderr,"Input graph %ld too big!\n",inputnumber+1);
               schluss();}
    }
  }  
}
/* Alles von Grund auf konstruieren: */
else {zweig[3]++;  zweig[4]=0;  konstruiere(3,True);}


/* Auswertung: */
times(&TMS);
savetime = TMS.tms_utime + prevtime;
if (ramsey) 
   {werte_testgraphen_aus(!ramseygraph_ausgabe && !ramseygraph_ausgabe_all);}  
   /* dabei werden Testgraphen in die Outputliste geschrieben,
      falls !ramseygraph_ausgabe und !ramseygraph_ausgabe_all */
if (ramseygraph_ausgabe && !ramseyfilefilled) {  /* leere Datei kennzeichnen */
  *(outputbufferstart[nn+1]+outputbufferused[nn+1]) = 1;
  outputbufferused[nn+1]++;
}


/* Restliche in der Outputliste verbliebene Graphen ausgeben: */
speichere_graphen_aus_outputliste_in_files();


if (save) {schreibe_dumpfile(True);}   /* das ist wichtig, damit man von dem
   vorangegangenen Dumpfile aus keinen Restart mehr vornehmen kann, der die
   Ausgabedateien beeinflusst */

 
logfile = repeated_fopen(logfilename,"a",5);
if (inputfilename) {
  fprintf(stderr,"Read %ld graphs from input file.\n",inputnumber);
  fprintf(logfile,"Read %ld graphs from input file.\n",inputnumber);
}
for (i=4; i<=nn; i++) {
  if (sizeof(unsigned long)==sizeof(unsigned long long)) {
    fprintf(stderr,"Vertex number: %2d    Number of graphs: %ld\n",
            i,graphenzahl[i]);
    fprintf(logfile,"Vertex number: %2d    Number of graphs: %ld\n",
            i,graphenzahl[i]);
  }
  else {
    fprintf(stderr,"Vertex number: %2d    Number of graphs: %lld\n",
            i,graphenzahl[i]);
    fprintf(logfile,"Vertex number: %2d    Number of graphs: %lld\n",
            i,graphenzahl[i]);
  }
}
if (ramsey) {
  for (i=0; i<=nn; i++) {
    if (testgraphenzahl[i]>0) {
      fprintf(stderr,"Number of test graphs with Ramsey number %2d:  %d\n",
              i,testgraphenzahl[i]);
      fprintf(logfile,"Number of test graphs with Ramsey number %2d:  %d\n",
              i,testgraphenzahl[i]);
    }
  }
  /* im folgenden kann auch die 0 als Ausgabe interessant sein: */
  fprintf(stderr,"Number of test graphs with Ramsey number at least %2d:"
          "  %d\n",nn+1,testgraphenzahl[nn+1]);
  fprintf(logfile,"Number of test graphs with Ramsey number at least %2d:"
          "  %d\n",nn+1,testgraphenzahl[nn+1]);
}
if (grpsizes) {
  gib_groupsizes_aus(stderr,20,nn,nn);
  gib_groupsizes_aus(logfile,0,4,nn);
}
fprintf(logfile,"Time: %.1f seconds \n",(double)savetime/time_factor);
if (extinfo) {
  if (sizeof(unsigned long)==sizeof(unsigned long long)) {
    fprintf(logfile,"Number of accepts without nauty: %ld\n",acceptcount);
    fprintf(logfile,"Spanning sets: %ld\n",spannende);
    fprintf(logfile,"Good spanning sets: %ld\n",gute);
    fprintf(logfile,"Unique sets (not trivially): %ld\n",eindeutig);
    fprintf(logfile,"Non-unique sets: %ld\n",nicht_eindeutig);
    fprintf(logfile,"Nauty-calls for sets: %ld\n",nautys);
  }
  else {
    fprintf(logfile,"Number of accepts without nauty: %lld\n",acceptcount);
    fprintf(logfile,"Spanning sets: %lld\n",spannende);
    fprintf(logfile,"Good spanning sets: %lld\n",gute);
    fprintf(logfile,"Unique sets (not trivially): %lld\n",eindeutig);
    fprintf(logfile,"Non-unique sets: %lld\n",nicht_eindeutig);
    fprintf(logfile,"Nauty-calls for sets: %lld\n",nautys);
  }
} 
if (sizeof(unsigned long long)<8) {
  fprintf(logfile,"\nWarning: Size of unsigned long long is less than 8.\n");
  fprintf(logfile,"Big numbers might have been divided by modulo"
          " operations!\n");
}
fprintf(logfile,"end of program\n");
fclose(logfile);
fprintf(stderr,"Time: %.1f seconds \n",(double)savetime/time_factor);
if (extinfo) {
  if (sizeof(unsigned long)==sizeof(unsigned long long)) {
    fprintf(stderr,"Number of accepts without nauty: %ld\n",acceptcount);
    fprintf(stderr,"Spanning sets: %ld\n",spannende);
    fprintf(stderr,"Good spanning sets: %ld\n",gute);
    fprintf(stderr,"Unique sets (not trivially): %ld\n",eindeutig);
    fprintf(stderr,"Non-unique sets: %ld\n",nicht_eindeutig);
    fprintf(stderr,"Nauty-calls for sets: %ld\n",nautys);
  }
  else {
    fprintf(stderr,"Number of accepts without nauty: %lld\n",acceptcount);
    fprintf(stderr,"Spanning sets: %lld\n",spannende);
    fprintf(stderr,"Good spanning sets: %lld\n",gute);
    fprintf(stderr,"Unique sets (not trivially): %lld\n",eindeutig);
    fprintf(stderr,"Non-unique sets: %lld\n",nicht_eindeutig);
    fprintf(stderr,"Nauty-calls for sets: %lld\n",nautys);
  }
}
if (sizeof(unsigned long long)<8) {
  fprintf(stderr,"\nWarning: Size of unsigned long long is less than 8.\n");
  fprintf(stderr,"Big numbers might have been divided by modulo"
          " operations!\n");
}
fprintf(stderr,"end of program\n");
schluss();
}








