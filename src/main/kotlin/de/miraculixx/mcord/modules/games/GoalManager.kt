package de.miraculixx.mcord.modules.games

import de.miraculixx.mcord.modules.games.utils.enums.DailyGoals
import de.miraculixx.mcord.modules.games.utils.enums.Game
import de.miraculixx.mcord.utils.Color
import de.miraculixx.mcord.utils.api.SQL
import de.miraculixx.mcord.utils.dailyGoals
import de.miraculixx.mcord.utils.log
import java.sql.ResultSet

object GoalManager {
    suspend fun registerWin(game: Game, bot: Boolean, userSnowflake: Long, guildSnowflake: Long) {
        SQL.addWin(userSnowflake, guildSnowflake, game.short + if (bot) "_Bot" else "")
        val goal = try {
            DailyGoals.valueOf("WIN_${game.short.uppercase()}_${if (bot) "BOT" else "USER"}")
        } catch (_: IllegalArgumentException) { //No Win Goal / No Bot Goal
            return
        }
        checkGoal(goal, userSnowflake, guildSnowflake)
    }
    suspend fun registerDraw(game: Game, userSnowflake: Long, guildSnowflake: Long) {
        val goal = try {
            DailyGoals.valueOf("DRAW_${game.short.uppercase()}")
        } catch (e: IllegalArgumentException) {
            return
        }
        checkGoal(goal, userSnowflake, guildSnowflake)
    }

    suspend fun registerNewGame(game: Game, replay: Boolean, userSnowflake: Long, guildSnowflake: Long) {
        val goal = if (replay) DailyGoals.REPLAY
        else try {
            DailyGoals.valueOf("PLAY_${game.short.uppercase()}")
        } catch (_: IllegalArgumentException) { //No New Game Goal
            return
        }
        checkGoal(goal, userSnowflake, guildSnowflake)
    }

    suspend fun registerSkinChange(game: Game, userSnowflake: Long, guildSnowflake: Long) {
        val goal = try {
            DailyGoals.valueOf("CHANGE_${game.short.uppercase()}_SKIN")
        } catch (_: IllegalArgumentException) {
            return
        }
        checkGoal(goal, userSnowflake, guildSnowflake)
    }

    private suspend fun checkGoal(goal: DailyGoals, userSnowflake: Long, guildSnowflake: Long) {
        val goalPostion = dailyGoals?.lastIndexOf(goal) ?: -1
        if (goalPostion == -1) return
        val task = when (goalPostion) {
            0 -> Task.Task_1
            1 -> Task.Task_2
            2 -> Task.Task_3
            else -> Task.Task_Bonus
        }

        val response = SQL.call("SELECT userData.ID, ${task.name} FROM userData JOIN userDaily WHERE Guild_ID=$guildSnowflake && Discord_ID=$userSnowflake && userData.ID=userDaily.ID")
        response.next()
        val userID = response.getInt("ID")
        val finished = response.getBoolean(task.name)
        if (!finished) {
            SQL.call("UPDATE userDaily SET ${task.name}=1 WHERE ID=$userID")
            SQL.call("UPDATE userData SET Coins=Coins+${goal.reward} WHERE ID=$userID")
        }
    }

    @Suppress("EnumEntryName")
    enum class Task {
        Task_1, Task_2, Task_3, Task_Bonus
    }
}