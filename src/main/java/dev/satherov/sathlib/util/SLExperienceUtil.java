package dev.satherov.sathlib.util;

import lombok.experimental.UtilityClass;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import net.minecraft.world.entity.player.Player;

import com.google.common.math.IntMath;

@UtilityClass
public class SLExperienceUtil {
    
    public static void addExperiencePoints(Player player, int count, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.XpChange event = new PlayerXpEvent.XpChange(player, count);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            count = event.getAmount();
        }
        
        player.increaseScore(count);
        player.experienceProgress = player.experienceProgress + (float) count / (float) SLExperienceUtil.getXPForNextLevel(player);
        player.totalExperience = Math.clamp(player.totalExperience + count, 0, Integer.MAX_VALUE);
        
        while (player.experienceProgress < 0.0F) {
            float remaining = player.experienceProgress * SLExperienceUtil.getXPForNextLevel(player);
            if (player.experienceLevel > 0) {
                SLExperienceUtil.giveExperienceLevels(player, -1, fireEvent);
                player.experienceProgress = 1.0F + remaining / SLExperienceUtil.getXPForNextLevel(player);
            } else {
                SLExperienceUtil.giveExperienceLevels(player, -1, fireEvent);
                player.experienceProgress = 0.0F;
            }
        }
        
        while (player.experienceProgress >= 1.0F) {
            player.experienceProgress = (player.experienceProgress - 1.0F) * SLExperienceUtil.getXPForNextLevel(player);
            SLExperienceUtil.giveExperienceLevels(player, 1, fireEvent);
            player.experienceProgress = player.experienceProgress / SLExperienceUtil.getXPForNextLevel(player);
        }
    }
    
    public static void giveExperienceLevels(Player player, int amount, boolean fireEvent) {
        if (fireEvent) {
            PlayerXpEvent.LevelChange event = new PlayerXpEvent.LevelChange(player, amount);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) return;
            amount = event.getLevels();
        }
        
        player.experienceLevel = IntMath.saturatedAdd(player.experienceLevel, amount);
        if (player.experienceLevel < 0) {
            player.experienceLevel = 0;
            player.experienceProgress = 0.0F;
            player.totalExperience = 0;
        }
    }
    
    public static int getXPForNextLevel(Player player) {
        if (player.experienceLevel >= 30) return 112 + (player.experienceLevel - 30) * 9;
        return player.experienceLevel >= 15 ? 37 + (player.experienceLevel - 15) * 5 : 7 + player.experienceLevel * 2;
    }
}
