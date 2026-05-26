package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import net.minecraft.world.entity.player.Player;

///
/// Experience point and level helpers.
///
@UtilityClass
public class SLExperienceUtil {
    
    ///
    /// Highest total experience value vanilla can store on a player.
    ///
    public static final int MAX_PLAYER_TOTAL_XP = Integer.MAX_VALUE;
    
    ///
    /// Highest level fully reachable from the vanilla player total experience limit.
    ///
    public static final int MAX_PLAYER_LEVEL = 21_863;
    
    ///
    /// Highest level whose next-level experience cost fits into an integer.
    ///
    public static final int MAX_PLAYER_XP_COST_LEVEL = 238_609_311;
    
    ///
    /// Highest level supported by the long-based experience helpers.
    ///
    public static final long MAX_LONG_LEVEL = 1_431_655_783L;
    
    ///
    /// Highest total experience value supported by the long-based experience helpers.
    ///
    public static final long MAX_LONG_TOTAL_XP = 9_223_372_031_843_981_383L;
    
    ///
    /// Highest next-level experience cost supported by the long-based experience helpers.
    ///
    public static final long MAX_LONG_XP_FOR_NEXT_LEVEL = 12_884_901_889L;
    
    ///
    /// Adds or removes raw experience points using vanilla-style looping.
    ///
    /// Experience outside the vanilla player range is discarded.
    ///
    /// @param player    player to modify
    /// @param number    number of experience points to add
    /// @param fireEvent whether to fire the matching NeoForge event first
    ///
    public static void addExperiencePoints(Player player, int number, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.XpChange event = new PlayerXpEvent.XpChange(player, number);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            number = event.getAmount();
        }
        
        number = SLExperienceUtil.clampExperienceChange(player.totalExperience, number);
        if (number == 0) return;
        
        player.increaseScore(number);
        player.experienceProgress = player.experienceProgress + (float) number / (float) SLExperienceUtil.getXPForNextLevel(player);
        player.totalExperience = Math.clamp(player.totalExperience + number, 0, SLExperienceUtil.MAX_PLAYER_TOTAL_XP);
        
        while (player.experienceProgress < 0.0F) {
            float remaining = player.experienceProgress * SLExperienceUtil.getXPForNextLevel(player);
            
            if (player.experienceLevel > 0) {
                SLExperienceUtil.addExperienceLevels(player, -1, fireEvent);
                player.experienceProgress = 1.0F + remaining / SLExperienceUtil.getXPForNextLevel(player);
            } else {
                SLExperienceUtil.addExperienceLevels(player, -1, fireEvent);
                player.experienceProgress = 0.0F;
            }
        }
        
