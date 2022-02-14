package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.PowerUps;
import za.co.entelect.challenge.enums.Terrain;

import java.util.*;

import javax.management.Descriptor;

import static java.lang.Math.max;

import java.security.SecureRandom;

public class Bot {

    private static final int maxSpeed = 9;
    private List<Command> directionList = new ArrayList<>();

    private final Random random;

    private final static Command ACCELERATE = new AccelerateCommand();
    private final static Command DECELERATE = new DecelerateCommand();
    private final static Command LIZARD = new LizardCommand();
    private final static Command OIL = new OilCommand();
    private final static Command BOOST = new BoostCommand();
    private final static Command EMP = new EmpCommand();
    private final static Command FIX = new FixCommand();
    private final static Command DO_NOTHING = new DoNothingCommand();
    private final static Command TURN_RIGHT = new ChangeLaneCommand(1);
    private final static Command TURN_LEFT = new ChangeLaneCommand(-1);
    
    private double weightPosition = 10;
    private double weightSpeed = 23;
    private double weightDamage = -76;
    private double weightBoost = 10;
    private double weightLizard = 35;
    
    private int[] nextSpeed = {3, 0, 0, 5, 0, 6, 8, 0, 9, 9, 0, 0, 0, 0, 0, 15};
    private int[] prevSpeed = {3, 0, 0, 3, 0, 3, 5, 0, 6, 8, 0, 0, 0, 0, 0, 9};
    

    public Bot() {
        this.random = new SecureRandom();
        directionList.add(TURN_LEFT);
        directionList.add(TURN_RIGHT);
    }

