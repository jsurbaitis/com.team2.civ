package com.team2.civ.AI;

public class AIGameResult {
	public AI winner;
	public float score;
	
	public AIGameResult(AI winner) {
		this.winner = winner;
		this.score = 1;
	}
	
	public AIGameResult(AI winner, float score) {
		this.winner = winner;
		this.score = score;
	}
}
