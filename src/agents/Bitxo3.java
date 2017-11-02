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
    static final int VELOCIDAD_LINEAL_POR_DEFECTO = 4;
    static final int VELOCIDAD_ANGULAR_POR_DEFECTO = 5;
    static final int DISTANCIA_VISORES_POR_DEFECTO = 300;
    static final int OBSTACULO_CERCANO = 60;
    static final int RECURSO_CERCANO = 60;
    static final int MAX_DIST_ENEMIGO = 100;

    private Estat estat;
    private int espera = 0;
    private int colisionesConsecutivas = 0;
    private Bonificacio recursoMasCercano;
    private Punt        enemigoMasCercano;
    private int prev_impactesRebuts = 0;

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
        recursoMasCercano = null;
        prev_impactesRebuts = 0;
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
        
        if(atascado()){
            hyperespai();
        }
        else if(enCombate()){
            
            if(enemigoAtaca()){
                if(aPuntoDeMorir()){
                    hyperespai();
                    endavant();
                } else {
                    activarEscudo();
                    if(enemigoMasCercanoDetectado()){
                        if(hayBalas()){
                            atacarEnemigoMasCercano();
                        } else {
                            evitarDisparos();
                        }
                    } else {
                        evitarDisparos();
                    }
                }
            } else {
                if(enemigoMasCercanoDetectado() && hayBalas()){
                    atacarEnemigoMasCercano();
                    
                } else if(enemigoMasCercanoDetectado() && !hayBalas()) {
                    evitarDisparos();
                } else {
                    evitarDisparos();
                }
            }
                
        }
        else if(colisionOcurrida()){
       
            enrere();
            evitarChoque();
            espera = 7;
        }                          
        else if(colisionConParedInminente()){
        
            evitarChoque();
            endavant();
        }            
        else if(recursoCercanoDetectado(hayBalas())){
            mira(recursoMasCercano.posicio.x, recursoMasCercano.posicio.y);
            endavant();
        }
        else {
            endavant();
        }
        
        actualizarMemoria();
        
        
    }
    
    
    /*
     * Event triggers 
     */
    
    private void actualizarMemoria(){
        prev_impactesRebuts = estat.impactesRebuts;
    }
    
    private boolean hayBalas(){
            return estat.bales > 0 || estat.perforadores > 0;
    }
    
    private void evitarDisparos(){
        gira(15);
        enrere();
    }
    
    private void activarEscudo(){
        activaEscut();
    }
    
    private void atacarEnemigoMasCercano(){
        
        mira(enemigoMasCercano.x,enemigoMasCercano.y);
        if(estat.perforadores != 0) perforadora();
        else                         dispara();
        endavant();
    }
    
    private boolean enemigoMasCercanoDetectado(){

            double distanciaEnemigoMasCercano = Integer.MAX_VALUE;
            enemigoMasCercano = null;
            for (Punt posicionEnemigo : estat.posicioEnemic) {
                    if(posicionEnemigo != null){                    
                        if(posicionEnemigo.x != 0 && posicionEnemigo.y != 0){

                            double distEnemigo = estat.posicio.distancia(posicionEnemigo);
                            if(distEnemigo < distanciaEnemigoMasCercano){
                                distanciaEnemigoMasCercano = distEnemigo; 
                                enemigoMasCercano = posicionEnemigo;
                            }
                        }
                }
            }
                     
            return enemigoMasCercano != null;
    }
    
    private boolean disparoRecibido(){
 
        return estat.impactesRebuts > prev_impactesRebuts;
    }
    
    private boolean balaDetectada(){
        return estat.balaEnemigaDetectada;
    }
    
    private boolean enCombate(){
        return (balaDetectada() || disparoRecibido() || enemigoMasCercanoDetectado());
    }
    
    private boolean aPuntoDeMorir(){
        return estat.impactesRebuts > 3;
    }
    
    private boolean enemigoAtaca(){
        return (balaDetectada() || disparoRecibido());
    }
    
    
    
    
    private boolean colisionOcurrida(){
        if(estat.enCollisio){
            colisionesConsecutivas++;
//            System.out.println("Colision!");
            return true;
        } else {
            colisionesConsecutivas = 0;
            return false;
        }
    }
    
    private boolean atascado(){
        boolean colision = colisionesConsecutivas >= 6;
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
    
    private boolean recursoCercanoDetectado(boolean hayBalas){
        double distanciaRecursoMasCercano = 99999.0;
        recursoMasCercano = null;
            
        for (Bonificacio bonificacio : estat.bonificacions) {
            if (bonificacio.tipus == MINA) continue;
            
            if(!hayBalas && bonificacio.tipus != Agent.RECURSOS) continue;
            
            double distanciaRecursoActual = estat.posicio.distancia(bonificacio.posicio);
            
            if(
                    distanciaRecursoActual <= RECURSO_CERCANO &&
                    distanciaRecursoActual < distanciaRecursoMasCercano
            ){
                distanciaRecursoMasCercano = distanciaRecursoActual; 
                recursoMasCercano = bonificacio;
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
            if(estat.distanciaVisors[i] <= OBSTACULO_CERCANO) distanciaCerca[i] = true;
        }
        
        if     (distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.ICD;     // 111
        else if(distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.IC;     // 110
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.ID;     // 101
        else if(distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.I;    // 100
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.CD;     // 011
        else if(!distanciaCerca[ESQUERRA] && distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.C;    // 010
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && distanciaCerca[DRETA]) s = situacion.D;    // 001
        else if(!distanciaCerca[ESQUERRA] && !distanciaCerca[CENTRAL] && !distanciaCerca[DRETA]) s = situacion.L;   // 000
        
        
        switch(s){
            case I:
//                System.out.println("Gira derecha");
                gira(derecha);
                break;
            case IC:
//                System.out.println("Gira derecha");
                gira(derecha);
                break; 
            case D:
//                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case C:
//                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case CD:
//                System.out.println("Gira izquierda");
                gira(izquierda);
                break;
            case ID:
//                System.out.println("Gira izquierda");
                gira(izquierda*2);
            case ICD:
//                System.out.println("Gira izquierda");
                gira(izquierda*2);
                break;
            case L:
                gira(izquierda);
                break;
        }
    }
    
    
    /**
     * Funciones auxiliares
     */
   
}