    public Command run(GameState gameState) {
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        

        if(myCar.damage >= 5) {
            return FIX;
        }

        if (myCar.position.block < opponent.position.block){
            // emp jedar jedar atau jalan
            int acc = nextSpeed[myCar.speed] - opponent.speed;
            if (hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                acc = 15 - opponent.speed;
            }
            //emp perlu merhatiin speed kita setelah lurus kalo ada nabrak
            int emp = myCar.speed - 3;
            if (acc > emp){
                return move(gameState);
            }
            else{
                if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
                    //speed minimum supaya worth it nembak emp
                    if (Math.abs(myCar.position.lane - opponent.position.lane) <= 1 
                        && countWeight(myCar.position.lane, myCar.position.block, myCar.speed, gameState) < 50){
                        return EMP;
                    }
                }
                return move(gameState);
            }
        }
        else if (myCar.position.block == opponent.position.block) {
            return move(gameState);
        }
        else if (myCar.position.block <= opponent.position.block + 20){
            //pake oil
            return move(gameState);
        }
        else {
            //pake tweet
            return move(gameState);
        }
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
    }

    private int countPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        int count = 0;
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                count++;
            }
        }
        return count;
    }

    private Command accelerate(GameState gameState){
        Car myCar = gameState.player;
        int[] maxSpeeed = {15, 9, 8, 6, 3, 0};
        if(myCar.speed == maxSpeeed[myCar.damage]){
            if (myCar.damage == 1 && !hasPowerUp(PowerUps.BOOST, myCar.powerups)){
                return offensive(gameState);
            }
            else if (myCar.damage == 0 && hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.boostCounter == 0){
                return BOOST;
            }
            else if(myCar.damage == 0){
                return offensive(gameState);
            }
            else return FIX;
        }
        else{
            if (myCar.damage == 0 && hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.boostCounter == 0){
                return BOOST;
            }
            else if (myCar.damage == 0 && hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.boostCounter != 0){
                return offensive(gameState);
            }
            else return ACCELERATE;
        }
        // if(hasPowerUp(PowerUps.BOOST, myCar.powerups)){
        //     if (myCar.damage == 0){
        //         return BOOST;
        //     }
        //     else {
        //         return FIX;
        //     }
        // }
    }

    private Command move(GameState gameState){
        Car myCar = gameState.player;
        double weightKiri = countWeight(myCar.position.lane - 1, myCar.position.block - 1, myCar.speed, gameState),
            weightLurus = countWeight(myCar.position.lane, myCar.position.block, myCar.speed, gameState),
            weightKanan = countWeight(myCar.position.lane + 1, myCar.position.block - 1, myCar.speed, gameState),
            weightUjungTengah = countWeightLizard(myCar.position.lane, myCar.position.block, myCar.speed, gameState),
            weightAccelerate = countWeight(myCar.position.lane, myCar.position.block, nextSpeed[myCar.speed], gameState),
            weightDecelerate = countWeight(myCar.position.lane, myCar.position.block, prevSpeed[myCar.speed], gameState);
        
        if (weightAccelerate <= weightKiri && weightAccelerate <= weightKanan && weightAccelerate <= weightUjungTengah
        && weightAccelerate <= weightLurus && weightAccelerate <= weightDecelerate){
            return accelerate(gameState);
        }
        if (weightUjungTengah <= weightKanan && weightUjungTengah <= weightKiri && weightUjungTengah <= weightLurus
            && weightUjungTengah <= weightDecelerate && hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
            return LIZARD;
        }
        else if (weightAccelerate <= weightKiri && weightAccelerate <= weightKanan && weightAccelerate <= weightLurus 
        && weightAccelerate <= weightDecelerate){
            return accelerate(gameState);
        }
        else if (weightLurus <= weightKanan && weightLurus <= weightKiri && weightLurus <= weightDecelerate){
            return offensive(gameState);
        }
        else if (weightKanan == weightKiri && weightKiri <= weightDecelerate){
            if (myCar.position.lane <= 2) return TURN_RIGHT;
            else return TURN_LEFT;
        }
        else if (weightKanan < weightKiri && weightKanan <= weightDecelerate){
            return TURN_RIGHT;
        }
        else if (weightKiri < weightKanan && weightKiri <= weightDecelerate){
            return TURN_LEFT;
        }
        else return DECELERATE;
    }


    private Command offensive(GameState gameState){
        Car myCar = gameState.player;
        Car opponent = gameState.opponent;
        
        if (myCar.position.block < opponent.position.block){
            if (hasPowerUp(PowerUps.EMP, myCar.powerups)){
                //speed minimum supaya worth it nembak emp
                if(Math.abs(myCar.position.lane - opponent.position.lane) <= 1){
                    return EMP;
                }
            }
        }
        else if (myCar.position.block == opponent.position.block) {
            return DO_NOTHING;
        }
        else if (myCar.position.block <= opponent.position.block + 20){
            if(hasPowerUp(PowerUps.OIL, myCar.powerups)){
                return OIL;
            }
        }
        else {
            return new TweetCommand(opponent.position.lane, opponent.position.block + opponent.speed + 5);
            // if(countPowerUp(PowerUps.OIL, myCar.powerups) > 3){
            //     return OIL;
            // }
        }
        return DO_NOTHING;
    }

    private double countWeight(int lane, int block, int speed, GameState gameState) {
        if(lane <= 0 || lane >= 5) return 9999;
        
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int totalDamage = 0, speedAkhir = speed, totalMaju = 0, boostBlock = 0, lizardBlock = 0;
        Lane[] laneList = map.get(lane - 1);


        for (int i = block - startBlock + 1; i <= block - startBlock + speed; i++) {
            if (laneList[i].terrain == Terrain.FINISH) {
                totalMaju = 9999;
                break;
            }
            if (laneList[i].isTruck) {
                totalDamage += 2;
                speedAkhir = 3;
                totalMaju = i - (block - startBlock);
                break;
            }
            if (laneList[i].terrain == Terrain.OIL_SPILL) {
                totalDamage++;
                speedAkhir = prevSpeed[speedAkhir];
            }
            if (laneList[i].terrain == Terrain.MUD) {
                totalDamage++;
                speedAkhir = prevSpeed[speedAkhir];
            }
            if (laneList[i].terrain == Terrain.WALL) {
                speedAkhir = 3;
                totalDamage += 2;
            }
            if (laneList[i].terrain == Terrain.BOOST) {
                boostBlock++;
            }
            if (laneList[i].terrain == Terrain.LIZARD) {
                lizardBlock++;
            }
        }

        double laneWeight = totalDamage * weightDamage
                          + speedAkhir * weightSpeed
                          + totalMaju * weightPosition
                          + boostBlock * weightBoost
                          + lizardBlock * weightLizard;
        return -laneWeight;
    }

    private double countWeightLizard(int lane, int block, int speed, GameState gameState) {
        if(lane <= 0 || lane >= 5) return 9999;
        
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int totalDamage = 0, speedAkhir = speed, totalMaju = 0, boostBlock = 0, lizardBlock = 0;
        Lane[] laneList = map.get(lane - 1);


        for (int i = block - startBlock + 1; i <= block - startBlock + speed; i++) {
            if (laneList[i].terrain == Terrain.FINISH) {
                return -9999;
            }
        }

        int numBlock = block - startBlock + speed;
        if (laneList[numBlock].isTruck) {
            totalDamage += 2;
            speedAkhir = 3;
            totalMaju = numBlock - (block - startBlock);
        }
        if (laneList[numBlock].terrain == Terrain.OIL_SPILL) {
            totalDamage++;
            speedAkhir = prevSpeed[speedAkhir];
        }
        if (laneList[numBlock].terrain == Terrain.MUD) {
            totalDamage++;
            speedAkhir = prevSpeed[speedAkhir];
        }
        if (laneList[numBlock].terrain == Terrain.WALL) {
            speedAkhir = 3;
            totalDamage += 2;
        }
        if (laneList[numBlock].terrain == Terrain.BOOST) {
            boostBlock++;
        }
        if (laneList[numBlock].terrain == Terrain.LIZARD) {
            lizardBlock++;
        }
        double laneWeight = totalDamage * weightDamage
                          + speedAkhir * weightSpeed
                          + totalMaju * weightPosition
                          + boostBlock * weightBoost
                          + lizardBlock * weightLizard;
        return -laneWeight;
    }
}

