package main;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


class Player {

    public Player(){}

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        List<Site> sites = new ArrayList<>();
        int numSites = in.nextInt();
        for (int i = 0; i < numSites; i++) {
            int siteId = in.nextInt();
            int x = in.nextInt();
            int y = in.nextInt();
            int radius = in.nextInt();
            sites.add(new Site(siteId, x, y, radius));
        }

        // game loop
        while (true) {
            int gold = in.nextInt();
            int touchedSite = in.nextInt(); // -1 if none

            List<Structure> structures = new ArrayList<>();
            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                int goldInMine = in.nextInt(); // gold in mine
                int maxMineSize = in.nextInt(); // max mine size
                int structureType = in.nextInt(); // -1 = No structure, 0 = Mine, 1 = Tower, 2 = Barracks
                int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
                int param1 = in.nextInt();
                int param2 = in.nextInt();
                structures.add(new Structure(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
            }

            List<Unit> units = new ArrayList<>();
            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER, 2 = GIANT
                int health = in.nextInt();
                units.add(new Unit(x, y, owner, unitType, health));
            }

            Manager manager = new Manager(gold, touchedSite, sites, structures, units);
            manager.manage();

        }
    }
}

class Manager {

    int gold;
    int touchedSite;
    List<Site> sites;
    List<Structure> structures;
    List<Unit> units;

    public Manager( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.sites = sites;
        this.structures = structures;
        this.units = units;
    }

    public void manage(){
        makeQueenDecision();
        makeTrainingDecision();
    }

    public void makeQueenDecision(){
        QueenDecisionMaker qdm = new QueenDecisionMaker(this.gold, this.touchedSite, this.sites, this.structures, this.units );
        qdm.decide();
    }

    public void makeTrainingDecision(){

        TrainingDecisionMaker tdm = new TrainingDecisionMaker(this.gold, this.touchedSite, this.sites, this.structures, this.units );
        tdm.decide();

    }

}

interface DecisionMaker {

    void decide();
}

class QueenDecisionMaker implements DecisionMaker {

    protected int touchedSite;


    Query query;

    public QueenDecisionMaker( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.touchedSite = touchedSite;
        this.query = new Query(structures, units, sites);
    }

    public void decide(){
        if( canBuildStructure() ){
            buildStructure();
        } else {
            System.out.println(move());
        }
    }

    protected boolean canBuildStructure(){

        // log("canBuildStructure");
        return this.touchedSite != Constants.NOT_TOUCHING
                && siteIsEmpty(this.touchedSite);
    }

    protected boolean siteIsEmpty( int siteId){
        // log("siteIsEmpty " + siteId);
        return query.getStructureBySiteId(siteId).structureType == Constants.EMPTY_SITE;
    }

    protected void buildStructure(){
        // log("buildStructure");
        System.out.println("BUILD " + this.touchedSite + " " + getNextStructureType());
    }

