/**
*
AGENT

	MOVE();


	IF(DANGER ) { // BULLET DETECTED OR ATTACKING US FROM BLIND SPOT
		
		IF(LT 2 ENEMY LEFT){

			IF(ENEMY HAS MORE LIFE){
				KEEP SAFE();
			}ELSE{
				KILL();
			}

		}ELSE {
				KEEP SAFE();
		}
		

	}ELSE { // WE’RE SAFE
		
		
		IF(ENEMY DETECTED){ //BLIND SPOT
				KILL();
		}ELSE{
				TAKE RESOURCES ();
		}
	} 
*/
package agents;

// Exemple de Bitxo
public class Bitxo1 extends Agent
{
    static final boolean DEBUG = false;

    
    static final int PARET = 0;
    static final int NAU   = 1;
    static final int RES   = -1;

    static final int ESQUERRA = 0;
    static final int CENTRAL  = 1;
    static final int DRETA    = 2;

    private Estat estat;
    private int espera = 0;
    
    private int prev_impactesRebuts = -1;
    
    /*************************************************
     *      ESQUIVAR PAREDES
     ************************************************/
    
    private double d_esq,d_cen,d_dre;
    private int visor_menor,visor_medio,visor_mayor;
    private int angulo;
    
    private final int BITXO_LADO = 25; 
    private final int BITXO_ESPACIO = 35; 
    private final int MIN_DIST_ESQ = BITXO_LADO + BITXO_ESPACIO;
    private final int MAX_ANGLE_GIR = 30;
    private final int MIN_ANGLE_GIR = 1;
    private final int MAX_VECES_COLISION = 5;  
    private final int MAX_ANGLE_VISOR = 20;
    private final int MIN_ANGLE_VISOR = 10;
    
    /*************************************************
     *      OBTENER RECURSOS
     ************************************************/
    
    private int recurso_x = -1 , recurso_y = -1;
    private boolean mirandoRecurso = false;
    
    private final int MAX_DIST_REC = 60;
    
    /*************************************************
     *      ATACAR
     ************************************************/
    
    private boolean mirandoEnemigo = false;
    private int enemigo_x = -1 , enemigo_y = -1;
    
    private final int MAX_DIST_ENEMIGO = 800;

    public Bitxo1(Agents pare) {
        super(pare, "Bitxo1Grup20", "imatges/robotank1.gif");
    }

    @Override
    public void inicia()
    {
        
        setAngleVisors(MAX_ANGLE_VISOR);
        setDistanciaVisors(350);
        setVelocitatLineal(5);
        setVelocitatAngular(9);
        espera = 0;
    }

    @Override
    public void avaluaComportament()
            
    {
       
        if(espera > 0){
            espera--;
            return;
        }
        atura();
       
        estat = estatCombat();
        
        //this.activaEscut();
        
        if(prev_impactesRebuts != estat.impactesRebuts && prev_impactesRebuts!=3) {
//            if(estat.hyperespaiDisponibles > 0){
//                this.hyperespai();
//            }else{
//                this.activaEscut();
//            }
            
             this.activaEscut();
        }
        
        if(mirandoEnemigo  && estat.objecteVisor[CENTRAL] == NAU && estat.distanciaVisors[CENTRAL] < estat.distanciaVisor ){
                if(estat.perforadores > 0){
                    this.perforadora();
                }else{
                    this.dispara();
                }
        }
        
                    
        esquivarParedes();
        
        
      
        if(!paredCerca() && !inColissionFrontal() && !mirandoEnemigo){
          
            if (mirandoRecurso ){
                endavant();
            }          
            if(recursoEncontrado()){
                mira(recurso_x,recurso_y);
                mirandoRecurso = true;
            }else{
                mirandoRecurso = false;
            }     
        }
             
        
        
        
        if(enemigoEncontrado()){
                mira(enemigo_x,enemigo_y);
                System.out.println(enemigo_x + " , "+enemigo_y);
                mirandoEnemigo = true;
        }else{
                mirandoEnemigo = false;
        }
        
        
               
            
            prev_impactesRebuts = estat.impactesRebuts;
    }

    
    /***************************************************
     *          ESQUIVAR PAREDES
     * ************************************************/
    