// private Command move2(GameState gameState){
//     Car myCar = gameState.player;
//     double weightKiri = countWeight(myCar.position.lane - 1, myCar.position.block - 1, myCar.speed, gameState),
//         weightLurus = countWeight(myCar.position.lane, myCar.position.block, myCar.speed, gameState),
//         weightKanan = countWeight(myCar.position.lane + 1, myCar.position.block - 1, myCar.speed, gameState),
//         weightUjungTengah = countWeightLizard(myCar.position.lane, myCar.position.block, myCar.speed, gameState),
//         weightAccelerate = countWeight(myCar.position.lane, myCar.position.block, nextSpeed[myCar.speed], gameState),
//         weightDecelerate = countWeight(myCar.position.lane, myCar.position.block, prevSpeed[myCar.speed], gameState);
        
//     if (weightAccelerate <= 0){
//         return accelerate(gameState);
//     }
//     else if (weightLurus <= 0 && weightAccelerate > 0){
//         return offensive(gameState);
//     }
//     else if (weightKanan <= 0 && weightKiri <= 0) {
//         if (myCar.position.lane <= 2) return TURN_RIGHT;
//         else return TURN_LEFT;
//     }
//     else if (weightKanan <= 0){
//         return TURN_RIGHT;
//     }
//     else if (weightKiri <= 0){
//         return TURN_LEFT;
//     }
//     else {
//         if (weightUjungTengah == 0 && hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
//                 return LIZARD;
//         }
//         else{
//             if (weightUjungTengah <= weightKanan && weightUjungTengah <= weightKiri 
//             && weightUjungTengah < weightDecelerate && hasPowerUp(PowerUps.LIZARD, myCar.powerups)){
//                 return LIZARD;
//             }
//             else if (weightLurus <= weightKanan && weightLurus <= weightKiri && weightLurus == weightDecelerate){
//                 return offensive(gameState);
//             }
//             else if (weightKanan == weightKiri && weightKiri < weightLurus && weightKiri <= weightDecelerate){
//                 if (myCar.position.lane <= 2) return TURN_RIGHT;
//                 else return TURN_LEFT;
//             }
//             else if (weightKanan <= weightUjungTengah && weightKanan < weightKiri && weightKanan <= weightDecelerate){
//                 return TURN_RIGHT;
//             }
//             else if (weightKiri < weightKanan && weightKiri <= weightDecelerate && weightKiri <= weightUjungTengah){
//                 return TURN_LEFT;
//             }
//             else return DECELERATE;
//         }
//     }
// }

//     private float countWeight(int lane, int block, int speed, GameState gameState) {
//         if(lane <= 0 || lane >= 5) return 99999;

