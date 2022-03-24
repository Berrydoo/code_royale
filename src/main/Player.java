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
                structures.add( Structure.createStructure(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
            }

            List<Unit> units = new ArrayList<>();
            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER, 2 = GIANT
                int health = in.nextInt();
                units.add(Unit.createUnit(x, y, owner, unitType, health));
            }

            new Commander(gold, touchedSite, sites, structures, units);

        }
    }
}

class Grunt {

    Query query;

    public Grunt(Query query){
        this.query = query;
    }

    public void executeStrategy(QueenStrategy queenStrategy, TrainingStrategy trainingStrategy){
        getQueenCommand(queenStrategy).executeCommand();
        getTrainCommand(trainingStrategy).executeCommand();
    }

    private CommandInterface getQueenCommand(QueenStrategy queenStrategy){
        switch (queenStrategy){
            case BuildArcherFactory: {
                return new BuildArcherFactoryCommand(query);
            }
            default:
                return new BuildArcherFactoryCommand(query);
        }
    }

    private CommandInterface getTrainCommand(TrainingStrategy trainingStrategy){
        switch (trainingStrategy){
            case TrainArcher: {
                return new TrainArcherCommand(query);
            }
            default:
                return new TrainArcherCommand(query);
        }
    }


    public void decide(){

        List<Structure> allTowers = query.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure);
        Stats stats = query.getStats();
        if ( (stats.eKnights > stats.archers + 2) && !allTowers.isEmpty() ) {
            goToClosestTower(allTowers);
        } else {
            buildNextStructure();
        }
    }

    protected String getNextStructureType(){

        int archers = query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).size();
        int knights = query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).size();
        int giants = query.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure).size();
        int towers = query.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure).size();
        int mines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure).size();

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

        if( query.isLessOrEqual(knights, archers, giants, towers, mines)) {
            return "BARRACKS-KNIGHT";
        } else if( query.isLessOrEqual(archers, knights, giants, towers, mines)) {
            return "BARRACKS-ARCHER";
        } else if( query.isLessOrEqual(giants, knights, archers, towers, mines)) {
            return "BARRACKS-GIANT";
        } else if( query.isLessOrEqual(towers, knights, archers, giants, mines)) {
            return "TOWER";
        } else {
            return "BARRACKS-KNIGHT";
        }

    }

    protected void goToClosestTower(List<Structure> allTowers){
        Tower tower = query.getClosestFromListToUnit(allTowers, Predicates.towerStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        System.out.println("BUILD " + tower.siteId + " TOWER");
    }

    protected void buildNextStructure(){

        long numKnightFactories = query.structures.stream()
                .filter( s -> s.structureType != Constants.MINE && s.structureType != Constants.TOWER)
                .filter( s -> s.owner == Constants.FRIENDLY_OWNER)
                .count();

        if( numKnightFactories == 0) {
            Structure targetSite = query.getClosestSiteToUnit(Predicates.emptyOrMineOrBarracks, Predicates.enemyOrNoOwner, Predicates.friendlyUnit, Predicates.queenUnitType);
            System.out.println("BUILD " + targetSite.siteId + " BARRACKS-ARCHER");
        } else {
            if( getIncomeFromMines() < 15 ){
                buildMine();
            } else {
                Structure targetSite = query.getClosestSiteToUnit(Predicates.emptyOrMineOrBarracks, Predicates.enemyOrNoOwner, Predicates.friendlyUnit, Predicates.queenUnitType);
                System.out.println("BUILD " + targetSite.siteId + " " + getNextStructureType());
            }
        }
    }

    private void buildMine(){

        Mine closestMine = query.structures.stream()
                .filter( s -> s.structureType == Constants.MINE )
                .filter( s -> s.owner == Constants.FRIENDLY_OWNER )
                .map( s -> (Mine)s)
                .peek( s -> System.err.println("Mine " + s.siteId + ", Income: " + s.incomeRate + ", max: " + s.maxMineSize + ", goldRemaining: " + s.goldInMine) )
                .filter( s -> !s.rateIsMaxed() && s.goldInMine > 0)
                .min(Comparator.comparingDouble(s ->  getDistanceFromUnitToSite(s.siteId, query.findUnitByTypeAndOwner(Predicates.queenUnitType, Predicates.friendlyUnit))))
                .orElse(null);

        if( Objects.isNull(closestMine)){
            Logger.log("No mines that are not maxed out");

            Structure alternate = query.structures.stream()
                    .filter( s -> s.structureType != Constants.TOWER )
                    .filter( s -> s.owner != Constants.FRIENDLY_OWNER )
                    .filter( s -> s.goldInMine > 0 )
                    .peek( s -> System.err.println("Structure2 " + s.siteId + ", max mine: " + s.maxMineSize + ", goldRemaining: " + s.goldInMine) )
                    .min(Comparator.comparingDouble(s ->  getDistanceFromUnitToSite(s.siteId, query.findUnitByTypeAndOwner(Predicates.queenUnitType, Predicates.friendlyUnit))))
                    .orElse(null);

            if( Objects.isNull(alternate)) {
                Structure alternate2 = query.structures.stream()
                        .filter( s -> s.structureType != Constants.TOWER )
                        .filter( s -> s.owner != Constants.FRIENDLY_OWNER )
                        .peek( s -> System.err.println("Structure3 " + s.siteId + ", max mine: " + s.maxMineSize + ", goldRemaining: " + s.goldInMine) )
                        .min(Comparator.comparingDouble(s ->  getDistanceFromUnitToSite(s.siteId, query.findUnitByTypeAndOwner(Predicates.queenUnitType, Predicates.friendlyUnit))))
                        .orElse(null);
                if( Objects.nonNull(alternate2) ){
                    System.out.println("BUILD " + alternate2.siteId + " " + getNextStructureType());
                } else {
                    Structure alternate3 = query.structures.stream()
                            .filter( s -> s.structureType == Constants.TOWER )
                            .filter( s -> s.owner == Constants.FRIENDLY_OWNER )
                            .peek( s -> System.err.println("Structure4 " + s.siteId + ", max mine: " + s.maxMineSize + ", goldRemaining: " + s.goldInMine) )
                            .min(Comparator.comparingDouble(s ->  getDistanceFromUnitToSite(s.siteId, query.findUnitByTypeAndOwner(Predicates.queenUnitType, Predicates.friendlyUnit))))
                            .orElse(null);

                    Site site = query.getSiteById(alternate3.siteId);
                    System.out.println("MOVE " + site.x + " " + site.y );
                }
            } else {
                System.out.println("BUILD " + alternate.siteId + " MINE");
            }
        } else {
            Logger.log("Closest mine is site " + closestMine.siteId + ", isMaxed: " + closestMine.rateIsMaxed() + ", output: " + closestMine.incomeRate + ", Max: " + closestMine.maxMineSize);
            System.out.println("BUILD " + closestMine.siteId + " MINE");
        }
    }

    private double getDistanceFromUnitToSite(int siteId, Queen queen ){

        Site site = query.getSiteById(siteId);

        int yDiff = Math.max(queen.y, site.y) - Math.min(queen.y, site.y);
        int xDiff = Math.max(queen.x, site.x) - Math.min(queen.x, site.x);

        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );

    }

    protected int getIncomeFromMines(){
        List<Mine> mines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure);
        int mineIncome =  mines.stream().reduce(0, (subtotal, mine) -> subtotal + mine.incomeRate, Integer::sum);
        Logger.log("Mine Income: " + mineIncome);
        return mineIncome;
    }
}

