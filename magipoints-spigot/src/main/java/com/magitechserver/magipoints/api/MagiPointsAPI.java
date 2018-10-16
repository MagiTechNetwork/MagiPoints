package com.magitechserver.magipoints.api;

/**
 * Created by Frani on 11/12/2017.
 */
public interface MagiPointsAPI {

    int getPoints(String uuid);

    void addPoints(String uuid, int points);

    boolean takePoints(String uuid, int points);

    void setPoints(String uuid, int points);

}