    protected String getNextStructureType(){

        int archers = query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).size();
        int knights = query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).size();
        int giants = query.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure).size();
        int towers = query.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure).size();
        int mines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure).size();
        int eMines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.enemyStructure).size();

        if( mines == 0){
            return "MINE";
        }
        if( archers == 0){
            return "BARRACKS-ARCHER";
        }
        if( towers == 0){
            return "TOWER";
        }
        if( knights == 0){
            return "BARRACKS-KNIGHT";
        }
        if(giants == 0){
            return "BARRACKS-GIANT";
        }

        if( mines < eMines || mines < 4 ){
            return "MINE";
        } else if( query.isLessOrEqual(archers, knights, giants, towers, mines)) {
            return "BARRACKS-ARCHER";
        } else if( query.isLessOrEqual(knights, archers, giants, towers, mines)) {
            return "BARRACKS-KNIGHT";
        } else if( query.isLessOrEqual(giants, knights, archers, towers, mines)) {
            return "BARRACKS-GIANT";
        } else if( query.isLessOrEqual(towers, knights, archers, giants, mines)) {
            return "TOWER";
        } else {
            return "BARRACKS-KNIGHT";
        }

    }

    protected String move(){

        List<Structure> allTowers = query.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure);
        List<Structure> allMines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure);

        Stats stats = query.getStats();

        if ( (stats.eKnights > stats.archers + 2) && !allTowers.isEmpty() ) {
            return goToClosestTower(allTowers);
        } else {
            return goToClosestMineOrEmptyOrEnemySite();
        }
    }

    protected String goToClosestTower(List<Structure> allTowers){
        Site targetSite = query.getClosestFromListToUnit(allTowers, Predicates.towerStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        Structure targetStructure = query.getStructureBySiteId(targetSite.siteId);
        if( targetSite.radius > 475){
            Logger.log("Safety Tower radius: " + targetStructure.param2);
            return "BUILD " + targetSite.siteId + " TOWER";
        } else {
            Site closestMine = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure),
                    Predicates.towerStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);

            if (Objects.nonNull(closestMine)) {
                return "BUILD " + closestMine.siteId + " MINE";
            } else {
                return "BUILD " + targetSite.siteId + " TOWER";
            }
        }
    }

    protected String goToClosestMineOrEmptyOrEnemySite(){
        Site targetSite = query.getClosestSiteToUnit(Predicates.emptyOrMineOrBarracks, Predicates.enemyOrNoOwner, Predicates.friendlyUnit, Predicates.queenUnitType);
        if (Objects.nonNull(targetSite)) {
            return "MOVE " + targetSite.x + " " + targetSite.y;
        } else {
            return "WAIT";
        }
    }

    protected String goToClosestMine(List<Structure> allMines){
        Site targetSite = query.getClosestFromListToUnit(allMines, Predicates.mineStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        Structure targetStructure = query.getStructureBySiteId(targetSite.siteId);
        return "BUILD " + targetSite.siteId + " MINE";
    }

    protected String goToClosestArcherFactory(List<Structure> archerFactories){
        Site targetSite = query.getClosestFromListToUnit( archerFactories, Predicates.factoryTypeArcher, Predicates.friendlyStructure,Predicates.queenUnitType, Predicates.friendlyUnit );
        return "MOVE " + targetSite.x + " " + targetSite.y;
    }
}

class TrainingDecisionMaker implements DecisionMaker {

    int gold;
    int touchedSite;
    Query query;

    public TrainingDecisionMaker(int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.query = new Query(structures, units, sites);
    }

    public void decide(){

        int targetUnitType = getWhichTypeToBuild();
        int friendlyStructuresCount = query.getAllStructuresOfType(Predicates.barracksStructure, Predicates.friendlyStructure).size();

        if( capableOfTraining( friendlyStructuresCount, targetUnitType ) ){
            String siteString;
            if( targetUnitType == Constants.GIANT){
                siteString = getGiantFactoryClosestToEnemyQueen();
            } else if ( targetUnitType == Constants.ARCHER ){
                siteString = getArcherFactoryClosestToFriendlyQueen();
            } else {
                siteString = getKnightFactoryClosestToEnemyQueen();
            }
            System.out.println("TRAIN " + siteString);
        } else {
            noTraining();
        }
    }

    protected int getWhichTypeToBuild(){

        int result = 0;

        Stats stats = query.getStats();

        Logger.log("Current Count: Knights: " + stats.knights + ", Archers: " + stats.archers + ", Giants: " + stats.giants);

        if ( stats.eKnights > stats.archers || stats.archers < 3 ) {
            result = Constants.ARCHER;
        } else if( stats.eTowers > 0 && stats.giants <= 1 ) {
            result = Constants.GIANT;
        } else {
            result = Constants.KNIGHT;
        }

        Logger.log("Training target: " + query.unitTypeOf(result));
        return result;
    }

    private void noTraining(){
        Logger.log("cannot train");
        System.out.println("TRAIN");
    }

    protected boolean capableOfTraining(int friendsCount, int targetUnitType){
        return friendsCount > 0
                && this.gold >= ( targetUnitType == Constants.ARCHER ? Constants.ARCHER_COST : targetUnitType == Constants.GIANT ? Constants.GIANT_COST : Constants.KNIGHT_COST )
                && factoryAvailable(targetUnitType);
    }

    protected boolean factoryAvailable(int targetUnitType){
        if( targetUnitType == Constants.ARCHER ){
            return query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).size() > 0;
        } else if (targetUnitType == Constants.KNIGHT){
            return query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).size() > 0;
        } else {
            return query.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure).size() > 0;
        }
    }

    protected boolean areEqual(int val1, int val2){
        return val1 == val2;
    }

    private String getKnightFactoryClosestToEnemyQueen(){
            Site closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure),
                                                                Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.enemyUnit);
            return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

    private String getArcherFactoryClosestToFriendlyQueen(){
            Site closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure),
                    Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
            return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

    private String getGiantFactoryClosestToEnemyQueen(){
        Site closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure),
                Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.enemyUnit);
        return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

