package agents;

// Exemple de Bitxo
public class Bitxo4 extends Agent
{
    static final boolean DEBUG = false;

    // per entendre millor els valors
    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;

    
    private Estat estat;
    private int espera = 0;

    public Bitxo4(Agents pare) {
        super(pare, "Bitxo4", "imatges/robotank2.gif");
    }

    @Override
    public void inicia()
    {
        setAngleVisors(30);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(7);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
    {
        if(true)return;
        boolean enemic;

        enemic = false;

        estat = estatCombat();  // Recuperam la informació actualitzada de l'entorn
        
        // Si volem repetir una determinada acció durant varies interaccions
        // ho hem de gestionar amb una variable (per exemple "espera") que faci
        // l'acció que volem durant el temps que necessitem
        
        if (espera > 0) {  // no facis res, continua amb el que estaves fent
            espera--;
            return;
        }
        else
        {
            atura();  // ens asseguram de que esteim aturats, preparats pel nou moviment
            
            // si veig un enemic en el visor central, dispara
            if (estat.objecteVisor[CENTRAL] == NAU) dispara();
            
            // també podríem assegurar-nos de que no està massa enfora:
            // if (estat.objecteVisor[CENTRAL] == NAU && estat.distanciaVisor[CENTRAL] < minim) dispara();
         
            if (estat.enCollisio) // situació de nau bloquejada, fer accions per desbloquejar
            {
                // Ves enrera varies vegades i gira un poc
                gira(5); // 30 graus
                enrere();
                espera = 3;  // gira i enrere 5 vegades
            } else {
                endavant();  // si tens el camí lliure, cap endavant
                
                // Exemple comportament 1: si tenc una paret molt propera, volta ja !
                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] <10 && estat.distanciaVisors[DRETA] >40)
                {
                    atura(); // no vull anar cap envant perquè no podria voltar
                    dreta();
                    return;
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] <10 && estat.distanciaVisors[ESQUERRA] >40)
                {
                    atura(); // no vull anar cap envant perquè no podria voltar
                    esquerra();
                    return;
                }
                
                // Exemple comportament 2: si veig algun enemic, el perseguiré
                if (estat.veigAlgunEnemic)  
                {
                    // cercar quin és l'enemic que veig més proper:
                    int mesProper = -1;
                    
        
                    double distanciaMesProper = 9999;  
                    double distancia;
                    
                    for (int n = 0; n < estat.numBitxos; n++)
                    {
                        if (n != estat.id) // jo no compt !
                        {
                            if (estat.veigEnemic[n]) // el veig
                            {
                                distancia = estat.posicio.distancia(estat.posicioEnemic[n]);  // calcul a quina distància es troba

                                if (distancia < distanciaMesProper)  // n'he trobat un de més proper
                                {
                                    mesProper = n;
                                    distanciaMesProper = distancia;
                                }
                            }
                        }
                    }
                        
                    int sector = estat.sector[mesProper];
                    
                    if (sector == 2 || sector == 3)  // ben visible, puc saber la seva posició
                    {
                        mira(estat.posicioEnemic[mesProper].x, estat.posicioEnemic[mesProper].y);
                    }
                    else if (sector == 1)   // gira per situar el bitxo dins els sectors 2 o 3
                    {
                        dreta();
                    }
                    else
                    {
                        esquerra();
                    }
                }
                

                
                // Exemple comportament 3: evitar les parets
                // Miram els visors per detectar els obstacles
                // Utilitzaré una variable sensor on tendré un número binari amb l'estat dels sensors
                int sensor = 0;

                if (estat.objecteVisor[ESQUERRA] == PARET && estat.distanciaVisors[ESQUERRA] < 40) {
                    sensor += 1;  // primer bit per l'esquerra
                }
                if (estat.objecteVisor[CENTRAL] == PARET && estat.distanciaVisors[CENTRAL] < 40) {
                    sensor += 2;  // segon bit pel visor central
                }
                if (estat.objecteVisor[DRETA] == PARET && estat.distanciaVisors[DRETA] < 40) {
                    sensor += 4;  // tercer bit per la dreta
                }
                
                switch (sensor) {  // on veig la pared ?
                    case 0:  // no hi ha paret, anar recta
                    case 5:  // centre lliure
                        endavant();
                        break;
                    case 1:
                    case 3:  // hi ha paret a l'esquerra, gira a la dreta
                        dreta();
                        break;
                    case 4:
                    case 6:  // hi ha paret a la dreta, gira a l'esquerra
                        esquerra();
                        break;

                    case 2:  
                    case 7:  // paret devant
                        double distancia;
                        distancia = estat.distanciaVisors[CENTRAL];

                        if (distancia < 25) {
                            espera = 5;  // si està molt aprop ves enrere un ratet;
                            enrere();
                        } else // gira a la dreta
                            dreta();
                        break;
                }

            }

        }
    }
    
    private int minim(double a, double b)
    {
        if (a <= b) return (int) a;
        else return (int) b;
    }
    
    private int minimaDistancia()   // Calcula la distància mínima dels visors
    {
        return  minim(estat.distanciaVisors[ESQUERRA], minim(estat.distanciaVisors[CENTRAL], estat.distanciaVisors[DRETA]));
    }
}