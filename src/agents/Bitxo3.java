package agents;

// Exemple de Bitxo
public class Bitxo3 extends Agent
{
    static final boolean DEBUG = true;

    // per entendre millor els valors
    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;
    
    final int CERCA = 30;
    final int VELOCIDAD_LINEAL_POR_DEFECTO = 5;
    final int VELOCIDAD_ANGULAR_POR_DEFECTO = 5;
    final int DISTANCIA_VISORES_POR_DEFECTO = 300;

    private Estat estat;
    private int espera = 0;
    private int colisionesConsecutivas = 0;

    public Bitxo3(Agents pare) {
        super(pare, "Javi", "imatges/robotank3.gif");
    }

    @Override
    public void inicia()
    {
        setAngleVisors(5);
        setDistanciaVisors(DISTANCIA_VISORES_POR_DEFECTO);
        setVelocitatLineal(VELOCIDAD_LINEAL_POR_DEFECTO);
        setVelocitatAngular(VELOCIDAD_LINEAL_POR_DEFECTO);
        espera = 0;
    }

    @Override
    public void avaluaComportament(){
        
        if(espera > 0){
            espera--;
            return;
        }
        
        atura();
        evaluarEventos();
    }
    
    private void evaluarEventos(){
        estat = estatCombat();
        
        if(atascadisimo()){
            hyperespai();
        }
        else if(atascado()){
            enrere();
        }
        
        else if(enemigoDetectado())             perseguirEnemigo();
        else if(colisionOcurrida())             gira(45);
        else if(colisionConParedInminente())    evitarChoque();
        else if(colisionConBombaInminente())    evitarChoque();
        else                                    meta();
        
    }
    
    private void meta(){
        if (estat.objecteVisor[CENTRAL] == NAU) dispara();
        endavant();
    }
    
    
    /*
     * Event triggers 
     */
    
    private boolean enemigoDetectado(){
        return estat.veigAlgunEnemic;
    }
    
    private boolean colisionOcurrida(){
        if(estat.enCollisio){
            colisionesConsecutivas++;
            System.out.println("Colision!");
            return true;
        } else {
            colisionesConsecutivas = 0;
            return false;
        }
    }
    
    private boolean atascado(){
        boolean colision = colisionesConsecutivas >= 50;
        return colision;
    }
    
    private boolean atascadisimo(){
        boolean colision = colisionesConsecutivas >= 60;
        return colision;
    }
    
    private boolean colisionConParedInminente(){
        
        for (int i = 0; i < 3; i++) {
            if(
                    estat.estatVisor[i] &&
                    estat.objecteVisor[i] == PARET &&
                    estat.distanciaVisors[i] <= CERCA
                    ){
                return true;
            }
        }
        
        return false;
    }
    
    private boolean colisionConBombaInminente(){
        return false;
    }
    
    
    /*
     * Event handlers
     */
    private enum situacion { L, I, D, C, IC, ID, CD, ICD }
    private void evitarChoque(){
        boolean distanciaCerca[] = {false, false, false};
        situacion s = situacion.L;
        int derecha = -10, izquierda = 20;
        
        for (int i = 0; i < estat.distanciaVisors.length; i++) {
            if(estat.distanciaVisors[i] > CERCA) distanciaCerca[i] = true;
        }
        
        if(distanciaCerca[0] && distanciaCerca[1] && !distanciaCerca[2]) s = situacion.D;
        else if(distanciaCerca[0] && !distanciaCerca[1] && distanciaCerca[2]) s = situacion.C;
        else if(distanciaCerca[0] && !distanciaCerca[1] && !distanciaCerca[2]) s = situacion.CD;
        else if(!distanciaCerca[0] && distanciaCerca[1] && distanciaCerca[2]) s = situacion.I;
        else if(!distanciaCerca[0] && distanciaCerca[1] && !distanciaCerca[2]) s = situacion.ID;
        else if(!distanciaCerca[0] && !distanciaCerca[1] && distanciaCerca[2]) s = situacion.IC;
        else if(!distanciaCerca[0] && !distanciaCerca[1] && !distanciaCerca[2]) s = situacion.ICD;
        
        switch(s){
            case I:
                gira(derecha);
                break;
            case IC:
                gira(derecha);
                break;
            case D:
                gira(izquierda);
                break;
            case C:
                gira(izquierda);
                break;
            case CD:
                gira(izquierda);
                break;
            case ID:
                gira(izquierda*2);
            case ICD:
                gira(izquierda*2);
                break;
            case L:
                break;
        }
    }
    private void perseguirEnemigo(){
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
        
        if (estat.objecteVisor[CENTRAL] == NAU) dispara();
    }
}