//    private void printUnitCounts(){
//
//        int enemyKnights = query.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.enemyUnit).size();
//        int enemyArchers = query.getAllUnitsOfType(this.units, Predicates.archerUnitType, Predicates.enemyUnit).size();
//        int enemyGiants = query.getAllUnitsOfType(this.units, Predicates.giantUnitType, Predicates.enemyUnit).size();
//        int enemyFactories = query.getAllStructuresOfType(Predicates.barracksStructure, Predicates.enemyStructure).size();
//        int friendlyKnights = query.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.friendlyUnit).size();
//        int friendlyArchers = query.getAllUnitsOfType(this.units, Predicates.archerUnitType, Predicates.friendlyUnit).size();
//        int friendlyGiants = query.getAllUnitsOfType(this.units, Predicates.giantUnitType, Predicates.friendlyUnit).size();
//        int friendlyFactories = query.getAllStructuresOfType(Predicates.barracksStructure, Predicates.friendlyStructure).size();
//
//
//        Logger.log("Enemy Knights:" + enemyKnights + ", Archers: " + enemyArchers + ", Giants: " + enemyGiants + ", Factories: " + enemyFactories);
//        Logger.log("Friend Knights:" + friendlyKnights + ", Archers: " + friendlyArchers + ", Giants: " + friendlyGiants + ", Factories: " + friendlyFactories);
//
//    }

}

class Logger {
    public static void log(String msg){
        System.err.println(msg);
    }

}

class Site {

    int siteId;
    int x;
    int y;
    int radius;

    public Site( int siteId, int x, int y, int radius){
        this.siteId = siteId;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }
}

class Unit {

    int x;
    int y;
    int owner;
    int unitType;
    String unitText;
    int health;

    public Unit( int x, int y, int owner, int unitType, int health){
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.unitType = unitType;
        this.unitText = unitType == -1 ? "QUEEN" : unitType == 0 ? "KNIGHT" : "ARCHER";
        this.health = health;
    }
}

class Structure {

    int siteId;
    int goldInMine;
    int maxMineSize;
    int structureType;
    String structureText;
    int owner;
    String ownerText;
    int param1;
    int param2;

    public Structure(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2){
        this.siteId = siteId;
        this.goldInMine = goldInMine;
        this.maxMineSize = maxMineSize;
        this.structureType = structureType;
        this.structureText = structureType ==  -1 ? "No main.Structure" : "Barracks";
        this.owner = owner;
        this.ownerText = owner == -1 ? "No Structure" : owner == 0 ? "Friendly" : "Enemy";
        this.param1 = param1;
        this.param2 = param2;
    }

}

class Constants {
    static int EMPTY_SITE = -1;
    static int NOT_TOUCHING = -1;
    static int NO_STRUCTURE = -1;
    static int MINE = 0;
    static int TOWER = 1;
    static int BARRACKS = 2;
    static int QUEEN = -1;
    static int KNIGHT = 0;
    static int ARCHER = 1;
    static int GIANT = 2;
    static int FRIENDLY_OWNER = 0;
    static int ENEMY_OWNER = 1;
    static int NO_OWNER = -1;
    static int ARCHER_COST = 100;
    static int KNIGHT_COST = 80;
    static int GIANT_COST = 140;
}

class Predicates {
    public static final Predicate<Structure> noOwner = s -> s.owner == Constants.NO_OWNER;
    public static final Predicate<Structure> friendlyStructure = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Structure> enemyStructure = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Structure> enemyOrNoOwner = s -> (s.owner == Constants.NO_OWNER) || (s.owner == Constants.ENEMY_OWNER);

    public static final Predicate<Structure> barracksStructure = s -> s.structureType == Constants.BARRACKS;
    public static final Predicate<Structure> towerStructure = s -> s.structureType == Constants.TOWER;
    public static final Predicate<Structure> mineStructure = s -> s.structureType == Constants.MINE;

    public static final Predicate<Structure> emptyOrMineOrBarracks = s -> (s.structureType == Constants.NO_STRUCTURE) || (s.structureType == Constants.BARRACKS) || (s.structureType == Constants.MINE);

    public static final Predicate<Structure> factoryTypeArcher = s -> s.structureType == Constants.BARRACKS && s.param2 == Constants.ARCHER;
    public static final Predicate<Structure> factoryTypeKnight = s -> s.structureType == Constants.BARRACKS && s.param2 == Constants.KNIGHT;
    public static final Predicate<Structure> factoryTypeGiant = s -> s.structureType == Constants.BARRACKS && s.param2 == Constants.GIANT;

