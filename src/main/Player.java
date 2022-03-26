package main;

import java.util.*;
import java.util.stream.Stream;


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

            List<ArcherBarracks> friendlyArcherBarracks = new ArrayList<>();
            List<KnightBarracks> friendlyKnightBarracks = new ArrayList<>();
            List<GiantBarracks> friendlyGiantBarracks = new ArrayList<>();
            List<Mine> friendlyMines = new ArrayList<>();
            List<Tower> friendlyTowers = new ArrayList<>();
            List<ArcherBarracks> enemyArcherBarracks = new ArrayList<>();
            List<KnightBarracks> enemyKnightBarracks = new ArrayList<>();
            List<GiantBarracks> enemyGiantBarracks = new ArrayList<>();
            List<Mine> enemyMines = new ArrayList<>();
            List<Tower> enemyTowers = new ArrayList<>();
            List<Structure> noStructures = new ArrayList<>();

            for (int i = 0; i < numSites; i++) {
                int siteId = in.nextInt();
                int goldInMine = in.nextInt(); // gold in mine
                int maxMineSize = in.nextInt(); // max mine size
                int structureType = in.nextInt(); // -1 = No structure, 0 = Mine, 1 = Tower, 2 = Barracks
                int owner = in.nextInt(); // -1 = No structure, 0 = Friendly, 1 = Enemy
                int param1 = in.nextInt();
                int param2 = in.nextInt();

                if( owner == 0) { // FRIENDLY
                    switch (structureType) {
                        case 0: // MINE
                           friendlyMines.add(new Mine(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                           break;
                        case 1: // TOWER
                            friendlyTowers.add(new Tower(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                            break;
                        case 2:
                            switch (param2) {
                                case 0: // KNIGHT
                                    friendlyKnightBarracks.add(new KnightBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                                case 1: // ARCHER
                                    friendlyArcherBarracks.add(new ArcherBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                                case 2: // GIANT
                                    friendlyGiantBarracks.add(new GiantBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                            }
                            break;
                        default:
                            throw new RuntimeException("Unknown structure type: " + structureType);
                    }
                } else if( owner == 1){
                    switch (structureType) {
                        case 0: // MINE
                             enemyMines.add(new Mine(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                            break;
                        case 1: // TOWER
                             enemyTowers.add(new Tower(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                            break;
                        case 2:
                            switch (param2) {
                                case 0: // KNIGHT
                                     enemyKnightBarracks.add(new KnightBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                                case 1: // ARCHER
                                     enemyArcherBarracks.add(new ArcherBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                                case 2: // GIANT
                                     enemyGiantBarracks.add(new GiantBarracks(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                                    break;
                            }
                            break;
                        default:
                            throw new RuntimeException("Unknown structure type: " + structureType);
                    }
                } else {
                    noStructures.add(new NoStructure(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2));
                }
            }
            Structures structures = new Structures(friendlyArcherBarracks, friendlyKnightBarracks, friendlyGiantBarracks,
                                            friendlyMines, friendlyTowers, enemyArcherBarracks, enemyKnightBarracks,
                                            enemyGiantBarracks, enemyMines, enemyTowers, noStructures );

            Queen friendlyQueen = null;
            List<Knight> friendlyKnights = new ArrayList<>();
            List<Archer> friendlyArchers = new ArrayList<>();
            List<Giant> friendlyGiants = new ArrayList<>();
            Queen enemyQueen = null;
            List<Knight> enemyKnights = new ArrayList<>();
            List<Archer> enemyArchers = new ArrayList<>();
            List<Giant> enemyGiants = new ArrayList<>();

            int numUnits = in.nextInt();
            for (int i = 0; i < numUnits; i++) {
                int x = in.nextInt();
                int y = in.nextInt();
                int owner = in.nextInt();
                int unitType = in.nextInt(); // -1 = QUEEN, 0 = KNIGHT, 1 = ARCHER, 2 = GIANT
                int health = in.nextInt();

                if ( owner == 0){
                    switch (unitType){
                        case -1: // Queen
                            friendlyQueen = new Queen(x, y, owner, unitType, health);
                            break;
                        case 0: // Knight
                            friendlyKnights.add(new Knight(x, y, owner, unitType, health));
                            break;
                        case 1: // Archer
                            friendlyArchers.add(new Archer(x, y, owner, unitType, health));
                            break;
                        case 2: // Giant
                            friendlyGiants.add(new Giant(x, y, owner, unitType, health));
                            break;
                        default:
                            throw new RuntimeException("Unknown unit type: " + unitType);
                    }
                } else {
                    switch (unitType){
                        case -1: // Queen
                            enemyQueen = new Queen(x, y, owner, unitType, health);
                            break;
                        case 0: // Knight
                            enemyKnights.add(new Knight(x, y, owner, unitType, health));
                            break;
                        case 1: // Archer
                            enemyArchers.add(new Archer(x, y, owner, unitType, health));
                            break;
                        case 2: // Giant
                            enemyGiants.add(new Giant(x, y, owner, unitType, health));
                            break;
                        default:
                            throw new RuntimeException("Unknown unit type: " + unitType);
                    }
                }
            }

            Units units = new Units(friendlyQueen, friendlyKnights, friendlyArchers, friendlyGiants, enemyQueen, enemyKnights, enemyArchers, enemyGiants);

            GameData gameData = new GameData(units, structures, sites);

            new Commander(gold, touchedSite, gameData);
        }
    }
}

class Commander {

    GameData gameData;
    Grunt grunt;

    public Commander(int gold, int touchedSite, GameData gameData) {
        this.gameData = gameData;
        this.grunt = new Grunt(gameData);
        createStrategy();
    }

    void createStrategy() {
        QueenStrategy queenStrategy = getQueenStrategy();
        TrainingStrategy trainingStrategy = getTrainingStrategy();

        this.grunt.executeStrategy(queenStrategy, trainingStrategy);

    }

    QueenStrategy getQueenStrategy() {

        if (stressedByAttack()) {
            return QueenStrategy.Retreat;
        }
        if (haveLimitedIncome(7)) {
            return QueenStrategy.BuildMine;
        }
        if (haveNoKnightBarracks()) {
            return QueenStrategy.BuildKnightBarracks;
        }
        if (haveNoTowers()) {
            return QueenStrategy.BuildTower;
        }
        if (haveNoArcherBarracks()) {
            return QueenStrategy.BuildArcherBarracks;
        }
        if (haveLimitedIncome(15)) {
            return QueenStrategy.BuildMine;
        }
        if (enemyHasTowers()) {
            return QueenStrategy.BuildGiantBarracks;
        }
        return QueenStrategy.BuildMine;

    }

    TrainingStrategy getTrainingStrategy() {

        if(haveKnightsLessThanThreshold(8)){
            return TrainingStrategy.TrainKnight;
        }
        if (haveNoArchers()) {
            return TrainingStrategy.TrainArcher;
        }
        if (haveFewerArchersThanEnemyKnights()) {
            return TrainingStrategy.TrainArcher;
        }
        if (enemyHasTowers()) {
            return TrainingStrategy.TrainGiant;
        }
        return TrainingStrategy.TrainKnight;
    }

    private boolean haveNoArcherBarracks() {
        return gameData.structures.friendlyArcherBarracks.size() == 0;
    }

    private boolean haveNoTowers() {
        return gameData.structures.friendlyTowers.size() == 0;
    }

    private boolean haveLimitedIncome(int incomeThreshold) {
        int income = gameData.structures.friendlyMines.stream().reduce(0, (subtotal, mine) -> subtotal + mine.incomeRate, Integer::sum);
        return income < incomeThreshold;
    }

    private boolean stressedByAttack() {
        return gameData.units.enemyKnights.size() > gameData.units.friendlyArchers.size();
    }

    private boolean haveNoKnightBarracks() {
        return gameData.structures.friendlyKnightBarracks.size() == 0;
    }

    private boolean enemyHasTowers() {
        return gameData.structures.enemyTowers.size() > 0;
    }

    private boolean haveNoArchers() {
        return gameData.units.friendlyArchers.size() == 0;
    }

    private boolean haveFewerArchersThanEnemyKnights() {
        return gameData.units.friendlyArchers.size() < gameData.units.enemyKnights.size();
    }
    private boolean haveKnightsLessThanThreshold(int threshold){
        return gameData.units.friendlyKnights.size() < threshold;
    }
}

class Site {

    int siteId;
    int x;
    int y;
    int radius;

    public Site(int siteId, int x, int y, int radius) {
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

    public Unit(int x, int y, int owner, int unitType, int health) {
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

    public Structure(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
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

    boolean rateIsMaxed() {
        return incomeRate == maxMineSize;
    }

    public Mine(int siteId, int goldInMine, int maxMineSize, int structureType, int owner, int param1, int param2) {
        super(siteId, goldInMine, maxMineSize, structureType, owner, param1, param2);
        this.incomeRate = param1;
    }
}

class GameData {
    Units units;
    Structures structures;
    List<Site> sites;

    public GameData(Units units, Structures structures, List<Site> sites){
        this.units = units;
        this.structures = structures;
        this.sites = sites;
    }


    public Site getSiteFromStructure(int siteId, List<Site> sites){
        return sites.stream().filter( s -> s.siteId == siteId).findFirst().orElse(null);
    }

    public Structure getStructureFromSite(int siteId, List<? extends Structure> structures){
        return structures.stream().filter( s -> s.siteId == siteId).findFirst().orElse(null);
    }

    public double getDistanceFromUnitToSite(Site site, Unit unit ){

        if( Objects.isNull(site) ){
            return 10000000d;
        }

        int yDiff = Math.max(unit.y, site.y) - Math.min(unit.y, site.y);
        int xDiff = Math.max(unit.x, site.x) - Math.min(unit.x, site.x);
        return Math.sqrt( (yDiff*yDiff) + (xDiff*xDiff) );
    }

    public Site getClosestOf(List<? extends Structure> structures){
        if( structures.isEmpty() ){ return null; }

        return structures.stream()
                .map( s -> getSiteFromStructure(s.siteId, this.sites))
                .min(Comparator.comparingDouble(site -> getDistanceFromUnitToSite(site, this.units.friendlyQueen)))
                .get();

    }

    public Site getStructureClosestToQueen(){
        Site closestNoStructure = getClosestOf(this.structures.noStructures);
        Site closestArcherBarracks = getClosestOf(this.structures.enemyArcherBarracks);
        Site closestKnightBarracks = getClosestOf(this.structures.enemyKnightBarracks);
        Site closestGiantBarracks = getClosestOf(this.structures.enemyGiantBarracks);
        Site closestMines = getClosestOf(this.structures.enemyMines);
        return Stream.of(closestNoStructure, closestArcherBarracks, closestKnightBarracks, closestGiantBarracks, closestMines)
                .filter(Objects::nonNull)
                .map( s -> getSiteFromStructure(s.siteId, this.sites))
                .min(Comparator.comparingDouble(site -> getDistanceFromUnitToSite(site, this.units.friendlyQueen)))
                .get();

    }
}

class Units {

    Queen friendlyQueen;
    List<Knight> friendlyKnights;
    List<Archer> friendlyArchers;
    List<Giant> friendlyGiants;
    Queen enemyQueen;
    List<Knight> enemyKnights;
    List<Archer> enemyArchers;
    List<Giant> enemyGiants;

    public Units(Queen queen, List<Knight> knights, List<Archer> archers, List<Giant> giants, Queen eQueen, List<Knight> eKnights, List<Archer> eArchers, List<Giant> eGiants){
        this.friendlyQueen = queen;
        this.friendlyKnights = knights;
        this.friendlyArchers = archers;
        this.friendlyGiants = giants;
        this.enemyQueen = eQueen;
        this.enemyKnights = eKnights;
        this.enemyArchers = eArchers;
        this.enemyGiants = eGiants;
    }

}

class Structures {
    List<ArcherBarracks> friendlyArcherBarracks;
    List<KnightBarracks> friendlyKnightBarracks;
    List<GiantBarracks> friendlyGiantBarracks;
    List<Mine> friendlyMines;
    List<Tower> friendlyTowers;
    List<ArcherBarracks> enemyArcherBarracks;
    List<KnightBarracks> enemyKnightBarracks;
    List<GiantBarracks> enemyGiantBarracks;
    List<Mine> enemyMines;
    List<Tower> enemyTowers;
    List<Structure> noStructures;

    public Structures( List<ArcherBarracks> friendlyArcherBarracks, List<KnightBarracks> friendlyKnightBarracks,
                       List<GiantBarracks> friendlyGiantBarracks, List<Mine> friendlyMines, List<Tower> friendlyTowers,
                       List<ArcherBarracks> enemyArcherBarracks, List<KnightBarracks> enemyKnightBarracks,
                       List<GiantBarracks> enemyGiantBarracks, List<Mine> enemyMines, List<Tower> enemyTowers,
                       List<Structure> noStructures ){
        this.friendlyArcherBarracks = friendlyArcherBarracks;
        this.friendlyKnightBarracks = friendlyKnightBarracks;
        this.friendlyGiantBarracks = friendlyGiantBarracks;
        this.friendlyMines = friendlyMines;
        this.friendlyTowers = friendlyTowers;
        this.enemyArcherBarracks = enemyArcherBarracks;
        this.enemyKnightBarracks = enemyKnightBarracks;
        this.enemyGiantBarracks = enemyGiantBarracks;
        this.enemyMines = enemyMines;
        this.enemyTowers = enemyTowers;
        this.noStructures  = noStructures;
    }
}

class Grunt {

    GameData gameData;

    public Grunt(GameData gameData) {
        this.gameData = gameData;
    }

    public void executeStrategy(QueenStrategy queenStrategy, TrainingStrategy trainingStrategy) {
        getQueenCommand(queenStrategy).executeCommand();
        getTrainCommand(trainingStrategy).executeCommand();
    }

    private CommandInterface getQueenCommand(QueenStrategy queenStrategy) {
        switch (queenStrategy) {
            case BuildArcherBarracks: {
                return new BuildArcherBarracksCommand(gameData);
            }
            case BuildGiantBarracks: {
                return new BuildGiantBarracksCommand(gameData);
            }
            case BuildKnightBarracks: {
                return new BuildKnightBarracksCommand(gameData);
            }
            case BuildTower: {
                return new BuildTowerCommand(gameData);
            }
            case Retreat: {
                return new RetreatCommand(gameData);
            }
            case BuildMine:
            default: {
                return new BuildMineCommand(gameData);
            }
        }
    }

    private CommandInterface getTrainCommand(TrainingStrategy trainingStrategy) {
        switch (trainingStrategy) {
            case TrainArcher: {
                return new TrainArcherCommand(gameData);
            }
            case TrainGiant: {
                return new TrainGiantCommand(gameData);
            }
            case TrainKnight:
            default:
                return new TrainKnightCommand(gameData);
        }
    }
}

enum QueenStrategy {
    BuildArcherBarracks,
    BuildKnightBarracks,
    BuildGiantBarracks,
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

    protected GameData gameData;
    public AbstractCommand(GameData gameData) {
        this.gameData = gameData;
    }

    public abstract void executeCommand();

    public void writeCommandMessage(String message) {
        System.out.println(message);
    }

    public void writeLogMessage(String message) {
        System.err.println(message);
    }
}

class BuildArcherBarracksCommand extends AbstractCommand {

    BuildArcherBarracksCommand(GameData gameData) {
        super(gameData);
        writeLogMessage("BUILD ARCHER BARRACKS");
    }

    @Override
    public void executeCommand() {
        Site closestSite = gameData.getStructureClosestToQueen();
        writeCommandMessage("BUILD " + closestSite.siteId + " BARRACKS-ARCHER");
    }
}

class BuildGiantBarracksCommand extends AbstractCommand {

    public BuildGiantBarracksCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        Site closestSite = gameData.getStructureClosestToQueen();
        writeCommandMessage("BUILD " + closestSite.siteId + " BARRACKS-GIANT");
    }
}

class BuildKnightBarracksCommand extends AbstractCommand {

    public BuildKnightBarracksCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        Site closestSite = gameData.getStructureClosestToQueen();
        writeCommandMessage("BUILD " + closestSite.siteId + " BARRACKS-KNIGHT");
    }
}

class BuildTowerCommand extends AbstractCommand {

    public BuildTowerCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        Site closestSite = gameData.getStructureClosestToQueen();
        writeCommandMessage("BUILD " + closestSite.siteId + " TOWER");
    }
}

class BuildMineCommand extends AbstractCommand {

    public BuildMineCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        Site closestSite = gameData.getStructureClosestToQueen();
        Site closestFriendlyMine = gameData.getClosestOf(gameData.structures.friendlyMines);

        List<Site> potentialSites = new ArrayList<>();
        potentialSites.add(closestSite);

        if (Objects.nonNull(closestFriendlyMine)) {
            Mine closestMine = (Mine) gameData.getStructureFromSite(closestFriendlyMine.siteId, gameData.structures.friendlyMines);
            if (!closestMine.rateIsMaxed()) {
                potentialSites.add(closestFriendlyMine);
            }
        }

        Site target = potentialSites.stream()
            .filter(Objects::nonNull)
            .min(Comparator.comparingDouble(site -> gameData.getDistanceFromUnitToSite(site, gameData.units.friendlyQueen)))
            .get();

        writeCommandMessage("BUILD " + target.siteId + " MINE");

    }
}

class TrainArcherCommand extends AbstractCommand {

    public TrainArcherCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        if( !gameData.structures.friendlyArcherBarracks.isEmpty()){
            Site closest = gameData.getClosestOf(gameData.structures.friendlyArcherBarracks);
            writeCommandMessage("TRAIN " + closest.siteId);
        } else {
            writeCommandMessage("TRAIN");
        }
    }

}

class TrainKnightCommand extends AbstractCommand {

    public TrainKnightCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        if( !gameData.structures.friendlyKnightBarracks.isEmpty()){
            Site closest = gameData.getClosestOf(gameData.structures.friendlyKnightBarracks);
            writeCommandMessage("TRAIN " + closest.siteId);
        } else {
            writeCommandMessage("TRAIN");
        }
    }
}

class TrainGiantCommand extends AbstractCommand {

    public TrainGiantCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        if( !gameData.structures.friendlyGiantBarracks.isEmpty()){
            Site closest = gameData.getClosestOf(gameData.structures.friendlyGiantBarracks);
            writeCommandMessage("TRAIN " + closest.siteId);
        } else {
            writeCommandMessage("TRAIN");
        }
    }
}

class RetreatCommand extends AbstractCommand {

    public RetreatCommand(GameData gameData) {
        super(gameData);
    }

    @Override
    public void executeCommand() {
        if( gameData.structures.friendlyTowers.size() == 0){
            Site closest = gameData.getClosestOf(gameData.structures.friendlyGiantBarracks);
            writeCommandMessage("BUILD " + closest.siteId + " TOWER");
        } else {
            Site site = gameData.getClosestOf(gameData.structures.friendlyTowers);
            writeCommandMessage("MOVE " + site.x + " " + site.y);
        }
    }
}