//         List<Lane[]> map = gameState.lanes;
//         int startBlock = map.get(0)[0].position.block;
//         int finishBlock = 0, oilBlock = 0, mudBlock = 0, wallBlock = 0, truckBlock = 0, boostBlock = 0;

//         Lane[] laneList = map.get(lane - 1);
//         for (int i = block - startBlock + 1; i <= block - startBlock + speed; i++) {
//             if (laneList[i].terrain == Terrain.FINISH) {
//                 finishBlock++;
//                 break;
//             }
//             if (laneList[i].isTruck) {
//                 truckBlock++;
//                 continue;
//             }
//             if (laneList[i].terrain == Terrain.OIL_SPILL) {
//                 oilBlock++;
//             }
//             if (laneList[i].terrain == Terrain.WALL) {
//                 wallBlock++;
//             }
//             if (laneList[i].terrain == Terrain.MUD) {
//                 mudBlock++;
//             }
//             if (laneList[i].terrain == Terrain.BOOST) {
//                 boostBlock++;
//             }
//         }

//         float laneWeight = weightFinish * finishBlock 
//                          + weightMud * mudBlock
//                          + weightOil * oilBlock
//                          + weightWall * wallBlock
//                          + weightTruck * truckBlock
//                          + boostBlock * weightBoost;

//         return laneWeight;
//     }
// }
//Basic fix logic
        // List<Object> blocks = getBlocksInFront(myCar.position.lane, myCar.position.block, gameState);
        // List<Object> nextBlocks = blocks.subList(0,1);
/**
     * Returns map of blocks and the objects in the for the current lanes, returns
     * the amount of blocks that can be traversed at max speed.
     **/
    // private List<Object> getBlocksInFront(int lane, int block, GameState gameState) {
    //     List<Lane[]> map = gameState.lanes;
    //     List<Object> blocks = new ArrayList<>();
    //     int startBlock = map.get(0)[0].position.block;

    //     Lane[] laneList = map.get(lane - 1);
    //     for (int i = max(block - startBlock, 0); i <= block - startBlock + Bot.maxSpeed; i++) {
    //         if (laneList[i] == null || laneList[i].terrain == Terrain.FINISH) {
    //             break;
    //         }

    //         blocks.add(laneList[i].terrain);

    //     }
    //     return blocks;
    // }

    //Basic avoidance logic
        // if (blocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
        //     if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
        //         return LIZARD;
        //     }
        //     if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
        //         int i = random.nextInt(directionList.size());
        //         return directionList.get(i);
        //     }


        //Basic improvement logic
        // if (hasPowerUp(PowerUps.BOOST, myCar.powerups)) {
        //     return BOOST;
        // }

        //Accelerate first if going to slow
        // if(myCar.speed <= 3) {
        //     return ACCELERATE;
        // }

        //Basic fix logic

        // if(hasPowerUp(PowerUps.EMP, myCar.powerups) && myCar.position.block < opponent.position.block){
        //     if(Math.abs(myCar.position.lane - opponent.position.lane) <= 1){
        //         //jangan sampe collision, harus diimplemen
        //         return EMP;
        //     }
        // }

        // if(hasPowerUp(PowerUps.OIL, myCar.powerups)){
        //     if(myCar.position.lane == opponent.position.lane){
        //         if(myCar.position.block > opponent.position.block && myCar.position.block <= opponent.position.block + 15){
        //             return OIL;
        //         }
        //     }
        // }

        //Pake TWEET

        // if(countPowerUp(PowerUps.OIL, myCar.powerups) > 3){
        //     return OIL;
        // }

        // //Basic avoidance logic
        // if (blocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
        //     if (hasPowerUp(PowerUps.LIZARD, myCar.powerups)) {
        //         return LIZARD;
        //     }
        //     if (nextBlocks.contains(Terrain.MUD) || nextBlocks.contains(Terrain.WALL)) {
        //         int i = random.nextInt(directionList.size());
        //         return directionList.get(i);
        //     }
        // }


        // //Basic aggression logic
        // if (myCar.speed == maxSpeed) {
        //     if (hasPowerUp(PowerUps.OIL, myCar.powerups)) {
        //         return OIL;
        //     }
        //     if (hasPowerUp(PowerUps.EMP, myCar.powerups)) {
        //         return EMP;
        //     }
        // }
