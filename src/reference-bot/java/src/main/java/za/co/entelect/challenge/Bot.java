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
    private double weightSpeed = 24;
    private double weightDamage = -76;
    private double weightBoost = 102;
    private double weightLizard = 40;
    private double weightEMP = 41;
    private double weightTweet = 20;
    
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
        if(myCar.speed <= 0) {
            return accelerate(gameState);
        }   
        return move(gameState);
    }

    private Boolean hasPowerUp(PowerUps powerUpToCheck, PowerUps[] available) {
        for (PowerUps powerUp: available) {
            if (powerUp.equals(powerUpToCheck)) {
                return true;
            }
        }
        return false;
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
            else if(myCar.damage == 0 && !hasPowerUp(PowerUps.BOOST, myCar.powerups) && myCar.speed == 9){
                return offensive(gameState);
            }
            else return ACCELERATE;
        }
    }

    private Command move(GameState gameState){
        Car myCar = gameState.player;
        int sepid = myCar.speed;
        int sepidEkseleret = myCar.speed;
        if (myCar.speed == 15 && myCar.boostCounter == 1){
            sepid = 9;
        }
        if ((myCar.speed == 9 || myCar.speed == 8) && myCar.damage == 0){
            sepidEkseleret = 15;
        }
        else {
            sepidEkseleret = nextSpeed[sepid];
            }
        double weightKiri = countWeight(myCar.position.lane - 1, myCar.position.block - 1, sepid, gameState),
            weightLurus = countWeight(myCar.position.lane, myCar.position.block, sepid, gameState),
            weightKanan = countWeight(myCar.position.lane + 1, myCar.position.block - 1, sepid, gameState),
            weightUjungTengah = countWeightLizard(myCar.position.lane, myCar.position.block, sepid, gameState),
            weightAccelerate = countWeight(myCar.position.lane, myCar.position.block, sepidEkseleret, gameState),
            weightDecelerate = countWeight(myCar.position.lane, myCar.position.block, prevSpeed[sepid], gameState);
        
        if (weightAccelerate <= weightKiri && weightAccelerate <= weightKanan && weightAccelerate <= weightUjungTengah
        && weightAccelerate <= weightLurus && weightAccelerate <= weightDecelerate){
            return accelerate(gameState);
        }
        if (weightUjungTengah <= weightKanan && weightUjungTengah <= weightKiri && weightUjungTengah < weightLurus
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
                if(Math.abs(myCar.position.lane - opponent.position.lane) <= 1){
                    return EMP;
                }
            }
            return DO_NOTHING;
        }
        else if (myCar.position.block == opponent.position.block) {
            return DO_NOTHING;
        }
        else {
            if (hasPowerUp(PowerUps.TWEET, myCar.powerups) && opponent.position.block + nextSpeed[opponent.speed] + 1 <= myCar.position.block) {
                return new TweetCommand(opponent.position.lane, opponent.position.block + nextSpeed[opponent.speed] + 1);
            }
            if(hasPowerUp(PowerUps.OIL, myCar.powerups)){
                return OIL;
            }
            return DO_NOTHING;
        }
    }

    private double countWeight(int lane, int block, int speed, GameState gameState) {
        if(lane <= 0 || lane >= 5) return 9999;
        
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int totalDamage = 0, speedAkhir = speed, totalMaju = 0, boostBlock = 0, lizardBlock = 0, empBlock = 0, twtBlock = 0;
        Lane[] laneList = map.get(lane - 1);
        int blockAkhirOpp = gameState.opponent.speed + gameState.opponent.position.block - startBlock;
        if(gameState.opponent.position.block < gameState.player.position.block || lane != gameState.opponent.position.lane) blockAkhirOpp = -1;

        for (int i = block - startBlock + 1; i <= block - startBlock + speed; i++) {
            if (laneList[i].terrain == Terrain.FINISH) {
                totalMaju = 9999;
                break;
            }
            if (i == blockAkhirOpp) {
                totalMaju = i - (block - startBlock) - 1;
                speedAkhir = gameState.opponent.speed;
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
            if (laneList[i].terrain == Terrain.EMP) {
                empBlock++;
            }
            if (laneList[i].terrain == Terrain.TWEET) {
                twtBlock++;
            }
        }

        double laneWeight = totalDamage * weightDamage
                          + speedAkhir * weightSpeed
                          + totalMaju * weightPosition
                          + boostBlock * weightBoost
                          + lizardBlock * weightLizard
                          + empBlock * weightEMP
                          + twtBlock * weightTweet;
        return -laneWeight;
    }

    private double countWeightLizard(int lane, int block, int speed, GameState gameState) {
        if(lane <= 0 || lane >= 5) return 9999;
        
        List<Lane[]> map = gameState.lanes;
        int startBlock = map.get(0)[0].position.block;
        int totalDamage = 0, speedAkhir = speed, totalMaju = 0, boostBlock = 0, lizardBlock = 0, empBlock = 0;
        Lane[] laneList = map.get(lane - 1);


        for (int i = block - startBlock + 1; i <= block - startBlock + speed; i++) {
            if (laneList[i].terrain == Terrain.FINISH) {
                return -999999;
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
        if (laneList[numBlock].terrain == Terrain.EMP) {
            empBlock++;
        }
        double laneWeight = totalDamage * weightDamage
                          + speedAkhir * weightSpeed
                          + totalMaju * weightPosition
                          + boostBlock * weightBoost
                          + lizardBlock * weightLizard
                          + empBlock * weightEMP;
        return -laneWeight;
    }
}
