package com.nbp.weblab.date;

import java.util.Date;

public class Game {
	private final Date startTime;
	private final Date endTime;
	
	public Game(Date startTime, Date endTime) {
		this.startTime = startTime;
		//   new com.nbp.weblab.date.Game(Date, Date) may expose internal representation 
		// by storing an externally mutable object into Game.startTime
		this.endTime = new Date(endTime.getTime());;
	}

	public Date getStartTime() {
		return this.startTime;
		//   com.nbp.weblab.date.Game.getStartTime() may expose internal representation
		// by returning Game.startTime
	}
	
	public Date getEndTime() {
		return new Date(this.endTime.getTime());
	}
	
	public long getGameTimeMillis(){
		return endTime.getTime() - startTime.getTime();
	}
}
