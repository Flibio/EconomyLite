package me.Flibio.EconomyLite.Utils;

import me.Flibio.EconomyLite.EconomyLite;

import org.spongepowered.api.Game;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.text.Text;

import java.util.HashMap;

public class ScoreboardUtils {
	
	private Game game;
	
	/**
	 * Creates a new instance of the ScoreboardUtils class
	 * 
	 */
	public ScoreboardUtils() {
		this.game = EconomyLite.access.game;
	}
	
	/**
	 * Creates a scoreboard based on the parameters that you give it
	 * 
	 * @param objectiveName
	 * 			Name of the scoreboard
	 * @param displayName
	 * 			Display name of the scoreboard
	 * @param objectiveValues
	 * 			HashMap containing all of the values that will be placed in the objective
	 * @return Completed scoreboard
	 */
	public Scoreboard createScoreboard(String objectiveName, Text displayName, HashMap<Text, Integer> objectiveValues) {
		Scoreboard board = game.getRegistry().createBuilder(Scoreboard.Builder.class).build();
		Objective obj = game.getRegistry().createBuilder(Objective.Builder.class).name(objectiveName).criterion(Criteria.DUMMY).displayName(displayName).build();
		for(Text name : objectiveValues.keySet()) {
			int value = objectiveValues.get(name);
			obj.getOrCreateScore(name).setScore(value);
		}
		board.updateDisplaySlot(obj,DisplaySlots.SIDEBAR);
		return board;
	}
}