class TrainingDecisionMaker {

    Query query;

    public TrainingDecisionMaker(Query query){
        this.query = query;
    }

    public void executeCommand(QueenStrategy queenStrategy){
        decide();
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
                && this.query.gold >= ( targetUnitType == Constants.ARCHER ? Constants.ARCHER_COST : targetUnitType == Constants.GIANT ? Constants.GIANT_COST : Constants.KNIGHT_COST )
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

    private String getKnightFactoryClosestToEnemyQueen(){
        KnightBarracks closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure),
                                                            Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.enemyUnit);
        return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

    private String getArcherFactoryClosestToFriendlyQueen(){
        ArcherBarracks closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure),
                Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.friendlyUnit);
        return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

    private String getGiantFactoryClosestToEnemyQueen(){
        GiantBarracks closestSite = query.getClosestFromListToUnit(query.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure),
                Predicates.barracksStructure, Predicates.friendlyStructure, Predicates.queenUnitType, Predicates.enemyUnit);
        return Objects.nonNull(closestSite) ? "" + closestSite.siteId : "";
    }

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
    int health;

    public static Unit createUnit( int x, int y, int owner, int unitType, int health){
        switch (unitType){
            case -1: // Queen
                return new Queen(x, y, owner, unitType, health);
            case 0: // Knight
                return new Knight(x, y, owner, unitType, health);
            case 1: // Archer
                return new Archer(x, y, owner, unitType, health);
            case 2: // Giant
                return new Giant(x, y, owner, unitType, health);
            default:
                throw new RuntimeException("Unknown unit type: " + unitType);
        }
    }

    public Unit( int x, int y, int owner, int unitType, int health ){
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.unitType = unitType;
        this.health = health;
    }
}