        while (player.experienceProgress >= 1.0F) {
            player.experienceProgress = (player.experienceProgress - 1.0F) * SLExperienceUtil.getXPForNextLevel(player);
            SLExperienceUtil.addExperienceLevels(player, 1, fireEvent);
            player.experienceProgress = player.experienceProgress / SLExperienceUtil.getXPForNextLevel(player);
        }
    }
    
    ///
    /// Adds or removes raw experience points by recalculating the player state directly.
    ///
    /// This avoids vanilla's per-level loop and posts at most one level change event.
    /// Experience outside the vanilla player range is discarded.
    ///
    /// @param player    player to modify
    /// @param number    number of experience points to add
    /// @param fireEvent whether to fire the matching NeoForge event first
    ///
    public static void addExperiencePointsFast(Player player, int number, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.XpChange event = new PlayerXpEvent.XpChange(player, number);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            number = event.getAmount();
        }
        
        int oldTotalXP = SLExperienceUtil.getTotalXP(player);
        int oldLevel = player.experienceLevel;
        int safeNumber = SLExperienceUtil.clampExperienceChange(oldTotalXP, number);
        
        if (safeNumber == 0) return;
        
        int newTotalXP = oldTotalXP + safeNumber;
        int newLevel = Math.toIntExact(SLExperienceUtil.getLevelForTotalXP(newTotalXP));
        int levelNumber = newLevel - oldLevel;
        
        if (fireEvent && levelNumber != 0) {
            PlayerXpEvent.LevelChange event = new PlayerXpEvent.LevelChange(player, levelNumber);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            
            levelNumber = event.getLevels();
            newLevel = SLExperienceUtil.clampLevelTarget(oldLevel, levelNumber);
            newTotalXP = SLExperienceUtil.getTotalXPForPlayerLevel(newLevel);
        }
        
        player.increaseScore(safeNumber);
        SLExperienceUtil.setExperienceFromTotal(player, newTotalXP);
    }
    
    ///
    /// Adds or removes whole experience levels.
    ///
    /// Levels outside the vanilla player range are discarded.
    ///
    /// @param player    player to modify
    /// @param number    number of levels to add
    /// @param fireEvent whether to fire the matching NeoForge event first
    ///
    public static void addExperienceLevels(Player player, int number, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.LevelChange event = new PlayerXpEvent.LevelChange(player, number);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            number = event.getLevels();
        }
        
        number = SLExperienceUtil.clampLevelChange(player.experienceLevel, number);
        player.experienceLevel += number;
        
        if (player.experienceLevel <= 0) {
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
        }
    }
    
    ///
    /// Adds or removes whole experience levels by recalculating the player state directly.
    ///
    /// Levels outside the vanilla player range are discarded.
    ///
    /// @param player    player to modify
    /// @param number    number of levels to add
    /// @param fireEvent whether to fire the matching NeoForge event first
    ///
    public static void addExperienceLevelsFast(Player player, int number, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.LevelChange event = new PlayerXpEvent.LevelChange(player, number);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            number = event.getLevels();
        }
        
        int newLevel = SLExperienceUtil.clampLevelTarget(player.experienceLevel, number);
        if (newLevel == player.experienceLevel) return;
        
        SLExperienceUtil.setExperienceFromTotal(player, SLExperienceUtil.getTotalXPForPlayerLevel(newLevel));
    }
    
    ///
    /// Returns the player's current total experience reconstructed from level and progress.
    ///
    /// @param player player whose experience should be inspected
    ///
    /// @return reconstructed total experience
    ///
    public static int getTotalXP(Player player) {
        long levelXP = SLExperienceUtil.getTotalXPForLevel(player.experienceLevel);
        long nextLevelXP = SLExperienceUtil.getXPForNextLevel(player.experienceLevel);
        long progressXP = Math.round(player.experienceProgress * nextLevelXP);
        
        return Math.toIntExact(Math.clamp(levelXP + progressXP, 0L, SLExperienceUtil.MAX_PLAYER_TOTAL_XP));
    }
    
    ///
    /// Sets the player's experience from a total experience value.
    ///
    /// @param player  player to modify
    /// @param totalXP target total experience
    ///
    public static void setExperienceFromTotal(Player player, int totalXP) {
        int clampedXP = Math.clamp(totalXP, 0, SLExperienceUtil.MAX_PLAYER_TOTAL_XP);
        
        long level = SLExperienceUtil.getLevelForTotalXP(clampedXP);
        long xpIntoLevel = SLExperienceUtil.getXPIntoLevel(clampedXP);
        long xpForNextLevel = SLExperienceUtil.getXPForNextLevel(level);
        
        player.totalExperience = clampedXP;
        player.experienceLevel = Math.toIntExact(level);
        player.experienceProgress = (float) xpIntoLevel / (float) xpForNextLevel;
    }
    
    ///
    /// Returns the experience required for the player's next level.
    ///
    /// @param player player whose level curve should be inspected
    ///
    /// @return experience points required for the next level
    ///
    public static int getXPForNextLevel(Player player) {
        return SLExperienceUtil.getXPForNextLevel(player.experienceLevel);
    }
    
    ///
    /// Returns the experience required for the next level.
    ///
    /// @param level current experience level
    ///
    /// @return experience points required for the next level
    ///
    public static int getXPForNextLevel(int level) {
        int clampedLevel = Math.clamp(level, 0, SLExperienceUtil.MAX_PLAYER_XP_COST_LEVEL);
        
        if (clampedLevel >= 30) return 112 + (clampedLevel - 30) * 9;
        return clampedLevel >= 15 ? 37 + (clampedLevel - 15) * 5 : 7 + clampedLevel * 2;
    }
    
    ///
    /// Returns the experience required for the next level.
    ///
    /// @param level current experience level
    ///
    /// @return experience points required for the next level
    ///
    public static long getXPForNextLevel(long level) {
        long clampedLevel = Math.clamp(level, 0L, SLExperienceUtil.MAX_LONG_LEVEL);
        
        if (clampedLevel >= 30L) return 112L + (clampedLevel - 30L) * 9L;
        return clampedLevel >= 15L ? 37L + (clampedLevel - 15L) * 5L : 7L + clampedLevel * 2L;
    }
    
    ///
    /// Returns the total experience required to reach the given level.
    ///
    /// @param level target level
    ///
    /// @return total required experience points
    ///
    public static long getTotalXPForLevel(long level) {
        long clampedLevel = Math.clamp(level, 0L, SLExperienceUtil.MAX_LONG_LEVEL);
        
        if (clampedLevel >= 30L) {
            return (9L * clampedLevel * clampedLevel - 325L * clampedLevel + 4440L) / 2L;
        }
        
        if (clampedLevel >= 15L) {
            return (5L * clampedLevel * clampedLevel - 81L * clampedLevel + 720L) / 2L;
        }
        
        return clampedLevel * clampedLevel + 6L * clampedLevel;
    }
    
    ///
    /// Returns the level for the given total experience number.
    ///
    /// @param totalXP total experience points
    ///
    /// @return calculated experience level
    ///
    public static long getLevelForTotalXP(long totalXP) {
        long clampedXP = Math.clamp(totalXP, 0L, SLExperienceUtil.MAX_LONG_TOTAL_XP);
        
        long lowLevel = 0L;
        long highLevel = SLExperienceUtil.MAX_LONG_LEVEL;
        
        while (lowLevel < highLevel) {
            long midLevel = lowLevel + (highLevel - lowLevel + 1L) / 2L;
            
            if (SLExperienceUtil.getTotalXPForLevel(midLevel) <= clampedXP) {
                lowLevel = midLevel;
            } else {
                highLevel = midLevel - 1L;
            }
        }
        
        return lowLevel;
    }
    
    ///
    /// Returns the experience accumulated inside the current level.
    ///
    /// @param totalXP total experience points
    ///
    /// @return current level progress in raw experience points
    ///
    public static long getXPIntoLevel(long totalXP) {
        long clampedXP = Math.clamp(totalXP, 0L, SLExperienceUtil.MAX_LONG_TOTAL_XP);
        long level = SLExperienceUtil.getLevelForTotalXP(clampedXP);
        
        return clampedXP - SLExperienceUtil.getTotalXPForLevel(level);
    }
    
    ///
    /// Returns the progress toward the next level.
    ///
    /// @param totalXP total experience points
    ///
    /// @return progress from 0.0D to 1.0D
    ///
    public static double getLevelProgress(long totalXP) {
        long clampedXP = Math.clamp(totalXP, 0L, SLExperienceUtil.MAX_LONG_TOTAL_XP);
        long level = SLExperienceUtil.getLevelForTotalXP(clampedXP);
        long xpIntoLevel = SLExperienceUtil.getXPIntoLevel(clampedXP);
        long xpForNextLevel = SLExperienceUtil.getXPForNextLevel(level);
        
        return (double) xpIntoLevel / (double) xpForNextLevel;
    }
    
    ///
    /// Adds or removes raw experience points from a total experience value.
    ///
    /// Experience outside the safe long range is discarded.
    ///
    /// @param totalXP current total experience
    /// @param number  number of experience points to add
    ///
    /// @return modified total experience value
    ///
    public static long addExperiencePoints(long totalXP, long number) {
        return SLExperienceUtil.clampLongChange(totalXP, number, SLExperienceUtil.MAX_LONG_TOTAL_XP);
    }
    
    ///
    /// Adds or removes whole experience levels.
    ///
    /// Levels outside the safe long range are discarded.
    ///
    /// @param level  current level
    /// @param number number of levels to add
    ///
    /// @return modified level value
    ///
    public static long addExperienceLevels(long level, long number) {
        return SLExperienceUtil.clampLongChange(level, number, SLExperienceUtil.MAX_LONG_LEVEL);
    }
    
    ///
    /// Returns the total experience for a player level, clamped to the vanilla player total experience range.
    ///
    /// @param level target player level
    ///
    /// @return total player experience
    ///
    private static int getTotalXPForPlayerLevel(int level) {
        long clampedLevel = Math.clamp(level, 0L, SLExperienceUtil.MAX_PLAYER_LEVEL);
        long totalXP = SLExperienceUtil.getTotalXPForLevel(clampedLevel);
        
        return Math.toIntExact(Math.min(totalXP, SLExperienceUtil.MAX_PLAYER_TOTAL_XP));
    }
    
    ///
    /// Returns the target level after applying a level change.
    ///
    /// Levels outside the vanilla player range are discarded.
    ///
    /// @param level  current level
    /// @param number number of levels to add
    ///
    /// @return clamped target level
    ///
    private static int clampLevelTarget(int level, int number) {
        int clampedLevel = Math.clamp(level, 0, SLExperienceUtil.MAX_PLAYER_LEVEL);
        
        if (number > 0 && clampedLevel > SLExperienceUtil.MAX_PLAYER_LEVEL - number) {
            return SLExperienceUtil.MAX_PLAYER_LEVEL;
        }
        
        if (number < 0 && clampedLevel < -number) {
            return 0;
        }
        
        return clampedLevel + number;
    }
    
    ///
    /// Returns the usable experience change for a player.
    ///
    /// Experience outside the vanilla player range is discarded.
    ///
    /// @param value  current total experience
    /// @param number number of experience points to add
    ///
    /// @return usable experience change
    ///
    private static int clampExperienceChange(int value, int number) {
        int clampedValue = Math.clamp(value, 0, SLExperienceUtil.MAX_PLAYER_TOTAL_XP);
        
        if (number > 0 && clampedValue > SLExperienceUtil.MAX_PLAYER_TOTAL_XP - number) {
            return SLExperienceUtil.MAX_PLAYER_TOTAL_XP - clampedValue;
        }
        
        if (number < 0 && clampedValue < -number) {
            return -clampedValue;
        }
        
        return number;
    }
    
    ///
    /// Returns the usable level change for a player.
    ///
    /// Levels outside the vanilla player range are discarded.
    ///
    /// @param value  current level
    /// @param number number of levels to add
    ///
    /// @return usable level change
    ///
    private static int clampLevelChange(int value, int number) {
        int clampedValue = Math.clamp(value, 0, SLExperienceUtil.MAX_PLAYER_LEVEL);
        
        if (number > 0 && clampedValue > SLExperienceUtil.MAX_PLAYER_LEVEL - number) {
            return SLExperienceUtil.MAX_PLAYER_LEVEL - clampedValue;
        }
        
        if (number < 0 && clampedValue < -number) {
            return -clampedValue;
        }
        
        return number;
    }
    
    ///
    /// Returns the result of a long change clamped to a non-negative range.
    ///
    /// Values outside the given range are discarded.
    ///
    /// @param value    current value
    /// @param number   number to add
    /// @param maxValue highest allowed value
    ///
    /// @return clamped result
    ///
    private static long clampLongChange(long value, long number, long maxValue) {
        long clampedValue = Math.clamp(value, 0L, maxValue);
        
        if (number > 0L && clampedValue > maxValue - number) {
            return maxValue;
        }
        
        if (number < 0L && clampedValue < -number) {
            return 0L;
        }
        
        return clampedValue + number;
    }
}