    private void esquivarParedes(){
        
        
        ajustarVisores();       
        if(paredCerca()){
            if(inColissionFrontal()){
                System.out.println(" colision !");
                manejarColision();
            }else{
                esquivar();
                endavant();
            }
        }else{
            evitarColision();
            endavant();
        }
        
  }
    
    private boolean paredCerca(){
    
        obtenerDistVisores();
        
        
        ordenarVisoresDist();
       
 
        boolean paredCerca = false;
        // Calcula cuántos grados tiene que girar
        if(estat.distanciaVisors[visor_menor] < MIN_DIST_ESQ){  
            try {
                  calcularAnguloGiro();
                  paredCerca = true;
            } catch(Exception e){
                
            }                   
        }
        
        return paredCerca;
    }
    
    private void obtenerDistVisores(){
        
        d_esq = estat.distanciaVisors[ESQUERRA];
        d_cen = estat.distanciaVisors[CENTRAL];
        d_dre = estat.distanciaVisors[DRETA];
    }
    
    private void ordenarVisoresDist(){
    
        visor_mayor = ESQUERRA;
        visor_menor = ESQUERRA;
        visor_medio = ESQUERRA + CENTRAL + DRETA;
        for (int i = 1; i < estat.distanciaVisors.length; i++) {
            if(estat.distanciaVisors[i] > estat.distanciaVisors[visor_mayor]) {
                visor_mayor = i;
            }
            
            if(estat.distanciaVisors[i] < estat.distanciaVisors[visor_menor]){
                visor_menor = i;
            }
            
        }
        
        visor_medio = visor_medio - visor_mayor - visor_menor;
    }
    
    private void calcularAnguloGiro(){
        double factorB = Math.abs((estat.distanciaVisors[visor_menor]*estat.distanciaVisors[visor_menor] + (estat.distanciaVisors[visor_medio] + estat.distanciaVisors[visor_mayor])/2)/(MIN_DIST_ESQ*MIN_DIST_ESQ + (estat.distanciaVisors[visor_medio]+estat.distanciaVisors[visor_mayor])/2));
        angulo = MIN_ANGLE_GIR + (int)((MAX_ANGLE_GIR-MIN_ANGLE_GIR)*(1-factorB));
    }
    
    private void ajustarVisores(){
    
            if(estat.angleVisors == this.MAX_ANGLE_VISOR){
                this.setAngleVisors(this.MIN_ANGLE_VISOR);
            }else{
                this.setAngleVisors(this.MAX_ANGLE_VISOR);
            }

    }
    
    private boolean inColissionFrontal(){
    
        if(estat.angleVisors == MIN_ANGLE_VISOR){
         boolean inColissionFrontal = false;
          for (int i = 0; i < estat.distanciaVisors.length; i++) {
               if(!inColissionFrontal && estat.distanciaVisors[i] <= 10){
                   inColissionFrontal = true;
               }
          }
          return inColissionFrontal;
        }
        return false;
               
           
    }
    
    
    private void manejarColision(){
      
               enrere();
               obtenerDistVisores();
               ordenarVisoresDist();
               calcularAnguloGiro();
               double deltaDist_EC = Math.abs(d_esq-d_cen);
               double deltaDist_DC = Math.abs(d_dre-d_cen);
               
               if(deltaDist_EC < deltaDist_DC){ 
                        gira(angulo); 
                 }else{
                        gira(-angulo);
                 } 
               espera = MAX_VECES_COLISION;

    }
    
    private void evitarColision(){
      if(estat.objecteVisor[ESQUERRA] == PARET && this.estat.angleVisors > MIN_ANGLE_VISOR
              && estat.distanciaVisors[ESQUERRA] > estat.distanciaVisors[DRETA]){
                gira(-5);
                
      }else  if(estat.objecteVisor[DRETA] == PARET && this.estat.angleVisors > MIN_ANGLE_VISOR){
                gira(5);
                
      }       
    }
    