class Queen extends Unit {

    public Queen(int x, int y, int owner, int unitType, int health) {
        super(x, y, owner, unitType, health);
    }
}

class Knight extends Unit {

    public Knight(int x, int y, int owner, int unitType, int health) {
        super(x, y, owner, unitType, health);
    }
}

class Archer extends Unit {

    public Archer(int x, int y, int owner, int unitType, int health) {
        super(x, y, owner, unitType, health);
    }
}

class Giant extends Unit {

    public Giant(int x, int y, int owner, int unitType, int health) {
        super(x, y, owner, unitType, health);
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

class Structure {

    int siteId;
    int structureType;
    int owner;
    int goldInMine;
    int maxMineSize;

    public static Structure createStructure(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2){
        switch (structureType) {
            case -1 : // No Structure
                return new NoStructure(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
            case 0: // MINE
                return new Mine(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
            case 1: // TOWER
                return new Tower(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
            case 2:
                switch (param2) {
                    case 0: // KNIGHT
                        return new KnightBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
                    case 1: // ARCHER
                        return new ArcherBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
                    case 2: // GIANT
                        return new GiantBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
                }
            default:
                throw new RuntimeException("Unknown structure type: " + structureType);
        }
    }

    public Structure(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2){
        this.siteId = siteId;
        this.structureType = structureType;
        this.owner = owner;
        this.goldInMine = goldInMine;
        this.maxMineSize = maxMineSize;
    }
}

class ArcherBarracks extends Structure {

    public ArcherBarracks(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
    }
}

class KnightBarracks extends Structure {

    public KnightBarracks(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
    }
}

class GiantBarracks extends Structure {

    public GiantBarracks(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
    }
}

class NoStructure extends Structure {

    public NoStructure(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
    }
}

class Tower extends Structure {

    int remainingHP;
    int attackRadius;

    public Tower(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
        this.remainingHP = param1;
        this.attackRadius = param2;
    }
}

class Mine extends Structure {

    int incomeRate;

    boolean rateIsMaxed(){
        return incomeRate == maxMineSize;
    }

    public Mine(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
        this.incomeRate = param1;
    }
}

class Predicates {
    public static final Predicate<Structure> noOwner = s -> s.owner == Constants.NO_OWNER;
    public static final Predicate<Structure> friendlyStructure = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Structure> enemyStructure = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Structure> enemyOrNoOwner = s -> (s.owner == Constants.NO_OWNER) || (s.owner == Constants.ENEMY_OWNER);

    public static final Predicate<Structure> barracksStructure = s -> s instanceof KnightBarracks || s instanceof ArcherBarracks || s instanceof GiantBarracks;
    public static final Predicate<Structure> towerStructure = s -> s instanceof Tower;
    public static final Predicate<Structure> mineStructure = s -> s instanceof Mine;

    public static final Predicate<Structure> emptyOrMineOrBarracks = s -> s instanceof NoStructure
                                                                            || s instanceof ArcherBarracks
                                                                            || s instanceof KnightBarracks
                                                                            || s instanceof GiantBarracks
                                                                            || s instanceof Mine;

    public static final Predicate<Structure> factoryTypeArcher = s -> s instanceof ArcherBarracks;
    public static final Predicate<Structure> factoryTypeKnight = s -> s instanceof KnightBarracks;
    public static final Predicate<Structure> factoryTypeGiant = s -> s instanceof GiantBarracks;

    public static final Predicate<Unit> friendlyUnit = s -> s.owner == Constants.FRIENDLY_OWNER;
    public static final Predicate<Unit> enemyUnit = s -> s.owner == Constants.ENEMY_OWNER;
    public static final Predicate<Unit> queenUnitType = s -> s instanceof Queen;
    public static final Predicate<Unit> knightUnitType = s -> s instanceof Knight;
    public static final Predicate<Unit> archerUnitType = s -> s instanceof Archer;
    public static final Predicate<Unit> giantUnitType = s -> s instanceof Giant;

}

class Stats {

    int knights;
    int archers;
    int giants;
    int knightBarracks;
    int archerBarracks;
    int giantBarracks;
    int towers;
    int mines;
    int income;
    int gold;
    int eKnights;
    int eArchers;
    int eGiants;
    int eKnightBarracks;
    int eArcherBarracks;
    int eGiantBarracks;
    int eTowers;
    int eMines;
    int eIncome;

    int totalBarracks = knightBarracks + archerBarracks + giantBarracks;
    int eTotalBarracks = eKnightBarracks + eArcherBarracks + eGiantBarracks;
}

class Query {


    final int gold;
    final int touchedSite;
    final List<Structure> structures;
    final List<Unit> units;
    final List<Site> sites;

    public Query(int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units){
        this.gold = gold;
        this.touchedSite = touchedSite;
        this.structures = structures;
        this.units = units;
        this.sites = sites;
    }

    public <T> T getClosestFromListToUnit(List<Structure> structures, Predicate<? super Structure> structureType,
                                         Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        return structures.stream()
                .filter( structureType )
                .filter( structureOwner )
                .min(Comparator.comparingDouble(s -> getDistanceFromUnitToSite(s.siteId, unitOwner, unitType)))
                .map( s -> (T)s)
                .orElse(null);

    }

    public <T> T getClosestSiteToUnit(Predicate<? super Structure> structureType, Predicate<? super Structure> structureOwner, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return getClosestFromListToUnit(this.structures, structureType, structureOwner, unitType, unitOwner);
    }

    public double getDistanceFromUnitToSite(int siteId, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){

        Unit unit = findUnitByTypeAndOwner(unitType, unitOwner );
        Site site = getSiteById(siteId);

        int yDiff = Math.max(unit.y, site.y) - Math.min(unit.y, site.y);
        int xDiff = Math.max(unit.x, site.x) - Math.min(unit.x, site.x);

        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );

    }

    public <T> T findUnitByTypeAndOwner( Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner ){
        return this.units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .map( s -> (T)s)
                .findFirst()
                .orElse(null);

    }

    public <T> T getSiteById( int siteId){

        return this.sites.stream()
                .filter( s -> s.siteId == siteId)
                .map( s -> (T)s)
                .findAny()
                .orElse(null);
    }

    public <T> T getStructureBySiteId(int siteId ){

        return this.structures.stream()
                .filter( s -> s.siteId == siteId)
                .map( s -> (T)s)
                .findAny()
                .orElse(null);
    }

    public <T> List<T> getAllStructuresOfType(Predicate<? super Structure> factoryType, Predicate<? super Structure> structureOwner){

        return this.structures.stream()
                .filter( factoryType )
                .filter( structureOwner )
                .map( s -> (T)s)
                .collect(Collectors.toList());

    }

    public <T> List<T> getAllUnitsOfType(List<Unit> units, Predicate<? super Unit> unitType, Predicate<? super Unit> unitOwner){
        return units.stream()
                .filter( unitType )
                .filter( unitOwner )
                .map( s -> (T)s)
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

    protected int getIncomeFromMines(List<Mine> mines){
//        List<Mine> mines = query.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure);
        int mineIncome =  mines.stream().reduce(0, (subtotal, mine) -> subtotal + mine.incomeRate, Integer::sum);
        Logger.log("Mine Income: " + mineIncome);
        return mineIncome;
    }

    protected Stats getStats(){

        Stats stats = new Stats();

        stats.knights = this.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.friendlyUnit).size();
        stats.archers = this.getAllUnitsOfType(this.units, Predicates.archerUnitType, Predicates.friendlyUnit).size();
        stats.giants = this.getAllUnitsOfType(this.units, Predicates.giantUnitType, Predicates.friendlyUnit).size();
        stats.knightBarracks = this.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).size();
        stats.archerBarracks = this.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).size();
        stats.giantBarracks = this.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure).size();
        stats.towers = this.getAllStructuresOfType(Predicates.towerStructure, Predicates.friendlyStructure).size();
        stats.mines = this.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure).size();
        stats.income = getIncomeFromMines(this.getAllStructuresOfType(Predicates.mineStructure, Predicates.friendlyStructure));
        stats.gold = this.gold;
        stats.eKnights = this.getAllUnitsOfType(this.units, Predicates.knightUnitType, Predicates.enemyUnit).size();
        stats.eArchers = this.getAllUnitsOfType(this.units, Predicates.archerUnitType, Predicates.enemyUnit).size();
        stats.eGiants = this.getAllUnitsOfType(this.units, Predicates.giantUnitType, Predicates.enemyUnit).size();
        stats.eKnightBarracks = this.getAllStructuresOfType(Predicates.factoryTypeKnight, Predicates.friendlyStructure).size();
        stats.eArcherBarracks = this.getAllStructuresOfType(Predicates.factoryTypeArcher, Predicates.friendlyStructure).size();
        stats.eGiantBarracks = this.getAllStructuresOfType(Predicates.factoryTypeGiant, Predicates.friendlyStructure).size();
        stats.eTowers = this.getAllStructuresOfType(Predicates.towerStructure, Predicates.enemyStructure).size();
        stats.eMines = this.getAllStructuresOfType(Predicates.mineStructure, Predicates.enemyStructure).size();
        stats.eIncome = getIncomeFromMines(this.getAllStructuresOfType(Predicates.mineStructure, Predicates.enemyStructure));

        return stats;
    }

}