    public static final Predicate<Unit> friendlyUnit = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Unit> enemyUnit = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Unit> queenUnitType = s -> s.unitType == Constants.QUEEN;
    public static final Predicate<Unit> knightUnitType = s -> s.unitType == Constants.KNIGHT;
    public static final Predicate<Unit> archerUnitType = s -> s.unitType == Constants.ARCHER;
    public static final Predicate<Unit> giantUnitType = s -> s.unitType == Constants.GIANT;

}

class Stats {

    int knights;
    int archers;
    int giants;
    int eTowers;
    int eKnights;
}

class Query {

    private final List<Structure> structures;
    private final List<Unit> units;
    private final List<Site> sites;

    public Query(List<Structure> structures, List<Unit> units, List<Site> sites){
        this.structures = structures;
        this.units = units;
        this.sites = sites;
    }

    public Site getClosestFromListToUnit(List<Structure> structures, Predicate<? super Structure> structureType,
                                         Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        Structure structure = structures.stream()
                .filter( structureType )
                .filter( structureOwner )
                .min(Comparator.comparingDouble(s -> getDistanceFromUnitToSite(s.siteId, unitOwner, unitType)))
                .orElse(null);

        if( Objects.nonNull(structure) ){
            Site site = getSiteById( structure.siteId );
            Logger.log("Closest main.Structure: main.Site " + site.siteId + ", X:" + site.x + ", Y:" + site.y);
            return site;
        } else {
            return null;
        }

    }

    public Site getClosestSiteToUnit(Predicate<? super Structure> structureType, Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return getClosestFromListToUnit(this.structures, structureType, structureOwner, unitType, unitOwner);
    }

    public double getDistanceFromUnitToSite(int siteId, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        // log("getDistanceFromQueen " + structure);

        Unit unit = findUnitByTypeAndOwner(unitType, unitOwner );
        Site site = getSiteById(siteId);

        int yDiff = Math.max(unit.y, site.y) - Math.min(unit.y, site.y);
        int xDiff = Math.max(unit.x, site.x) - Math.min(unit.x, site.x);

        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );

    }

    public Unit findUnitByTypeAndOwner( Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){
        return this.units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .findFirst()
                .orElse(null);

    }

    public Site getSiteById( int siteId){

        // log("getSiteById " + siteId);
        return this.sites.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
    }

    public Structure getStructureBySiteId(int siteId ){

        // log("getStructureBySiteId " + siteId);
        return this.structures.stream()
                .filter( s -> s.siteId == siteId)
                .findAny()
                .orElse(null);
    }

    public List<Structure> getAllStructuresOfType(Predicate<? super Structure> factoryType, Predicate<? super Structure> structureOwner){

        List<Structure> structsOfType =  this.structures.stream()
                .filter( structureOwner )
                .filter( factoryType )
                .collect(Collectors.toList());

        if( !structsOfType.isEmpty() ){
            Logger.log("Structs of target type: " + structsOfType.stream().map(s -> s.siteId).map(String::valueOf).collect(Collectors.joining(", ") )  );
        } else {
            Logger.log("Structs of target type: None");
        }
        return structsOfType;
    }

    public List<Unit> getAllUnitsOfType(List<Unit> units, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .collect(Collectors.toList());
    }

    public String unitTypeOf(int unitType){
        if( unitType == Constants.ARCHER){
            return "Archer";
        } else if( unitType == Constants.KNIGHT ){
            return "Knight";
        } else if ( unitType == Constants.QUEEN ){
            return "Queen";
        } else {
            return "Giant";
        }

    }

    public boolean areEqual(int val1, int... vals ){
        return Arrays.stream(vals)
                .allMatch(val -> val1 == val);
    }

    public boolean isLessOrEqual(int val1, int... vals ){
        return Arrays.stream(vals)
                .allMatch(val -> val1 <= val);
    }

    protected Stats getStats(){

        Stats stats = new Stats();

        stats.knights = this.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.friendlyUnit).size();
        stats.archers = this.getAllUnitsOfType(this.units, Predicates.archerUnitType, Predicates.friendlyUnit).size();
        stats.giants = this.getAllUnitsOfType(this.units, Predicates.giantUnitType, Predicates.friendlyUnit).size();
        stats.eTowers = this.getAllStructuresOfType(Predicates.towerStructure, Predicates.enemyStructure).size();
        stats.eKnights = this.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.enemyUnit).size();
        return stats;
    }

}
