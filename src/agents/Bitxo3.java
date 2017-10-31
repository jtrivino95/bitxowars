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
    
    final int VELOCIDAD_LINEAL_POR_DEFECTO = 5;
    final int VELOCIDAD_ANGULAR_POR_DEFECTO = 5;
    final int DISTANCIA_VISORES_POR_DEFECTO = 300;
    final int OBSTACULO_CERCANO = 60;
    final int RECURSO_CERCANO = 100;

    private Estat estat;
    private int espera = 0;
    private int colisionesConsecutivas = 0;
    private Bonificacio recursoMasCercano;
    private boolean vaAMirarRecurso = false;

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
        colisionesConsecutivas = 0;
        vaAMirarRecurso = false;
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
        
        //else if(enemigoDetectado())             perseguirEnemigo();
        if(atascado()){
            hyperespai();
        }
        else if(colisionOcurrida()){
            enrere();
            evitarChoque();
            espera = 5;
        }                          
        else if(colisionConParedInminente()){
            evitarChoque();
            endavant();
        }            
        else if(recursoCercanoDetectado()){
            mirarRecurso();
            endavant();
        }
        else {
            endavant();
        }
        
    }
    
    private void mirarRecurso(){
        mira(recursoMasCercano.posicio.x, recursoMasCercano.posicio.y);
        vaAMirarRecurso = true;
    }
    
        private void conseguirRecurso(){
        mira(recursoMasCercano.posicio.x, recursoMasCercano.posicio.y);
        vaAMirarRecurso = true;
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
        boolean colision = colisionesConsecutivas >= 10;
        return colision;
    }
    
    private boolean colisionConParedInminente(){
        
        for (int i = 0; i < 3; i++) {
            if(
                    estat.estatVisor[i] &&
                    //estat.objecteVisor[i] == PARET &&
                    estat.distanciaVisors[i] <= OBSTACULO_CERCANO
                    ){
                return true;
            }
        }
        
        return false;
    }
    
    private boolean colisionConBombaInminente(){
        return false;
    }
    
    private boolean visorCentralDetectaPared(){
        return estat.objecteVisor[CENTRAL] == PARET;
    }
    
    private boolean recursoCercanoDetectado(){
        double distanciaRecursoMasCercano = 99999.0;
        recursoMasCercano = null;
            
        for (Bonificacio bonificacion : estat.bonificacions) {
            if (bonificacion.tipus == MINA) continue;
            
            // Calculamos hipotenusa
            int distX = Math.abs(estat.posicio.x - bonificacion.posicio.x);
            int distY = Math.abs(estat.posicio.y - bonificacion.posicio.y);
            double distanciaRecursoActual = Math.sqrt(distX*distX + distY*distY);
            
            if(
                    distanciaRecursoActual <= RECURSO_CERCANO &&
                    distanciaRecursoActual < distanciaRecursoMasCercano
            ){
                distanciaRecursoMasCercano = distanciaRecursoActual; 
                recursoMasCercano = bonificacion;
            }
        }
        
        return recursoMasCercano != null;
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
            if(estat.distanciaVisors[i] > OBSTACULO_CERCANO) distanciaCerca[i] = true;
        }
        
        if     (distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.ICD;     // 111
        else if(distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.IC;     // 110
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.ID;     // 101
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.I;    // 100
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.CD;     // 011
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.C;    // 010
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.D;    // 001
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.L;   // 000
        
        
        switch(s){
            case I:
                System.out.println("Gira derecha");
                gira(derecha);
                break;
            case IC:
                System.out.println("Gira derecha");
                gira(derecha);
                break; 
            case D:
                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case C:
                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case CD:
                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case ID:
                System.out.println("Gira izquierda");
                gira(izquierda*2);
            case ICD:
                System.out.println("Gira izquierda");
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
    
    /**
     * Funciones auxiliares
     */
   
}