class Commander {

    Query query;
    Grunt grunt;
    Stats stats;

    public Commander( int gold, int touchedSite, List<Site> sites, List<Structure> structures, List<Unit> units ){

        Query query = new Query(gold, touchedSite, sites, structures, units);
        this.grunt = new Grunt(query);
        this.stats = query.getStats();
        createStrategy();

    }

    void createStrategy(){
        QueenStrategy queenStrategy = getQueenStrategy();
        TrainingStrategy trainingStrategy = getTrainingStrategy();

        this.grunt.executeStrategy(queenStrategy, trainingStrategy);

    }

    QueenStrategy getQueenStrategy(){

        return QueenStrategy.BuildArcherFactory;

//        Stats stats = query.getStats();
//        if( haveNoStructures(stats) ){
//            return QueenStrategy.CreateArcherFactory;
//        }


    }

    TrainingStrategy getTrainingStrategy(){
        return TrainingStrategy.TrainArcher;
    }

    private boolean haveNoStructures(Stats stats){
        return query.areEqual(0, stats.totalBarracks, stats.towers, stats.mines);
    }

}

enum QueenStrategy {
    BuildArcherFactory,
    BuildKnightFactory,
    BuildGiantFactory,
    BuildMine,
    BuildTower,
    Retreat
}

enum TrainingStrategy {
    TrainArcher,
    TrainKnight,
    TrainGiant
}

interface CommandInterface {
    void executeCommand();
    void writeCommandMessage(String message);

}

abstract class AbstractCommand implements CommandInterface {

    private Query query;

    public AbstractCommand( Query query ){ this.query = query;}

    public abstract void executeCommand();

    public void writeCommandMessage(String message) {
        System.out.println(message);
    }

    public void writeLogMessage(String message){
        System.err.println(message);
    }
}

class BuildArcherFactoryCommand extends AbstractCommand {

    public BuildArcherFactoryCommand(Query query){
        super(query);
    }

    @Override
    public void executeCommand() {

    }

}

class TrainArcherCommand extends AbstractCommand {

    public TrainArcherCommand(Query query ){
        super(query);
    }

    @Override
    public void executeCommand() {

    }

}

class TrainKnightCommand extends AbstractCommand {

    public TrainKnightCommand(Query query) {
        super(query);
    }

    @Override
    public void executeCommand() {

    }
}

class TrainGiantCommand extends AbstractCommand {

    public TrainGiantCommand(Query query) {
        super(query);
        writeLogMessage("TRAINING GIANT");
    }

    @Override
    public void executeCommand() {

    }
}