    private void esquivar(){
      
    
      if(paredCerca()){
            double deltaDist_EC = Math.abs(d_esq-d_cen);
            double deltaDist_DC = Math.abs(d_dre-d_cen);
            
            switch (visor_mayor) {
                case CENTRAL:
                    
                 if(Math.abs(deltaDist_EC - deltaDist_DC) <20){
                     angulo = (int)Math.log(angulo);
                 }
                 if(deltaDist_EC < deltaDist_DC){ 
                        gira(angulo); 
                 }else{
                        gira(-angulo);
                 } 
                 break;               
                case ESQUERRA:
                    gira(angulo);
                    break;
                default:
                    gira(-angulo);
                    break;
            }           
        }
    }
    
   /***************************************************
     *         END  ESQUIVAR PAREDES
     * ************************************************/
  
  
  
  
  
  
  
  
  
  
  
  
   /***************************************************
     *          OBTENER RECURSOS
     * ************************************************/
    
    private boolean recursoEncontrado(){
        
            int tipus , bx ,by ,mx ,my ;
            int min_x = -1,min_y = -1;
            int min_distX = (int)Math.sqrt(Integer.MAX_VALUE);
            int min_distY = (int)Math.sqrt(Integer.MAX_VALUE);
            //System.out.println(min_distX + " , "+min_distY);
            boolean encontrado = false;
            for (Bonificacio bonificacion : estat.bonificacions) {
                // bonificación
                bx = bonificacion.posicio.x;
                by = bonificacion.posicio.y;
                tipus = bonificacion.tipus;
                // mi bitxo
                mx = estat.posicio.x;
                my = estat.posicio.y;
                //si no es mina calcular distancia y actualizar min_dist
                if(tipus != Agent.MINA){
                    int distX = Math.abs(mx-bx);
                    int distY = Math.abs(my-by);
                    double distRecurso = distX*distX+distY*distY;
                    distRecurso = Math.sqrt(distRecurso);
                    if(distX*distY < min_distX*min_distY && distRecurso < MAX_DIST_REC){
                        encontrado = true;
                        min_distX = distX;
                        min_distY = distY;
                        min_x = bx;
                        min_y = by;
                    }
                }
            }
            recurso_x = min_x;
            recurso_y = min_y;
            return encontrado;
    }
    
    
    
    
    
    
     /***************************************************
     *         END Obtener Recursos
     * ************************************************/
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
     /***************************************************
     *          MATAR ENEMIGO
     * ************************************************/
    
    private boolean enemigoEncontrado(){
        if(estat.veigAlgunEnemic){

            int bx ,by ,mx ,my ;
            int min_x = -1,min_y = -1;
            int min_distX = (int)Math.sqrt(Integer.MAX_VALUE);
            int min_distY = (int)Math.sqrt(Integer.MAX_VALUE);
             boolean enemigoEncontrado = false;
            
                // enemigo
                for (Punt posicioEnemic : estat.posicioEnemic) {
                    if(posicioEnemic != null){
                   
                    bx = posicioEnemic.x;
                    by = posicioEnemic.y;
                    
                    if(bx != 0 && by != 0){
                
                        // mi bitxo
                        mx = estat.posicio.x;
                        my = estat.posicio.y;

                        int distX = Math.abs(mx-bx);
                        int distY = Math.abs(my-by);
                        double distEnemigo = distX*distX+distY*distY;
                        distEnemigo = Math.sqrt(distEnemigo);
                        if(distX*distY < min_distX*min_distY && distEnemigo < MAX_DIST_ENEMIGO){
                            enemigoEncontrado = true;
                            min_distX = distX;
                            min_distY = distY;
                            min_x = bx;
                            min_y = by;
                        }
                    }
                }
            }
                enemigo_x = min_x;
                enemigo_y = min_y;
                return enemigoEncontrado;
        }
        return false;
    }
}
